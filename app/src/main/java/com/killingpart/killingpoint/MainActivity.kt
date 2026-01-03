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
                mutableStateOf<LaunchState?>(null)
            }

            // 자동 로그인 시도
            LaunchedEffect(Unit) {
                loginViewModel.tryAutoLogin(context)
            }

            // 자동 로그인 완료 후 스플래시 표시
            LaunchedEffect(loginState) {
                when (val state = loginState) {
                    is LoginUiState.AutoLoginSuccess,
                    is LoginUiState.Idle,
                    is LoginUiState.Error -> {
                        // 자동 로그인 성공/실패 여부와 관계없이 스플래시 표시
                        if (launchState == null) {
                            launchState = LaunchState.SPLASH
                        }
                    }
                    else -> {
                        // Loading 상태는 처리하지 않음
                    }
                }
            }

            when (launchState) {
                null -> {
                    // 자동 로그인 확인 중 - 로딩 화면 표시
                    Box(modifier = Modifier.fillMaxSize())
                }

                LaunchState.SPLASH -> {
                    VideoSplashScreen(
                        onFinish = {
                            // 스플래시 종료 후 로그인 상태에 따라 화면 전환
                            when (val state = loginState) {
                                is LoginUiState.AutoLoginSuccess -> {
                                    if (state.isNew) {
                                        // 새 사용자는 튜토리얼 표시
                                        launchState = LaunchState.TUTORIAL
                                    } else {
                                        // 기존 사용자는 메인으로
                                        launchState = LaunchState.MAIN
                                    }
                                }
                                else -> {
                                    // 자동 로그인 실패 시 튜토리얼로
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
                    
                    val startDestination = remember {
                        when (val state = loginState) {
                            is LoginUiState.AutoLoginSuccess -> {
                                if (!state.isNew) "main" else "home"
                            }
                            else -> "home"
                        }
                    }
                    
                    LaunchedEffect(Unit) {
                        if (startDestination != "home") {
                            navController.navigate(startDestination) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                    
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .navigationBarsPadding()
                    ) {
                        NavGraph(
                            navController = navController,
                            startDestination = startDestination
                        )
                    }
                }
            }
        }
    }
}
