package com.killingpart.killingpoint.ui.screen.MainScreen


import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily

@Composable
fun TopPillTabs(
    options: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 45.dp,
    containerColor: Color = Color(0xFF101010),
    indicatorColor: Color = Color(0xFFEEEFF3),
    selectedTextColor: Color = Color.Black,
    unselectedTextColor: Color = Color(0xFF7B7B7B),
    cornerRadius: Dp = 40.dp,
    textSize: TextUnit = 12.sp
) {
    require(options.isNotEmpty())

    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current

    val segmentWidth: Dp by remember(containerSize, options.size) {
        mutableStateOf(
            if (containerSize.width == 0) 0.dp
            else with(density) {
                val innerWidthPx = containerSize.width
                (innerWidthPx / options.size).toDp()
            }
        )
    }

    val targetOffsetX: Dp = segmentWidth * selectedIndex
    val animatedOffsetX by animateDpAsState(
        targetValue = targetOffsetX,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "seg_offset"
    )

    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(containerColor)
            .onGloballyPositioned { containerSize = it.size },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = animatedOffsetX)
                .fillMaxHeight()
                .width(segmentWidth)
                .clip(RoundedCornerShape(cornerRadius))
                .background(indicatorColor)
        )

        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            options.forEachIndexed { index, label ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onSelected(index) },
                    contentAlignment = Alignment.Center
                ) {
                    val isSelected = index == selectedIndex
                    Text(
                        text = label,
                        color = if (isSelected) selectedTextColor else unselectedTextColor,
                        fontFamily = PaperlogyFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = textSize
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun SegmentedPillTabsPreview() {
    TopPillTabs(options = listOf("나의 창고", "킬링파트 재생", "뮤직캘린더"),
        selectedIndex = 0,
        onSelected = { idx ->
            0
        },
        modifier = Modifier.fillMaxWidth())
}
