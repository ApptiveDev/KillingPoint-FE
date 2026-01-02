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

    private var currentPage = 0
    private val pageSize = 5

    fun loadFeeds(context: Context, page: Int = 0) {
        _state.value = FeedUiState.Loading
        val repo = repoFactory(context)
        viewModelScope.launch {
            try {
                val result = repo.getFeeds(page = page, size = pageSize)
                currentPage = page
                _state.value = FeedUiState.Success(
                    feeds = result.content,
                    page = result.page,
                    hasMore = page < result.page.totalPages - 1
                )
            } catch (e: Exception) {
                _state.value = FeedUiState.Error(e.message ?: "피드 로드 실패")
            }
        }
    }

    fun loadMoreFeeds(context: Context) {
        val currentState = _state.value
        if (currentState is FeedUiState.Success && currentState.hasMore) {
            loadFeeds(context, currentPage + 1)
        }
    }
}

