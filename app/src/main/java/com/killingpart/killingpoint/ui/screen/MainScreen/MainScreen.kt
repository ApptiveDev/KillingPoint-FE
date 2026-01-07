package com.killingpart.killingpoint.ui.screen.MainScreen

import android.view.RoundedCorner
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.killingpart.killingpoint.R
import com.killingpart.killingpoint.ui.component.AppBackground
import com.killingpart.killingpoint.ui.component.BottomBar
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import com.killingpart.killingpoint.ui.theme.UnboundedFontFamily
import com.killingpart.killingpoint.ui.theme.mainGreen
import com.killingpart.killingpoint.ui.viewmodel.DiaryUiState
import com.killingpart.killingpoint.ui.viewmodel.DiaryViewModel
import com.killingpart.killingpoint.ui.viewmodel.UserUiState
import com.killingpart.killingpoint.ui.viewmodel.UserViewModel
import com.killingpart.killingpoint.ui.screen.ArchiveScreen.DiaryCard
import com.killingpart.killingpoint.ui.screen.ArchiveScreen.OuterBox
import com.killingpart.killingpoint.ui.screen.MusicCalendarScreen.MusicCalendarScreen
import com.killingpart.killingpoint.ui.screen.ProfileScreen.ProfileSettingsScreen
import kotlinx.coroutines.launch

