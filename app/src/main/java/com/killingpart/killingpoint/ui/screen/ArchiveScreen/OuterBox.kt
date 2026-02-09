package com.killingpart.killingpoint.ui.screen.ArchiveScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Surface
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.killingpart.killingpoint.R
import com.killingpart.killingpoint.data.model.Diary
import com.killingpart.killingpoint.data.model.FeedDiary
import com.killingpart.killingpoint.ui.screen.ArchiveScreen.DiaryCard
import android.net.Uri
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.text.style.TextAlign
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import com.killingpart.killingpoint.ui.theme.mainGreen
import com.killingpart.killingpoint.ui.viewmodel.UserUiState
import com.killingpart.killingpoint.ui.viewmodel.UserViewModel
import com.killingpart.killingpoint.data.repository.AuthRepository

@Composable
fun OuterBox(
    diaries: List<Diary>,
    modifier: Modifier = Modifier,
    onProfileClick: () -> Unit = {},
    navController: androidx.navigation.NavController? = null
) {
    val context = LocalContext.current
    val userViewModel: UserViewModel = viewModel()
    val userState by userViewModel.state.collectAsState()
    
    // 통계 상태 관리
    var userStatistics by remember { mutableStateOf<com.killingpart.killingpoint.data.model.UserStatistics?>(null) }
    var isLoadingStatistics by remember { mutableStateOf(false) }

    // 탭: 0 = 내 킬링파트, 1 = 보관한 킬링파트
    var selectedTabIndex by remember { mutableStateOf(0) }
    var storedDiaries by remember { mutableStateOf<List<FeedDiary>>(emptyList()) }
    var totalStoredPages by remember { mutableStateOf(0) }
    var currentStoredPage by remember { mutableStateOf(-1) }
    var isLoadingStored by remember { mutableStateOf(false) }
    var isLoadingMoreStored by remember { mutableStateOf(false) }
    val gridListState = rememberLazyListState()

    LaunchedEffect(Unit) {
        userViewModel.loadUserInfo(context)
        val repo = AuthRepository(context)
        val userId = repo.getUserIdFromToken()
        if (userId != null) {
            isLoadingStatistics = true
            repo.getUserStatistics(userId)
                .onSuccess { statistics ->
                    userStatistics = statistics
                    isLoadingStatistics = false
                }
                .onFailure { e ->
                    android.util.Log.e("OuterBox", "통계 조회 실패: ${e.message}")
                    isLoadingStatistics = false
                }
        }
        isLoadingStored = true
        repo.getStoredDiariesPage(page = 0, size = 20)
            .onSuccess { response ->
                storedDiaries = response.content
                totalStoredPages = response.page.totalPages
                currentStoredPage = 0
            }
            .onFailure { e ->
                android.util.Log.e("OuterBox", "보관 일기 조회 실패: ${e.message}")
            }
        isLoadingStored = false
    }

    val chunkedRowsForLoadMore = if (selectedTabIndex == 0) 0 else storedDiaries.chunked(2).size
    LaunchedEffect(
        selectedTabIndex,
        gridListState.firstVisibleItemIndex,
        gridListState.layoutInfo.visibleItemsInfo.size,
        chunkedRowsForLoadMore,
        currentStoredPage,
        totalStoredPages
    ) {
        if (selectedTabIndex != 1 || currentStoredPage < 0) return@LaunchedEffect
        if (currentStoredPage + 1 >= totalStoredPages) return@LaunchedEffect
        if (isLoadingMoreStored) return@LaunchedEffect
        val layoutInfo = gridListState.layoutInfo
        val totalItems = layoutInfo.totalItemsCount
        if (totalItems == 0) return@LaunchedEffect
        val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
        if (lastVisible < totalItems - 2) return@LaunchedEffect
        val repo = AuthRepository(context)
        isLoadingMoreStored = true
        val nextPage = currentStoredPage + 1
        repo.getStoredDiariesPage(page = nextPage, size = 20)
            .onSuccess { response ->
                storedDiaries = storedDiaries + response.content
                currentStoredPage = nextPage
            }
            .onFailure { e ->
                android.util.Log.e("OuterBox", "보관 일기 추가 로드 실패: ${e.message}")
            }
        isLoadingMoreStored = false
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight() // 가능한 최대 높이 사용
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight() // Column도 최대 높이 사용
        ) {
                    // 프로필 영역
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
                            when (val s = userState) {
                                is UserUiState.Success -> {
                                    AsyncImage(
                                        model = s.userInfo.profileImageUrl,
                                        contentDescription = "프로필 사진",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(60.dp)
                                            .clip(RoundedCornerShape(50))
                                            .border(3.dp, mainGreen, RoundedCornerShape(50)),
                                        placeholder = painterResource(id = R.drawable.default_profile),
                                        error = painterResource(id = R.drawable.default_profile)
                                    )
                                }

                                else -> {
                                    Image(
                                        painter = painterResource(id = R.drawable.default_profile),
                                        contentDescription = "프로필 사진",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(60.dp)
                                            .clip(RoundedCornerShape(50))
                                            .border(3.dp, mainGreen, RoundedCornerShape(50))
                                    )
                                }
                            }

                            // username과 tag (클릭 가능)
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onProfileClick() }
                            ) {
                                Text(
                                    text = when (val s = userState) {
                                        is UserUiState.Success -> s.userInfo.username
                                        is UserUiState.Loading -> "LOADING..."
                                        is UserUiState.Error -> "KILLING_PART"
                                    },
                                    fontFamily = PaperlogyFontFamily,
                                    fontWeight = FontWeight.W400,
                                    fontSize = 14.sp,
                                    color = mainGreen,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                Spacer(modifier=Modifier.height(3.dp))
                                Text(
                                    text = when (val s = userState) {
                                        is UserUiState.Success -> "@${s.userInfo.tag}"
                                        is UserUiState.Loading -> "@LOADING"
                                        is UserUiState.Error -> "@KILLING_PART"
                                    },
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
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 킬링파트
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${userStatistics?.killingPartCount ?: diaries.size}",
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
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${userStatistics?.fanCount ?: 0}",
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
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${userStatistics?.pickCount ?: 0}",
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = { 
                                android.util.Log.d("OuterBox", "프로필 편집 버튼 클릭")
                                onProfileClick()
                                android.util.Log.d("OuterBox", "onProfileClick 호출 완료")
                            },
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(32.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF262626)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "프로필 편집",
                                tint = mainGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "프로필 편집",
                                color = mainGreen,
                                fontFamily = PaperlogyFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.W400
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        listOf("내 킬링파트", "보관한 킬링파트").forEachIndexed { index, label ->
                            val selected = selectedTabIndex == index
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .clickable { selectedTabIndex = index },
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = label,
                                    fontFamily = PaperlogyFontFamily,
                                    fontWeight = FontWeight.W400,
                                    fontSize = 12.sp,
                                    color = if (selected) Color(0xFFE7E7E7) else Color(0xFF5F5C5C),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(
                                            color = if (selected) Color(0xFFE7E7E7) else Color(0xFF5F5C5C),
                                        )
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // 다이어리 그리드 (2x2)
                    val configuration = LocalConfiguration.current
                    val screenWidth = configuration.screenWidthDp.dp
                    val horizontalContainerPadding = 20.dp
                    val interColumnSpacing = 12.dp
                    val rowSpacing = 20.dp
                    val itemSize =
                        (screenWidth - horizontalContainerPadding * 2 - interColumnSpacing) / 2

                    // (Diary, authorTag?, authorUsername?) - 보관 탭일 때만 작성자 정보 있음
                    val displayList: List<Triple<Diary, String?, String?>> = if (selectedTabIndex == 0) {
                        diaries.map { Triple(it, null, null) }
                    } else {
                        storedDiaries.map { Triple(it.toDiary, it.tag, it.username) }
                    }
                    val chunkedDiaries = displayList.chunked(2)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f) // 남은 공간을 모두 차지
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

                        if (selectedTabIndex == 1 && isLoadingStored) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = mainGreen)
                            }
                        } else {
                        LazyColumn(
                            state = gridListState,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(chunkedDiaries.size) { index ->
                                val rowItems = chunkedDiaries[index]
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = rowSpacing),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    rowItems.forEach { (diary, authorTag, authorUsername) ->
                                        DiaryCard(
                                            diary = diary,
                                            authorTag = authorTag,
                                            showDate = selectedTabIndex == 0,
                                            modifier = Modifier.weight(1f),
                                            onClick = {
                                                navController?.let { nav ->
                                                    val diaryIdParam =
                                                        diary.id?.let { "&diaryId=$it" } ?: ""

                                                    val totalDurationParam =
                                                        diary.totalDuration?.let { "&totalDuration=$it" }
                                                            ?: ""

                                                    val scopeParam = "&scope=${diary.scope.name}"

                                                    val authorUsernameParam =
                                                        "&authorUsername=${Uri.encode(authorUsername.orEmpty())}"
                                                    val authorTagParam =
                                                        "&authorTag=${Uri.encode(authorTag.orEmpty())}"

                                                    nav.navigate(
                                                        "diary_detail" +
                                                                "?artist=${Uri.encode(diary.artist)}" +
                                                                "&musicTitle=${Uri.encode(diary.musicTitle)}" +
                                                                "&albumImageUrl=${Uri.encode(diary.albumImageUrl)}" +
                                                                "&content=${Uri.encode(diary.content)}" +
                                                                "&videoUrl=${Uri.encode(diary.videoUrl)}" +
                                                                "&duration=${Uri.encode(diary.duration)}" +
                                                                "&start=${Uri.encode(diary.start)}" +
                                                                "&end=${Uri.encode(diary.end)}" +
                                                                "&createDate=${Uri.encode(diary.createDate)}" +
                                                                scopeParam +
                                                                diaryIdParam +
                                                                totalDurationParam +
                                                                "&fromTab=profile" +
                                                                authorUsernameParam +
                                                                authorTagParam
                                                    )
                                                }
                                            }
                                        )
                                    }
                                    // 홀수 개일 경우 빈 공간 추가
                                    if (rowItems.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                        }
                    }
                }
            }
}

