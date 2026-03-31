package com.killingpart.killingpoint.ui.screen.AddMusicScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ColorMatrixColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.killingpart.killingpoint.R
import com.killingpart.killingpoint.ui.component.BottomBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextDecoration
import com.killingpart.killingpoint.data.spotify.SimpleTrack
import androidx.compose.ui.platform.LocalContext
import com.killingpart.killingpoint.ui.viewmodel.SpotifyViewModel
import com.killingpart.killingpoint.ui.viewmodel.SpotifyUiState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.font.FontWeight
import com.killingpart.killingpoint.navigation.navigateToMainClearingStack
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalInspectionMode
import com.killingpart.killingpoint.data.repository.AuthRepository
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
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
fun AddMusicScreen(
    navController: NavController,
    tutorialMode: Boolean = false
) {
    var globalLoading by remember { mutableStateOf(false) }
    val bg = if (tutorialMode) Color.Black else Color(0xFF1D1E20)
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
    ) {
        // Center background logo (subtle)
            Image(
                painter = painterResource(id = R.drawable.killingpart_logo_dark),
                contentDescription = "앱 배경 로고",
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (-140).dp)
                    .size(280.dp)
            )

            val context = LocalContext.current
            val viewModel = remember { SpotifyViewModel() }
            val state by viewModel.state.collectAsState()

            var query by remember { mutableStateOf("") }

            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (tutorialMode) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { navController.navigateToMainClearingStack() }) {
                            Text(
                                "건너뛰기",
                                color = Color.White,
                                fontFamily = PaperlogyFontFamily,
                                fontSize = 15.sp,
                                textDecoration = TextDecoration.Underline
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(if (tutorialMode) 8.dp else 60.dp))
                if (tutorialMode) {
                    Text(
                        text = "어떤 곡으로 시작할까요?",
                        color = Color.White,
                        fontFamily = PaperlogyFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "킬링파트 제작하기",
                        color = Color(0xFF9A9B9E),
                        fontFamily = PaperlogyFontFamily,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }
                SearchField(
                    modifier = Modifier.fillMaxWidth(0.85f),
                    value = query,
                    onValueChange = { query = it },
                    onSearchClick = {
                        if (query.isNotBlank()) {
                            viewModel.search(query)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))

                when (state) {
                    is SpotifyUiState.Success -> {
                        val tracks = (state as SpotifyUiState.Success).tracks
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .weight(1f),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(tracks) { track ->
                                TrackRowWithVideoSearch(
                                    track = track,
                                    navController = navController,
                                    context = context,
                                    onLoadingChange = { globalLoading = it },
                                    tutorialMode = tutorialMode
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }
                    }
                    is SpotifyUiState.Error -> {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    SpotifyUiState.Loading, SpotifyUiState.Idle -> {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                if (!tutorialMode) {
                    BottomBar(navController = navController)
                }
            }
            
            if (globalLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .graphicsLayer {
                            alpha = 0.7f
                        },
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
    }
}

@Composable
private fun TrackRowWithVideoSearch(
    track: SimpleTrack,
    navController: NavController,
    context: android.content.Context,
    onLoadingChange: (Boolean) -> Unit = {},
    tutorialMode: Boolean = false
) {
    val scope = rememberCoroutineScope()
    val repo = remember { AuthRepository(context) }
    var isLoading by remember { mutableStateOf(false) }

    TrackRow(track, onClick = {
        if (isLoading) return@TrackRow
        
        isLoading = true
        onLoadingChange(true)
        scope.launch {
            try {
                val videos = repo.searchVideos(track.title, track.artist)
                val firstVideo = videos.firstOrNull()
                val videoId = firstVideo?.id ?: ""

                val totalDuration = firstVideo?.duration ?: 180
                
                val encodedTitle = java.net.URLEncoder.encode(track.title, "UTF-8")
                val encodedArtist = java.net.URLEncoder.encode(track.artist, "UTF-8")
                val encodedImage = java.net.URLEncoder.encode(track.albumImageUrl ?: "", "UTF-8")
                val encodedVideoUrl = java.net.URLEncoder.encode(videoId, "UTF-8")
                val tutorialArg = if (tutorialMode) "true" else "false"

                navController.navigate(
                    "select_duration" +
                            "?title=$encodedTitle" +
                            "&artist=$encodedArtist" +
                            "&image=$encodedImage" +
                            "&videoUrl=$encodedVideoUrl" +
                            "&totalDuration=$totalDuration" +
                            "&tutorial=$tutorialArg"
                )
            } catch (e: Exception) {
                android.util.Log.e("AddMusicScreen", "YouTube search failed: ${e.message}")
                // 에러 발생 시에도 기본값으로 진행
                val encodedTitle = java.net.URLEncoder.encode(track.title, "UTF-8")
                val encodedArtist = java.net.URLEncoder.encode(track.artist, "UTF-8")
                val encodedImage = java.net.URLEncoder.encode(track.albumImageUrl ?: "", "UTF-8")
                val tutorialArg = if (tutorialMode) "true" else "false"
                navController.navigate(
                    "select_duration" +
                            "?title=$encodedTitle" +
                            "&artist=$encodedArtist" +
                            "&image=$encodedImage" +
                            "&videoUrl=" +
                            "&totalDuration=180" +
                            "&tutorial=$tutorialArg"
                )
            } finally {
                isLoading = false
                onLoadingChange(false)
            }
        }
    }, isLoading = isLoading)
}

@Composable
private fun TrackRow(track: SimpleTrack, onClick: () -> Unit = {}, isLoading: Boolean = false) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF232427)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isLoading) { onClick() }
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            if (LocalInspectionMode.current) {
                Image(
                    painter = painterResource(id = R.drawable.example_video),
                    contentDescription = "album",
                    modifier = Modifier.size(56.dp)
                )
            } else {
                AsyncImage(
                    model = track.albumImageUrl ?: R.drawable.example_video,
                    contentDescription = "album",
                    modifier = Modifier.size(56.dp),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = track.title, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = track.artist, color = Color(0xFFA4A4A6), maxLines = 1)
            }
        }
    }
}
@Preview
@Composable
fun AddMusicScreenPreview() {
    AddMusicScreen(navController = rememberNavController())
}

@Preview
@Composable
fun AddMusicScreenSuccessPreview() {
    val mockTracks = listOf(
        SimpleTrack("track1", "Effie - CAN I SIP 담배", "Effie", "https://i.scdn.co/image/ab67616d00001e02c6b31f5f1ce2958380fdb9b0", "album1"),
        SimpleTrack("track2", "A Night At The Symphony", "Laufey", null, "album2"),
        SimpleTrack("track3", "Love Story", "Taylor Swift", null, "album3"),
        SimpleTrack("track4", "Dynamite", "BTS", null, "album4"),
        SimpleTrack("track5", "HypeBoy", "NewJeans", null, "album5")
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1D1E20))
    ) {
        Image(
            painter = painterResource(id = R.drawable.killingpart_logo_dark),
            contentDescription = "앱 배경 로고",
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-140).dp)
                .size(280.dp)
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            SearchField(
                modifier = Modifier.fillMaxWidth(0.85f),
                value = "A Night At The Symphony",
                onValueChange = {},
                onSearchClick = {}
            )
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .weight(1f),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(mockTracks) { track ->
                    TrackRow(track)
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
            BottomBar(navController = rememberNavController())
        }
    }
}
