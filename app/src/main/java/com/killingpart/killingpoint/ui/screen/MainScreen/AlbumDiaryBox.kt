package com.killingpart.killingpoint.ui.screen.MainScreen

import android.graphics.Shader
import android.os.Build
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.killingpart.killingpoint.R
import com.killingpart.killingpoint.data.model.Diary
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily

@Composable
fun AlbumDiaryBox(diary: Diary?) {
    // CD 회전 애니메이션
    val infiniteTransition = rememberInfiniteTransition(label = "cd_rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cd_rotation"
    )
    
    Column (
        modifier = Modifier.fillMaxHeight()
            .padding(horizontal = 25.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Box(
            modifier = Modifier.size(267.dp,175.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.cd),
                contentDescription = "CD",
                modifier = Modifier
                    .size(175.dp)
                    .background(color = Color.Transparent, shape = RoundedCornerShape(8.dp))
                    .offset(x = 33.dp, y = 0.dp)
                    .graphicsLayer {
                        rotationZ = rotation
                    },
                contentScale = ContentScale.Fit
            )

            diary?.albumImageUrl?.let { imageUrl ->
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "앨범 이미지",
                    modifier = Modifier
                        .size(175.dp)
                        .offset(x = (-40).dp, y = 0.dp),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(modifier = Modifier.height(15.dp))

        diary?.musicTitle?.let { title ->
            Text(
                text = title,
                fontFamily = PaperlogyFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        diary?.artist?.let { artist ->
            Text(
                text = artist,
                fontFamily = PaperlogyFontFamily,
                fontWeight = FontWeight.Light,
                fontSize = 14.sp,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "킬링파트 일기",
            fontFamily = PaperlogyFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = Color(0xFFA4A4A6)
        )

        Spacer(modifier = Modifier.height(24.dp))

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
fun AlbumDiaryPreview() {
    AlbumDiaryBox(diary = null)
}
