package com.killingpart.killingpoint.ui.screen.DiaryDetailScreen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.killingpart.killingpoint.R
import com.killingpart.killingpoint.data.model.Diary
import com.killingpart.killingpoint.data.spotify.SimpleTrack
import com.killingpart.killingpoint.ui.component.AppBackground
import com.killingpart.killingpoint.ui.component.BottomBar
import com.killingpart.killingpoint.ui.screen.DiaryDetailScreen.MusicTimeBarForDiaryDetail
import com.killingpart.killingpoint.ui.screen.MainScreen.YouTubePlayerBox
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import com.killingpart.killingpoint.ui.theme.mainGreen

@Composable
fun DiaryDetailScreenForStored(
    navController: NavController,
    artist: String,
    musicTitle: String,
    albumImageUrl: String,
    videoUrl: String,
    duration: String,
    start: String,
    end: String,
    createDate: String,
    totalDuration: Int? = null
) {
    val startSeconds = parseTimeToSecondsForStored(start)
    val endSeconds = parseTimeToSecondsForStored(end)
    val duringSeconds = (endSeconds - startSeconds).coerceAtLeast(0)

    val diary = Diary(
        id = null,
        artist = artist,
        musicTitle = musicTitle,
        albumImageUrl = albumImageUrl,
        content = "",
        videoUrl = videoUrl,
        scope = com.killingpart.killingpoint.data.model.Scope.KILLING_PART,
        duration = duration,
        start = start,
        end = end,
        totalDuration = totalDuration,
        createDate = createDate,
        updateDate = createDate
    )

    BackHandler {
        navController.popBackStack()
    }

    AppBackground {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 수정: Box로 감싸 타이틀을 진짜 정중앙 배치
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "뒤로 가기",
                        tint = Color.White
                    )
                }

                // 타이틀 - 진짜 정중앙
                Text(
                    text = "보관한 킬링파트",
                    color = Color.White,
                    fontFamily = PaperlogyFontFamily,
                    fontWeight = FontWeight.W500,
                    fontSize = 16.sp,
                    modifier = Modifier.align(Alignment.Center),
                    maxLines = 1
                )

                // 북마크 아이콘 - 우측 고정
                Icon(
                    painter = painterResource(id = R.drawable.is_stored),
                    contentDescription = "보관됨",
                    tint = mainGreen,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp)
                        .size(22.dp)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 수정: 동영상 박스 크기 확대 (250x150 → fillMaxWidth x 220dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                ) {
                    YouTubePlayerBox(
                        diary = diary,
                        startSeconds = startSeconds.toFloat(),
                        durationSeconds = duringSeconds.toFloat(),
                        shouldLoop = true
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))


                Spacer(modifier = Modifier.height(16.dp))

                AlbumDiaryBoxWithTimeBar(
                    track = SimpleTrack(
                        id = "",
                        title = musicTitle,
                        artist = artist,
                        albumImageUrl = albumImageUrl,
                        albumId = ""
                    ),
                    artist = artist,
                    musicTitle = musicTitle,
                    startSeconds = startSeconds,
                    duringSeconds = duringSeconds,
                    totalDuration = totalDuration
                )
            }

            // 수정: weight(1f)로 남은 공간을 채워 버튼/하단바를 아래로 밀기
            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF202020)
                    )
                ) {
                    Text(
                        text = "내 킬링파트로 등록",
                        fontFamily = PaperlogyFontFamily,
                        fontWeight = FontWeight.W500,
                        fontSize = 14.sp,
                        color = mainGreen
                    )
                }
                Button(
                    onClick = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF202020)
                    )
                ) {
                    Text(
                        text = "보관함에서 삭제",
                        fontFamily = PaperlogyFontFamily,
                        fontWeight = FontWeight.W500,
                        fontSize = 14.sp,
                        color = Color(0xFFE7E7E7)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            BottomBar(navController = navController)
        }
    }
}

private fun parseTimeToSecondsForStored(timeStr: String): Int {
    return try {
        if (timeStr.contains(":")) {
            val parts = timeStr.split(":")
            if (parts.size == 2) {
                val minutes = parts[0].toIntOrNull() ?: 0
                val seconds = parts[1].toIntOrNull() ?: 0
                minutes * 60 + seconds
            } else {
                timeStr.toIntOrNull() ?: 0
            }
        } else {
            timeStr.toFloatOrNull()?.toInt() ?: 0
        }
    } catch (e: Exception) {
        0
    }
}