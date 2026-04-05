package com.killingpart.killingpoint.ui.viewmodel

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * [NavHost] 내부에서는 기본 [viewModel] 이 백스택 엔트리 스코프가 되어
 * [MainAppContent] 의 Activity 스코프 [LoginViewModel] 과 **서로 다른 인스턴스**가 된다.
 * 로그인/자동로그인/시작 목적지는 모두 같은 VM 을 봐야 하므로 Activity 기준으로 통일한다.
 */
@Composable
fun activityScopedLoginViewModel(): LoginViewModel {
    val activity = LocalActivity.current as? ComponentActivity
    return if (activity != null) {
        viewModel(viewModelStoreOwner = activity)
    } else {
        viewModel()
    }
}
