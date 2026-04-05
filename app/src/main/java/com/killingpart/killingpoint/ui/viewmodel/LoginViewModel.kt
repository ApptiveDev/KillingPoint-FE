package com.killingpart.killingpoint.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.killingpart.killingpoint.auth.KakaoLoginClient
import com.killingpart.killingpoint.data.local.TokenStore
import com.killingpart.killingpoint.data.remote.RetrofitClient
import com.killingpart.killingpoint.data.repository.AuthRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface LoginUiState {
    data object Idle : LoginUiState
    data object Loading : LoginUiState
    data object Success : LoginUiState
    data class Error(val message: String) : LoginUiState
    data class AutoLoginSuccess(val isNew: Boolean) : LoginUiState // 자동 로그인 성공 (isNew 포함)
}

class LoginViewModel(
    private val repoFactory: (Context) -> AuthRepository = { ctx ->
        AuthRepository(ctx)
    }
) : ViewModel() {

    private val _state = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val state: StateFlow<LoginUiState> = _state

    fun loginWithKakao(context: Context, onSuccess: (String) -> Unit) {
        _state.value = LoginUiState.Loading
        viewModelScope.launch {
            try {
                val kakaoAccessToken = KakaoLoginClient.getAccessToken(context)
                onSuccess(kakaoAccessToken)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value = LoginUiState.Error(e.message ?: "카카오 로그인 실패")
            }
        }
    }

    fun loginWithServer(context: Context, kakaoAccessToken: String) {
        _state.value = LoginUiState.Loading
        val repo = repoFactory(context)
        viewModelScope.launch {
            repo.exchangeKakaoAccessToken(kakaoAccessToken)
                .onSuccess { isNew ->
                    _state.value = LoginUiState.AutoLoginSuccess(isNew)
                }
                .onFailure { _state.value = LoginUiState.Error(it.message ?: "로그인 실패") }
        }
    }

    fun loginWithTest(context: Context) {
        _state.value = LoginUiState.Loading
        val repo = repoFactory(context)
        viewModelScope.launch {
            repo.loginWithTest()
                .onSuccess { isNew ->
                    _state.value = LoginUiState.AutoLoginSuccess(isNew)
                }
                .onFailure { _state.value = LoginUiState.Error(it.message ?: "테스터 로그인 실패") }
        }
    }

    fun tryAutoLogin(context: Context) {
        _state.value = LoginUiState.Loading
        val repo = repoFactory(context)
        viewModelScope.launch {
            val refreshToken = repo.getRefreshToken()
            android.util.Log.d("LoginViewModel", "자동 로그인 시도: refreshToken 존재=${refreshToken != null}")
            
            if (refreshToken != null) {
                repo.refreshAccessToken()
                    .onSuccess { isNew ->
                        android.util.Log.d("LoginViewModel", "자동 로그인 성공: isNew=$isNew")
                        _state.value = LoginUiState.AutoLoginSuccess(isNew)
                    }
                    .onFailure { e ->
                        android.util.Log.e("LoginViewModel", "자동 로그인 실패: ${e.message}")
                        // 토큰 갱신 실패 시 로그인 화면 유지
                        _state.value = LoginUiState.Idle
                    }
            } else {
                android.util.Log.d("LoginViewModel", "자동 로그인 실패: refreshToken 없음")
                // refreshToken이 없으면 로그인 화면 유지
                _state.value = LoginUiState.Idle
            }
        }
    }
}
