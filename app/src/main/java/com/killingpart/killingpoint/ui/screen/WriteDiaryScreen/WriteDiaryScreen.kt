package com.killingpart.killingpoint.ui.screen.WriteDiaryScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
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
import com.killingpart.killingpoint.R
import com.killingpart.killingpoint.ui.screen.AddMusicScreen.korean_font_medium
import com.killingpart.killingpoint.data.model.Diary
import com.killingpart.killingpoint.data.model.Scope
import com.killingpart.killingpoint.ui.component.BottomBar
import com.killingpart.killingpoint.navigation.navigateToMainClearingStack
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import androidx.compose.material3.TextButton
import com.killingpart.killingpoint.ui.screen.MainScreen.YouTubePlayerBox
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

val kor_font_medium = FontFamily(Font(R.font.paperlogy_medium))
val eng_font_extrabold = FontFamily(Font(R.font.unbounded_extrabold))

@Composable
fun WriteDiaryScreen(
    navController: NavController,
    title: String,
    artist: String,
    imageUrl: String,
    duration: String,
    start: String,
    end: String,
    videoUrl: String,
    totalDuration: Int = 0, // YouTube 비디오 전체 길이 (초 단위)
    tutorialMode: Boolean = false
) {
    val coroutineScope = rememberCoroutineScope()
    var content by remember { mutableStateOf("") }
    var scope by remember { mutableStateOf("PUBLIC") }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val repo = remember { AuthRepository(context) }

    val startSeconds = start.toFloatOrNull() ?: 0f
    val endSeconds = end.toFloatOrNull() ?: 0f
    val durationSeconds = duration.toFloatOrNull() ?: (endSeconds - startSeconds).coerceAtLeast(0f)

    val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF060606))
    ) {
        // 본문 영역
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 15.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(if (tutorialMode) 30.dp else 60.dp))

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
                Spacer(modifier = Modifier.height(60.dp))
                Text(
                    text = "선택한 구간에서 느낀\n감정과 생각을 적어보세요.",
                    color = Color.White,
                    fontFamily = PaperlogyFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                )
                Spacer(modifier = Modifier.height(18.dp))
            }

            val tempDiary = Diary(
                artist = artist,
                musicTitle = title,
                albumImageUrl = imageUrl,
                videoUrl = videoUrl,
                content = "",
                scope = Scope.PUBLIC,
                duration = duration,
                start = start,
                end = end,
                createDate = "",
                updateDate = ""
            )
            if (!tutorialMode) {
                Box(
                    modifier = Modifier.size(250.dp, 150.dp)
                ) {
                    YouTubePlayerBox(tempDiary, startSeconds, durationSeconds)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            AlbumDiaryBoxWithoutContent(
                track = SimpleTrack(
                    id = "",
                    title = title,
                    artist = artist,
                    albumImageUrl = imageUrl,
                    albumId = ""
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(150.dp)) {
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    placeholder = {
                        Text(
                            text = "코멘트 추가…",
                            fontSize = 12.sp,
                            fontFamily = korean_font_medium,
                            color = Color(0xFFA4A4A6)
                        )
                    },
                    textStyle = TextStyle(fontSize = 12.sp, fontFamily = korean_font_medium),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF232427),
                        unfocusedContainerColor = Color(0xFF232427),
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                        cursorColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                Text(
                    text = today,
                    color = Color(0xFFA4A4A6),
                    fontSize = 10.sp,
                    fontFamily = korean_font_medium,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp, bottom = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(13.dp))

            // Scope header
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .clickable { isDropdownExpanded = !isDropdownExpanded },
            ) {
                Icon(
                    imageVector = Icons.Filled.Language,
                    contentDescription = "globe",
                    tint = Color(0xFFA4A4A6),
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "공개 상태 : ",
                    fontFamily = korean_font_medium,
                    color = Color(0xFF7B7B7B),
                    fontSize = 10.sp
                )
                Text(
                    fontFamily = korean_font_medium,
                    text = " ${when (scope) {
                        "PUBLIC" -> "전체 공개"
                        "KILLING_PART" -> "킬링파트만 공개"
                        "PRIVATE" -> "전체 비공개"
                        else -> "전체 공개"
                    }}",
                    color = Color(0xFFFFFFFF),
                    fontSize = 10.sp
                )
                Icon(
                    imageVector = if (isDropdownExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = "dropdown",
                    tint = Color(0xFF7B7B7B),
                    modifier = Modifier.size(15.dp)
                )
                if (isDropdownExpanded) {
                    Box(
                        modifier = Modifier.fillMaxWidth(0.9f),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        Column(
                            modifier = Modifier
                                .background(Color.Transparent, RoundedCornerShape(8.dp))

                        ) {
                            ScopeOption("전체 비공개", "PRIVATE", scope) { scope = it; isDropdownExpanded = false }
                            ScopeOption("킬링파트만 공개", "KILLING_PART", scope) { scope = it; isDropdownExpanded = false }
                            ScopeOption("전체 공개", "PUBLIC", scope) { scope = it; isDropdownExpanded = false }

                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        // 저장 버튼 (중앙정렬) — 본문이 비어 있으면 비활성 + alpha 0.4
        val canSubmit = content.isNotBlank()
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = {
                    coroutineScope.launch {
                        runCatching {
                            val body = CreateDiaryRequest(
                                artist = artist,
                                musicTitle = title,
                                albumImageUrl = imageUrl,
                                videoUrl = videoUrl,
                                scope = scope,
                                content = content,
                                duration = duration,
                                start = start,
                                end = end,
                                totalDuration = totalDuration
                            )
                            repo.createDiary(body)
                        }.onSuccess {
                            android.util.Log.d("WriteDiaryScreen", "Diary created successfully")
                            if (tutorialMode) {
                                navController.navigate("onboarding_home_preview") {
                                    popUpTo("onboarding_kp_intro") { inclusive = false }
                                }
                            } else {
                                navController.navigate("main")
                            }
                        }.onFailure { e ->
                            android.util.Log.e("WriteDiaryScreen", "Failed to create diary: ${e.message}")
                        }
                    }
                },
                enabled = canSubmit,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .alpha(if (canSubmit) 1f else 0.4f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (tutorialMode && !canSubmit) Color(0xFF3D4A2E) else Color(0xFFCCFF33),
                    contentColor = Color.Black,
                    disabledContainerColor = Color(0xFF3D4A2E),
                    disabledContentColor = Color(0xFF5C5C5C)
                )
            ) {
                Text(
                    fontFamily = korean_font_medium,
                    text = if (tutorialMode) "다음으로 →" else "저장하기 →"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (!tutorialMode) {
            BottomBar(navController = navController)
        }
    }
}

@Composable private fun ScopeOption(label: String, value: String, selected: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier
                .clickable { onSelect(value) }
                .background( color = if (selected == value) Color(0xFF1D1E20) else Color.Transparent,
                    shape = RoundedCornerShape(8.dp) )
                .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically )
    {
        Text( fontFamily = korean_font_medium, text = if (selected == value) "●" else "○", color = if (selected == value) Color(0xFFFFFFFF) else Color.Gray, fontSize = 10.sp )
        Spacer(modifier = Modifier.width(8.dp))
        Text( fontFamily = korean_font_medium, text = label, color = Color.White, fontSize = 10.sp )
    }
}

@Preview
@Composable
fun WriteDiaryScreenPreview() {
    val isDropdownExpanded = 1
    WriteDiaryScreen(
        navController = rememberNavController(),
        title = "Death Sonnet von Dat",
        artist = "Davinci Leo",
        imageUrl = "https://i.scdn.co/image/ab67616d00001e02c6b31f5f1ce2958380fdb9b0",
        duration = "10",
        start = "2",
        end = "12",
        videoUrl = "https://www.youtube.com/embed/example"
    )
}


