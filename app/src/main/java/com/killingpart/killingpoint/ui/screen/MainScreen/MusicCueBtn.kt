package com.killingpart.killingpoint.ui.screen.MainScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.killingpart.killingpoint.R

@Composable
fun MusicCueBtn(
    modifier: Modifier = Modifier,
    onPrevious: () -> Unit = {},
    onNext: () -> Unit = {},
    onPlayPause: () -> Unit = {},
    isPlaying: Boolean = true
) {
    Box(
        modifier = modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.height(60.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(35.dp)
        ) {
//            Image(
//                painter = painterResource(id = R.drawable.shuffle),
//                contentDescription = "셔플",
//                modifier = Modifier.size(24.dp)
//            )

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(color = Color(0xFF161616), RoundedCornerShape(30.dp))
                    .clickable { onPrevious() },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.skip_back),
                    contentDescription = "이전 음악 듣기",
                    modifier = Modifier.size(28.dp)
                )
            }

            Box(
                modifier = Modifier.size(60.dp)
                    .background(color = Color.White, RoundedCornerShape(50.dp))
                    .clickable { onPlayPause() },
                contentAlignment = Alignment.Center
            ) {
                if (isPlaying) {
                    Image(
                        painter = painterResource(id = R.drawable.pause),
                        contentDescription = "일시정지",
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "재생",
                        modifier = Modifier.size(40.dp),
                        tint = Color.Black
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(color = Color(0xFF161616), RoundedCornerShape(30.dp))
                    .clickable { onNext() },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.skip_fwd),
                    contentDescription = "다음 음악 듣기",
                    modifier = Modifier.size(28.dp)
                )
            }

//            Image(
//                painter = painterResource(id = R.drawable.repeat),
//                contentDescription = "반복재생",
//                modifier = Modifier.size(24.dp)
//            )
        }
    }
}

@Preview
@Composable
fun MusicCueBtnPreview() {
    MusicCueBtn()
}