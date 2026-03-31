package com.killingpart.killingpoint.ui.screen.WriteDiaryScreen

import android.R.attr.fontWeight
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.killingpart.killingpoint.data.model.CreateDiaryRequest
import com.killingpart.killingpoint.data.repository.AuthRepository
import com.killingpart.killingpoint.data.spotify.SimpleTrack
import com.killingpart.killingpoint.data.model.YouTubeVideo
import com.killingpart.killingpoint.R
import com.killingpart.killingpoint.ui.screen.AddMusicScreen.korean_font_medium
import com.killingpart.killingpoint.ui.screen.MainScreen.YouTubePlayerBox
import com.killingpart.killingpoint.ui.screen.WriteDiaryScreen.AlbumDiaryBoxWithoutContent
import com.killingpart.killingpoint.data.model.Diary
import com.killingpart.killingpoint.data.model.Scope
import com.killingpart.killingpoint.ui.component.BottomBar
import com.killingpart.killingpoint.navigation.navigateToMainClearingStack
import androidx.compose.material3.TextButton
import androidx.compose.ui.graphics.graphicsLayer
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import com.killingpart.killingpoint.ui.theme.mainGreen
import java.time.LocalDate
import java.util.regex.Pattern

/**
 * ISO 8601 duration 형식(예: "PT2M28S")을 초 단위로 변환
 * @param duration ISO 8601 duration 문자열 (예: "PT2M28S", "PT1H2M30S", "PT30S")
 * @return 초 단위로 변환된 값 (예: 148, 3750, 30)
 */
fun parseDurationToSeconds(duration: String): Int {
    // PT 제거
    val durationStr = duration.removePrefix("PT")
    if (durationStr.isEmpty()) return 0

    var totalSeconds = 0

    // 시간(H) 파싱
    val hourPattern = Pattern.compile("(\\d+)H")
    val hourMatcher = hourPattern.matcher(durationStr)
    if (hourMatcher.find()) {
        totalSeconds += hourMatcher.group(1).toInt() * 3600
    }

    // 분(M) 파싱
    val minutePattern = Pattern.compile("(\\d+)M")
    val minuteMatcher = minutePattern.matcher(durationStr)
    if (minuteMatcher.find()) {
        totalSeconds += minuteMatcher.group(1).toInt() * 60
    }

    // 초(S) 파싱
    val secondPattern = Pattern.compile("(\\d+)S")
    val secondMatcher = secondPattern.matcher(durationStr)
    if (secondMatcher.find()) {
        totalSeconds += secondMatcher.group(1).toInt()
    }

    return totalSeconds
}

