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

    fun loadFriendProfile(context: Context, userId: Long) {
        _state.value = FriendProfileUiState.Loading
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
                        diariesResult.exceptionOrNull()?.message ?: "프로필 로드 실패"
                    )
                }
            }
        }
    }
}

