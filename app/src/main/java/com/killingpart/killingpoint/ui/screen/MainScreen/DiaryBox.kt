package com.killingpart.killingpoint.ui.screen.MainScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.killingpart.killingpoint.data.model.Diary
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import com.killingpart.killingpoint.ui.theme.mainGreen

@Composable
fun DiaryBox(diary: Diary?) {
    Column (
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text(
            text = "킬링파트 일기",
            fontFamily = PaperlogyFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            color = Color(0xFFA4A4A6)
        )

        Spacer(modifier = Modifier.height(18.dp))

        diary?.content?.let { diaryContent ->
            Text(
                text = diaryContent,
                fontFamily = PaperlogyFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(150.dp))
    }
}

@Preview
@Composable
fun DiaryPreview() {
    DiaryBox(diary = null)
}
