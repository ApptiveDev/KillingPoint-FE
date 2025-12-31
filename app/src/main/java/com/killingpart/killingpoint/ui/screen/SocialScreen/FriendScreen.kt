package com.killingpart.killingpoint.ui.screen.SocialScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import com.killingpart.killingpoint.ui.theme.mainGreen
import com.killingpart.killingpoint.ui.viewmodel.FriendViewModel
import com.killingpart.killingpoint.ui.viewmodel.FriendUiState
import com.killingpart.killingpoint.ui.viewmodel.UserViewModel
import com.killingpart.killingpoint.ui.viewmodel.UserUiState

enum class FriendTab {
    PICKS, FANS
}

@Composable
fun FriendScreen(navController: NavController) {
    val context = LocalContext.current
    var searchText by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(FriendTab.PICKS) }
    
    val userViewModel: UserViewModel = viewModel()
    val userState by userViewModel.state.collectAsState()
    val friendViewModel: FriendViewModel = viewModel()
    val friendState by friendViewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        userViewModel.loadUserInfo(context)
    }

    // JWT 토큰에서 userId 추출
    LaunchedEffect(userState) {
        when (userState) {
            is UserUiState.Success -> {
                val repo = com.killingpart.killingpoint.data.repository.AuthRepository(context)
                val userId = repo.getUserIdFromToken()
                if (userId != null) {
                    friendViewModel.loadFriends(context, userId)
                } else {
                    // 토큰에서 userId를 추출할 수 없으면 에러 상태로 설정
                    android.util.Log.e("FriendScreen", "userId를 토큰에서 추출할 수 없습니다")
                }
            }
            else -> {}
        }
    }

    // 검색 기능
    LaunchedEffect(searchText) {
        if (searchText.isBlank()) {
            friendViewModel.clearSearch()
        } else {
            // 검색어가 있으면 검색 실행
            friendViewModel.searchUsers(context, searchText)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 30.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 검색 바
        TextField(
            value = searchText,
            onValueChange = { searchText = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp),
            textStyle = TextStyle(
                fontFamily = PaperlogyFontFamily,
                fontWeight = FontWeight.Light,
                fontSize = 12.sp,
                color = Color.White,
                lineHeight = 12.sp
            ),
            placeholder = {
                Text(
                    text = "친구 검색",
                    fontFamily = PaperlogyFontFamily,
                    fontWeight = FontWeight.Light,
                    fontSize = 12.sp,
                    color = Color(0xFF7B7B7B)
                )
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF101010),
                unfocusedContainerColor = Color(0xFF101010),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(18.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    if (searchText.isNotBlank()) {
                        friendViewModel.searchUsers(context, searchText)
                    }
                }
            ),
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Icon",
                    tint = Color.White,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(2.dp)
                        .clickable {
                            if (searchText.isNotBlank()) {
                                friendViewModel.searchUsers(context, searchText)
                            }
                        }
                )
            },
        )

        // 카테고리 라벨 (탭으로 변경)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "나의 픽",
                fontFamily = PaperlogyFontFamily,
                fontWeight = if (selectedTab == FriendTab.PICKS) FontWeight.Medium else FontWeight.Light,
                fontSize = 12.sp,
                color = if (selectedTab == FriendTab.PICKS) Color.White else Color(0xFFA4A4A6),
                modifier = Modifier.clickable { selectedTab = FriendTab.PICKS }
            )
            Text(
                text = "${friendState.let {
                        if (it is FriendUiState.Success) it.picks?.page?.totalElements ?: 0 else 0
                    }}",
                fontFamily = PaperlogyFontFamily,
                fontWeight = if (selectedTab == FriendTab.PICKS) FontWeight.Medium else FontWeight.Light,
                fontSize = 12.sp,
                color = if (selectedTab == FriendTab.PICKS) Color.White else Color(0xFFA4A4A6),
                modifier = Modifier.clickable { selectedTab = FriendTab.PICKS }
            )
            Text(
                text = "나의 팬덤 ${friendState.let { 
                    if (it is FriendUiState.Success) it.fans?.page?.totalElements ?: 0 else 0 
                }}",
                fontFamily = PaperlogyFontFamily,
                fontWeight = if (selectedTab == FriendTab.FANS) FontWeight.Medium else FontWeight.Light,
                fontSize = 12.sp,
                color = if (selectedTab == FriendTab.FANS) Color.White else Color(0xFFA4A4A6),
                modifier = Modifier.clickable { selectedTab = FriendTab.FANS }
            )
        }

        // 친구 목록
        when (val state = friendState) {
            is FriendUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
            is FriendUiState.Success -> {
                // 검색 결과가 있으면 검색 결과를 표시, 없으면 탭 내용 표시
                val friends = if (state.searchResults != null && searchText.isNotBlank()) {
                    state.searchResults.content
                } else {
                    when (selectedTab) {
                        FriendTab.PICKS -> state.picks?.content ?: emptyList()
                        FriendTab.FANS -> state.fans?.content ?: emptyList()
                    }
                }
                
                if (friends.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when {
                                state.searchResults != null && searchText.isNotBlank() -> "검색 결과가 없습니다"
                                selectedTab == FriendTab.PICKS -> "구독한 친구가 없습니다"
                                else -> "팬덤이 없습니다"
                            },
                            fontFamily = PaperlogyFontFamily,
                            fontWeight = FontWeight.Light,
                            fontSize = 14.sp,
                            color = Color(0xFF7B7B7B)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(friends) { user ->
                            // 검색 결과인 경우와 일반 목록인 경우를 구분
                            val isSearchResult = state.searchResults != null && searchText.isNotBlank()
                            FriendItemCard(
                                user = user,
                                navController = navController,
                                isPickTab = if (isSearchResult) {
                                    // 검색 결과에서는 user.isMyPick만 확인
                                    user.isMyPick
                                } else {
                                    // 일반 목록에서는 탭이 PICKS이거나 이미 나의 픽인 경우
                                    selectedTab == FriendTab.PICKS || user.isMyPick
                                },
                                onSubscribeClick = {
                                    // TODO: userId를 얻는 방법 필요
                                    // friendViewModel.addSubscribe(context, user.userId, currentUserId)
                                }
                            )
                        }
                    }
                }
            }
            is FriendUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
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

