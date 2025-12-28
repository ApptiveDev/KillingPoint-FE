package com.killingpart.killingpoint.ui.screen.TutorialScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import com.killingpart.killingpoint.ui.theme.mainGreen

@Composable
fun TutorialLastPage(onStart: () -> Unit) {
    Column (
        modifier = Modifier.fillMaxSize().padding(bottom = 50.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ){

        Row (
            modifier = Modifier.fillMaxWidth().height(36.dp)
        ){  }

        Text(
            text = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        fontFamily = PaperlogyFontFamily,
                        fontWeight = FontWeight.Medium
                    )
                ) {
                    append("이제 첫 번째 ")
                }

                withStyle(
                    style = SpanStyle(
                        fontFamily = PaperlogyFontFamily,
                        fontWeight = FontWeight.SemiBold
                    )
                ) {
                    append("킬링파트")
                }

                withStyle(
                    style = SpanStyle(
                        fontFamily = PaperlogyFontFamily,
                        fontWeight = FontWeight.Medium
                    )
                ) {
                    append("를\n기록할 시간이예요!")
                }
            },
            fontSize = 35.sp,
            color = Color.White,
            textAlign = TextAlign.Center
        )


        Box(
            modifier = Modifier.size(342.dp, 48.dp)
                .background(color = mainGreen, RoundedCornerShape(100))
                .clickable{ onStart() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "지금 시작하기",
                fontFamily = PaperlogyFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
            )
        }
    }
}

@Preview
@Composable
fun TutorialLastPagePreview() {
    TutorialLastPage(onStart = {})
}