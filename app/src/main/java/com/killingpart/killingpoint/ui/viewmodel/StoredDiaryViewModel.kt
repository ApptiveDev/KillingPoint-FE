package com.killingpart.killingpoint.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.killingpart.killingpoint.data.model.StoredDiary
import com.killingpart.killingpoint.data.repository.AuthRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class StoredDiariesUiState(
    val diaries: List<StoredDiary> = emptyList(),
    val totalPages: Int = 0,
    val currentPage: Int = -1,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false
)

class StoredDiaryViewModel(
    private val repoFactory: (Context) -> AuthRepository = { ctx ->
        AuthRepository(ctx)
    }
) : ViewModel() {

    private val _state = MutableStateFlow(StoredDiariesUiState())
    val state: StateFlow<StoredDiariesUiState> = _state

    private var refreshJob: Job? = null

    fun loadStoredDiaries(context: Context) {
        val current = _state.value
        if (current.isLoading || current.isLoadingMore || refreshJob?.isActive == true) return
        val repo = repoFactory(context)

        if (current.currentPage < 0) {
            _state.value = current.copy(isLoading = true)
            viewModelScope.launch {
                repo.getStoredDiariesPage(page = 0, size = PAGE_SIZE)
                    .onSuccess { response ->
                        _state.value = _state.value.copy(
                            diaries = response.content,
                            totalPages = response.page.totalPages,
                            currentPage = 0
                        )
                    }
                    .onFailure { e ->
                        android.util.Log.e("StoredDiaryViewModel", "보관 일기 조회 실패: ${e.message}")
                    }
                _state.value = _state.value.copy(isLoading = false)
            }
        } else {
            // 이미 불러온 범위를 Loading 없이 한 번에 새로고침 (스크롤 위치 유지)
            val loadedPages = current.currentPage + 1
            refreshJob = viewModelScope.launch {
                repo.getStoredDiariesPage(page = 0, size = PAGE_SIZE * loadedPages)
                    .onSuccess { response ->
                        val totalPages =
                            (response.page.totalElements + PAGE_SIZE - 1) / PAGE_SIZE
                        _state.value = _state.value.copy(
                            diaries = response.content,
                            totalPages = totalPages,
                            currentPage = minOf(current.currentPage, maxOf(totalPages - 1, 0))
                        )
                    }
                    .onFailure { e ->
                        android.util.Log.e("StoredDiaryViewModel", "보관 일기 새로고침 실패: ${e.message}")
                    }
            }
        }
    }

    fun loadMore(context: Context) {
        val current = _state.value
        if (current.currentPage < 0 || current.currentPage + 1 >= current.totalPages) return
        if (current.isLoading || current.isLoadingMore || refreshJob?.isActive == true) return
        val repo = repoFactory(context)
        val nextPage = current.currentPage + 1
        _state.value = current.copy(isLoadingMore = true)
        viewModelScope.launch {
            repo.getStoredDiariesPage(page = nextPage, size = PAGE_SIZE)
                .onSuccess { response ->
                    _state.value = _state.value.copy(
                        diaries = _state.value.diaries + response.content,
                        currentPage = nextPage
                    )
                }
                .onFailure { e ->
                    android.util.Log.e("StoredDiaryViewModel", "보관 일기 추가 로드 실패: ${e.message}")
                }
            _state.value = _state.value.copy(isLoadingMore = false)
        }
    }

    companion object {
        private const val PAGE_SIZE = 20
    }
}
