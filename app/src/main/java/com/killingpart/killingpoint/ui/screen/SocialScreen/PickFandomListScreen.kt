package com.killingpart.killingpoint.ui.screen.SocialScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.killingpart.killingpoint.data.model.SubscribePage
import com.killingpart.killingpoint.data.model.SubscribeResponse
import com.killingpart.killingpoint.data.model.SubscribeUser
import com.killingpart.killingpoint.data.repository.AuthRepository
import com.killingpart.killingpoint.ui.component.AppBackground
import com.killingpart.killingpoint.ui.component.BottomBar
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import com.killingpart.killingpoint.ui.theme.mainGreen

private enum class PickFandomTab { Picks, Fandom }

@Composable
private fun PickFandomListContent(
    selectedTab: PickFandomTab,
    isLoadingPicks: Boolean,
    isLoadingFans: Boolean,
    currentList: List<SubscribeUser>,
    filteredList: List<SubscribeUser>,
    navController: NavController,
    currentUserId: Long?
) {
    when {
        selectedTab == PickFandomTab.Picks && isLoadingPicks -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = mainGreen)
            }
        }
        selectedTab == PickFandomTab.Fandom && isLoadingFans -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = mainGreen)
            }
        }
        currentList.isEmpty() -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (selectedTab) {
                        PickFandomTab.Picks -> "픽 목록이 없습니다"
                        PickFandomTab.Fandom -> "팬덤이 없습니다"
                    },
                    fontFamily = PaperlogyFontFamily,
                    fontSize = 14.sp,
                    color = Color(0xFFA4A4A6)
                )
            }
        }
        filteredList.isEmpty() -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "검색 결과가 없습니다",
                    fontFamily = PaperlogyFontFamily,
                    fontSize = 14.sp,
                    color = Color(0xFFA4A4A6)
                )
            }
        }
        else -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredList) { user: SubscribeUser ->
                    FriendItemCard(
                        user = user,
                        navController = navController,
                        currentUserId = currentUserId,
                        isPickTab = selectedTab == PickFandomTab.Picks,
                        fromPickFandomList = true
                    )
                }
            }
        }
    }
}

