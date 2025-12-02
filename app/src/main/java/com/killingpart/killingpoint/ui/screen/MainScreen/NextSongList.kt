package com.killingpart.killingpoint.ui.screen.MainScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.killingpart.killingpoint.R
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import com.killingpart.killingpoint.ui.theme.mainGreen

@Composable
fun NextSongList(title: String?, label: String, onToggle: ()-> Unit,) {
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .height(41.dp)
            .background(Color.Black, RoundedCornerShape(20.dp))
            .padding(horizontal = 21.dp, vertical = 8.dp)
            .clickable { onToggle() },
        verticalAlignment = Alignment.CenterVertically
    ){
        Text(
            text = label,
            fontSize = 14.sp,
            fontFamily = PaperlogyFontFamily,
            fontWeight = FontWeight.Light,
            color = mainGreen
        )

        Spacer(modifier = Modifier.width(13.dp))

        Text(
            text = title ?: "킬링파트를 추가하세요",
            fontSize = 14.sp,
            fontFamily = PaperlogyFontFamily,
            fontWeight = FontWeight.Light,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Image(
            painter = painterResource(id = R.drawable.music_list),
            contentDescription = "음악 리스트",
            modifier = Modifier.size(24.dp)
        )
    }
}

