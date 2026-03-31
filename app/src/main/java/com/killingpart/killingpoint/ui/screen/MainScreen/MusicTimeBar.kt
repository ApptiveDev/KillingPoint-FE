package com.killingpart.killingpoint.ui.screen.MainScreen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.killingpart.killingpoint.ui.component.ScrollableText
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import com.killingpart.killingpoint.ui.theme.mainGreen

@Composable
fun MusicTimeBar(
    title: String? = null,
    start: Int,
    during: Int,
    total: Int,
) {
    val density = LocalDensity.current
    var barSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color.Black, shape = RoundedCornerShape(16.dp))
            .padding(horizontal = 45.dp, vertical = 8.dp)
    ) {

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ScrollableText(
                    text = title ?: "킬링파트",
                    modifier = Modifier.weight(1f),
                    fontSize = 14.sp,
                    fontFamily = PaperlogyFontFamily,
                    fontWeight = FontWeight.Thin,
                    color = Color.White
                )
            }

            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { barSize = it.size },
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                ) {
                    val h = size.height
                    val w = size.width

                    drawLine(
                        color = Color.White.copy(alpha = 0.85f),
                        start = Offset(0f, h / 2f),
                        end = Offset(w, h / 2f),
                        strokeWidth = with(density) { 2.dp.toPx() }
                    )

                    val startX = (start.toFloat() / total) * w
                    val endX = ((start + during).toFloat() / total) * w

                    drawLine(
                        color = mainGreen,
                        start = Offset(startX, h / 2f),
                        end = Offset(endX, h / 2f),
                        strokeWidth = with(density) { 6.dp.toPx() },
                        cap = StrokeCap.Round
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            if (barSize.width > 0) {
                val barWidthPx = barSize.width.toFloat()
                val barWidthDp = with(density) { barWidthPx.toDp() }.value

                val xStart = (start.toFloat() / total) * barWidthDp
                val xEnd = ((start + during).toFloat() / total) * barWidthDp
                val xTotal = barWidthDp

                val measurer = rememberTextMeasurer()
                val style = TextStyle(
                    fontSize = 10.sp,
                    fontFamily = PaperlogyFontFamily,
                    fontWeight = FontWeight.Thin
                )

                val tStart = formatTime(start)
                val tEnd = formatTime(start + during)

                val startWidth = with(density) { measurer.measure(tStart, style).size.width.toDp().value }
                val endWidth = with(density) { measurer.measure(tEnd, style).size.width.toDp().value }

                val minSpacing = 8f

                // 초기 center clamp
                var adjustedXStart = xStart.coerceAtLeast(startWidth / 2f)
                var adjustedXEnd = xEnd.coerceAtMost(xTotal - endWidth / 2f)

                val endRightLimit = xTotal - endWidth / 2f
                val startLeftLimit = startWidth / 2f

                fun startRight() = adjustedXStart + startWidth / 2f
                fun endLeft() = adjustedXEnd - endWidth / 2f

                // 1. start → end 기본 충돌 처리
                val gap1 = endLeft() - startRight()
                if (gap1 < minSpacing) {
                    val need = minSpacing - gap1
                    val half = need / 2f

                    adjustedXStart = (adjustedXStart - half).coerceAtLeast(startLeftLimit)
                    adjustedXEnd = (adjustedXEnd + half).coerceAtMost(endRightLimit)
                }

                // 2. end 기준 total 경계로 충돌 방지
                val endRight = adjustedXEnd + endWidth / 2f
                if (endRight > endRightLimit) {
                    adjustedXEnd = endRightLimit
                }

                // 3. start ↔ end 재검증
                val gap2 = endLeft() - startRight()
                if (gap2 < minSpacing) {
                    val need = minSpacing - gap2
                    adjustedXEnd = (adjustedXEnd + need).coerceAtMost(endRightLimit)
                }

                // 4. end 기준으로 start가 밀려야 하는 케이스 처리
                val gap3 = endLeft() - startRight()
                if (gap3 < minSpacing) {
                    val need = minSpacing - gap3
                    adjustedXStart = (adjustedXStart - need).coerceAtLeast(startLeftLimit)
                }

                Box(Modifier.fillMaxWidth()) {
                    TimeLabelCentered(tStart, adjustedXStart, barWidthDp)
                    TimeLabelCentered(tEnd, adjustedXEnd, barWidthDp)
                }
            }
        }
    }
}

private fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%d:%02d".format(m, s)
}

@Preview
@Composable
fun MusicTimeBarPreview() {
    MusicTimeBar("테스트 곡", 200, 10, 210)
}
