package com.killingpart.killingpoint.ui.screen.SearchScreen

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
import com.killingpart.killingpoint.data.model.FeedDiary
import com.killingpart.killingpoint.ui.component.AppBackground
import com.killingpart.killingpoint.ui.component.BottomBar
import com.killingpart.killingpoint.ui.screen.MainScreen.MusicTimeBar
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import com.killingpart.killingpoint.ui.theme.mainGreen
import com.killingpart.killingpoint.ui.viewmodel.SearchUiState
import com.killingpart.killingpoint.ui.viewmodel.SearchViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SearchScreen(navController: NavController) {
    val context = LocalContext.current
    val searchViewModel: SearchViewModel = viewModel()
    val searchState by searchViewModel.state.collectAsState()
    val listState = rememberLazyListState()
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        searchViewModel.loadRandomDiaries(context)
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

    AppBackground {
        Column(
            modifier = Modifier.fillMaxSize().padding(top = 40.dp)
        ) {
            when (val state = searchState) {
            is SearchUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = mainGreen)
                }
            }
            is SearchUiState.Success -> {
                if (state.diaries.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "무작위 일기가 없습니다",
                            color = mainGreen,
                            fontFamily = PaperlogyFontFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    // 마지막에 '다음' 슬롯 추가 -> 스와이프로 새 5개 불러오기 (비디오 끝 안 봐도 됨)
                    val itemsWithNext = state.diaries + listOf<FeedDiary?>(null)
                    var isLoadingNext by remember { mutableStateOf(false) }

                    LaunchedEffect(currentItemIndex.value, state.diaries.size) {
                        if (currentItemIndex.value == state.diaries.size && !isLoadingNext) {
                            isLoadingNext = true
                            searchViewModel.loadNextRandomDiaries(
                                context = context,
                                onSuccess = {
                                    scope.launch {
                                        delay(150)
                                        listState.scrollToItem(0)
                                        isLoadingNext = false
                                    }
                                },
                                onFailure = { isLoadingNext = false }
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    ) {
                        LazyRow(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            flingBehavior = snapFlingBehavior
                        ) {
                            itemsIndexed(
                                items = itemsWithNext,
                                key = { index, item -> if (item == null) "next_sentinel" else item.diaryId }
                            ) { index, feedDiary ->
                                val isCurrentItem = index == currentItemIndex.value
                                
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .width(screenWidth)
                                ) {
                                    if (feedDiary != null) {
                                        SearchRunMusicBox(
                                            feedDiary = feedDiary,
                                            navController = navController,
                                            isActive = isCurrentItem,
                                            onVideoEnd = {
                                                val diaries = (searchState as? SearchUiState.Success)?.diaries ?: return@SearchRunMusicBox
                                                val currentIndex = currentItemIndex.value
                                                scope.launch {
                                                    if (currentIndex < diaries.size - 1) {
                                                        listState.animateScrollToItem(
                                                            index = currentIndex + 1,
                                                            scrollOffset = 0
                                                        )
                                                    } else {
                                                        // 마지막이면 '다음' 슬롯으로 스와이프 (거기서 새 5개 로드됨)
                                                        listState.animateScrollToItem(
                                                            index = diaries.size,
                                                            scrollOffset = 0
                                                        )
                                                    }
                                                }
                                            },
                                        onLikeClick = {
                                            feedDiary.diaryId.let { diaryId ->
                                                val currentState = searchViewModel.state.value
                                                if (currentState is SearchUiState.Success) {
                                                    val currentDiary = currentState.diaries.find { it.diaryId == diaryId }
                                                    val currentIsLiked = currentDiary?.isLiked ?: false

                                                    val updatedDiaries = currentState.diaries.map { diary ->
                                                        if (diary.diaryId == diaryId) {
                                                            diary.copy(
                                                                isLiked = !currentIsLiked,
                                                                likeCount = if (!currentIsLiked) diary.likeCount + 1 else (diary.likeCount - 1).coerceAtLeast(0)
                                                            )
                                                        } else {
                                                            diary
                                                        }
                                                    }
                                                    searchViewModel.updateDiaries(updatedDiaries)

                                                    searchViewModel.toggleLike(
                                                        context = context,
                                                        diaryId = diaryId,
                                                        onSuccess = { isLiked ->
                                                            val apiState = searchViewModel.state.value
                                                            if (apiState is SearchUiState.Success) {
                                                                val finalDiaries = apiState.diaries.map { diary ->
                                                                    if (diary.diaryId == diaryId) {
                                                                        val expectedCount = if (isLiked) {
                                                                            (currentDiary?.likeCount ?: 0) + 1
                                                                        } else {
                                                                            ((currentDiary?.likeCount ?: 0) - 1).coerceAtLeast(0)
                                                                        }
                                                                        diary.copy(
                                                                            isLiked = isLiked,
                                                                            likeCount = expectedCount
                                                                        )
                                                                    } else {
                                                                        diary
                                                                    }
                                                                }
                                                                searchViewModel.updateDiaries(finalDiaries)
                                                            }
                                                        },
                                                        onFailure = {
                                                            val apiState = searchViewModel.state.value
                                                            if (apiState is SearchUiState.Success) {
                                                                val revertedDiaries = apiState.diaries.map { diary ->
                                                                    if (diary.diaryId == diaryId) {
                                                                        diary.copy(
                                                                            isLiked = currentIsLiked,
                                                                            likeCount = currentDiary?.likeCount ?: 0
                                                                        )
                                                                    } else {
                                                                        diary
                                                                    }
                                                                }
                                                                searchViewModel.updateDiaries(revertedDiaries)
                                                            }
                                                        }
                                                    )
                                                }
                                            }
                                        },
                                        onStoreClick = {
                                            feedDiary.diaryId.let { diaryId ->
                                                val currentState = searchViewModel.state.value
                                                if (currentState is SearchUiState.Success) {
                                                    val currentDiary = currentState.diaries.find { it.diaryId == diaryId }
                                                    val currentIsStored = currentDiary?.isStored ?: false

                                                    val updatedDiaries = currentState.diaries.map { diary ->
                                                        if (diary.diaryId == diaryId) {
                                                            diary.copy(isStored = !currentIsStored)
                                                        } else {
                                                            diary
                                                        }
                                                    }
                                                    searchViewModel.updateDiaries(updatedDiaries)

                                                    searchViewModel.toggleStore(
                                                        context = context,
                                                        diaryId = diaryId,
                                                        onSuccess = { isStored ->
                                                            val apiState = searchViewModel.state.value
                                                            if (apiState is SearchUiState.Success) {
                                                                val finalDiaries = apiState.diaries.map { diary ->
                                                                    if (diary.diaryId == diaryId) {
                                                                        diary.copy(isStored = isStored)
                                                                    } else {
                                                                        diary
                                                                    }
                                                                }
                                                                searchViewModel.updateDiaries(finalDiaries)
                                                            }
                                                        },
                                                        onFailure = {
                                                            val apiState = searchViewModel.state.value
                                                            if (apiState is SearchUiState.Success) {
                                                                val revertedDiaries = apiState.diaries.map { diary ->
                                                                    if (diary.diaryId == diaryId) {
                                                                        diary.copy(isStored = currentIsStored)
                                                                    } else {
                                                                        diary
                                                                    }
                                                                }
                                                                searchViewModel.updateDiaries(revertedDiaries)
                                                            }
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    )
                                    } else {
                                        // '다음' 슬롯: 스와이프하면 새 5개 불러옴 (로딩 중이면 인디케이터)
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isLoadingNext) {
                                                CircularProgressIndicator(color = mainGreen)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        val currentDiary = state.diaries.getOrNull(currentItemIndex.value)
                        if (currentDiary != null) {
                            val diary = currentDiary.toDiary
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
            is SearchUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
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
            
            BottomBar(navController)
        }
    }
}