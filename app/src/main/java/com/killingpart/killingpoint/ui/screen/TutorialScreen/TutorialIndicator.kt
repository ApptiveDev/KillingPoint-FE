package com.killingpart.killingpoint.ui.screen.TutorialScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily

@Composable
fun TutorialIndicator(step: Int,
                      totalStep: Int = 6) {
    Box(
        modifier = Modifier.size(48.dp, 43.dp)
            .background(color = Color(0xFF383838), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = step.toString() + "/" + totalStep.toString(),
            fontFamily = PaperlogyFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = Color(0xFFA5A5A5)
        )
    }
}

@Preview
@Composable
fun TutorialIndicatorPreview() {
    TutorialIndicator(4)
}