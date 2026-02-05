package com.killingpart.killingpoint.ui.component

import android.graphics.Paint
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * 가로로 길어질 수 있는 텍스트를 자동 스크롤(마키)으로 표시합니다.
 * 텍스트가 영역보다 길면 자동으로 좌우로 흐르며, 짧으면 그대로 표시합니다.
 * @param centerAlign true면 텍스트가 짧을 때 가운데 정렬, 길면 스크롤 시 왼쪽부터 표시
 */
@Composable
fun ScrollableText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 14.sp,
    fontFamily: FontFamily? = null,
    fontWeight: FontWeight? = null,
    color: Color = Color.White,
    centerAlign: Boolean = false
) {
    val scrollState = rememberScrollState()
    var viewportWidth by remember(text) { mutableStateOf(0) }
    var contentWidth by remember(text) { mutableStateOf(0) }

    val contentAlignment = when {
        !centerAlign -> Alignment.Start
        contentWidth > viewportWidth && viewportWidth > 0 -> Alignment.CenterStart
        else -> Alignment.CenterHorizontally
    }

    if (centerAlign) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clipToBounds()
                .onGloballyPositioned { viewportWidth = it.size.width },
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.horizontalScroll(scrollState)
            ) {
                Text(
                    text = text,
                    fontSize = fontSize,
                    fontFamily = fontFamily,
                    fontWeight = fontWeight ?: FontWeight.Normal,
                    color = color,
                    softWrap = false,
                    modifier = Modifier.onGloballyPositioned { contentWidth = it.size.width }
                )
            }
        }
    } else {
        Row(
            modifier = modifier
                .onGloballyPositioned { viewportWidth = it.size.width }
                .horizontalScroll(scrollState)
        ) {
            Text(
                text = text,
                fontSize = fontSize,
                fontFamily = fontFamily,
                fontWeight = fontWeight ?: FontWeight.Normal,
                color = color,
                softWrap = false,
                modifier = Modifier.onGloballyPositioned { contentWidth = it.size.width }
            )
        }
    }

    LaunchedEffect(text, viewportWidth, contentWidth) {
        val maxScroll = (contentWidth - viewportWidth).coerceAtLeast(0)
        if (maxScroll <= 0) return@LaunchedEffect
        while (true) {
            delay(1500)
            scrollState.animateScrollTo(
                value = maxScroll,
                animationSpec = tween(
                    durationMillis = (maxScroll / 10).coerceIn(3000, 10000),
                    easing = LinearEasing
                )
            )
            delay(1500)
            scrollState.scrollTo(0)
        }
    }
}
