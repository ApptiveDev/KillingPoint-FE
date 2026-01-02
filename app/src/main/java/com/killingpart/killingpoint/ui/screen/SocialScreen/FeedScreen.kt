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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.killingpart.killingpoint.ui.screen.MainScreen.RunMusicBox
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import com.killingpart.killingpoint.ui.theme.mainGreen
import com.killingpart.killingpoint.ui.viewmodel.FeedUiState
import com.killingpart.killingpoint.ui.viewmodel.FeedViewModel
import kotlinx.coroutines.delay

@Composable
fun FeedScreen(navController: NavController) {
    val context = LocalContext.current
    val feedViewModel: FeedViewModel = viewModel()
    val feedState by feedViewModel.state.collectAsState()
    val listState = rememberLazyListState()
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val density = LocalDensity.current

    LaunchedEffect(Unit) {
        feedViewModel.loadFeeds(context)
    }

    val currentItemIndex = remember {
        derivedStateOf {
            val firstVisible = listState.firstVisibleItemIndex
            val scrollOffset = listState.firstVisibleItemScrollOffset
            val itemHeightPx = with(density) { screenHeight.toPx() }
            if (scrollOffset > itemHeightPx * 0.3f) {
                firstVisible + 1
            } else {
                firstVisible
            }
        }
    }

    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            delay(150)
            if (!listState.isScrollInProgress) {
                val firstVisible = listState.firstVisibleItemIndex
                val scrollOffset = listState.firstVisibleItemScrollOffset
                val itemHeightPx = with(density) { screenHeight.toPx() }
                val targetIndex = if (scrollOffset > itemHeightPx * 0.3f) {
                    (firstVisible + 1).coerceAtMost((feedState as? FeedUiState.Success)?.feeds?.size?.minus(1) ?: 0)
                } else {
                    firstVisible
                }
                if (targetIndex != firstVisible) {
                    listState.animateScrollToItem(targetIndex)
                }
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
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(
                        items = state.feeds,
                        key = { _, feed -> feed.diaryId ?: feed.hashCode() }
                    ) { index, feedDiary ->
                        val isCurrentItem = index == currentItemIndex.value
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(screenHeight)
                        ) {
                            if (isCurrentItem) {
                                RunMusicBox(
                                    currentIndex = index,
                                    currentDiary = feedDiary.toDiary,
                                    isPlaying = false,
                                    authorUsername = feedDiary.username,
                                    authorTag = feedDiary.tag,
                                    authorProfileImageUrl = feedDiary.profileImageUrl
                                )
                            }
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


