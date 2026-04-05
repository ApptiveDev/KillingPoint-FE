package com.killingpart.killingpoint

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.killingpart.killingpoint.data.repository.AuthRepository
import com.killingpart.killingpoint.navigation.NavGraph
import com.killingpart.killingpoint.navigation.OnboardingProgressStore
import com.killingpart.killingpoint.ui.component.VideoSplashScreen
import com.killingpart.killingpoint.ui.viewmodel.LoginUiState
import com.killingpart.killingpoint.ui.viewmodel.LoginViewModel
import com.killingpart.killingpoint.ui.viewmodel.activityScopedLoginViewModel

private enum class AppLaunchPhase {
    SPLASH,
    MAIN
}

/**
 * [MainActivity]의 setContent 본문을 분리한다.
 * setContent 내부에 LaunchedEffect/when 을 깊게 중첩하면 ComposableSingletons 기반 클래스가
 * 일부 기기에서 NoClassDefFoundError 로 로드되지 않는 경우가 있어, 별도 Composable 로 뺀다.
 */
@Composable
fun MainAppContent() {
    val context = LocalContext.current
    val loginViewModel: LoginViewModel = activityScopedLoginViewModel()
    val loginState by loginViewModel.state.collectAsState()

    var launchPhase by remember { mutableStateOf(AppLaunchPhase.SPLASH) }
    var canFinishSplash by remember { mutableStateOf(false) }
    var resolvedStartDestination by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        loginViewModel.tryAutoLogin(context)
    }

    LaunchedEffect(loginState) {
        when (loginState) {
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

    LaunchedEffect(loginState) {
        when (val s = loginState) {
            is LoginUiState.AutoLoginSuccess -> {
                val repo = AuthRepository(context)
                val start = repo.getUserInitSettings()
                    .getOrNull()
                    ?.let { init ->
                        when {
                            init.needsPolicyAgreement -> "onboarding_policy"
                            init.needsTagSetup -> "onboarding_name"
                            OnboardingProgressStore.isTutorialInProgress(context) -> "onboarding_kp_intro"
                            else -> "main"
                        }
                    } ?: "main"
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
        when (launchPhase) {
            AppLaunchPhase.SPLASH -> {
                VideoSplashScreen(
                    onFinish = { launchPhase = AppLaunchPhase.MAIN },
                    canFinish = canFinishSplash
                )
            }
            AppLaunchPhase.MAIN -> {
                MainNavHost(
                    resolvedStartDestination = resolvedStartDestination
                )
            }
        }
    }
}

@Composable
private fun MainNavHost(resolvedStartDestination: String?) {
    val navController = rememberNavController()
    val startDestination = resolvedStartDestination ?: "home"

    LaunchedEffect(startDestination, resolvedStartDestination) {
        if (resolvedStartDestination != null && startDestination != "home") {
            navController.navigate(startDestination) {
                popUpTo(navController.graph.id) { inclusive = true }
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
        if (BuildConfig.DEBUG && BuildConfig.SHOW_DEV_MENU) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 8.dp, end = 8.dp),
                horizontalAlignment = Alignment.End
            ) {
                TextButton(
                    onClick = { navController.navigate("onboarding_policy") }
                ) { Text("약관 화면") }
                TextButton(
                    onClick = { navController.navigate("onboarding_name") }
                ) { Text("이름·태그") }
                TextButton(
                    onClick = { navController.navigate("onboarding_kp_intro") }
                ) { Text("튜토리얼 시작") }
                TextButton(
                    onClick = { navController.navigate("add_music?tutorial=true") }
                ) { Text("곡 검색 튜토리얼") }
                TextButton(
                    onClick = { navController.navigate("onboarding_home_preview") }
                ) { Text("홈 프리뷰 튜토리얼") }
                TextButton(
                    onClick = { navController.navigate("onboarding_finish") }
                ) { Text("마지막 화면") }
            }
        }
    }
}
