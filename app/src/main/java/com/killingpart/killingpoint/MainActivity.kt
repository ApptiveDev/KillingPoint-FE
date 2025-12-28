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
import androidx.navigation.compose.rememberNavController
import com.kakao.sdk.common.KakaoSdk
import com.killingpart.killingpoint.navigation.NavGraph
import com.killingpart.killingpoint.ui.component.VideoSplashScreen
import com.killingpart.killingpoint.ui.screen.TutorialScreen.TutorialScreen

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

            var launchState by remember {
                mutableStateOf(LaunchState.SPLASH)
            }

            when (launchState) {

                LaunchState.SPLASH -> {
                    VideoSplashScreen(
                        onFinish = {
                            launchState = LaunchState.TUTORIAL
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
