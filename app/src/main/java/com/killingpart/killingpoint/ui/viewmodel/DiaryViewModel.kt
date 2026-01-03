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
                val firstPage = repo.getMyDiaries(page = 0, size = 1)
                val totalElements = firstPage.page.totalElements
                
                val result = if (totalElements > 0) {
                    repo.getMyDiaries(page = 0, size = totalElements)
                } else {
                    firstPage
                }
                
                _state.value = DiaryUiState.Success(result.content)
            } catch (e: Exception) {
                _state.value = DiaryUiState.Error(e.message ?: "다이어리 로드 실패")
            }
        }
    }
}
