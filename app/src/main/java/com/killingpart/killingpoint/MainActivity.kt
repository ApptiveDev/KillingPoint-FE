package com.killingpart.killingpoint

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
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

            var canFinishSplash by remember {
                mutableStateOf(false)
            }

            LaunchedEffect(Unit) {
                loginViewModel.tryAutoLogin(context)
            }

            LaunchedEffect(loginState) {
                when (val state = loginState) {
                    is LoginUiState.AutoLoginSuccess, 
                    is LoginUiState.Idle, 
                    is LoginUiState.Error, 
                    is LoginUiState.Success -> {
                        canFinishSplash = true
                    }
                    is LoginUiState.Loading -> {
                        canFinishSplash = false
                    }
                }
            }

            when (launchState) {
                LaunchState.SPLASH -> {
                    VideoSplashScreen(
                        onFinish = {
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
                        },
                        canFinish = canFinishSplash
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
                    
                    val startDestination = remember(loginState) {
                        when (val state = loginState) {
                            is LoginUiState.AutoLoginSuccess -> {
                                if (!state.isNew) "main" else "home"
                            }
                            else -> "home"
                        }
                    }
                    
                    LaunchedEffect(startDestination) {
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
