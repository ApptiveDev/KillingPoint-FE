package com.killingpart.killingpoint.ui.screen.DiaryDetailScreen

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.killingpart.killingpoint.ui.screen.MainScreen.TimeLabelCentered
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import com.killingpart.killingpoint.ui.theme.mainGreen
import java.util.regex.Pattern

/**
 * ISO 8601 duration 형식(예: "PT2M28S")을 초 단위로 변환
 * @param duration ISO 8601 duration 문자열 (예: "PT2M28S", "PT1H2M30S", "PT30S")
 * @return 초 단위로 변환된 값 (예: 148, 3750, 30)
 */


@Composable
fun MusicTimeBarForDiaryDetail(
    artist: String,
    musicTitle: String,
    start: Int,
    during: Int,
    totalDuration: Int? = null, // DB에서 가져온 totalDuration (초 단위)
) {
    val density = LocalDensity.current
    var barSize by remember { mutableStateOf(IntSize.Zero) }
    
    // DB에서 가져온 totalDuration 사용, 없으면 기본값 180초
    val total = totalDuration ?: 180

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { barSize = it.size },
                contentAlignment = Alignment.CenterStart
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
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
                    
                    android.util.Log.d("MusicTimeBarForDiaryDetail", "Canvas drawing values:")
                    android.util.Log.d("MusicTimeBarForDiaryDetail", "  - start: $start, during: $during, total: $total")
                    android.util.Log.d("MusicTimeBarForDiaryDetail", "  - width: $w, height: $h")
                    android.util.Log.d("MusicTimeBarForDiaryDetail", "  - startX: $startX, endX: $endX")
                    
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

                val x0 = 0f
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
                
                // 최소 간격 (텍스트 폭의 절반씩 + 여유 공간 8dp)
                val minSpacing = 8f
                val minDistanceStartEnd = (startTextWidth + endTextWidth) / 2f + minSpacing
                val minDistanceEndTotal = (endTextWidth + totalTextWidth) / 2f + minSpacing
                
                // 초기 위치
                var adjustedXStart = xStart
                var adjustedXEnd = xEnd
                
                // start 레이블의 실제 범위: [adjustedXStart - startTextWidth/2, adjustedXStart + startTextWidth/2]
                // end 레이블의 실제 범위: [adjustedXEnd - endTextWidth/2, adjustedXEnd + endTextWidth/2]
                // total 레이블의 실제 범위: [xTotal - totalTextWidth/2, xTotal + totalTextWidth/2]
                
                // 1. start와 end가 겹치는지 확인 및 조정
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
                
                // 2. end와 total이 겹치는지 확인 및 조정
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

                Box(Modifier.fillMaxWidth().height(20.dp)) {
                    // start
                    TimeLabelCentered(startTimeText, adjustedXStart.coerceAtLeast(startTextWidth / 2f), barWidthDp)
                    
                    // start + during (end)
                    TimeLabelCentered(endTimeText, adjustedXEnd.coerceAtMost(xTotal - totalTextWidth / 2f - minSpacing), barWidthDp)

                    // total
                    TimeLabelCentered(totalTimeText, xTotal, barWidthDp)
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

