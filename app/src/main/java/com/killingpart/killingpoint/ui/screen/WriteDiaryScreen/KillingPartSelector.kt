package com.killingpart.killingpoint.ui.screen.WriteDiaryScreen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.killingpart.killingpoint.R
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import com.killingpart.killingpoint.ui.theme.mainGreen
import kotlin.math.roundToInt

fun formatTime(seconds: Float): String {
    val totalSeconds = seconds.toInt().coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val secs = totalSeconds % 60
    return String.format("%02d:%02d", minutes, secs)
}

/** 모든 구성 요소를 하나의 시간 좌표계로 통일한다
 * 타임라인 바 : 절대 위치(px)
 * 핸들 : 화면 위치(px)
 * 스크롤 : 절대 이동량(px)
 * 시간 : (절대 px + 화면 px) / pxPerSecond
 *
 * 핸들은 화면에 고정된 UI 요소
 * 스크롤 + handle(px)를 합쳐야 핸들이 가리키는 절대 시간을 알 수 있음
 * 바의 highlight는 '절대 시간' 기준
 * barCenterSec, startSec, endSec 모두 절대 시간 통일
 */

@Composable
fun KillingPartSelector(
    totalDuration: Int,
    onStartChange: (start: Float, end: Float, duration: Float) -> Unit
) {
    val scrollState = rememberScrollState()
    val density = LocalDensity.current

    val barWidth = 8.dp
    val gap = 8.dp
    val barWidthPx = with(density) { barWidth.toPx() }
    val gapPx = with(density) { gap.toPx() }

    val basePxPerSecond = barWidthPx + gapPx
    var pxPerSecond by remember { mutableStateOf(basePxPerSecond) }

    val timelineWidthPx = (totalDuration + 5f) * pxPerSecond
    val timelineWidthDp = with(density) { timelineWidthPx.toDp() }

    val minDurationSec = 10f
    val maxDurationSec = 30f.coerceAtMost(totalDuration.toFloat())

    val barHeights = remember(totalDuration) {
        (0 until totalDuration).map { (20..50).random().dp }
    }

    var parentWidthPx by remember { mutableStateOf(0f) }

    var leftHandleX by remember { mutableStateOf(0f) }
    var rightHandleX by remember { mutableStateOf(0f) }
    var handlesInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(parentWidthPx) {
        if (parentWidthPx > 0f && !handlesInitialized) {
            val maxDurationSecForScaling =
                39f + ((totalDuration - 200).coerceAtLeast(0) / 50f) * 2f
            val maxAllowedPxPerSecond = parentWidthPx / maxDurationSecForScaling

            pxPerSecond = maxAllowedPxPerSecond
            val initialDurationSec = minDurationSec.coerceAtMost(maxDurationSec)
            val durationPx = initialDurationSec * pxPerSecond
            val center = parentWidthPx / 2f

            leftHandleX = center - durationPx / 2f
            rightHandleX = center + durationPx / 2f

            handlesInitialized = true
        }
    }

    val scrollX = scrollState.value.toFloat()

    val startTime =
        ((scrollX + leftHandleX) / pxPerSecond).coerceIn(0f, totalDuration.toFloat())
    val endTime =
        ((scrollX + rightHandleX) / pxPerSecond).coerceIn(0f, totalDuration.toFloat())
    val durationSec = (endTime - startTime).coerceAtLeast(0f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .onGloballyPositioned {
                parentWidthPx = it.size.width.toFloat()
            }
    ) {
        Row(
            modifier = Modifier
                .width(timelineWidthDp)
                .fillMaxHeight()
                .horizontalScroll(scrollState)
        ) {
            Canvas(
                modifier = Modifier
                    .width(timelineWidthDp)
                    .fillMaxHeight()
            ) {
                val absScrollX = scrollState.value.toFloat()

                val currentStartSec = (absScrollX + leftHandleX) / pxPerSecond
                val currentEndSec = (absScrollX + rightHandleX) / pxPerSecond

                for (i in 0 until totalDuration) {
                    val barAbsX = i * (pxPerSecond + gapPx)
                    val barVisibleX = barAbsX - absScrollX

                    if (barVisibleX + barWidthPx < 0 || barVisibleX > size.width) continue

                    val barHeightPx = barHeights[i].toPx()
                    val top = (size.height - barHeightPx) / 2f

                    val barCenterSec =
                        ((barAbsX - absScrollX) + barWidthPx / 2f) / pxPerSecond

                    val inSelection = barCenterSec - 1f in currentStartSec..currentEndSec

                    val color = if (inSelection) Color.White else Color(0xFF454545)

                    drawRoundRect(
                        color = color,
                        topLeft = Offset(barVisibleX, top),
                        size = Size(barWidthPx, barHeightPx),
                        cornerRadius = CornerRadius(12f, 12f)
                    )
                }
            }
        }

        val handleYOffsetPx = with(density) { 20.dp.toPx().roundToInt() }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .offset { IntOffset(leftHandleX.roundToInt(), handleYOffsetPx) }
                .pointerInput(Unit) {
                    detectDragGestures { change, drag ->
                        change.consume()

                        val parent = parentWidthPx
                        if (parent <= 0f) return@detectDragGestures

                        val candidateX = (leftHandleX + drag.x)
                            .coerceIn(0f, rightHandleX)

                        val candidateDurationSec =
                            (rightHandleX - candidateX) / pxPerSecond

                        if (candidateDurationSec in minDurationSec..maxDurationSec) {
                            leftHandleX = candidateX
                        }
                    }
                }
                .zIndex(10f)
        ) {
            Box(
                modifier = Modifier
                    .width(20.dp)
                    .height(70.dp)
                    .background(
                        mainGreen,
                        RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp)
                    )
                    .border(
                        2.dp,
                        mainGreen,
                        RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.move),
                    contentDescription = "left",
                    modifier = Modifier
                        .size(7.dp, 13.dp)
                        .rotate(180f)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = formatTime(startTime),
                fontFamily = PaperlogyFontFamily,
                fontWeight = FontWeight.W400,
                fontSize = 14.sp,
                color = Color.White
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .offset { IntOffset(rightHandleX.roundToInt(), handleYOffsetPx) }
                .pointerInput(Unit) {
                    detectDragGestures { change, drag ->
                        change.consume()

                        val parent = parentWidthPx
                        if (parent <= 0f) return@detectDragGestures

                        val handleWidthPx = with(density) { 40.dp.toPx() }

                        val candidateX = (rightHandleX + drag.x)
                            .coerceIn(leftHandleX, parent - handleWidthPx)

                        val candidateDurationSec =
                            (candidateX - leftHandleX) / pxPerSecond

                        if (candidateDurationSec in minDurationSec..maxDurationSec) {
                            rightHandleX = candidateX
                        }
                    }
                }
                .zIndex(10f)
        ) {
            Box(
                modifier = Modifier
                    .width(20.dp)
                    .height(70.dp)
                    .background(
                        mainGreen,
                        RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp)
                    )
                    .border(
                        2.dp,
                        mainGreen,
                        RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.move),
                    contentDescription = "right",
                    modifier = Modifier.size(7.dp, 13.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = formatTime(endTime),
                fontFamily = PaperlogyFontFamily,
                fontWeight = FontWeight.W400,
                fontSize = 14.sp,
                color = Color.White
            )
        }
    }

    LaunchedEffect(startTime, endTime, durationSec) {
        onStartChange(startTime, endTime, durationSec)
    }
}

@Preview
@Composable
fun KillingPartSelectorPreview() {
    KillingPartSelector(
        totalDuration = 150,
        onStartChange = { _, _, _ -> }
    )
}