@Composable
fun PickFandomListScreen(
    navController: NavController,
    userId: Long,
    tag: String,
    initialTab: String = "picks"
) {
    val context = LocalContext.current
    var selectedTab by remember(initialTab) {
        mutableStateOf(
            if (initialTab.equals("fandom", ignoreCase = true)) {
                PickFandomTab.Fandom
            } else {
                PickFandomTab.Picks
            }
        )
    }
    var picksResponse by remember { mutableStateOf<SubscribeResponse?>(null) }
    var fansResponse by remember { mutableStateOf<SubscribeResponse?>(null) }
    var isLoadingPicks by remember { mutableStateOf(false) }
    var isLoadingFans by remember { mutableStateOf(false) }
    var currentUserId by remember { mutableStateOf<Long?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    val searchFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        currentUserId = AuthRepository(context).getUserIdFromToken()
    }

    // 진입 시 픽/팬덤 둘 다 로드해서 picksTotal, fansTotal 모두 표시
    LaunchedEffect(userId) {
        val repo = AuthRepository(context)
        isLoadingPicks = true
        isLoadingFans = true
        repo.getSubscribes(userId, 50)
            .onSuccess { response: SubscribeResponse -> picksResponse = response }
            .onFailure { _: Throwable ->
                picksResponse = SubscribeResponse(emptyList(), SubscribePage(0, 0, 0, 0))
            }
        isLoadingPicks = false
        repo.getFans(userId, 50)
            .onSuccess { response: SubscribeResponse -> fansResponse = response }
            .onFailure { _: Throwable ->
                fansResponse = SubscribeResponse(emptyList(), SubscribePage(0, 0, 0, 0))
            }
        isLoadingFans = false
    }

    val currentList = when (selectedTab) {
        PickFandomTab.Picks -> picksResponse?.content ?: emptyList()
        PickFandomTab.Fandom -> fansResponse?.content ?: emptyList()
    }
    val picksTotal = picksResponse?.page?.totalElements ?: 0
    val fansTotal = fansResponse?.page?.totalElements ?: 0
    val filteredList = remember(currentList, searchQuery) {
        val q = searchQuery.trim()
        if (q.isEmpty()) currentList
        else currentList.filter { user: SubscribeUser ->
            user.username.contains(q, ignoreCase = true) || user.tag.contains(q, ignoreCase = true)
        }
    }

    AppBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 50.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 20.dp, start = 16.dp, end = 16.dp)
                                .background(Color(0xFF1A1A1A), shape = RoundedCornerShape(12.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 12.dp)
                            ) {
                                IconButton(
                                    onClick = { navController.popBackStack() },
                                    modifier = Modifier.align(Alignment.CenterStart)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ArrowBack,
                                        contentDescription = "뒤로가기",
                                        tint = Color.White
                                    )
                                }
                                Text(
                                    text =   "@$tag",
                                    fontFamily = PaperlogyFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = Color.White,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }

                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { value: String -> searchQuery = value },
                                modifier = Modifier
                                    .focusRequester(searchFocusRequester)
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 4.dp),
                                placeholder = {
                                    Text(
                                        text = "친구 검색",
                                        fontFamily = PaperlogyFontFamily,
                                        fontSize = 14.sp,
                                        color = Color(0xFFA4A4A6),
                                    )
                                },
                                trailingIcon = {
                                    IconButton(onClick = { searchFocusRequester.requestFocus() }) {
                                        Icon(
                                            imageVector = Icons.Filled.Search,
                                            contentDescription = "검색",
                                            tint = Color.White
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFF101010),
                                    unfocusedContainerColor = Color(0xFF101010),
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedBorderColor = Color.Transparent,
                                    cursorColor = Color.White,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                shape = RoundedCornerShape(50.dp)
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(24.dp)
                            ) {
                                Box(
                                    modifier = Modifier.clickable {
                                        selectedTab = PickFandomTab.Picks
                                    }
                                ) {
                                    val alpha = if (selectedTab == PickFandomTab.Picks) 1f else 0.5f
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "나의 픽",
                                            fontFamily = PaperlogyFontFamily,
                                            fontWeight = if (selectedTab == PickFandomTab.Picks) FontWeight.Medium else FontWeight.Light,
                                            fontSize = 14.sp,
                                            color = Color.White.copy(alpha = alpha)
                                        )
                                        Text(
                                            text = "$picksTotal",
                                            fontFamily = PaperlogyFontFamily,
                                            fontWeight = if (selectedTab == PickFandomTab.Picks) FontWeight.Medium else FontWeight.Light,
                                            fontSize = 14.sp,
                                            color = mainGreen.copy(alpha = alpha)
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedTab = PickFandomTab.Fandom }
                                ) {
                                    val alpha =
                                        if (selectedTab == PickFandomTab.Fandom) 1f else 0.5f
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "나의 팬덤",
                                            fontFamily = PaperlogyFontFamily,
                                            fontWeight = if (selectedTab == PickFandomTab.Fandom) FontWeight.Medium else FontWeight.Light,
                                            fontSize = 14.sp,
                                            color = Color.White.copy(alpha = alpha)
                                        )
                                        Text(
                                            text = "$fansTotal",
                                            fontFamily = PaperlogyFontFamily,
                                            fontWeight = if (selectedTab == PickFandomTab.Fandom) FontWeight.Medium else FontWeight.Light,
                                            fontSize = 14.sp,
                                            color = mainGreen.copy(alpha = alpha)
                                        )
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .heightIn(min = 0.dp)
                            ) {
                                PickFandomListContent(
                                    selectedTab = selectedTab,
                                    isLoadingPicks = isLoadingPicks,
                                    isLoadingFans = isLoadingFans,
                                    currentList = currentList,
                                    filteredList = filteredList,
                                    navController = navController,
                                    currentUserId = currentUserId
                                )
                            }
                        }
                    }
                }
                BottomBar(navController = navController)
            }
        }
    }
}