@Composable
fun FriendItemCard(
    user: com.killingpart.killingpoint.data.model.SubscribeUser,
    navController: NavController,
    isPickTab: Boolean = false,
    onSubscribeClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFF090909),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 프로필 이미지
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF404040))
            ) {
                AsyncImage(
                    model = user.profileImageUrl,
                    contentDescription = "프로필 이미지",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = user.username,
                        fontFamily = PaperlogyFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                    // 나의 픽 표시 (이미 구독한 경우에만)
                    if (user.isMyPick || isPickTab) {
                        Text(
                            text = "나의 픽",
                            fontFamily = PaperlogyFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = mainGreen
                        )
                    }
                }
                Text(
                    text = "@${user.tag}",
                    fontFamily = PaperlogyFontFamily,
                    fontWeight = FontWeight.Light,
                    fontSize = 12.sp,
                    color = Color(0xFFFFFFFF)
                )
            }
        }

        // 버튼 영역
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 구독 버튼 (나의 픽이 아닐 때만 표시)
//            if (!user.isMyPick && !isPickTab && onSubscribeClick != null) {
//                Text(
//                    text = "픽 하기",
//                    fontFamily = PaperlogyFontFamily,
//                    fontWeight = FontWeight.Medium,
//                    fontSize = 12.sp,
//                    color = mainGreen,
//                    modifier = Modifier
//                        .clickable { onSubscribeClick() }
//                        .padding(horizontal = 8.dp, vertical = 4.dp)
//                )
//            }
            
            // 프로필 방문 버튼
            Box(
                modifier = Modifier
                    .background(
                        color = Color(0xFF090909),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable {
                        val encodedUsername = java.net.URLEncoder.encode(user.username, "UTF-8")
                        val encodedTag = java.net.URLEncoder.encode(user.tag, "UTF-8")
                        val encodedProfileImageUrl = java.net.URLEncoder.encode(user.profileImageUrl, "UTF-8")
                        navController.navigate(
                            "friend_profile" +
                                    "?userId=${user.userId}" +
                                    "&username=$encodedUsername" +
                                    "&tag=$encodedTag" +
                                    "&profileImageUrl=$encodedProfileImageUrl" +
                                    "&isMyPick=${user.isMyPick}"
                        )
                    }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "프로필 방문",
                    fontFamily = PaperlogyFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = mainGreen
                )
            }
        }
    }
}
