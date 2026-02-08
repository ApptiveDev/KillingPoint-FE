package com.killingpart.killingpoint.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.killingpart.killingpoint.data.model.FeedDiary
import com.killingpart.killingpoint.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface FeedUiState {
    data object Loading : FeedUiState
    data class Success(
        val feeds: List<FeedDiary>,
        val page: com.killingpart.killingpoint.data.model.DiaryPage,
        val hasMore: Boolean
    ) : FeedUiState
    data class Error(val message: String) : FeedUiState
}

class FeedViewModel(
    private val repoFactory: (Context) -> AuthRepository = { ctx ->
        AuthRepository(ctx)
    }
) : ViewModel() {

    private val _state = MutableStateFlow<FeedUiState>(FeedUiState.Loading)
    val state: StateFlow<FeedUiState> = _state

    fun updateFeeds(feeds: List<FeedDiary>) {
        val currentState = _state.value
        if (currentState is FeedUiState.Success) {
            _state.value = FeedUiState.Success(
                feeds = feeds,
                page = currentState.page,
                hasMore = currentState.hasMore
            )
        }
    }

    fun loadFeeds(context: Context) {
        _state.value = FeedUiState.Loading
        val repo = repoFactory(context)
        viewModelScope.launch {
            try {
                val firstPageResult = repo.getFeeds(page = 0, size = 1)
                val totalElements = firstPageResult.page.totalElements
                
                val result = if (totalElements > 0) {
                    repo.getFeeds(page = 0, size = totalElements)
                } else {
                    firstPageResult
                }
                
                _state.value = FeedUiState.Success(
                    feeds = result.content,
                    page = result.page,
                    hasMore = false
                )
            } catch (e: Exception) {
                _state.value = FeedUiState.Error(e.message ?: "피드 로드 실패")
            }
        }
    }

    fun toggleLike(context: Context, diaryId: Long, onSuccess: (Boolean) -> Unit, onFailure: (() -> Unit)? = null) {
        val repo = repoFactory(context)
        viewModelScope.launch {
            try {
                val result = repo.toggleLike(diaryId)
                result.onSuccess { likeResponse ->
                    onSuccess(likeResponse.isLiked)
                }.onFailure { e ->
                    android.util.Log.e("FeedViewModel", "좋아요 토글 실패: ${e.message}")
                    onFailure?.invoke()
                }
            } catch (e: Exception) {
                android.util.Log.e("FeedViewModel", "좋아요 토글 실패: ${e.message}")
                onFailure?.invoke()
            }
        }
    }

    fun toggleStore(context: Context, diaryId: Long, onSuccess: (Boolean) -> Unit, onFailure: (() -> Unit)? = null) {
        val repo = repoFactory(context)
        viewModelScope.launch {
            try {
                val result = repo.toggleStore(diaryId)
                result.onSuccess { storeResponse ->
                    onSuccess(storeResponse.isStored)
                }.onFailure { e ->
                    android.util.Log.e("FeedViewModel", "보관 토글 실패: ${e.message}")
                    onFailure?.invoke()
                }
            } catch (e: Exception) {
                android.util.Log.e("FeedViewModel", "보관 토글 실패: ${e.message}")
                onFailure?.invoke()
            }
        }
    }
}

