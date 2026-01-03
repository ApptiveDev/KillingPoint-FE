package com.killingpart.killingpoint.ui.screen.MainScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.killingpart.killingpoint.R
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily

@Composable
fun MusicListOne(
    imageUrl: String,
    musicTitle: String,
    artist: String,
    isNow: Color,
    onClick: () -> Unit = {},
    isPlaying: Boolean = false
) {
    Row (
        modifier = Modifier.fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(10.dp)
            .background(color = isNow, shape = RoundedCornerShape(8.dp)),
        verticalAlignment = Alignment.CenterVertically
    ){
        AsyncImage(
            model = imageUrl,
            contentDescription = "앨범 표지",
            modifier = Modifier.size(52.dp)
                .background(color = Color.Transparent, shape = RoundedCornerShape(8.dp))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column (
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ){
            Text(
                text = musicTitle,
                fontFamily = PaperlogyFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = Color.White,
            )
            Text(
                text = artist,
                fontFamily = PaperlogyFontFamily,
                fontWeight = FontWeight.Light,
                fontSize = 10.sp,
                color = Color.White
            )
        }
        if (isPlaying) {
            androidx.compose.foundation.Image(
                painter = painterResource(id = R.drawable.music_note_yellow),
                contentDescription = "재생 중",
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
