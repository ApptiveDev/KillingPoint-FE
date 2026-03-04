package com.killingpart.killingpoint.ui.screen.SocialScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
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
import com.killingpart.killingpoint.data.model.Diary
import com.killingpart.killingpoint.data.model.Scope
import com.killingpart.killingpoint.ui.component.AppBackground
import com.killingpart.killingpoint.ui.component.BottomBar
import com.killingpart.killingpoint.ui.screen.ArchiveScreen.DiaryCard
import com.killingpart.killingpoint.ui.screen.MainScreen.TopPillTabs
import com.killingpart.killingpoint.ui.screen.SocialScreen.FeedScreen
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import com.killingpart.killingpoint.ui.theme.mainGreen
import com.killingpart.killingpoint.ui.viewmodel.FriendProfileViewModel
import com.killingpart.killingpoint.ui.viewmodel.FriendProfileUiState
import com.killingpart.killingpoint.ui.viewmodel.FriendViewModel
import android.net.Uri
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.draw.alpha
import androidx.compose.material.icons.filled.Search
import androidx.activity.compose.BackHandler
import kotlinx.coroutines.launch

enum class FriendProfileTab {
    FEED, FRIEND
}

@Composable
fun FriendProfileScreen(
    navController: NavController,
    userId: Long,
    username: String = "",
    tag: String = "",
    profileImageUrl: String = "",
    isMyPick: Boolean = false
) {
    var selectedTab by remember { mutableStateOf(FriendProfileTab.FRIEND) }
    var currentUserId by remember { mutableStateOf(0L) }
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val profileViewModel: FriendProfileViewModel = viewModel()
    val profileState by profileViewModel.state.collectAsState()
    val friendViewModel: FriendViewModel = viewModel()
    val friendState by friendViewModel.state.collectAsState()
    val userViewModel: com.killingpart.killingpoint.ui.viewmodel.UserViewModel = viewModel()
    val currentUserState by userViewModel.state.collectAsState()

    // picks 목록에서 현재 userId가 있는지 확인하여 isMyPick 상태 결정
    val currentIsMyPick = remember(friendState, userId) {
        when (val state = friendState) {
            is com.killingpart.killingpoint.ui.viewmodel.FriendUiState.Success -> {
                state.picks?.content?.any { it.userId == userId } ?: isMyPick
            }
            else -> isMyPick
        }
    }

    LaunchedEffect(userId) {
        profileViewModel.loadFriendProfile(context, userId)
        userViewModel.loadUserInfo(context)
        
        // 현재 사용자 ID 가져오기 (구독 추가/취소용)
        val repo = com.killingpart.killingpoint.data.repository.AuthRepository(context)
        val userIdFromToken = repo.getUserIdFromToken()
        if (userIdFromToken != null) {
            currentUserId = userIdFromToken
            // 통계를 가져와서 전체 picks 목록 로드
            repo.getUserStatistics(userIdFromToken)
                .onSuccess { statistics ->
                    // picks 목록 로드하여 isMyPick 상태 확인 (전체 목록을 가져오기 위해 pickCount 전달)
                    friendViewModel.loadFriends(
                        context, 
                        userIdFromToken, 
                        statistics.pickCount, 
                        statistics.fanCount
                    )
                }
                .onFailure { e ->
                    android.util.Log.e("FriendProfileScreen", "통계 조회 실패: ${e.message}")
                    // 통계 조회 실패해도 충분히 큰 값으로 picks 목록 로드
                    friendViewModel.loadFriends(context, userIdFromToken, 100, 100)
                }
        }
    }

    // 시스템 뒤로가기 처리 - 네비게이션 스택에서 자동으로 이전 화면으로 이동
    BackHandler {
        navController.popBackStack()
    }

    AppBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(35.dp))

                TopPillTabs(
                    options = listOf("피드", "친구"),
                    selectedIndex = when (selectedTab) {
                        FriendProfileTab.FEED -> 0
                        FriendProfileTab.FRIEND -> 1
                    },
                    onSelected = { idx ->
                        selectedTab = when (idx) {
                            0 -> FriendProfileTab.FEED
                            else -> FriendProfileTab.FRIEND
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 30.dp)
                )

                Spacer(modifier = Modifier.height(7.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    when (selectedTab) {
                        FriendProfileTab.FEED -> FeedScreen(navController)
                        FriendProfileTab.FRIEND -> {
                            when (val state = profileState) {
                                is FriendProfileUiState.Loading -> {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(color = mainGreen)
                                    }
                                }

                                is FriendProfileUiState.Success -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 30.dp, vertical = 10.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .fillMaxHeight()
                                        ) {
                                            // 프로필 영역 (OuterBox 스타일)
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // 프로필 사진과 이름
                                                Row(
                                                    modifier = Modifier.weight(1f, fill = false),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                                ) {
                                                    // 프로필 사진
                                                    AsyncImage(
                                                        model = profileImageUrl.ifEmpty { R.drawable.default_profile },
                                                        contentDescription = "프로필 사진",
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier
                                                            .size(60.dp)
                                                            .clip(RoundedCornerShape(50))
                                                            .border(
                                                                3.dp,
                                                                mainGreen,
                                                                RoundedCornerShape(50)
                                                            ),
                                                        placeholder = painterResource(id = R.drawable.default_profile),
                                                        error = painterResource(id = R.drawable.default_profile)
                                                    )

                                                    // username과 tag
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = username.ifEmpty { "사용자" },
                                                            fontFamily = PaperlogyFontFamily,
                                                            fontWeight = FontWeight.W400,
                                                            fontSize = 14.sp,
                                                            color = mainGreen,
                                                            maxLines = 1,
                                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                        )
                                                        Spacer(modifier=Modifier.height(6.dp))

                                                        Text(
                                                            text = "@${tag.ifEmpty { "unknown" }}",
                                                            fontFamily = PaperlogyFontFamily,
                                                            fontWeight = FontWeight.W400,
                                                            fontSize = 12.sp,
                                                            color = mainGreen,
                                                            maxLines = 1,
                                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                        )
                                                    }
                                                }
                                                
                                                Spacer(modifier = Modifier.width(8.dp))

                                                // 통계 표시 (팬덤, PICKS, 킬링파트)
                                                Row(
//                                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    // 킬링파트
                                                    Column(
                                                        horizontalAlignment = Alignment.CenterHorizontally
                                                    ) {
                                                        Text(
                                                            text = "${state.diaries?.content?.size ?: 0}",
                                                            fontFamily = PaperlogyFontFamily,
                                                            fontWeight = FontWeight.W400,
                                                            fontSize = 16.sp,
                                                            color = mainGreen,
                                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                                        )
                                                        Spacer(modifier=Modifier.height(3.dp))
                                                        Text(
                                                            text = "킬링파트",
                                                            fontFamily = PaperlogyFontFamily,
                                                            fontWeight = FontWeight.W400,
                                                            fontSize = 10.sp,
                                                            color = mainGreen,
                                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                                        )
                                                    }
                                                    Spacer(modifier=Modifier.width(10.dp))
                                                    // 팬덤
                                                    Column(
                                                        modifier = Modifier.clickable {
                                                            navController.navigate(
                                                                "pick_fandom_list?userId=$userId&tag=${Uri.encode(tag)}"
                                                            )
                                                        },
                                                        horizontalAlignment = Alignment.CenterHorizontally
                                                    ) {
                                                        Text(
                                                            text = "${state.fansCount}",
                                                            fontFamily = PaperlogyFontFamily,
                                                            fontWeight = FontWeight.W400,
                                                            fontSize = 16.sp,
                                                            color = mainGreen,
                                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                                        )
                                                        Spacer(modifier=Modifier.height(3.dp))
                                                        Text(
                                                            text = "팬덤",
                                                            fontFamily = PaperlogyFontFamily,
                                                            fontWeight = FontWeight.W400,
                                                            fontSize = 10.sp,
                                                            color = mainGreen,
                                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                                        )
                                                    }
                                                    Spacer(modifier=Modifier.width(12.dp))
                                                    // PICKS
                                                    Column(
                                                        modifier = Modifier.clickable {
                                                            navController.navigate(
                                                                "pick_fandom_list?userId=$userId&tag=${Uri.encode(tag)}"
                                                            )
                                                        },
                                                        horizontalAlignment = Alignment.CenterHorizontally
                                                    ) {
                                                        Text(
                                                            text = "${state.picksCount}",
                                                            fontFamily = PaperlogyFontFamily,
                                                            fontWeight = FontWeight.W400,
                                                            fontSize = 16.sp,
                                                            color = mainGreen,
                                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                                        )
                                                        Spacer(modifier=Modifier.height(3.dp))

                                                        Text(
                                                            text = "PICKS",
                                                            fontFamily = PaperlogyFontFamily,
                                                            fontWeight = FontWeight.W400,
                                                            fontSize = 10.sp,
                                                            color = mainGreen,
                                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                                        )
                                                    }



                                                }
                                            }

                                            // 구독 버튼 (프로필 편집 버튼 위치)
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.End
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth(0.8f)
                                                        .height(32.dp)
                                                        .background(
                                                            color = if (currentIsMyPick) Color(0xFFCEFF43) else Color(
                                                                0xFF262626
                                                            ),
                                                            shape = RoundedCornerShape(10.dp)
                                                        )
                                                        .clickable {
                                                            val repo = com.killingpart.killingpoint.data.repository.AuthRepository(context)
                                                            if (currentIsMyPick) {
                                                                // 구독 취소
                                                                friendViewModel.removeSubscribe(
                                                                    context = context,
                                                                    subscribeToUserId = userId,
                                                                    currentUserId = currentUserId
                                                                ) {
                                                                    // 구독 취소 후 통계를 다시 가져와서 전체 목록 로드
                                                                    scope.launch {
                                                                        repo.getUserStatistics(currentUserId)
                                                                            .onSuccess { statistics ->
                                                                                friendViewModel.loadFriends(
                                                                                    context,
                                                                                    currentUserId,
                                                                                    statistics.pickCount,
                                                                                    statistics.fanCount
                                                                                )
                                                                                profileViewModel.loadFriendProfile(
                                                                                    context,
                                                                                    userId
                                                                                )
                                                                            }
                                                                            .onFailure { e ->
                                                                                android.util.Log.e("FriendProfileScreen", "통계 조회 실패: ${e.message}")
                                                                                friendViewModel.loadFriends(context, currentUserId, 100, 100)
                                                                                profileViewModel.loadFriendProfile(context, userId)
                                                                            }
                                                                    }
                                                                }
                                                            } else {
                                                                // 구독 추가
                                                                friendViewModel.addSubscribe(
                                                                    context = context,
                                                                    subscribeToUserId = userId,
                                                                    currentUserId = currentUserId
                                                                ) {
                                                                    // 구독 추가 후 통계를 다시 가져와서 전체 목록 로드
                                                                    scope.launch {
                                                                        repo.getUserStatistics(currentUserId)
                                                                            .onSuccess { statistics ->
                                                                                friendViewModel.loadFriends(
                                                                                    context,
                                                                                    currentUserId,
                                                                                    statistics.pickCount,
                                                                                    statistics.fanCount
                                                                                )
                                                                                profileViewModel.loadFriendProfile(
                                                                                    context,
                                                                                    userId
                                                                                )
                                                                            }
                                                                            .onFailure { e ->
                                                                                android.util.Log.e("FriendProfileScreen", "통계 조회 실패: ${e.message}")
                                                                                friendViewModel.loadFriends(context, currentUserId, 100, 100)
                                                                                profileViewModel.loadFriendProfile(context, userId)
                                                                            }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                        .then(
                                                            if (currentIsMyPick) {
                                                                Modifier.border(
                                                                    1.dp,
                                                                    mainGreen,
                                                                    RoundedCornerShape(10.dp)
                                                                )
                                                            } else {
                                                                Modifier
                                                            }
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = if (currentIsMyPick) "나의 PICK!" else "나의 픽으로 추가",
                                                        color = if (currentIsMyPick) Color(0xFF000000) else Color(0xFFCEFF43),
                                                        fontFamily = PaperlogyFontFamily,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.W400
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(20.dp))

                                            // 다이어리 그리드 (2x2 레이아웃)
                                            val configuration = LocalConfiguration.current
                                            val screenWidth = configuration.screenWidthDp.dp
                                            val horizontalContainerPadding = 20.dp
                                            val interColumnSpacing = 12.dp
                                            val rowSpacing = 20.dp
                                            val itemSize =
                                                (screenWidth - horizontalContainerPadding * 2 - interColumnSpacing) / 2

                                            val diaries = state.diaries?.content ?: emptyList()
                                            val chunkedDiaries = diaries.chunked(2)

                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .weight(1f)
                                            ) {
                                                // 배경 로고
                                                Image(
                                                    painter = painterResource(id = R.drawable.killingpart_logo_gray),
                                                    contentDescription = null,
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .alpha(0.3f),
                                                    contentScale = ContentScale.Fit,
                                                    alignment = Alignment.Center
                                                )

                                                if (diaries.isEmpty()) {
                                                    Box(
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = "작성한 일기가 없습니다",
                                                            fontFamily = PaperlogyFontFamily,
                                                            fontWeight = FontWeight.Light,
                                                            fontSize = 14.sp,
                                                            color = Color(0xFF7B7B7B)
                                                        )
                                                    }
                                                } else {
                                                    LazyColumn(
                                                        modifier = Modifier.fillMaxSize()
                                                    ) {
                                                        items(chunkedDiaries.size) { index ->
                                                            val rowItems = chunkedDiaries[index]
                                                            Row(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .padding(bottom = rowSpacing),
                                                                horizontalArrangement = Arrangement.spacedBy(
                                                                    12.dp
                                                                )
                                                            ) {
                                                                rowItems.forEach { diary ->
                                                                    DiaryCard(
                                                                        diary = diary,
                                                                        modifier = Modifier.weight(
                                                                            1f
                                                                        ),
                                                                        onClick = {
                                                                            // DiaryDetailScreen으로 이동
                                                                            val diaryIdParam =
                                                                                diary.id?.let { "&diaryId=$it" }
                                                                                    ?: ""

                                                                            val totalDurationParam =
                                                                                diary.totalDuration?.let { "&totalDuration=$it" }
                                                                                    ?: ""

                                                                            val scopeParam =
                                                                                "&scope=${diary.scope.name}"
                                                                            
                                                                            val authorUsernameParam =
                                                                                "&authorUsername=${Uri.encode(username)}"
                                                                            val authorTagParam =
                                                                                "&authorTag=${Uri.encode(tag)}"

                                                                            navController.navigate(
                                                                                "diary_detail" +
                                                                                        "?artist=${
                                                                                            Uri.encode(
                                                                                                diary.artist
                                                                                            )
                                                                                        }" +
                                                                                        "&musicTitle=${
                                                                                            Uri.encode(
                                                                                                diary.musicTitle
                                                                                            )
                                                                                        }" +
                                                                                        "&albumImageUrl=${
                                                                                            Uri.encode(
                                                                                                diary.albumImageUrl
                                                                                            )
                                                                                        }" +
                                                                                        "&content=${
                                                                                            Uri.encode(
                                                                                                if (diary.scope == Scope.PRIVATE) "비공개 일기입니다." else diary.content
                                                                                            )
                                                                                        }" +
                                                                                        "&videoUrl=${
                                                                                            Uri.encode(
                                                                                                diary.videoUrl
                                                                                            )
                                                                                        }" +
                                                                                        "&duration=${
                                                                                            Uri.encode(
                                                                                                diary.duration
                                                                                            )
                                                                                        }" +
                                                                                        "&start=${
                                                                                            Uri.encode(
                                                                                                diary.start
                                                                                            )
                                                                                        }" +
                                                                                        "&end=${
                                                                                            Uri.encode(
                                                                                                diary.end
                                                                                            )
                                                                                        }" +
                                                                                        "&createDate=${
                                                                                            Uri.encode(
                                                                                                diary.createDate
                                                                                            )
                                                                                        }" +
                                                                                        scopeParam +
                                                                                        diaryIdParam +
                                                                                        totalDurationParam +
                                                                                        "&fromTab=social" +
                                                                                        authorUsernameParam +
                                                                                        authorTagParam
                                                                            )
                                                                        }
                                                                    )
                                                                }
                                                                // 홀수 개일 경우 빈 공간 추가
                                                                if (rowItems.size == 1) {
                                                                    Spacer(
                                                                        modifier = Modifier.weight(
                                                                            1f
                                                                        )
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

                                is FriendProfileUiState.Error -> {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = state.message,
                                            fontFamily = PaperlogyFontFamily,
                                            fontWeight = FontWeight.Light,
                                            fontSize = 14.sp,
                                            color = Color.Red
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                BottomBar(navController = navController)
            }
        }
    }
}


