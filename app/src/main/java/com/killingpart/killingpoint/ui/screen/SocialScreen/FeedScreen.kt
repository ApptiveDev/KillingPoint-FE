package com.killingpart.killingpoint.ui.screen.SocialScreen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.killingpart.killingpoint.ui.screen.MainScreen.MusicTimeBar
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import com.killingpart.killingpoint.ui.theme.mainGreen
import com.killingpart.killingpoint.ui.viewmodel.FeedUiState
import com.killingpart.killingpoint.ui.viewmodel.FeedViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FeedScreen(navController: NavController) {
    val context = LocalContext.current
    val feedViewModel: FeedViewModel = viewModel()
    val feedState by feedViewModel.state.collectAsState()
    val listState = rememberLazyListState()
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        feedViewModel.loadFeeds(context)
    }

    val currentItemIndex = remember {
        derivedStateOf {
            val firstVisible = listState.firstVisibleItemIndex
            val scrollOffset = listState.firstVisibleItemScrollOffset
            val itemHeightPx = with(density) { screenHeight.toPx() }
            if (scrollOffset > itemHeightPx * 0.5f) {
                firstVisible + 1
            } else {
                firstVisible
            }
        }
    }

    var lastSnappedIndex by remember { mutableStateOf(0) }

    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
        val firstVisible = listState.firstVisibleItemIndex
        val scrollOffset = listState.firstVisibleItemScrollOffset
        val itemHeightPx = with(density) { screenHeight.toPx() }
        val feeds = (feedState as? FeedUiState.Success)?.feeds ?: return@LaunchedEffect
        val maxIndex = feeds.size - 1
        
        val targetIndex = if (scrollOffset > itemHeightPx * 0.5f) {
            (firstVisible + 1).coerceAtMost(maxIndex)
        } else {
            firstVisible
        }
        
        // 스크롤 방향과 관계없이 항상 고정 위치로 스크롤
        if (targetIndex != lastSnappedIndex) {
            lastSnappedIndex = targetIndex
            kotlinx.coroutines.delay(150) // 스크롤이 진행 중일 때도 고정 위치로 이동하기 위한 딜레이
            if (targetIndex == lastSnappedIndex) { // 딜레이 중에 targetIndex가 변경되지 않았는지 확인
                listState.animateScrollToItem(
                    index = targetIndex,
                    scrollOffset = 0
                )
            }
        }
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
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize()
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
                                    .fillMaxWidth()
                                    .height(screenHeight)
                            ) {
                                if (isCurrentItem) {
                                    FeedRunMusicBox(
                                        feedDiary = feedDiary,
                                        navController = navController,
                                        onVideoEnd = {
                                            val feeds = (feedState as? FeedUiState.Success)?.feeds ?: return@FeedRunMusicBox
                                            val currentIndex = currentItemIndex.value
                                            if (currentIndex < feeds.size - 1) {
                                                scope.launch {
                                                    listState.animateScrollToItem(
                                                        index = currentIndex + 1,
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
                                        }
                                    )
                                }
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


