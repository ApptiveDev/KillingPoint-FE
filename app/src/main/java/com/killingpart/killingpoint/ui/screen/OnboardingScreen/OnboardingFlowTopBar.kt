package com.killingpart.killingpoint.ui.screen.OnboardingScreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily

/** 상태 표시줄 아래 동일한 영역에 맞추기 위한 튜토리얼 상단 바 공통 패딩 */
private val TopBarHorizontalPadding = 8.dp
private val TopBarVerticalPadding = 8.dp
private val TopBarMinHeight = 48.dp

/**
 * 온보딩 튜토리얼 연속 화면(곡 검색 → 홈 프리뷰 → 피드 데모 등)에서 상단 바 위치를 통일한다.
 * [onBack] 이 null 이면 뒤로 버튼만 숨기고 건너뛰기는 우측 정렬을 유지한다.
 */
@Composable
fun OnboardingFlowTopBar(
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .heightIn(min = TopBarMinHeight)
            .padding(
                horizontal = TopBarHorizontalPadding,
                vertical = TopBarVerticalPadding
            )
    ) {
        if (onBack != null) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "뒤로",
                    tint = Color.White
                )
            }
        }
        TextButton(
            onClick = onSkip,
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Text(
                text = "건너뛰기",
                color = Color.White,
                fontFamily = PaperlogyFontFamily,
                fontSize = 15.sp,
                textDecoration = TextDecoration.Underline
            )
        }
    }
}
