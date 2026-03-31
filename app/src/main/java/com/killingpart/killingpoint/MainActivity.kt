package com.killingpart.killingpoint

import android.content.pm.ActivityInfo
import android.graphics.Color as AndroidColor
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.kakao.sdk.common.KakaoSdk
import com.killingpart.killingpoint.data.repository.AuthRepository
import com.killingpart.killingpoint.navigation.NavGraph
import com.killingpart.killingpoint.ui.component.VideoSplashScreen
import com.killingpart.killingpoint.ui.viewmodel.LoginViewModel
import com.killingpart.killingpoint.ui.viewmodel.LoginUiState
import com.killingpart.killingpoint.BuildConfig

class MainActivity : ComponentActivity() {

    enum class LaunchState {
        SPLASH,
        MAIN
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        KakaoSdk.init(this, getString(R.string.kakao_native_app_key))
        enableEdgeToEdge()
        window.statusBarColor = AndroidColor.BLACK
        window.navigationBarColor = AndroidColor.BLACK
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
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
            var resolvedStartDestination by rememberSaveable {
                mutableStateOf<String?>(null)
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

            LaunchedEffect(loginState, context) {
                when (val s = loginState) {
                    is LoginUiState.AutoLoginSuccess -> {
                        val isNew = s.isNew
                        val repo = AuthRepository(context)
                        val start = repo.getUserInitSettings()
                            .getOrNull()
                            ?.let { init ->
                                when {
                                    init.needsPolicyAgreement -> "onboarding_policy"
                                    init.needsTagSetup || isNew -> "onboarding_name"
                                    else -> "main"
                                }
                            } ?: "home"
                        resolvedStartDestination = start
                    }

                    is LoginUiState.Idle, is LoginUiState.Error -> {
                        resolvedStartDestination = "home"
                    }

                    is LoginUiState.Success -> {
                        resolvedStartDestination = "home"
                    }

                    is LoginUiState.Loading -> {
                        resolvedStartDestination = null
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                when (launchState) {
                    LaunchState.SPLASH -> {
                        VideoSplashScreen(
                            onFinish = {
                                launchState = LaunchState.MAIN
                            },
                            canFinish = canFinishSplash
                        )
                    }

                    LaunchState.MAIN -> {
                        val navController = rememberNavController()

                        val startDestination = resolvedStartDestination ?: "home"

                        LaunchedEffect(startDestination, resolvedStartDestination) {
                            if (resolvedStartDestination != null && startDestination != "home") {
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
                            if (BuildConfig.DEBUG) {
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .statusBarsPadding()
                                        .padding(top = 8.dp, end = 8.dp),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    TextButton(
                                        onClick = { navController.navigate("onboarding_policy") }
                                    ) {
                                        Text("약관 화면")
                                    }
                                    TextButton(
                                        onClick = { navController.navigate("onboarding_name") }
                                    ) {
                                        Text("이름·태그")
                                    }
                                    TextButton(
                                        onClick = { navController.navigate("onboarding_kp_intro") }
                                    ) {
                                        Text("튜토리얼 시작")
                                    }
                                    TextButton(
                                        onClick = { navController.navigate("add_music?tutorial=true") }
                                    ) {
                                        Text("곡 검색 튜토리얼")
                                    }
                                    TextButton(
                                        onClick = { navController.navigate("onboarding_home_preview") }
                                    ) {
                                        Text("홈 프리뷰 튜토리얼")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
