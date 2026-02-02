package com.killingpart.killingpoint.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.killingpart.killingpoint.data.model.FeedDiary
import com.killingpart.killingpoint.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface SearchUiState {
    data object Loading : SearchUiState
    data class Success(
        val diaries: List<FeedDiary>
    ) : SearchUiState
    data class Error(val message: String) : SearchUiState
}

class SearchViewModel(
    private val repoFactory: (Context) -> AuthRepository = { ctx ->
        AuthRepository(ctx)
    }
) : ViewModel() {

    private val _state = MutableStateFlow<SearchUiState>(SearchUiState.Loading)
    val state: StateFlow<SearchUiState> = _state

    fun updateDiaries(diaries: List<FeedDiary>) {
        _state.value = SearchUiState.Success(diaries = diaries)
    }

    fun loadRandomDiaries(context: Context) {
        _state.value = SearchUiState.Loading
        val repo = repoFactory(context)
        viewModelScope.launch {
            try {
                val result = repo.getRandomDiaries()
                _state.value = SearchUiState.Success(diaries = result)
            } catch (e: Exception) {
                _state.value = SearchUiState.Error(e.message ?: "무작위 일기 로드 실패")
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
                    android.util.Log.e("SearchViewModel", "좋아요 토글 실패: ${e.message}")
                    onFailure?.invoke()
                }
            } catch (e: Exception) {
                android.util.Log.e("SearchViewModel", "좋아요 토글 실패: ${e.message}")
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
                    android.util.Log.e("SearchViewModel", "보관 토글 실패: ${e.message}")
                    onFailure?.invoke()
                }
            } catch (e: Exception) {
                android.util.Log.e("SearchViewModel", "보관 토글 실패: ${e.message}")
                onFailure?.invoke()
            }
        }
    }
}