@Composable
fun SelectDurationScreen(
    navController: NavController,
    title: String,
    artist: String,
    imageUrl: String,
    videoUrl: String = "",
    totalDuration: Int,
    tutorialMode: Boolean = false
) {
    var duration by remember { mutableStateOf(10f) }
    var start by remember { mutableStateOf(0f) }

    val startSeconds = remember(start) {
        val seconds = start ?: 0f
        seconds
    }

    val durationSeconds = remember(duration) {
        val seconds = duration ?: 10f
        seconds
    }

    var end = remember(startSeconds, durationSeconds) {
        val endValue = (startSeconds + durationSeconds)
        endValue
    }

    var currentVideoUrl by remember { mutableStateOf<String?>(if (videoUrl.isNotEmpty()) videoUrl else null) }
    var currentTotalDuration by remember { mutableStateOf(if (totalDuration > 0) totalDuration else 10) }
    var candidateVideos by remember { mutableStateOf<List<YouTubeVideo>>(emptyList()) }
    var isLoadingVideo by remember { mutableStateOf(false) }
    var isCandidateExpanded by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val repo = remember { AuthRepository(context) }
    val tutorialTouchInteraction = remember { MutableInteractionSource() }

    LaunchedEffect(title, artist) {
        if (videoUrl.isEmpty()) {
            isLoadingVideo = true
            try {
                val videos = repo.searchVideos(title, artist)
                videos.forEachIndexed { index, video ->
                    android.util.Log.d("SelectDurationScreen", "  비디오[$index]: url=${video.id}")
                }
                candidateVideos = videos
                val firstVideo = videos.firstOrNull()
                val newVideoId = firstVideo?.id
                currentVideoUrl = newVideoId
                currentTotalDuration = firstVideo?.duration ?: 10
                isCandidateExpanded = false
            } catch (e: Exception) {
                candidateVideos = emptyList()
                currentVideoUrl = null
                currentTotalDuration = 10
            }
            isLoadingVideo = false
        }
    }


    val scrollState = rememberScrollState()
    val navigateNext: () -> Unit = {
        val encodedVideoUrl = Uri.encode(currentVideoUrl ?: "")
        val tutorialArg = if (tutorialMode) "true" else "false"

        navController.navigate(
            "write_diary" +
                    "?title=${Uri.encode(title)}" +
                    "&artist=${Uri.encode(artist)}" +
                    "&image=${Uri.encode(imageUrl)}" +
                    "&duration=${duration.toInt()}" +
                    "&start=${start.toInt()}" +
                    "&end=${end.toInt()}" +
                    "&videoUrl=$encodedVideoUrl" +
                    "&totalDuration=${currentTotalDuration}" +
                    "&tutorial=$tutorialArg"
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF060606))
    ) {

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 15.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Top bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        modifier = Modifier.size(50.dp),
                        imageVector = Icons.Filled.KeyboardArrowLeft,
                        contentDescription = "back",
                        tint = Color.White
                    )
                }
                if (!tutorialMode) {
                    Text(
                        text = "Killing Part",
                        fontSize = 33.sp,
                        fontFamily = eng_font_extrabold,
                        color = Color(0xFF1D1E20),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                if (tutorialMode) {
                    TextButton(
                        onClick = { navController.navigateToMainClearingStack() },
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Text(
                            "건너뛰기",
                            color = Color.White,
                            fontFamily = PaperlogyFontFamily,
                            fontSize = 14.sp,
                            textDecoration = TextDecoration.Underline
                        )
                    }
                }
            }

            if (tutorialMode) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "킬링파트로 사용할 구간을 정해보세요!",
                    fontSize = 21.sp,
                    fontFamily = korean_font_medium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp),
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(if (tutorialMode) 18.dp else 24.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (tutorialMode) {
                            Modifier.clickable(
                                interactionSource = tutorialTouchInteraction,
                                indication = null,
                                onClick = navigateNext
                            )
                        } else {
                            Modifier
                        }
                    )
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val dimmedModifier = if (tutorialMode) {
                    Modifier.graphicsLayer { alpha = 0.38f }
                } else {
                    Modifier
                }
                if (isLoadingVideo || currentVideoUrl == null) {
                    Box(
                        modifier = Modifier
                            .then(dimmedModifier)
                            .size(250.dp, 150.dp)
                            .background(Color(0xFF1A1A1A), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isLoadingVideo) "비디오 검색 중..." else "비디오를 찾을 수 없습니다",
                            fontFamily = PaperlogyFontFamily,
                            color = Color.White,
                            fontSize = 10.sp
                        )
                    }
                } else {
                    val tempDiary = Diary(
                        artist = artist,
                        musicTitle = title,
                        albumImageUrl = imageUrl,
                        videoUrl = currentVideoUrl!!,
                        content = "",
                        scope = Scope.PUBLIC,
                        duration = "0",
                        start = "0",
                        end = "0",
                        createDate = "",
                        updateDate = ""
                    )
                    Box(
                        modifier = Modifier
                            .then(dimmedModifier)
                            .size(250.dp, 150.dp)
                    ) {

                        YouTubePlayerBox(tempDiary, startSeconds, durationSeconds, shouldLoop = true)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                Box(modifier = dimmedModifier) {
                    AlbumDiaryBoxWithoutContent(
                        track = SimpleTrack(
                            id = "",
                            title = title,
                            artist = artist,
                            albumImageUrl = imageUrl,
                            albumId = ""
                        )
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "킬링파트 자르기",
                        fontFamily = PaperlogyFontFamily,
                        fontWeight = FontWeight.Light,
                        fontSize = 14.sp,
                        color = Color(0xFFEBEBEB)
                    )
                    Spacer(Modifier.height(3.dp))

                    KillingPartSelector(

                        currentTotalDuration, onStartChange = { s,e,d ->
                            start = s
                            end = e
                            duration =d
                        }

                    )
                    Spacer(Modifier.height(24.dp))

                    if (candidateVideos.isNotEmpty()) {
                        val toggleColor = if (isCandidateExpanded) Color(0xFFD9D9D9) else Color(0xFF878787)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth().padding(horizontal = 10.dp)
                                .clickable { isCandidateExpanded = !isCandidateExpanded },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "다른 영상 검색 결과",
                                fontFamily = korean_font_medium,
                                fontSize = 15.sp,
                                color = toggleColor,
                                textDecoration = TextDecoration.Underline
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "toggle candidate videos",
                                tint = toggleColor,
                                modifier = Modifier.rotate(if (isCandidateExpanded) 90f else 0f)
                            )
                        }

                        if (isCandidateExpanded) {
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth().padding(horizontal = 10.dp)
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                candidateVideos.forEach { video ->
                                    val isSelected = currentVideoUrl == video.id
                                    val thumbnailShape = RoundedCornerShape(6.dp)
                                    Column(
                                        modifier = Modifier
                                            .width(146.dp)
                                            .clickable {
                                                currentVideoUrl = video.id
                                                currentTotalDuration = video.duration
                                                start = 0f
                                                duration = 10f.coerceAtMost(video.duration.toFloat())
                                                end = (start + duration).coerceAtMost(video.duration.toFloat())
                                            }
                                    ) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data("https://img.youtube.com/vi/${video.id}/hqdefault.jpg")
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = "video thumbnail",
                                            contentScale = ContentScale.FillWidth,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(85.dp)
                                                .clip(thumbnailShape)
                                                .background(Color(0xFF1A1A1A), thumbnailShape)
                                                .then(
                                                    if (isSelected) {
                                                        Modifier
                                                            .border(
                                                                width = 1.dp,
                                                                color = mainGreen,
                                                                shape = thumbnailShape
                                                            )
                                                    } else {
                                                        Modifier
                                                            .border(
                                                                width = 1.dp,
                                                                color = Color(0xFFD9D9D9),
                                                                shape = thumbnailShape
                                                            )
                                                    }
                                                )
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = video.title,
                                            fontFamily = korean_font_medium,
                                            fontSize = 12.sp,
                                            color = Color(0xFFEBEBEB),
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(if (tutorialMode) 20.dp else 38.dp))
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }


        Button(
            onClick = navigateNext,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .align(Alignment.CenterHorizontally),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFCCFF33),
                contentColor = Color.Black
            )
        ) {
            Text(
                fontFamily = korean_font_medium,
                text = "다음으로 →"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (!tutorialMode) {
            BottomBar(navController = navController)
        }
    }
}
@Preview
@Composable
fun SelectDurationPreview() {
    SelectDurationScreen(
        navController = rememberNavController(),
        title = "Death Sonnet von Dat",
        artist = "Davinci Leo",
        imageUrl = "https://i.scdn.co/image/ab67616d00001e02c6b31f5f1ce2958380fdb9b0",
        videoUrl = "",
        totalDuration = 0,
        tutorialMode = false
    )
}


