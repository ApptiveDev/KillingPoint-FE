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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.rememberNavController
import com.kakao.sdk.common.KakaoSdk
import com.killingpart.killingpoint.navigation.NavGraph
import com.kakao.sdk.common.util.Utility
import com.killingpart.killingpoint.ui.component.SplashScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        KakaoSdk.init(this, getString(R.string.kakao_native_app_key))
        enableEdgeToEdge()

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        setContent {
            var showSplash by remember { mutableStateOf(true) }

            if (showSplash) {
                SplashScreen {
                    showSplash = false
                }
            } else {
                val navController = rememberNavController()
                Box(
                    modifier = Modifier.fillMaxSize()
                        .navigationBarsPadding()
                ) {
                    NavGraph(navController)
                }
            }
        }
    }
}
