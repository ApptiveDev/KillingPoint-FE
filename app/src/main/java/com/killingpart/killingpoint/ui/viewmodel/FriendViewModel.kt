package com.killingpart.killingpoint.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.killingpart.killingpoint.data.model.SubscribeResponse
import com.killingpart.killingpoint.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface FriendUiState {
    data object Loading : FriendUiState
    data class Success(
        val picks: SubscribeResponse?,
        val fans: SubscribeResponse?,
        val searchResults: SubscribeResponse? = null
    ) : FriendUiState
    data class Error(val message: String) : FriendUiState
}

class FriendViewModel(
    private val repoFactory: (Context) -> AuthRepository = { ctx ->
        AuthRepository(ctx)
    }
) : ViewModel() {

    private val _state = MutableStateFlow<FriendUiState>(FriendUiState.Loading)
    val state: StateFlow<FriendUiState> = _state

    fun loadFriends(context: Context, userId: Long) {
        _state.value = FriendUiState.Loading
        val repo = repoFactory(context)
        viewModelScope.launch {
            val picksResult = repo.getSubscribes(userId)
            val fansResult = repo.getFans(userId)

            when {
                picksResult.isSuccess && fansResult.isSuccess -> {
                    _state.value = FriendUiState.Success(
                        picks = picksResult.getOrNull(),
                        fans = fansResult.getOrNull()
                    )
                }
                picksResult.isFailure -> {
                    _state.value = FriendUiState.Error(
                        picksResult.exceptionOrNull()?.message ?: "구독 목록 조회 실패"
                    )
                }
                fansResult.isFailure -> {
                    _state.value = FriendUiState.Error(
                        fansResult.exceptionOrNull()?.message ?: "팬덤 목록 조회 실패"
                    )
                }
            }
        }
    }

    fun addSubscribe(context: Context, subscribeToUserId: Long, currentUserId: Long, onSuccess: () -> Unit = {}) {
        val repo = repoFactory(context)
        viewModelScope.launch {
            repo.addSubscribe(subscribeToUserId)
                .onSuccess {
                    // 구독 추가 성공 후 목록 새로고침
                    loadFriends(context, currentUserId)
                    onSuccess()
                }
                .onFailure { e ->
                    // 에러 처리 (필요시 상태 업데이트)
                    android.util.Log.e("FriendViewModel", "구독 추가 실패: ${e.message}")
                }
        }
    }

    fun removeSubscribe(context: Context, subscribeToUserId: Long, currentUserId: Long, onSuccess: () -> Unit = {}) {
        val repo = repoFactory(context)
        viewModelScope.launch {
            repo.removeSubscribe(subscribeToUserId)
                .onSuccess {
                    // 구독 취소 성공 후 목록 새로고침
                    loadFriends(context, currentUserId)
                    onSuccess()
                }
                .onFailure { e ->
                    // 에러 처리 (필요시 상태 업데이트)
                    android.util.Log.e("FriendViewModel", "구독 취소 실패: ${e.message}")
                }
        }
    }

    fun searchUsers(context: Context, searchCond: String, page: Int = 0, size: Int = 5) {
        val repo = repoFactory(context)
        viewModelScope.launch {
            repo.searchUsers(searchCond, page, size)
                .onSuccess { searchResults ->
                    // 검색 결과를 현재 상태에 추가
                    val currentState = _state.value
                    // picks 목록과 비교하여 isMyPick 설정
                    val currentPicks = (currentState as? FriendUiState.Success)?.picks?.content ?: emptyList()
                    val updatedSearchResults = searchResults.copy(
                        content = searchResults.content.map { user ->
                            user.copy(isMyPick = currentPicks.any { it.userId == user.userId })
                        }
                    )
                    
                    if (currentState is FriendUiState.Success) {
                        _state.value = currentState.copy(searchResults = updatedSearchResults)
                    } else {
                        _state.value = FriendUiState.Success(
                            picks = null,
                            fans = null,
                            searchResults = updatedSearchResults
                        )
                    }
                }
                .onFailure { e ->
                    _state.value = FriendUiState.Error(
                        e.message ?: "회원 검색 실패"
                    )
                }
        }
    }

    fun clearSearch() {
        val currentState = _state.value
        if (currentState is FriendUiState.Success) {
            _state.value = currentState.copy(searchResults = null)
        }
    }
}