@Preview(showBackground = true)
@Composable
fun OuterBoxPreview() {
    Surface(
        color = Color(0xFF060606)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            val mockDiaries = listOf(
                Diary(
                    artist = "Michael Jackson",
                    musicTitle = "Xscape",
                    albumImageUrl = "https://i.scdn.co/image/ab67616d0000b27375cc718da9eb0b39bd9cbfb3",
                    content = "목데이터1",
                    videoUrl = "https://www.youtube-nocookie.com/embed/ki08IcGubwQ",
                    scope = com.killingpart.killingpoint.data.model.Scope.PUBLIC,
                    duration = "string",
                    start = "string",
                    end = "string",
                    createDate = "1999.12.12",
                    updateDate = "string"
                ),
                Diary(
                    artist = "The Notorious B.I.G.",
                    musicTitle = "Ready to Die",
                    albumImageUrl = "https://i.scdn.co/image/ab67616d0000b27375cc718da9eb0b39bd9cbfb3",
                    content = "목데이터2",
                    videoUrl = "https://www.youtube-nocookie.com/embed/ki08IcGubwQ",
                    scope = com.killingpart.killingpoint.data.model.Scope.PUBLIC,
                    duration = "string",
                    start = "string",
                    end = "string",
                    createDate = "1999.12.12",
                    updateDate = "string"
                ),
                Diary(
                    artist = "Artist 3",
                    musicTitle = "Title 3",
                    albumImageUrl = "https://i.scdn.co/image/ab67616d0000b27375cc718da9eb0b39bd9cbfb3",
                    content = "목데이터3",
                    videoUrl = "https://www.youtube-nocookie.com/embed/ki08IcGubwQ",
                    scope = com.killingpart.killingpoint.data.model.Scope.PUBLIC,
                    duration = "string",
                    start = "string",
                    end = "string",
                    createDate = "1999.12.12",
                    updateDate = "string"
                ),
                Diary(
                    artist = "Artist 4",
                    musicTitle = "Title 4",
                    albumImageUrl = "https://i.scdn.co/image/ab67616d0000b27375cc718da9eb0b39bd9cbfb3",
                    content = "목데이터4",
                    videoUrl = "https://www.youtube-nocookie.com/embed/ki08IcGubwQ",
                    scope = com.killingpart.killingpoint.data.model.Scope.PUBLIC,
                    duration = "string",
                    start = "string",
                    end = "string",
                    createDate = "1999.12.12",
                    updateDate = "string"
                )
            )
            OuterBox(diaries = mockDiaries)
        }
    }
}
