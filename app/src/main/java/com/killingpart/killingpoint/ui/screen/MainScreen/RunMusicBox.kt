package com.killingpart.killingpoint.ui.screen.MainScreen

import android.os.Build

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import android.graphics.Shader
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalDensity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.killingpart.killingpoint.R
import com.killingpart.killingpoint.data.model.Diary
import com.killingpart.killingpoint.data.repository.AuthRepository
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import com.killingpart.killingpoint.ui.theme.mainGreen
import com.killingpart.killingpoint.ui.viewmodel.UserUiState
import com.killingpart.killingpoint.ui.viewmodel.UserViewModel
import com.killingpart.killingpoint.ui.screen.ProfileScreen.ProfileSettingsScreen
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
fun RunMusicBox(
    currentIndex: Int,
    currentDiary: Diary?,
    isPlaying: Boolean? = null,
    authorUsername: String? = null,
    authorTag: String? = null,
    authorProfileImageUrl: String? = null,
    navController: NavController,
    onVideoEnd: () -> Unit = {}
) {
    val context = LocalContext.current
    val userViewModel: UserViewModel = viewModel()
    val userState by userViewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        if (authorUsername == null) {
            userViewModel.loadUserInfo(context)
        }
    }


    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(start = 15.dp, end = 17.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (authorProfileImageUrl != null) {
                    AsyncImage(
                        model = authorProfileImageUrl,
                        contentDescription = "프로필 사진",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(100))
                            .border(3.dp, mainGreen, RoundedCornerShape(50)),
                        placeholder = painterResource(id = R.drawable.default_profile),
                        error = painterResource(id = R.drawable.default_profile)
                    )
                } else {
                    when (val s = userState) {
                        is UserUiState.Success -> {
                            AsyncImage(
                                model = s.userInfo.profileImageUrl,
                                contentDescription = "프로필 사진",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(100))
                                    .border(3.dp, mainGreen, RoundedCornerShape(50)),
                                placeholder = painterResource(id = R.drawable.default_profile),
                                error = painterResource(id = R.drawable.default_profile)
                            )
                        }
                        else -> {
                            Image(
                                painter = painterResource(id = R.drawable.default_profile),
                                contentDescription = "프로필 사진",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(100))
                                    .border(3.dp, mainGreen, RoundedCornerShape(50))
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = authorUsername ?: when (val s = userState) {
                            is UserUiState.Success -> s.userInfo.username
                            is UserUiState.Loading -> "LOADING..."
                            is UserUiState.Error -> "KILLING_PART"
                        },
                        fontFamily = PaperlogyFontFamily,
                        fontWeight = FontWeight.W400,
                        fontSize = 12.sp,
                        color = mainGreen,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = if (authorTag != null) "@$authorTag" else when (val s = userState) {
                            is UserUiState.Success -> "@${s.userInfo.tag}"
                            is UserUiState.Loading -> "@LOADING"
                            is UserUiState.Error -> "@KILLING_PART"
                        },
                        fontFamily = PaperlogyFontFamily,
                        fontWeight = FontWeight.W400,
                        fontSize = 11.sp,
                        color = mainGreen,
                    )
                }
                


            }

            if (currentDiary == null) {
                Column (
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(80.dp))
                    Text(
                        text = "재생 가능한 킬링파트가 없습니다",
                        fontFamily = PaperlogyFontFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = mainGreen,
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "킬링 파트를 추가해 보세요",
                        fontSize = 14.sp,
                        fontFamily = PaperlogyFontFamily,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF9C9C9C)
                    )
                    Spacer(modifier = Modifier.height(26.dp))
                    Image(
                        painter = painterResource(id = R.drawable.navi_add),
                        contentDescription = "킬링파트 추가 아이콘",
                        modifier = Modifier.size(60.dp)
                            .clickable { navController.navigate("add_music") }
                    )
                    Spacer(modifier = Modifier.height(50.dp))
                }
            } else {
                val scrollState = rememberScrollState()
                val density = LocalDensity.current

                key(currentDiary.videoUrl) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(500.dp)
                            .verticalScroll(scrollState),
                    ) {
                        val startSeconds = currentDiary.start?.toFloatOrNull() ?: 0f
                        val durationSeconds = currentDiary.duration?.toFloatOrNull() ?: 0f
                        val endSeconds = currentDiary.end?.toFloatOrNull()

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            YouTubePlayerBox(
                                currentDiary, 
                                startSeconds, 
                                durationSeconds,
                                isPlayingState = isPlaying,
                                onVideoEnd = onVideoEnd
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = currentDiary.musicTitle ?: "",
                                fontFamily = PaperlogyFontFamily,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currentDiary.artist ?: "",
                                fontFamily = PaperlogyFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Light
                            )
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            DiaryBox(currentDiary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .size(316.dp)
                .offset(y = 370.dp)
                .background(color = Color.Transparent, shape = RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        compositingStrategy = CompositingStrategy.Offscreen
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            renderEffect = android.graphics.RenderEffect
                                .createBlurEffect(16f, 16f, Shader.TileMode.CLAMP)
                                .asComposeRenderEffect()
                        }
                    }
                    .background(Color.Black.copy(alpha = 0.2f))
            )
        }

    }
}

