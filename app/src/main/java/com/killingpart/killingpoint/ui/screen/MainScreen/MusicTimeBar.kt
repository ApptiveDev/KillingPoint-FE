package com.killingpart.killingpoint.ui.screen.MainScreen

import android.view.RoundedCorner
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
            .background(color = Color.Black.copy(alpha = 0.8f), shape = RoundedCornerShape(16.dp))
            .padding(horizontal = 45.dp, vertical = 8.dp)
    ) {

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title ?: "로딩 중...",
                fontSize = 14.sp,
                fontFamily = PaperlogyFontFamily,
                fontWeight = FontWeight.Thin,
                color = Color.White
            )
            Spacer(Modifier.weight(1f))
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

            // 텍스트 측정을 위한 measurer
            val textMeasurer = rememberTextMeasurer()
            val textStyle = TextStyle(
                fontSize = 10.sp,
                fontFamily = PaperlogyFontFamily,
                fontWeight = FontWeight.Thin
            )

            val startTimeText = formatTime(start)
            val endTimeText = formatTime(start + during)
            val totalTimeText = formatTime(total)

            val startTextWidth = with(density) {
                textMeasurer.measure(startTimeText, textStyle).size.width.toDp().value
            }
            val endTextWidth = with(density) {
                textMeasurer.measure(endTimeText, textStyle).size.width.toDp().value
            }
            val totalTextWidth = with(density) {
                textMeasurer.measure(totalTimeText, textStyle).size.width.toDp().value
            }

            val minSpacing = 8f
            val minDistanceStartEnd = (startTextWidth + endTextWidth) / 2f + minSpacing
            val minDistanceEndTotal = (endTextWidth + totalTextWidth) / 2f + minSpacing

            // 초기 위치
            var adjustedXStart = xStart
            var adjustedXEnd = xEnd

            val startRightEdge = adjustedXStart + startTextWidth / 2f
            val endLeftEdge = adjustedXEnd - endTextWidth / 2f
            val currentStartEndGap = endLeftEdge - startRightEdge

            if (currentStartEndGap < minSpacing) {
                // 겹치거나 너무 가까우면 분리
                val neededGap = minSpacing - currentStartEndGap
                val halfGap = neededGap / 2f

                // start를 왼쪽으로, end를 오른쪽으로 밀기
                adjustedXStart = (adjustedXStart - halfGap).coerceAtLeast(startTextWidth / 2f)
                adjustedXEnd = (adjustedXEnd + halfGap).coerceAtMost(xTotal - totalTextWidth / 2f - minSpacing)

                // start가 왼쪽 경계에 닿으면 end만 오른쪽으로 더 밀기
                if (adjustedXStart <= startTextWidth / 2f) {
                    val overflow = startTextWidth / 2f - adjustedXStart
                    adjustedXStart = startTextWidth / 2f
                    adjustedXEnd = (adjustedXEnd + overflow).coerceAtMost(xTotal - totalTextWidth / 2f - minSpacing)
                }
            }

            val endRightEdge = adjustedXEnd + endTextWidth / 2f
            val totalLeftEdge = xTotal - totalTextWidth / 2f
            val currentEndTotalGap = totalLeftEdge - endRightEdge

            if (currentEndTotalGap < minSpacing) {
                // end를 왼쪽으로 밀기
                val neededGap = minSpacing - currentEndTotalGap
                adjustedXEnd = (adjustedXEnd - neededGap).coerceAtLeast(adjustedXStart + minDistanceStartEnd)
            }

            // 3. 최종 검증: start와 end가 여전히 겹치지 않는지 확인
            val finalStartRightEdge = adjustedXStart + startTextWidth / 2f
            val finalEndLeftEdge = adjustedXEnd - endTextWidth / 2f
            if (finalEndLeftEdge - finalStartRightEdge < minSpacing) {
                // 여전히 겹치면 end를 오른쪽으로 밀기 (단, total과 겹치지 않도록)
                val finalGap = minSpacing - (finalEndLeftEdge - finalStartRightEdge)
                adjustedXEnd = (adjustedXEnd + finalGap).coerceAtMost(xTotal - totalTextWidth / 2f - minSpacing)
            }

            Box(Modifier.fillMaxWidth()) {
                // start
                TimeLabelCentered(formatTime(start), adjustedXStart.coerceAtLeast(startTextWidth / 2f), barWidthDp)

                // start + during
                TimeLabelCentered(formatTime(start + during), adjustedXEnd.coerceAtMost(xTotal - totalTextWidth / 2f - minSpacing), barWidthDp)
            }
        }
        }
    }
}

@Composable
private fun BoxScope.TimeLabelCentered(text: String, x: Float) {
    Text(
        text = text,
        fontSize = 7.sp,
        color = Color.White,
        fontFamily = PaperlogyFontFamily,
        fontWeight = FontWeight.Thin,
        modifier = Modifier
            .align(Alignment.BottomStart)
            .absoluteOffset(x.dp) // X 좌표에 배치
    )
}

private fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%d:%02d".format(m, s)
}


@Preview
@Composable
fun MusicTimeBarPreview() {
    MusicTimeBar("사랑한단 말의 뜻을 알아가자", 8, 9, 180)
}