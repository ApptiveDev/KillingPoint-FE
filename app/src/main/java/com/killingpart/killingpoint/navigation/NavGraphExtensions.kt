package com.killingpart.killingpoint.navigation

import androidx.navigation.NavController

/**
 * 온보딩/튜토리얼을 건너뛸 때 메인으로 이동하고 백스택을 정리한다.
 * destination id 0(popUpTo(0))은 그래프에 없어 런타임 크래시가 날 수 있으므로 graph.id 를 쓴다.
 */
fun NavController.navigateToMainClearingStack() {
    OnboardingProgressStore.clearTutorialInProgress(context)
    navigate("main") {
        popUpTo(graph.id) { inclusive = true }
    }
}
