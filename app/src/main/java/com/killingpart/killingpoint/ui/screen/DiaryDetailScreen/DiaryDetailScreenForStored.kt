package com.killingpart.killingpoint.ui.screen.DiaryDetailScreen

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.killingpart.killingpoint.R
import com.killingpart.killingpoint.data.model.Diary
import com.killingpart.killingpoint.data.repository.AuthRepository
import com.killingpart.killingpoint.data.spotify.SimpleTrack
import com.killingpart.killingpoint.ui.component.AppBackground
import com.killingpart.killingpoint.ui.component.BottomBar
import com.killingpart.killingpoint.ui.screen.MainScreen.YouTubePlayerBox
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import com.killingpart.killingpoint.ui.theme.mainGreen
import kotlinx.coroutines.launch

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
    totalDuration: Int? = null,
    diaryId: Long? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isStored by remember { mutableStateOf(true) }
    var showUnstoreDialog by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }

    val startSeconds = parseTimeToSecondsForStored(start)
    val endSeconds = parseTimeToSecondsForStored(end)
    val duringSeconds = (endSeconds - startSeconds).coerceAtLeast(0)

    val diary = Diary(
        id = diaryId,
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

                Text(
                    text = "보관한 킬링파트",
                    color = Color.White,
                    fontFamily = PaperlogyFontFamily,
                    fontWeight = FontWeight.W500,
                    fontSize = 16.sp,
                    modifier = Modifier.align(Alignment.Center),
                    maxLines = 1
                )

                Icon(
                    painter = painterResource(id = R.drawable.is_stored),
                    contentDescription = "보관됨",
                    tint = if (isStored) mainGreen else Color(0xFF5F5C5C),
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp)
                        .size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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

            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 내 킬링파트로 등록
                Button(
                    onClick = {
                        val encodedVideoUrl = Uri.encode(videoUrl)
                        navController.navigate(
                            "write_diary" +
                                    "?title=${Uri.encode(musicTitle)}" +
                                    "&artist=${Uri.encode(artist)}" +
                                    "&image=${Uri.encode(albumImageUrl)}" +
                                    "&duration=${duration.toFloatOrNull()?.toInt() ?: 0}" +
                                    "&start=${start.toFloatOrNull()?.toInt() ?: 0}" +
                                    "&end=${end.toFloatOrNull()?.toInt() ?: 0}" +
                                    "&videoUrl=$encodedVideoUrl" +
                                    "&totalDuration=${totalDuration ?: 0}"
                        )
                    },
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

                // 보관함에서 삭제
                Button(
                    onClick = { showUnstoreDialog = true },
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

        if (showUnstoreDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x80000000))
                    .clickable(enabled = !isProcessing) {
                        // 바깥 영역 탭 시 닫기
                        showUnstoreDialog = false
                    }
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                            .background(Color(0xFF101010))
                            .padding(horizontal = 24.dp, vertical = 28.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "해당 킬링파트를 더이상 보관하지 말까요?",
                                color = Color.White,
                                fontFamily = PaperlogyFontFamily,
                                fontWeight = FontWeight.W500,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "담기를 취소하면 해당 킬링파트는\n보관함에서 사라집니다",
                                color = Color.White,
                                fontFamily = PaperlogyFontFamily,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // 돌아가기
                                Button(
                                    onClick = {
                                        if (!isProcessing) showUnstoreDialog = false
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White,
                                        contentColor = Color.Black
                                    ),
                                    enabled = !isProcessing
                                ) {
                                    Text(
                                        text = "돌아가기",
                                        fontFamily = PaperlogyFontFamily,
                                        fontWeight = FontWeight.W500,
                                        fontSize = 14.sp
                                    )
                                }

                                // 제거하기
                                Button(
                                    onClick = {
                                        if (diaryId == null || isProcessing) return@Button
                                        isProcessing = true
                                        coroutineScope.launch {
                                            try {
                                                val repo = AuthRepository(context)
                                                val result = repo.toggleStore(diaryId)
                                                result.onSuccess { storeResponse ->
                                                    isStored = storeResponse.isStored
                                                    // 보관 해제가 완료되면 내 컬렉션(프로필) 화면으로 이동
                                                    if (!storeResponse.isStored) {
                                                        navController.navigate("main?tab=profile") {
                                                            popUpTo("main") { inclusive = false }
                                                        }
                                                    }
                                                }
                                            } catch (e: Exception) {
                                                android.util.Log.e("DiaryDetailStored", "보관 취소 실패: ${e.message}")
                                            } finally {
                                                isProcessing = false
                                                showUnstoreDialog = false
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFFF6666),
                                        contentColor = Color.White
                                    ),
                                    enabled = !isProcessing
                                ) {
                                    Text(
                                        text = if (isProcessing) "처리 중..." else "제거하기",
                                        fontFamily = PaperlogyFontFamily,
                                        fontWeight = FontWeight.W500,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
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