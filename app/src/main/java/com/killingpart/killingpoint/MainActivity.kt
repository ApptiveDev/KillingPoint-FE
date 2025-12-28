package com.killingpart.killingpoint

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.kakao.sdk.common.KakaoSdk
import com.killingpart.killingpoint.navigation.NavGraph
import com.killingpart.killingpoint.ui.component.VideoSplashScreen
import com.killingpart.killingpoint.ui.screen.TutorialScreen.TutorialScreen
import com.killingpart.killingpoint.ui.viewmodel.LoginViewModel
import com.killingpart.killingpoint.ui.viewmodel.LoginUiState

class MainActivity : ComponentActivity() {

    enum class LaunchState {
        SPLASH,
        TUTORIAL,
        MAIN
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        KakaoSdk.init(this, getString(R.string.kakao_native_app_key))
        enableEdgeToEdge()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        setContent {
            val context = LocalContext.current
            val loginViewModel: LoginViewModel = viewModel()
            val loginState by loginViewModel.state.collectAsState()

            var launchState by remember {
                mutableStateOf(LaunchState.SPLASH)
            }

            // 자동 로그인 시도
            LaunchedEffect(Unit) {
                loginViewModel.tryAutoLogin(context)
            }

            // 로그인 상태에 따라 튜토리얼 표시 여부 결정
            LaunchedEffect(loginState) {
                when (val state = loginState) {
                    is LoginUiState.AutoLoginSuccess -> {
                        if (state.isNew) {
                            // 새 사용자는 튜토리얼 표시
                            if (launchState == LaunchState.SPLASH) {
                                launchState = LaunchState.TUTORIAL
                            }
                        } else {
                            // 기존 사용자는 바로 메인으로
                            if (launchState == LaunchState.SPLASH) {
                                launchState = LaunchState.MAIN
                            }
                        }
                    }
                    is LoginUiState.Idle -> {
                        // 로그인 안 된 상태면 튜토리얼 후 로그인 화면
                        if (launchState == LaunchState.SPLASH) {
                            launchState = LaunchState.TUTORIAL
                        }
                    }
                    else -> {
                        // Loading, Error 등은 처리하지 않음
                    }
                }
            }

            when (launchState) {

                LaunchState.SPLASH -> {
                    VideoSplashScreen(
                        onFinish = {
                            // 스플래시 종료 후 로그인 상태 확인
                            when (val state = loginState) {
                                is LoginUiState.AutoLoginSuccess -> {
                                    if (state.isNew) {
                                        launchState = LaunchState.TUTORIAL
                                    } else {
                                        launchState = LaunchState.MAIN
                                    }
                                }
                                else -> {
                                    launchState = LaunchState.TUTORIAL
                                }
                            }
                        }
                    )
                }

                LaunchState.TUTORIAL -> {
                    TutorialScreen(
                        onFinish = {
                            launchState = LaunchState.MAIN
                        }
                    )
                }

                LaunchState.MAIN -> {
                    val navController = rememberNavController()
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .navigationBarsPadding()
                    ) {
                        NavGraph(navController)
                    }
                }
            }
        }
    }
}
