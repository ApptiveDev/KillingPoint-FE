package com.killingpart.killingpoint.ui.screen.DiaryDetailScreen
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.killingpart.killingpoint.R
import com.killingpart.killingpoint.data.spotify.SimpleTrack
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily

@Composable
fun AlbumDiaryBoxWithTimeBar(
    track: SimpleTrack?,
    artist: String = "",
    musicTitle: String = "",
    startSeconds: Int = 0,
    duringSeconds: Int = 0,
    totalDuration: Int? = null
) {
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

    Row (
        modifier = Modifier.fillMaxWidth()
            .height(130.dp)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF272727),
                        Color.Black
                    ),
                ), shape = RoundedCornerShape(12.dp)
            ),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ){
        Row (
            modifier = Modifier.fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 14.dp)
        ) {
            Box(
                modifier = Modifier.size(140.dp, 100.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.cd),
                    contentDescription = "CD",
                    modifier = Modifier
                        .size(100.dp)
                        .background(color = Color.Transparent, shape = RoundedCornerShape(8.dp))
                        .offset(x = 30.dp, y = 0.dp)
                        .graphicsLayer {
                            rotationZ = rotation
                        },
                    contentScale = ContentScale.Fit
                )

                track?.albumImageUrl?.let { imageUrl ->
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "앨범 이미지",
                        modifier = Modifier
                            .size(100.dp)
                            .offset((-10).dp, 0.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.width(30.dp))

            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                track?.title?.let { title ->
                    Text(
                        text = title,
                        fontFamily = PaperlogyFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                track?.artist?.let { artist ->
                    Text(
                        text = artist,
                        fontFamily = PaperlogyFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                MusicTimeBarForDiaryDetail(
                    artist = artist,
                    musicTitle = musicTitle,
                    start = startSeconds,
                    during = duringSeconds,
                    totalDuration = totalDuration
                )
            }
        }

//        Spacer(modifier = Modifier.height(31.dp))
    }
}

@Preview
@Composable
fun AlbumDiaryPreview() {
    AlbumDiaryBoxWithTimeBar(
        track = SimpleTrack(
            id = "",
            title = "Beat It Up",
            artist = "NCT DREAM",
            albumImageUrl = "imageUrl",
            albumId = ""
        ),
        artist = "NCT DREAM",
        musicTitle = "Beat It Up",
        startSeconds = 10,
        duringSeconds = 20,
        totalDuration = 180
    )
}
