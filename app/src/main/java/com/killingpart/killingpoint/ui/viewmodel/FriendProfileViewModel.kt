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
        val diaries: MyDiaries?
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
            // TODO: 유저 정보를 가져오는 API가 필요할 수 있음
            // 일단 일기만 로드
            repo.getUserDiaries(userId)
                .onSuccess { diaries ->
                    _state.value = FriendProfileUiState.Success(
                        user = null, // TODO: 유저 정보 API 추가 필요
                        diaries = diaries
                    )
                }
                .onFailure { e ->
                    _state.value = FriendProfileUiState.Error(
                        e.message ?: "프로필 로드 실패"
                    )
                }
        }
    }
}

