package com.killingpart.killingpoint.ui.screen.SocialScreen

import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.killingpart.killingpoint.ui.component.LikesModal
import com.killingpart.killingpoint.ui.screen.MainScreen.MusicTimeBar
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import com.killingpart.killingpoint.ui.theme.mainGreen
import com.killingpart.killingpoint.ui.viewmodel.FeedUiState
import com.killingpart.killingpoint.ui.viewmodel.FeedViewModel
import kotlinx.coroutines.launch

@Composable
fun FeedScreen(navController: NavController) {
    val context = LocalContext.current
    val feedViewModel: FeedViewModel = viewModel()
    val feedState by feedViewModel.state.collectAsState()
    val listState = rememberLazyListState()
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    // 좋아요 모달 상태 추가
    var likesDiaryId by remember { mutableStateOf<Long?>(null) }
    var likesUsers by remember { mutableStateOf<List<com.killingpart.killingpoint.data.model.DiaryLikeUser>>(emptyList()) }
    var isLoadingLikes by remember { mutableStateOf(false) }
    var likesError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        feedViewModel.loadFeeds(context)
    }

    val currentItemIndex = remember {
        derivedStateOf {
            val firstVisible = listState.firstVisibleItemIndex
            val scrollOffset = listState.firstVisibleItemScrollOffset
            val layoutInfo = listState.layoutInfo
            val firstVisibleItem = layoutInfo.visibleItemsInfo.firstOrNull { it.index == firstVisible }
            val itemWidthPx = firstVisibleItem?.size?.toFloat() ?: with(density) { screenWidth.toPx() }
            if (scrollOffset > itemWidthPx * 0.5f) {
                firstVisible + 1
            } else {
                firstVisible
            }
        }
    }

    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    LaunchedEffect(likesDiaryId) {
        val targetId = likesDiaryId ?: return@LaunchedEffect
        isLoadingLikes = true
        likesError = null
        val repo = com.killingpart.killingpoint.data.repository.AuthRepository(context)
        repo.getDiaryLikes(diaryId = targetId, page = 0, size = 50, searchCond = null)
            .onSuccess { response ->
                likesUsers = response.content
            }
            .onFailure { e ->
                likesError = e.message
            }
        isLoadingLikes = false
    }
    when (val state = feedState) {
        is FeedUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = mainGreen)
            }
        }
        is FeedUiState.Success -> {
            if (state.feeds.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "아직 올라온 킬링파트가 없습니다",
                        color = mainGreen,
                        fontFamily = PaperlogyFontFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    LazyRow(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        flingBehavior = snapFlingBehavior
                    ) {
                        itemsIndexed(
                            items = state.feeds,
                            key = { _, feed -> feed.diaryId ?: feed.hashCode() }
                        ) { index, feedDiary ->
                            val isCurrentItem = index == currentItemIndex.value
                            
                            LaunchedEffect(index, isCurrentItem) {
                                android.util.Log.d("FeedScreen", "Item[$index]: isCurrentItem=$isCurrentItem, diaryId=${feedDiary.diaryId}, videoUrl=${feedDiary.videoUrl}")
                            }
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(screenWidth)
                            ) {
                                FeedRunMusicBox(
                                    feedDiary = feedDiary,
                                    navController = navController,
                                    isActive = isCurrentItem,
                                    onVideoEnd = {
                                        val feeds = (feedState as? FeedUiState.Success)?.feeds ?: return@FeedRunMusicBox
                                        val currentIndex = currentItemIndex.value
                                        scope.launch {
                                            if (currentIndex < feeds.size - 1) {
                                                listState.animateScrollToItem(
                                                    index = currentIndex + 1,
                                                    scrollOffset = 0
                                                )
                                            } else {
                                                listState.animateScrollToItem(
                                                    index = 0,
                                                    scrollOffset = 0
                                                )
                                            }
                                        }
                                    },
                                    onLikeClick = {
                                        feedDiary.diaryId?.let { diaryId ->
                                            val currentState = feedViewModel.state.value
                                            if (currentState is FeedUiState.Success) {
                                                val currentFeed = currentState.feeds.find { it.diaryId == diaryId }
                                                val currentIsLiked = currentFeed?.isLiked ?: false

                                                val updatedFeeds = currentState.feeds.map { feed ->
                                                    if (feed.diaryId == diaryId) {
                                                        feed.copy(
                                                            isLiked = !currentIsLiked,
                                                            likeCount = if (!currentIsLiked) feed.likeCount + 1 else (feed.likeCount - 1).coerceAtLeast(0)
                                                        )
                                                    } else {
                                                        feed
                                                    }
                                                }
                                                feedViewModel.updateFeeds(updatedFeeds)

                                                feedViewModel.toggleLike(
                                                    context = context,
                                                    diaryId = diaryId,
                                                    onSuccess = { isLiked ->
                                                        val apiState = feedViewModel.state.value
                                                        if (apiState is FeedUiState.Success) {
                                                            val finalFeeds = apiState.feeds.map { feed ->
                                                                if (feed.diaryId == diaryId) {
                                                                    val expectedCount = if (isLiked) {
                                                                        (currentFeed?.likeCount ?: 0) + 1
                                                                    } else {
                                                                        ((currentFeed?.likeCount ?: 0) - 1).coerceAtLeast(0)
                                                                    }
                                                                    feed.copy(
                                                                        isLiked = isLiked,
                                                                        likeCount = expectedCount
                                                                    )
                                                                } else {
                                                                    feed
                                                                }
                                                            }
                                                            feedViewModel.updateFeeds(finalFeeds)
                                                        }
                                                    },
                                                    onFailure = {
                                                        val apiState = feedViewModel.state.value
                                                        if (apiState is FeedUiState.Success) {
                                                            val revertedFeeds = apiState.feeds.map { feed ->
                                                                if (feed.diaryId == diaryId) {
                                                                    feed.copy(
                                                                        isLiked = currentIsLiked,
                                                                        likeCount = currentFeed?.likeCount ?: 0
                                                                    )
                                                                } else {
                                                                    feed
                                                                }
                                                            }
                                                            feedViewModel.updateFeeds(revertedFeeds)
                                                        }
                                                    }
                                                )
                                            }
                                        }
                                    },
                                    onLongLikeClick = { diaryId ->
                                        likesDiaryId = diaryId
                                    },
                                    onStoreClick = {
                                        feedDiary.diaryId.let { diaryId ->
                                            val currentState = feedViewModel.state.value
                                            if (currentState is FeedUiState.Success) {
                                                val currentFeed = currentState.feeds.find { it.diaryId == diaryId }
                                                val currentIsStored = currentFeed?.isStored ?: false

                                                val updatedFeeds = currentState.feeds.map { feed ->
                                                    if (feed.diaryId == diaryId) {
                                                        feed.copy(isStored = !currentIsStored)
                                                    } else {
                                                        feed
                                                    }
                                                }
                                                feedViewModel.updateFeeds(updatedFeeds)

                                                feedViewModel.toggleStore(
                                                    context = context,
                                                    diaryId = diaryId,
                                                    onSuccess = { isStored ->
                                                        val apiState = feedViewModel.state.value
                                                        if (apiState is FeedUiState.Success) {
                                                            val finalFeeds = apiState.feeds.map { feed ->
                                                                if (feed.diaryId == diaryId) {
                                                                    feed.copy(isStored = isStored)
                                                                } else {
                                                                    feed
                                                                }
                                                            }
                                                            feedViewModel.updateFeeds(finalFeeds)
                                                        }
                                                    },
                                                    onFailure = {
                                                        val apiState = feedViewModel.state.value
                                                        if (apiState is FeedUiState.Success) {
                                                            val revertedFeeds = apiState.feeds.map { feed ->
                                                                if (feed.diaryId == diaryId) {
                                                                    feed.copy(isStored = currentIsStored)
                                                                } else {
                                                                    feed
                                                                }
                                                            }
                                                            feedViewModel.updateFeeds(revertedFeeds)
                                                        }
                                                    }
                                                )
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                    
                    val currentFeed = state.feeds.getOrNull(currentItemIndex.value)
                    if (currentFeed != null) {
                        val diary = currentFeed.toDiary
                        val videoTotalDuration = diary.totalDuration ?: 180
                        val startTime = diary.start.toFloatOrNull()?.toInt() ?: 0
                        val durationTime = diary.duration.toFloatOrNull()?.toInt() ?: 0

                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 16.dp)
                                .zIndex(1f)
                        ) {
                            MusicTimeBar(
                                title = diary.musicTitle,
                                start = startTime,
                                during = durationTime,
                                total = videoTotalDuration
                            )
                        }
                    }

                    if (likesDiaryId != null) {
                        Box(modifier = Modifier.fillMaxSize().zIndex(10f)) {  // zIndex 추가
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
                                    // 모달 상태 정리 후, 좋아요 목록에서 사용자 프로필로 이동
                                    likesDiaryId = null
                                    likesUsers = emptyList()
                                    likesError = null
                                    val encodedUsername = java.net.URLEncoder.encode(user.username, "UTF-8")
                                    val encodedTag = java.net.URLEncoder.encode(user.tag, "UTF-8")
                                    val encodedProfileImageUrl = java.net.URLEncoder.encode(user.profileImageUrl, "UTF-8")
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
        }
        is FeedUiState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.message,
                    color = Color.White,
                    fontFamily = PaperlogyFontFamily,
                    fontSize = 14.sp
                )
            }
        }
    }
}
