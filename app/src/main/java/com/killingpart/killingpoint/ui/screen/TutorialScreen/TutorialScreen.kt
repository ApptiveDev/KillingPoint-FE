package com.killingpart.killingpoint.ui.screen.TutorialScreen

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.killingpart.killingpoint.ui.viewmodel.LoginViewModel
import com.killingpart.killingpoint.ui.viewmodel.LoginUiState
import kotlinx.coroutines.launch

/**
 * 유일한 Screen으로 pagerState, pageIndex, navigation 발생 처리
 * */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TutorialScreen(
    navController: NavController,
    onFinish: () -> Unit
) {
    val context = LocalContext.current
    val loginViewModel: LoginViewModel = viewModel()
    val loginState by loginViewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        loginViewModel.tryAutoLogin(context)
    }

    LaunchedEffect(loginState) {
        when (val state = loginState) {
            is LoginUiState.AutoLoginSuccess -> {
                if (!state.isNew) {
                    navController.navigate("main") {
                        popUpTo("tutorial") { inclusive = true }
                    }
                }
            }
            else -> {
            }
        }
    }

    val pages = tutorialSteps
    val totalPageCount = pages.size + 1

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { totalPageCount }
    )

    val coroutineScope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) {
            pageIndex ->

            if (pageIndex == pages.size) {
                TutorialLastPage(
                    onStart = onFinish
                )
            } else {
                TutorialPage(
                    step = pages[pageIndex],
                    totalSteps = pages.size,
                    onNext = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pageIndex + 1)
                        }
                    },
                    onPrev = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pageIndex - 1)
                        }
                    }
                )
            }
        }
    }
}

@Preview
@Composable
fun TutorialScreenPreview() {
    TutorialScreen(
        navController = rememberNavController(),
        onFinish = {}
    )
}