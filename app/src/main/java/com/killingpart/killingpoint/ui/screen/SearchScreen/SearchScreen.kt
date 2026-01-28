package com.killingpart.killingpoint.ui.screen.SearchScreen

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
import com.killingpart.killingpoint.ui.component.BottomBar
import com.killingpart.killingpoint.ui.screen.MainScreen.MusicTimeBar
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import com.killingpart.killingpoint.ui.theme.mainGreen
import com.killingpart.killingpoint.ui.viewmodel.SearchUiState
import com.killingpart.killingpoint.ui.viewmodel.SearchViewModel
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

    var lastSnappedIndex by remember { mutableStateOf(0) }

    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
        val firstVisible = listState.firstVisibleItemIndex
        val scrollOffset = listState.firstVisibleItemScrollOffset
        val layoutInfo = listState.layoutInfo
        val firstVisibleItem = layoutInfo.visibleItemsInfo.firstOrNull { it.index == firstVisible }
        val itemWidthPx = firstVisibleItem?.size?.toFloat() ?: with(density) { screenWidth.toPx() }
        val diaries = (searchState as? SearchUiState.Success)?.diaries ?: return@LaunchedEffect
        val maxIndex = diaries.size - 1
        
        val targetIndex = if (scrollOffset > itemWidthPx * 0.5f) {
            (firstVisible + 1).coerceAtMost(maxIndex)
        } else {
            firstVisible.coerceAtLeast(0)
        }
        
        if (targetIndex != lastSnappedIndex && targetIndex != firstVisible) {
            lastSnappedIndex = targetIndex
            scope.launch {
                listState.animateScrollToItem(
                    index = targetIndex,
                    scrollOffset = 0
                )
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
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
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    ) {
                        LazyRow(
                            state = listState,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            itemsIndexed(
                                items = state.diaries,
                                key = { _, diary -> diary.diaryId }
                            ) { index, feedDiary ->
                                val isCurrentItem = index == currentItemIndex.value
                                
                                LaunchedEffect(index, isCurrentItem) {
                                    android.util.Log.d("SearchScreen", "Item[$index]: isCurrentItem=$isCurrentItem, diaryId=${feedDiary.diaryId}, videoUrl=${feedDiary.videoUrl}")
                                }
                                
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .widthIn(min = screenWidth)
                                ) {
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
                                                    // 끝에 도달하면 처음으로 돌아가기
                                                    listState.animateScrollToItem(
                                                        index = 0,
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
                                        }
                                    )
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