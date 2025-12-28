package com.killingpart.killingpoint.ui.screen.TutorialScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.killingpart.killingpoint.R
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import com.killingpart.killingpoint.ui.theme.mainGreen

/**
 * Screen 파일에서 step 데이터 받아서 한 페이지 UI 담당
 */

@Composable
fun TutorialPage(step: TutorialStep,
                 totalSteps: Int = 5,
                 onNext: () -> Unit,
                 onPrev: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {

            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TutorialIndicator(
                    step = step.step,
                    totalStep = totalSteps
                )

                Text(
                    text = "KILLING TIPS!",
                    fontFamily = PaperlogyFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    color = mainGreen
                )

                Box(
                    modifier = Modifier.width(48.dp)

                ) {
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally

            ) {
                Text(
                    text = step.description,
                    fontFamily = PaperlogyFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Image(
                    painter = painterResource(step.image),
                    contentDescription = "튜토리얼 이미지",
                    modifier = Modifier.size(329.dp, 681.dp)
                )
            }

        }

        if (step.step > 1) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 6.dp, top = 20.dp)
                    .size(46.dp, 94.dp)
                    .background(Color(0xFF6C6C6C).copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp))
                    .clickable {onPrev()},
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.on_prev),
                    contentDescription = "onPrev 버튼",
                    modifier = Modifier.size(19.dp, 31.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 6.dp, top = 20.dp)
                .size(46.dp, 94.dp)
                .background(Color(0xFF6C6C6C).copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp))
                .clickable {onNext()},
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.on_prev),
                contentDescription = "onNext 버튼",
                modifier = Modifier.size(19.dp, 31.dp).rotate(180f)
            )
        }
    }

}

@Preview
@Composable
fun TutorialPagePreview() {
    TutorialPage(
        step = tutorialSteps[1],
        totalSteps = tutorialSteps.size,
        onNext = {},
        onPrev = {})
}