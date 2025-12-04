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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import android.graphics.Shader
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
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
    isPlaying: Boolean? = null
) {
    val context = LocalContext.current
    val userViewModel: UserViewModel = viewModel()
    val userState by userViewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        userViewModel.loadUserInfo(context)
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
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // username과 tag (클릭 가능 - RunMusicBox에서는 클릭 불가)
                Column {
                    Text(
                        text = when (val s = userState) {
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
                        text = when (val s = userState) {
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

            val scrollState = rememberScrollState()
            val density = LocalDensity.current

            LaunchedEffect(currentDiary?.videoUrl) {
                if (currentDiary != null) {
                    kotlinx.coroutines.delay(300)
                    android.util.Log.d("RunMusicBox", "Auto scrolling down - diary: ${currentDiary.musicTitle}")
                    val scrollOffset = with(density) { 300.dp.toPx().toInt() }
                    scrollState.animateScrollTo(scrollOffset)
                }
            }
            
            key(currentDiary?.videoUrl) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp)
                        .verticalScroll(scrollState),
                ) {
                    
                    // Diary의 duration, start, end 값 확인 및 로그
                    val startSeconds = currentDiary?.start?.toFloatOrNull() ?: 0f
                    val durationSeconds = currentDiary?.duration?.toFloatOrNull() ?: 0f
                    val endSeconds = currentDiary?.end?.toFloatOrNull()

                    
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        YouTubePlayerBox(
                            currentDiary, 
                            startSeconds, 
                            durationSeconds,
                            isPlayingState = isPlaying
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        DiaryBox(currentDiary)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
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

