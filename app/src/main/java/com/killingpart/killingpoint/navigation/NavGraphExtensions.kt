package com.killingpart.killingpoint.navigation

import androidx.navigation.NavController

/**
 * 온보딩/튜토리얼을 건너뛸 때 메인으로 이동하고 백스택을 정리한다.
 * [MainActivity]의 초기 네비게이션과 동일하게 graph id 0 기준으로 pop 한다.
 */
fun NavController.navigateToMainClearingStack() {
    navigate("main") {
        popUpTo(0) { inclusive = true }
    }
}