enum class MainTab {
    PROFILE, PLAY, CALENDAR
}
@Composable
fun MainScreen(navController: NavController, initialTab: String = "play", initialSelectedDate: String = "") {
    var selected by remember(initialTab) { 
        mutableStateOf(
            when (initialTab) {
                "profile" -> MainTab.PROFILE
                "calendar" -> MainTab.CALENDAR
                else -> MainTab.PLAY
            }
        )
    }
    var currentIndex by remember { mutableStateOf(0) }
    val mainListState = rememberLazyListState()

    var isPlaying by remember { mutableStateOf(true) } // 기본값은 재생 중
    
    // DiaryViewModel을 MainScreen에서 관리
    val diaryViewModel: DiaryViewModel = viewModel()
    val diaryState by diaryViewModel.state.collectAsState()
    val diaries = (diaryState as? DiaryUiState.Success)?.diaries ?: emptyList()
    
    // UserViewModel을 MainScreen에서 관리
    val userViewModel: UserViewModel = viewModel()
    val userState by userViewModel.state.collectAsState()

    val MusicCueBtnHeight = 60.dp
    val BottomBarHeight = 94.dp
    val MusicCueBtnGap = 12.dp

    var listExpanded by remember { mutableStateOf(false ) }

    val density = LocalDensity.current
    val innerTopPadding = 20.dp
    val pullUpOffsetPx = with(density) {innerTopPadding.roundToPx()}
    val listIndex = 1
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        diaryViewModel.loadDiaries(context)
        userViewModel.loadUserInfo(context)
        
        // WebView 캐시 초기화
        try {
            val webView = android.webkit.WebView(context)
            webView.clearCache(true)
            webView.clearHistory()
            webView.destroy()
        } catch (e: Exception) {
        }
    }

    LaunchedEffect(listExpanded) {
        kotlinx.coroutines.delay(250)
        if (listExpanded) {
            mainListState.animateScrollToItem(1)
        } else {
            mainListState.animateScrollToItem(0)
        }
    }


    AppBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            // 프로필 설정 화면 상태 관리 (전역)
            var showProfileSettings by remember { mutableStateOf(false) }
            // TopPillTabs 위치 측정을 위한 상태
            var topPillTabsBottomY by remember { mutableStateOf(0.dp) }
            val density = LocalDensity.current
            val configuration = LocalConfiguration.current
            val screenHeight = configuration.screenHeightDp.dp
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.height(60.dp))
                Text(
                    text = "MY MUSIC SPACE",
                    color = Color.White,
                    fontFamily = UnboundedFontFamily,
                    fontWeight = FontWeight.Black,
                    fontSize = 28.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "나만의 뮤직 스페이스",
                    color = Color(0xFFA4A4A6),
                    fontFamily = PaperlogyFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(26.dp))

                TopPillTabs(
                    options = listOf("내 프로필", "킬링파트 재생", "뮤직캘린더"),
                    selectedIndex = when (selected) {
                        MainTab.PROFILE -> 0
                        MainTab.PLAY -> 1
                        MainTab.CALENDAR -> 2
                    },
                    onSelected = { idx ->
                        selected = when (idx) {
                            0 -> MainTab.PROFILE
                            1 -> MainTab.PLAY
                            else -> MainTab.CALENDAR
                        }
                        listExpanded = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 30.dp)
                        .onGloballyPositioned { coordinates ->
                            with(density) {
                                topPillTabsBottomY = coordinates.positionInParent().y.toDp() + coordinates.size.height.toDp()
                            }
                        }
                )

                Spacer(modifier = Modifier.height(15.dp))

                when (selected) {
                    MainTab.PROFILE -> {
                        when (val state = diaryState) {
                            is DiaryUiState.Loading -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = mainGreen)
                                }
                            }

                            is DiaryUiState.Success -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .padding(horizontal = 16.dp)
                                ) {
                                    val musicListHeight = if (listExpanded) 260.dp else 50.dp
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(bottom = musicListHeight)
                                    ) {
                                        item {
                                            OuterBox(
                                                navController = navController,
                                                diaries = state.diaries,
                                                onProfileClick = { showProfileSettings = true },
                                                modifier = Modifier.fillParentMaxHeight() // 가능한 최대 높이 사용
                                            )
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .fillMaxWidth()
                                    ) {
                                        MusicListBox(
                                            currentIndex = currentIndex,
                                            expanded = listExpanded,
                                            onToggle = { willOpen -> listExpanded = willOpen },
                                            diaries = state.diaries,
                                            showCurrentHeader = true
                                        )
                                    }
                                }
                            }

                            is DiaryUiState.Error -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = state.message,
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontFamily = PaperlogyFontFamily
                                    )
                                }
                            }
                        }
                    }

                    MainTab.PLAY -> {
                        LazyColumn(
                            state = mainListState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
//                            contentPadding = PaddingValues(bottom = BottomBarHeight + MusicCueBtnHeight + MusicCueBtnGap)
                            contentPadding = PaddingValues(bottom = BottomBarHeight)
                        ) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .background(color = Color.Black, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                                ) {
                                    RunMusicBox(
                                        currentIndex = currentIndex,
                                        currentDiary = diaries.getOrNull(currentIndex),
                                        isPlaying = isPlaying
                                    )
                                }
                            }

                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .offset(y = (-40).dp)
                                        .padding(horizontal = 16.dp)
                                        .background(color = Color.Black, RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                                ) {
                                    MusicListBox(
                                        currentIndex = currentIndex,
                                        expanded = listExpanded,
                                        onToggle = { willOpen ->
                                            listExpanded = willOpen
                                        },
                                        diaries = diaries
                                    )
                                }
                            }
                        }
                    }
                    MainTab.CALENDAR -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            MusicCalendarScreen(
                                diaries = diaries, 
                                navController = navController,
                                initialSelectedDate = if (initialTab == "calendar") initialSelectedDate else null
                            )
                        }
                    }
                }

                BottomBar(navController = navController)
            }

            // MusicCueBtn은 PLAY 탭에서만 표시
            if (selected == MainTab.PLAY) {
                MusicCueBtn(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = BottomBarHeight),
                    onPrevious = {
                        if (currentIndex > 0) {
                            currentIndex--
                            isPlaying = true
                            android.util.Log.d("MainScreen", "Previous clicked, new index: $currentIndex")
                        }
                    },
                    onNext = {
                        if (currentIndex < diaries.size - 1) {
                            currentIndex++
                            isPlaying = true
                        }
                    },
                    onPlayPause = {
                        isPlaying = !isPlaying
                    },
                    isPlaying = isPlaying

            )


            if (showProfileSettings) {
                val topOffset = topPillTabsBottomY + 15.dp
                val maxHeight = screenHeight - topOffset - BottomBarHeight
                ProfileSettingsScreen(
                    onDismiss = { showProfileSettings = false },
                    topOffset = topOffset,
                    maxHeight = maxHeight,
                    onLogout = {
                        // 로그아웃/회원탈퇴 후 로그인 화면으로 이동
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
}


@Preview
@Composable
fun MainPreivew() {
    MainScreen(navController = rememberNavController());
}