package com.killingpart.killingpoint.ui.screen.DiaryDetailScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.killingpart.killingpoint.R
import com.killingpart.killingpoint.ui.component.AppBackground
import com.killingpart.killingpoint.ui.component.BottomBar
import com.killingpart.killingpoint.data.model.CreateDiaryRequest
import com.killingpart.killingpoint.data.model.Scope
import com.killingpart.killingpoint.data.repository.AuthRepository
import com.killingpart.killingpoint.ui.screen.DiaryDetailScreen.MusicTimeBarForDiaryDetail
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import com.killingpart.killingpoint.ui.theme.mainGreen
import com.killingpart.killingpoint.ui.viewmodel.UserViewModel
import com.killingpart.killingpoint.ui.viewmodel.UserUiState
import com.killingpart.killingpoint.data.model.Diary
import com.killingpart.killingpoint.ui.screen.MainScreen.YouTubePlayerBox
import com.killingpart.killingpoint.ui.screen.MainScreen.AlbumDiaryBox
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import java.net.URLDecoder
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DiaryDetailScreen(
    navController: NavController,
    artist: String,
    musicTitle: String,
    albumImageUrl: String,
    content: String,
    videoUrl: String,
    duration: String,
    start: String,
    end: String,
    createDate: String,
    selectedDate: String = "",
    scope: String = "",
    diaryId: Long? = null,
    totalDuration: Int? = null, // YouTube 비디오 전체 길이 (초 단위)
    fromTab: String = "" // 어느 탭에서 왔는지 (profile, calendar, play)
) {
    val context = LocalContext.current
    val userViewModel: UserViewModel = viewModel()
    val userState by userViewModel.state.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val repo = remember { AuthRepository(context) }
    
    // 편집 모드 상태
    var isEditing by remember { mutableStateOf(false) }
    var currentContent by remember { mutableStateOf(content) }
    var editedContent by remember { mutableStateOf(content) }
    var isLoading by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        userViewModel.loadUserInfo(context)
    }
    
    // content가 변경되면 currentContent와 editedContent도 업데이트
    LaunchedEffect(content) {
        currentContent = content
        editedContent = content
    }

    // duration, start, end를 초 단위 Int로 변환
    // duration은 킬링파트의 길이(during), end는 킬링파트의 끝 시간
    val startSeconds = parseTimeToSeconds(start)
    val endSeconds = parseTimeToSeconds(end)

    // 디테일 페이지 진입 시 로그 출력
    LaunchedEffect(startSeconds, endSeconds, duration, totalDuration) {
        android.util.Log.d("DiaryDetailScreen", "=== DiaryDetailScreen 진입 ===")
        android.util.Log.d("DiaryDetailScreen", "시작초: $startSeconds")
        android.util.Log.d("DiaryDetailScreen", "끝초: $endSeconds")
        android.util.Log.d("DiaryDetailScreen", "duration: $duration")
        android.util.Log.d("DiaryDetailScreen", "totalDuration: $totalDuration")
        android.util.Log.d("DiaryDetailScreen", "===========================")
    }

    // 날짜 포맷팅 (2025.10.26 형식)
    val formattedDate = try {
        val date = LocalDate.parse(createDate.split("T")[0])
        date.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
    } catch (e: Exception) {
        createDate.split("T")[0]
    }

    AppBackground {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 상단 헤더
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { 
                        // 어느 탭에서 왔는지에 따라 적절한 탭으로 돌아가기
                        when (fromTab) {
                            "profile" -> {
                                // 내 프로필 탭으로 돌아가기
                                navController.navigate("main?tab=profile") {
                                    popUpTo("main") { inclusive = false }
                                }
                            }
                            "calendar" -> {
                                // MusicCalendarScreen으로 돌아가기 (CALENDAR 탭 선택 + 선택된 날짜 복원)
                                val selectedDateParam = if (selectedDate.isNotEmpty()) "&selectedDate=${android.net.Uri.encode(selectedDate)}" else ""
                                navController.navigate("main?tab=calendar$selectedDateParam") {
                                    popUpTo("main") { inclusive = false }
                                }
                            }
                            "play" -> {
                                // 킬링파트 재생 탭으로 돌아가기
                                navController.navigate("main?tab=play") {
                                    popUpTo("main") { inclusive = false }
                                }
                            }
                            else -> {
                                // 기본값: selectedDate가 있으면 calendar, 없으면 profile
                                if (selectedDate.isNotEmpty()) {
                                    val selectedDateParam = "&selectedDate=${android.net.Uri.encode(selectedDate)}"
                                    navController.navigate("main?tab=calendar$selectedDateParam") {
                                        popUpTo("main") { inclusive = false }
                                    }
                                } else {
                                    navController.navigate("main?tab=profile") {
                                        popUpTo("main") { inclusive = false }
                                    }
                                }
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "뒤로 가기",
                        tint = Color.White
                    )
                }

                if (!isEditing) {
                    if (diaryId != null) {
                        Row {
                            IconButton(
                                onClick = { showDeleteDialog = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "삭제",
                                    tint = Color.White
                                )
                            }
                            IconButton(
                                onClick = { isEditing = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "편집",
                                    tint = Color.White
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.width(48.dp))
                    }
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 음악 플레이어 섹션 (스와이프 가능)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                // Diary 객체 생성 (YouTubePlayerBox에 전달하기 위해)
                val diary = remember(diaryId, artist, musicTitle, albumImageUrl, videoUrl, duration, start, end, scope, totalDuration) {
                    val scopeEnum = try {
                        Scope.valueOf(scope.ifEmpty { "PRIVATE" })
                    } catch (e: Exception) {
                        Scope.PRIVATE
                    }
                    Diary(
                        id = diaryId,
                        artist = artist,
                        musicTitle = musicTitle,
                        albumImageUrl = albumImageUrl,
                        videoUrl = videoUrl,
                        duration = duration,
                        start = start,
                        end = end,
                        content = content,
                        createDate = createDate,
                        updateDate = createDate, // updateDate가 없으면 createDate 사용
                        scope = scopeEnum,
                        totalDuration = totalDuration
                    )
                }
                
                val pagerState = rememberPagerState(pageCount = { 2 }, initialPage = 0)
                
                // YouTube 플레이어는 항상 렌더링하여 재생 상태 유지
                Box(modifier = Modifier.fillMaxWidth()) {
                    // YouTube 플레이어 (항상 렌더링, 투명하게 오버레이)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .offset(x = if (pagerState.currentPage == 1) 0.dp else (-10000).dp) // 화면 밖으로 이동
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // YouTube 플레이어
                            YouTubePlayerBox(
                                diary = diary,
                                startSeconds = startSeconds.toFloat(),
                                durationSeconds = duration.toFloat()
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                    
                    // HorizontalPager (앨범 정보와 YouTube 플레이어 페이지)
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) { page ->
                        when (page) {
                            0 -> {
                                // 페이지 0: 앨범 정보
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // 앨범 아트
                                    AsyncImage(
                                        model = albumImageUrl,
                                        contentDescription = "앨범 아트",
                                        modifier = Modifier
                                            .size(110.dp)
                                            .clip(RoundedCornerShape(12.dp)),
                                        contentScale = ContentScale.Crop,
                                        placeholder = painterResource(id = R.drawable.example_video),
                                        error = painterResource(id = R.drawable.example_video)
                                    )

                                    Spacer(modifier = Modifier.width(20.dp))

                                    // 음악 정보
                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = musicTitle,
                                            color = Color.White,
                                            fontFamily = PaperlogyFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            maxLines = 2
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = artist,
                                            color = Color.White,
                                            fontFamily = PaperlogyFontFamily,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 10.sp,
                                            maxLines = 1
                                        )
                                        // 진행 바
                                        MusicTimeBarForDiaryDetail(
                                            artist = artist,
                                            musicTitle = musicTitle,
                                            start = startSeconds,
                                            during = duration.toInt(),
                                            totalDuration = diary.totalDuration
                                        )
                                    }
                                }
                            }
                            1 -> {
                                // 페이지 1: YouTube 플레이어 (위에서 항상 렌더링되므로 여기서는 빈 Box)
                                Box(modifier = Modifier.fillMaxSize())
                            }
                        }
                    }
                }
            }


            // 다이어리 콘텐츠 영역 - 남은 공간을 모두 차지
            Box(
                modifier = Modifier
                    .padding(horizontal = 24.dp) // margin
                    .fillMaxWidth()
                    .weight(1f)
                    .background(
                        color = Color(0xFF1D1E20),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(20.dp) // padding
            ) {
                // 다이어리 텍스트 (편집 모드에 따라 TextField 또는 Text)
                if (isEditing) {
                    OutlinedTextField(
                        value = editedContent,
                        onValueChange = { editedContent = it },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 70.dp), // 날짜/닉네임 공간 확보
                        textStyle = TextStyle(
                            fontSize = 16.sp,
                            fontFamily = PaperlogyFontFamily,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 30.sp,
                            color = Color.White
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = mainGreen.copy(alpha = 0.5f),
                            cursorColor = mainGreen,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = 60.dp) // 날짜/닉네임 공간 확보
                    ) {
                        Text(
                            text = currentContent,
                            color = Color.White,
                            fontFamily = PaperlogyFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp,
                            lineHeight = 30.sp
                        )
                    }
                }
                // 날짜와 닉네임을 우하단에 배치
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 5.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    // 날짜
                    Text(
                        text = formattedDate,
                        color = Color.White,
                        fontFamily = PaperlogyFontFamily,
                        fontWeight = FontWeight.W400,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 사용자명
                    Text(
                        text = when (val state = userState) {
                            is UserUiState.Success -> "@${state.userInfo.username}"
                            is UserUiState.Loading -> "@KILLINGPART"
                            is UserUiState.Error -> "@KILLINGPART"
                        },
                        color = Color.White,
                        fontFamily = PaperlogyFontFamily,
                        fontWeight = FontWeight.W400,
                        fontSize = 14.sp
                    )
                }
            }

            // 편집 모드일 때 저장/취소 버튼
            if (isEditing) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 취소 버튼
                    Text(
                        text = "취소",
                        color = Color(0xFFAAAAAA),
                        fontFamily = PaperlogyFontFamily,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .clickable {
                                editedContent = currentContent
                                isEditing = false
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // 저장 버튼
                    Text(
                        text = "저장",
                        color = mainGreen,
                        fontFamily = PaperlogyFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .clickable {
                                android.util.Log.d("DiaryDetailScreen", "저장 버튼 클릭 - diaryId: $diaryId, isLoading: $isLoading")
                                
                                if (diaryId == null) {
                                    android.util.Log.e("DiaryDetailScreen", "diaryId가 null입니다. 저장할 수 없습니다.")
                                    android.util.Log.e("DiaryDetailScreen", "백엔드 API가 id를 반환하지 않습니다. 백엔드 개발자에게 문의하세요.")
                                    return@clickable
                                }
                                
                                if (isLoading) {
                                    android.util.Log.d("DiaryDetailScreen", "이미 로딩 중입니다.")
                                    return@clickable
                                }
                                
                                isLoading = true
                                coroutineScope.launch {
                                    try {
                                        android.util.Log.d("DiaryDetailScreen", "다이어리 수정 시작 - diaryId: $diaryId")
                                        
                                        val scopeEnum = try {
                                            Scope.valueOf(scope.ifEmpty { "PRIVATE" })
                                        } catch (e: Exception) {
                                            Scope.PRIVATE
                                        }
                                        
                                        val updateRequest = CreateDiaryRequest(
                                            artist = artist,
                                            musicTitle = musicTitle,
                                            albumImageUrl = albumImageUrl,
                                            videoUrl = videoUrl,
                                            scope = scopeEnum.name,
                                            content = editedContent,
                                            duration = duration,
                                            start = start,
                                            end = end,
                                            totalDuration = totalDuration ?: 0 // DB에서 가져온 totalDuration 사용
                                        )
                                        
                                        android.util.Log.d("DiaryDetailScreen", "업데이트 요청 전송 중...")
                                        repo.updateDiary(diaryId, updateRequest)
                                        android.util.Log.d("DiaryDetailScreen", "다이어리 수정 성공")
                                        
                                        // 성공 시 현재 콘텐츠를 업데이트하고 편집 모드 종료
                                        currentContent = editedContent
                                        isEditing = false
                                    } catch (e: Exception) {
                                        android.util.Log.e("DiaryDetailScreen", "다이어리 수정 실패: ${e.message}", e)
                                        e.printStackTrace()
                                        // 에러 처리 (나중에 토스트 메시지 등 추가 가능)
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // 하단 네비게이션 바
            BottomBar(navController = navController)
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { 
                    if (!isDeleting) {
                        showDeleteDialog = false
                    }
                },
                title = {
                    Text(
                        text = "일기 삭제",
                        fontFamily = PaperlogyFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                },
                text = {
                    Text(
                        text = "일기를 삭제하시겠습니까?\n삭제된 일기는 복구할 수 없습니다.",
                        fontFamily = PaperlogyFontFamily,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (diaryId != null && !isDeleting) {
                                isDeleting = true
                                coroutineScope.launch {
                                    try {
                                        repo.deleteDiary(diaryId)

                                        val selectedDateParam = if (selectedDate.isNotEmpty()) "&selectedDate=${android.net.Uri.encode(selectedDate)}" else ""
                                        navController.navigate("main?tab=calendar$selectedDateParam") {
                                            popUpTo("main") { inclusive = false }
                                        }
                                    } catch (e: Exception) {
                                        android.util.Log.e("DiaryDetailScreen", "다이어리 삭제 실패: ${e.message}", e)
                                        e.printStackTrace()
                                        isDeleting = false
                                        showDeleteDialog = false
                                    }
                                }
                            }
                        },
                        enabled = !isDeleting
                    ) {
                        Text(
                            text = if (isDeleting) "삭제 중..." else "삭제",
                            fontFamily = PaperlogyFontFamily,
                            color = Color(0xFFFF4444),
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { 
                            if (!isDeleting) {
                                showDeleteDialog = false
                            }
                        },
                        enabled = !isDeleting
                    ) {
                        Text(
                            text = "취소",
                            fontFamily = PaperlogyFontFamily,
                            color = Color(0xFFAAAAAA)
                        )
                    }
                },
                containerColor = Color(0xFF1A1A1A),
                titleContentColor = Color.White,
                textContentColor = Color.White
            )
        }
    }
}

/**
 * 시간 문자열을 초 단위로 변환
 * "MM:SS" 형식, 숫자 문자열(소수점 포함 가능)을 지원
 */
private fun parseTimeToSeconds(timeStr: String): Int {
    return try {
        if (timeStr.contains(":")) {
            // "MM:SS" 형식
            val parts = timeStr.split(":")
            if (parts.size == 2) {
                val minutes = parts[0].toIntOrNull() ?: 0
                val seconds = parts[1].toIntOrNull() ?: 0
                minutes * 60 + seconds
            } else {
                0
            }
        } else {
            // 숫자 문자열 (초 단위, 소수점 포함 가능)
            // Float로 변환 후 Int로 반올림 (RunMusicBox와 동일한 방식)
            timeStr.toFloatOrNull()?.toInt() ?: 0
        }
    } catch (e: Exception) {
        0
    }
}

