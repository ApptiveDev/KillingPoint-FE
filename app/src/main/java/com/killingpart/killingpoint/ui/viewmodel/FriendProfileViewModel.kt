package com.killingpart.killingpoint.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.killingpart.killingpoint.data.model.MyDiaries
import com.killingpart.killingpoint.data.model.SubscribeUser
import com.killingpart.killingpoint.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface FriendProfileUiState {
    data object Loading : FriendProfileUiState
    data class Success(
        val user: SubscribeUser?,
        val diaries: MyDiaries?,
        val fansCount: Int = 0,
        val picksCount: Int = 0
    ) : FriendProfileUiState
    data class Error(val message: String) : FriendProfileUiState
}

class FriendProfileViewModel(
    private val repoFactory: (Context) -> AuthRepository = { ctx ->
        AuthRepository(ctx)
    }
) : ViewModel() {

    private val _state = MutableStateFlow<FriendProfileUiState>(FriendProfileUiState.Loading)
    val state: StateFlow<FriendProfileUiState> = _state

    private var loadedUserId: Long? = null

    fun loadFriendProfile(context: Context, userId: Long) {
        // 같은 유저 데이터가 이미 있으면 Loading으로 바꾸지 않음 (화면이 교체되며 스크롤 위치가 초기화되는 것 방지)
        if (_state.value !is FriendProfileUiState.Success || loadedUserId != userId) {
            _state.value = FriendProfileUiState.Loading
        }
        loadedUserId = userId
        val repo = repoFactory(context)
        viewModelScope.launch {
            val firstPageResult = repo.getUserDiaries(userId, 0, 1)
            val totalElements = firstPageResult.getOrNull()?.page?.totalElements ?: 0
            
            val diariesResult = if (totalElements > 0) {
                repo.getUserDiaries(userId, 0, totalElements)
            } else {
                firstPageResult
            }
            
            val statisticsResult = repo.getUserStatistics(userId)
            
            when {
                diariesResult.isSuccess -> {
                    val statistics = statisticsResult.getOrNull()
                    _state.value = FriendProfileUiState.Success(
                        user = null,
                        diaries = diariesResult.getOrNull(),
                        fansCount = statistics?.fanCount ?: 0,
                        picksCount = statistics?.pickCount ?: 0
                    )
                }
                else -> {
                    _state.value = FriendProfileUiState.Error(
                        mapFriendProfileErrorMessage(diariesResult.exceptionOrNull()?.message)
                    )
                }
            }
        }
    }
}

private fun mapFriendProfileErrorMessage(rawMessage: String?): String {
    val message = rawMessage.orEmpty()
    val isNotFound = message.contains("404") || message.contains("HTTP 404", ignoreCase = true)
    if (isNotFound) return "탈퇴했거나 존재하지 않는 회원입니다."
    return rawMessage ?: "프로필 로드 실패"
}

