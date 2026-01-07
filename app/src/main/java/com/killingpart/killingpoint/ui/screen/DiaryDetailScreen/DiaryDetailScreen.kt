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
import androidx.compose.material.icons.filled.KeyboardArrowLeft
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
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
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import com.killingpart.killingpoint.data.spotify.SimpleTrack
import java.net.URLDecoder
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.activity.compose.BackHandler

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
    fromTab: String = "", // 어느 탭에서 왔는지 (profile, calendar, play)
    authorUsername: String = "", // 일기 작성자 이름 (친구 프로필에서 올 때 사용)
    authorTag: String = "" // 일기 작성자 태그 (친구 프로필에서 올 때 사용)
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

    val startSeconds = parseTimeToSeconds(start)
    val endSeconds = parseTimeToSeconds(end)
    val duringSeconds = (endSeconds - startSeconds).coerceAtLeast(0)

    val formattedDate = try {
        val date = LocalDate.parse(createDate.split("T")[0])
        date.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
    } catch (e: Exception) {
        createDate.split("T")[0]
    }

    // 시스템 뒤로가기 처리 - 네비게이션 스택 확인
    BackHandler {
        val previousEntry = navController.previousBackStackEntry
        if (previousEntry != null) {
            // 이전 화면의 route 확인
            val previousRoute = previousEntry.destination.route
            // 이전 화면이 main이면 fromTab에 따라 올바른 탭으로 navigate
            if (previousRoute?.startsWith("main") == true) {
                when (fromTab) {
                    "profile" -> {
                        navController.navigate("main?tab=profile") {
                            popUpTo("main") { inclusive = false }
                        }
                    }
                    "calendar" -> {
                        val selectedDateParam = if (selectedDate.isNotEmpty()) "&selectedDate=${android.net.Uri.encode(selectedDate)}" else ""
                        navController.navigate("main?tab=calendar$selectedDateParam") {
                            popUpTo("main") { inclusive = false }
                        }
                    }
                    "play" -> {
                        navController.navigate("main?tab=play") {
                            popUpTo("main") { inclusive = false }
                        }
                    }
                    else -> {
                        navController.popBackStack()
                    }
                }
            } else {
                // 이전 화면이 main이 아니면 (예: friend_profile) 네비게이션 스택에서 pop
                navController.popBackStack()
            }
        } else {
            // 이전 화면이 없으면 fromTab에 따라 main으로 이동
            when (fromTab) {
                "profile" -> {
                    navController.navigate("main?tab=profile") {
                        popUpTo("main") { inclusive = false }
                    }
                }
                "calendar" -> {
                    val selectedDateParam = if (selectedDate.isNotEmpty()) "&selectedDate=${android.net.Uri.encode(selectedDate)}" else ""
                    navController.navigate("main?tab=calendar$selectedDateParam") {
                        popUpTo("main") { inclusive = false }
                    }
                }
                "play" -> {
                    navController.navigate("main?tab=play") {
                        popUpTo("main") { inclusive = false }
                    }
                }
                else -> {
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
    }

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
            updateDate = createDate,
            scope = scopeEnum,
            totalDuration = totalDuration
        )
    }

    AppBackground {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        // 네비게이션 스택 확인
                        val previousEntry = navController.previousBackStackEntry
                        if (previousEntry != null) {
                            // 이전 화면의 route 확인
                            val previousRoute = previousEntry.destination.route
                            // 이전 화면이 main이면 fromTab에 따라 올바른 탭으로 navigate
                            if (previousRoute?.startsWith("main") == true) {
                                when (fromTab) {
                                    "profile" -> {
                                        navController.navigate("main?tab=profile") {
                                            popUpTo("main") { inclusive = false }
                                        }
                                    }
                                    "calendar" -> {
                                        val selectedDateParam = if (selectedDate.isNotEmpty()) "&selectedDate=${android.net.Uri.encode(selectedDate)}" else ""
                                        navController.navigate("main?tab=calendar$selectedDateParam") {
                                            popUpTo("main") { inclusive = false }
                                        }
                                    }
                                    "play" -> {
                                        navController.navigate("main?tab=play") {
                                            popUpTo("main") { inclusive = false }
                                        }
                                    }
                                    else -> {
                                        navController.popBackStack()
                                    }
                                }
                            } else {
                                // 이전 화면이 main이 아니면 (예: friend_profile) 네비게이션 스택에서 pop
                                navController.popBackStack()
                            }
                        } else {
                            // 이전 화면이 없으면 fromTab에 따라 main으로 이동
                            when (fromTab) {
                                "profile" -> {
                                    navController.navigate("main?tab=profile") {
                                        popUpTo("main") { inclusive = false }
                                    }
                                }
                                "calendar" -> {
                                    val selectedDateParam = if (selectedDate.isNotEmpty()) "&selectedDate=${android.net.Uri.encode(selectedDate)}" else ""
                                    navController.navigate("main?tab=calendar$selectedDateParam") {
                                        popUpTo("main") { inclusive = false }
                                    }
                                }
                                "play" -> {
                                    navController.navigate("main?tab=play") {
                                        popUpTo("main") { inclusive = false }
                                    }
                                }
                                else -> {
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
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "뒤로 가기",
                        tint = Color.White
                    )
                }

                if (!isEditing) {
                    val isFriendProfile = authorUsername.isNotEmpty() && authorTag.isNotEmpty()
                    if (diaryId != null && !isFriendProfile) {
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

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(250.dp, 150.dp)
                ) {
                    YouTubePlayerBox(
                        diary = diary,
                        startSeconds = startSeconds.toFloat(),
                        durationSeconds = duringSeconds.toFloat()
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

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(
                            color = Color(0xFF1D1E20),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp)
                ) {
                    if (isEditing) {
                        OutlinedTextField(
                            value = editedContent,
                            onValueChange = { editedContent = it },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 50.dp),
                            textStyle = TextStyle(
                                fontSize = 13.sp,
                                fontFamily = PaperlogyFontFamily,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 20.sp,
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
                                .padding(bottom = 60.dp)
                        ) {
                            Text(
                                text = currentContent,
                                color = Color.White,
                                fontFamily = PaperlogyFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                lineHeight = 20.sp
                            )
                        }
                    }
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = 5.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = formattedDate,
                            color = Color.White,
                            fontFamily = PaperlogyFontFamily,
                            fontWeight = FontWeight.W400,
                            fontSize = 10.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (authorUsername.isNotEmpty() && authorTag.isNotEmpty()) {
                                // 친구 프로필에서 온 경우 친구 이름 표시
                                "@$authorTag"
                            } else {
                                // 내 일기인 경우 내 이름 표시
                                when (val state = userState) {
                                    is UserUiState.Success -> "@${state.userInfo.tag}"
                                    is UserUiState.Loading -> "@KILLINGPART"
                                    is UserUiState.Error -> "@KILLINGPART"
                                }
                            },
                            color = Color.White,
                            fontFamily = PaperlogyFontFamily,
                            fontWeight = FontWeight.W400,
                            fontSize = 10.sp
                        )
                    }
                }

                if (isEditing) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "취소",
                            color = Color(0xFFAAAAAA),
                            fontFamily = PaperlogyFontFamily,
                            fontSize = 10.sp,
                            modifier = Modifier
                                .clickable {
                                    editedContent = currentContent
                                    isEditing = false
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "저장",
                            color = mainGreen,
                            fontFamily = PaperlogyFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.sp,
                            modifier = Modifier
                                .clickable {
                                    if (diaryId == null || isLoading) return@clickable

                                    isLoading = true
                                    coroutineScope.launch {
                                        try {
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
                                                totalDuration = totalDuration ?: 0
                                            )

                                            repo.updateDiary(diaryId, updateRequest)
                                            currentContent = editedContent
                                            isEditing = false
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        } finally {
                                            isLoading = false
                                        }
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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

                                                val selectedDateParam =
                                                    if (selectedDate.isNotEmpty()) "&selectedDate=${
                                                        android.net.Uri.encode(selectedDate)
                                                    }" else ""
                                                navController.navigate("main?tab=calendar$selectedDateParam") {
                                                    popUpTo("main") { inclusive = false }
                                                }
                                            } catch (e: Exception) {
                                                android.util.Log.e(
                                                    "DiaryDetailScreen",
                                                    "다이어리 삭제 실패: ${e.message}",
                                                    e
                                                )
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

private fun parseTimeToSeconds(timeStr: String): Int {
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

@Preview
@Composable
fun DiaryDetailScreenPreview() {
    DiaryDetailScreen(
        navController = rememberNavController(),
        artist = "Davinci Leo",
        musicTitle = "Death Sonnet von Dat",
        albumImageUrl = "https://i.scdn.co/image/ab67616d00001e02c6b31f5f1ce2958380fdb9b0",
        content = "자세히 보아야 예쁘다\n오래 보아야 사랑스럽다.\n너도 그렇다",
        videoUrl = "https://www.youtube-nocookie.com/embed/example",
        duration = "10",
        start = "170",
        end = "180",
        createDate = "2025-09-19T00:00:00",
        selectedDate = "2025-09-19",
        scope = "PUBLIC",
        diaryId = 1L,
        totalDuration = 180,
        fromTab = "calendar"
    )
}
