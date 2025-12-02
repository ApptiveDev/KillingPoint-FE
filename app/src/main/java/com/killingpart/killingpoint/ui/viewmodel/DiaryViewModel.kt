package com.killingpart.killingpoint.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.killingpart.killingpoint.data.model.Diary
import com.killingpart.killingpoint.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface DiaryUiState {
    data object Loading : DiaryUiState
    data class Success(val diaries: List<Diary>) : DiaryUiState
    data class Error(val message: String) : DiaryUiState
}

class DiaryViewModel(
    private val repoFactory: (Context) -> AuthRepository = { ctx ->
        AuthRepository(ctx)
    }
) : ViewModel() {

    private val _state = MutableStateFlow<DiaryUiState>(DiaryUiState.Loading)
    val state: StateFlow<DiaryUiState> = _state

    fun loadDiaries(context: Context) {
        _state.value = DiaryUiState.Loading
        val repo = repoFactory(context)
        viewModelScope.launch {
            try {
                // 첫 페이지를 가져와서 전체 페이지 수 확인
                val firstPage = repo.getMyDiaries(page = 0, size = 100)
                val totalPages = firstPage.page.totalPages
                val allDiaries = mutableListOf<com.killingpart.killingpoint.data.model.Diary>()
                
                // 첫 페이지 추가
                allDiaries.addAll(firstPage.content)
                
                // 나머지 페이지들 가져오기
                for (page in 1 until totalPages) {
                    val pageResult = repo.getMyDiaries(page = page, size = 100)
                    allDiaries.addAll(pageResult.content)
                }
                
                if (allDiaries.isNotEmpty()) {
                    _state.value = DiaryUiState.Success(allDiaries)
                } else {
                    _state.value = DiaryUiState.Error("다이어리가 없습니다")
                }
            } catch (e: Exception) {
                _state.value = DiaryUiState.Error(e.message ?: "다이어리 로드 실패")
            }
        }
    }
}
