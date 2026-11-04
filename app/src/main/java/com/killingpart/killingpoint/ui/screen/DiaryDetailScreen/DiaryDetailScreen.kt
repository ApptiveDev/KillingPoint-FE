package com.killingpart.killingpoint.ui.screen.DiaryDetailScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import com.killingpart.killingpoint.ui.component.LikesModal
import com.killingpart.killingpoint.data.model.DiaryLikeUser
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.zIndex
import androidx.compose.ui.Alignment
import com.killingpart.killingpoint.data.spotify.SimpleTrack
import java.net.URLDecoder
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.activity.compose.BackHandler
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
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

    // 이미지 저장/공유 진행 상태
    var isSaving by remember { mutableStateOf(false) }

    // 공유 방식 선택 바텀시트
    var showShareSheet by remember { mutableStateOf(false) }
    val shareSheetState = rememberModalBottomSheetState()

    val isOtherPersonDiary = diaryId != null &&
        authorUsername.isNotEmpty() &&
        authorTag.isNotEmpty()

    var isLiked by remember { mutableStateOf(false) }
    var likeCount by remember { mutableStateOf(0) }
    var isStored by remember { mutableStateOf(false) }
    var likesDiaryId by remember { mutableStateOf<Long?>(null) }
    var likesUsers by remember { mutableStateOf<List<DiaryLikeUser>>(emptyList()) }
    var isLoadingLikes by remember { mutableStateOf(false) }
    var likesError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        userViewModel.loadUserInfo(context)
    }

    LaunchedEffect(diaryId, isOtherPersonDiary) {
        if (!isOtherPersonDiary || diaryId == null) return@LaunchedEffect
        repo.getDiaryDetail(diaryId).onSuccess { detail ->
            isLiked = detail.isLiked
            likeCount = detail.likeCount
            isStored = detail.isStored
        }
    }

    LaunchedEffect(likesDiaryId) {
        val targetDiaryId = likesDiaryId ?: return@LaunchedEffect
        isLoadingLikes = true
        likesError = null
        repo.getDiaryLikes(diaryId = targetDiaryId, page = 0, size = 50, searchCond = null)
            .onSuccess { response ->
                likesUsers = response.content
            }
            .onFailure { e ->
                likesError = e.message
            }
        isLoadingLikes = false
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

    // 공유 카드용 값
    val shareTag = if (authorTag.isNotEmpty()) {
        "@$authorTag"
    } else {
        when (val s = userState) {
            is UserUiState.Success -> "@${s.userInfo.tag}"
            else -> "@KILLINGPART"
        }
    }
    val shareTotalDuration = (totalDuration ?: 0).coerceAtLeast(1)
    val shareStartProgress = (startSeconds.toFloat() / shareTotalDuration).coerceIn(0f, 1f)
    val shareEndProgress = (endSeconds.toFloat() / shareTotalDuration).coerceIn(0f, 1f)

    // 공유 카드에 넣을 코멘트: 공개(PUBLIC) 일기만 내용 노출, 비공개(PRIVATE/KILLING_PART)는 "비공개"로 대체
    val shareCardContent = run {
        val diaryScope = try {
            Scope.valueOf(scope.ifEmpty { "PRIVATE" })
        } catch (e: Exception) {
            Scope.PRIVATE
        }
        if (diaryScope == Scope.PUBLIC) currentContent else "비공개"
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
                        // 기존 main 엔트리로 복귀해야 스크롤 위치가 유지됨
                        navController.popBackStack()
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
            Spacer(modifier = Modifier.height(35.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
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
                                        // 기존 main 엔트리로 복귀해야 스크롤 위치가 유지됨
                                        navController.popBackStack()
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

                if (diaryId != null && !isOtherPersonDiary) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.storing),
                            contentDescription = "저장",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .height(38.dp)
                                .aspectRatio(96f / 164f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(enabled = !isSaving) {
                                    coroutineScope.launch {
                                        try {
                                            isSaving = true
                                            val activity = context.findActivity()
                                            if (activity == null) {
                                                Toast.makeText(context, "저장 실패: 화면을 찾을 수 없어요", Toast.LENGTH_SHORT).show()
                                                return@launch
                                            }
                                            val artwork = DiaryShareImage.loadArtwork(context, albumImageUrl)
                                            val bitmap = renderComposableToBitmap(activity) {
                                                DiaryShareCard(
                                                    artwork = artwork,
                                                    musicTitle = musicTitle,
                                                    artist = artist,
                                                    content = currentContent,
                                                    dateText = formattedDate,
                                                    tagText = shareTag,
                                                    startText = "%d:%02d".format(startSeconds / 60, startSeconds % 60),
                                                    endText = "%d:%02d".format(endSeconds / 60, endSeconds % 60),
                                                    startProgress = shareStartProgress,
                                                    endProgress = shareEndProgress
                                                )
                                            }
                                            DiaryShareImage.saveToGallery(
                                                context = context,
                                                bitmap = bitmap,
                                                displayName = "killingpart_diary_${diaryId ?: 0}_${System.currentTimeMillis()}"
                                            ).fold(
                                                onSuccess = {
                                                    Toast.makeText(context, "이미지를 저장했어요", Toast.LENGTH_SHORT).show()
                                                },
                                                onFailure = {
                                                    Toast.makeText(context, "저장 실패: ${it.message ?: "알 수 없는 오류"}", Toast.LENGTH_SHORT).show()
                                                }
                                            )
                                        } finally {
                                            isSaving = false
                                        }
                                    }
                                }
                        )
                        Image(
                            painter = painterResource(id = R.drawable.sharing),
                            contentDescription = "공유",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .height(38.dp)
                                .aspectRatio(96f / 164f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(enabled = !isSaving) { showShareSheet = true }
                        )
                        Image(
                            painter = painterResource(id = R.drawable.fixing),
                            contentDescription = "수정",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .height(38.dp)
                                .aspectRatio(96f / 164f)
                                .clip(RoundedCornerShape(8.dp))
                                .alpha(if (isEditing) 0.4f else 1f)
                                .clickable(enabled = !isEditing) { isEditing = true }
                        )
                        Image(
                            painter = painterResource(id = R.drawable.deleting),
                            contentDescription = "삭제",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .height(38.dp)
                                .aspectRatio(96f / 164f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showDeleteDialog = true }
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }
            }

            if (isOtherPersonDiary && !isEditing) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val likeBadgeShape = RoundedCornerShape(50)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .then(
                                    if (isLiked) {
                                        Modifier
                                            .background(mainGreen.copy(alpha = 0.25f), likeBadgeShape)
                                            .border(1.dp, mainGreen, likeBadgeShape)
                                    } else {
                                        Modifier
                                            .background(Color(0xFF1A1A1A), likeBadgeShape)
                                            .border(1.dp, Color.White.copy(alpha = 0.4f), likeBadgeShape)
                                    }
                                )
                                .pointerInput(diaryId) {
                                    detectTapGestures(
                                        onTap = {
                                            val id = diaryId ?: return@detectTapGestures
                                            val previousIsLiked = isLiked
                                            val previousLikeCount = likeCount
                                            isLiked = !isLiked
                                            likeCount = if (!previousIsLiked) {
                                                likeCount + 1
                                            } else {
                                                (likeCount - 1).coerceAtLeast(0)
                                            }
                                            coroutineScope.launch {
                                                repo.toggleLike(id).fold(
                                                    onSuccess = { response ->
                                                        isLiked = response.isLiked
                                                    },
                                                    onFailure = {
                                                        isLiked = previousIsLiked
                                                        likeCount = previousLikeCount
                                                    }
                                                )
                                            }
                                        },
                                        onLongPress = {
                                            diaryId?.let { likesDiaryId = it }
                                        }
                                    )
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Favorite,
                                contentDescription = "좋아요",
                                tint = if (isLiked) mainGreen else Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = likeCount.toString(),
                                fontFamily = PaperlogyFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                // color = if (isLiked) mainGreen else Color.White
                                color = Color.White
                            )
                        }

                        Image(
                            painter = painterResource(
                                id = if (isStored) R.drawable.is_stored else R.drawable.is_not_stored
                            ),
                            contentDescription = if (isStored) "보관됨" else "보관하기",
                            modifier = Modifier
                                .size(24.dp)
                                .clickable {
                                    val id = diaryId ?: return@clickable
                                    val previousIsStored = isStored
                                    isStored = !isStored
                                    coroutineScope.launch {
                                        repo.toggleStore(id).fold(
                                            onSuccess = { response ->
                                                isStored = response.isStored
                                            },
                                            onFailure = {
                                                isStored = previousIsStored
                                            }
                                        )
                                    }
                                }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(5.dp))
            } else {
                Spacer(modifier = Modifier.height(6.dp))
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    YouTubePlayerBox(
                        diary = diary,
                        startSeconds = startSeconds.toFloat(),
                        durationSeconds = duringSeconds.toFloat(),
                        shouldLoop = true,
                        showTrackInfo = false
                    )
                }

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

            if (fromTab != "stored") {
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
                                    "@$authorTag"
                                } else {
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
                                fontSize = 11.sp,
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
                                fontSize = 11.sp,
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
            }

            Spacer(modifier = Modifier.height(16.dp))

            BottomBar(navController = navController)
        }

        if (showShareSheet) {
            ModalBottomSheet(
                onDismissRequest = { showShareSheet = false },
                sheetState = shareSheetState,
                containerColor = Color(0xFF1A1A1A)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 24.dp)
                ) {
                    Text(
                        text = "공유하기",
                        color = Color.White,
                        fontFamily = PaperlogyFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "공유할 방식을 선택해 주세요.",
                        color = Color(0xFFAAAAAA),
                        fontFamily = PaperlogyFontFamily,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    ShareOptionItem(text = "공유") {
                        showShareSheet = false
                        coroutineScope.launch {
                            try {
                                isSaving = true
                                val activity = context.findActivity()
                                if (activity == null) {
                                    Toast.makeText(context, "공유 실패: 화면을 찾을 수 없어요", Toast.LENGTH_SHORT).show()
                                    return@launch
                                }
                                val artwork = DiaryShareImage.loadArtwork(context, albumImageUrl)
                                val bitmap = renderComposableToBitmap(activity) {
                                    DiaryShareCard(
                                        artwork = artwork,
                                        musicTitle = musicTitle,
                                        artist = artist,
                                        content = shareCardContent,
                                        dateText = formattedDate,
                                        tagText = shareTag,
                                        startText = "%d:%02d".format(startSeconds / 60, startSeconds % 60),
                                        endText = "%d:%02d".format(endSeconds / 60, endSeconds % 60),
                                        startProgress = shareStartProgress,
                                        endProgress = shareEndProgress
                                    )
                                }
                                val link = "https://killingpart.com/diaries/${diaryId ?: 0}"
                                DiaryShareImage.shareImageNative(context, bitmap, link).onFailure {
                                    Toast.makeText(context, "공유 실패: ${it.message ?: "알 수 없는 오류"}", Toast.LENGTH_SHORT).show()
                                }
                            } finally {
                                isSaving = false
                            }
                        }
                    }
                    ShareOptionItem(text = "카카오톡 공유") {
                        showShareSheet = false
                        coroutineScope.launch {
                            try {
                                isSaving = true
                                val activity = context.findActivity()
                                if (activity == null) {
                                    Toast.makeText(context, "공유 실패: 화면을 찾을 수 없어요", Toast.LENGTH_SHORT).show()
                                    return@launch
                                }
                                val artwork = DiaryShareImage.loadArtwork(context, albumImageUrl)
                                val bitmap = renderComposableToBitmap(activity) {
                                    DiaryShareCard(
                                        artwork = artwork,
                                        musicTitle = musicTitle,
                                        artist = artist,
                                        content = shareCardContent,
                                        dateText = formattedDate,
                                        tagText = shareTag,
                                        startText = "%d:%02d".format(startSeconds / 60, startSeconds % 60),
                                        endText = "%d:%02d".format(endSeconds / 60, endSeconds % 60),
                                        startProgress = shareStartProgress,
                                        endProgress = shareEndProgress
                                    )
                                }
                                val link = "https://killingpart.com/diaries/${diaryId ?: 0}"
                                val title = if (artist.isNotBlank()) "$musicTitle - $artist" else musicTitle
                                val description = shareCardContent.ifBlank { "킬링파트에서 다이어리를 확인해 보세요." }
                                DiaryShareImage.shareKakao(context, bitmap, title, description, link, diaryId ?: 0L).onFailure {
                                    Toast.makeText(context, "카카오톡 공유 실패: ${it.message ?: "알 수 없는 오류"}", Toast.LENGTH_SHORT).show()
                                }
                            } finally {
                                isSaving = false
                            }
                        }
                    }
                    ShareOptionItem(text = "인스타 스토리 공유") {
                        showShareSheet = false
                        coroutineScope.launch {
                            try {
                                isSaving = true
                                val activity = context.findActivity()
                                if (activity == null) {
                                    Toast.makeText(context, "공유 실패: 화면을 찾을 수 없어요", Toast.LENGTH_SHORT).show()
                                    return@launch
                                }
                                val artwork = DiaryShareImage.loadArtwork(context, albumImageUrl)
                                val bitmap = renderComposableToBitmap(activity) {
                                    DiaryShareCard(
                                        artwork = artwork,
                                        musicTitle = musicTitle,
                                        artist = artist,
                                        content = shareCardContent,
                                        dateText = formattedDate,
                                        tagText = shareTag,
                                        startText = "%d:%02d".format(startSeconds / 60, startSeconds % 60),
                                        endText = "%d:%02d".format(endSeconds / 60, endSeconds % 60),
                                        startProgress = shareStartProgress,
                                        endProgress = shareEndProgress
                                    )
                                }
                                val link = "https://killingpart.com/diaries/${diaryId ?: 0}"
                                val fbAppId = context.getString(R.string.facebook_app_id)
                                DiaryShareImage.shareInstagramStory(context, bitmap, link, fbAppId).onFailure {
                                    Toast.makeText(context, "인스타 스토리 공유 실패: ${it.message ?: "알 수 없는 오류"}", Toast.LENGTH_SHORT).show()
                                }
                            } finally {
                                isSaving = false
                            }
                        }
                    }
                }
            }
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
                                                // 삭제 후 fromTab에 따라 올바른 탭으로 이동
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
                                                        // 기본값은 profile
                                                        navController.navigate("main?tab=profile") {
                                                            popUpTo("main") { inclusive = false }
                                                        }
                                                    }
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

        if (likesDiaryId != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(10f)
            ) {
                LikesModal(
                    isLoading = isLoadingLikes,
                    error = likesError,
                    users = likesUsers,
                    onDismiss = {
                        likesDiaryId = null
                        likesUsers = emptyList()
                        likesError = null
                    },
                    onUserClick = { user ->
                        likesDiaryId = null
                        likesUsers = emptyList()
                        likesError = null
                        val encodedUsername = java.net.URLEncoder.encode(user.username, "UTF-8")
                        val encodedTag = java.net.URLEncoder.encode(user.tag, "UTF-8")
                        val encodedProfileImageUrl =
                            java.net.URLEncoder.encode(user.profileImageUrl, "UTF-8")
                        navController.navigate(
                            "friend_profile" +
                                "?userId=${user.userId}" +
                                "&username=$encodedUsername" +
                                "&tag=$encodedTag" +
                                "&profileImageUrl=$encodedProfileImageUrl" +
                                "&isMyPick=false"
                        )
                    }
                )
            }
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

@Composable
private fun ShareOptionItem(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        color = Color.White,
        fontFamily = PaperlogyFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 16.dp, horizontal = 4.dp)
    )
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
