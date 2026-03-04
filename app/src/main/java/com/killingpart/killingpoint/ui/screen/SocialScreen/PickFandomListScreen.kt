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
import com.killingpart.killingpoint.data.repository.AuthRepository
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import com.killingpart.killingpoint.ui.theme.mainGreen

private enum class PickFandomTab { Picks, Fandom }

@Composable
fun PickFandomListScreen(
    navController: NavController,
    userId: Long,
    tag: String
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(PickFandomTab.Picks) }
    var picksResponse by remember { mutableStateOf<SubscribeResponse?>(null) }
    var fansResponse by remember { mutableStateOf<SubscribeResponse?>(null) }
    var isLoadingPicks by remember { mutableStateOf(false) }
    var isLoadingFans by remember { mutableStateOf(false) }
    var currentUserId by remember { mutableStateOf<Long?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        currentUserId = AuthRepository(context).getUserIdFromToken()
    }

    LaunchedEffect(userId, selectedTab) {
        val repo = AuthRepository(context)
        when (selectedTab) {
            PickFandomTab.Picks -> {
                isLoadingPicks = true
                repo.getSubscribes(userId, 20, 0)
                    .onSuccess { picksResponse = it }
                    .onFailure { picksResponse = SubscribeResponse(emptyList(), SubscribePage(0, 0, 0, 0)) }
                isLoadingPicks = false
            }
            PickFandomTab.Fandom -> {
                isLoadingFans = true
                repo.getFans(userId, 20, 0)
                    .onSuccess { fansResponse = it }
                    .onFailure { fansResponse = SubscribeResponse(emptyList(), SubscribePage(0, 0, 0, 0)) }
                isLoadingFans = false
            }
        }
    }

    LaunchedEffect(userId) {
        val repo = AuthRepository(context)
        if (fansResponse == null) {
            repo.getFans(userId, 20, 0)
                .onSuccess { fansResponse = it }
                .onFailure { }
        }
    }

    val currentList = when (selectedTab) {
        PickFandomTab.Picks -> picksResponse?.content ?: emptyList()
        PickFandomTab.Fandom -> fansResponse?.content ?: emptyList()
    }
    val picksTotal = picksResponse?.page?.totalElements ?: 0
    val fansTotal = fansResponse?.page?.totalElements ?: 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF060606))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "뒤로가기",
                    tint = Color.White
                )
            }
            Text(
                text = "@$tag",
                fontFamily = PaperlogyFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp,
                color = mainGreen,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            placeholder = {
                Text(
                    text = "친구 검색",
                    fontFamily = PaperlogyFontFamily,
                    fontSize = 14.sp,
                    color = Color(0xFFA4A4A6)
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "검색",
                    tint = mainGreen
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF232427),
                unfocusedContainerColor = Color(0xFF232427),
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color.Transparent,
                cursorColor = Color.White,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(20.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Box(
                modifier = Modifier.clickable { selectedTab = PickFandomTab.Picks }
            ) {
                Text(
                    text = "픽 $picksTotal",
                    fontFamily = PaperlogyFontFamily,
                    fontWeight = if (selectedTab == PickFandomTab.Picks) FontWeight.Medium else FontWeight.Light,
                    fontSize = 16.sp,
                    color = if (selectedTab == PickFandomTab.Picks) mainGreen else Color(0xFFA4A4A6)
                )
            }
            Box(
                modifier = Modifier.clickable { selectedTab = PickFandomTab.Fandom }
            ) {
                Text(
                    text = "팬덤 $fansTotal",
                    fontFamily = PaperlogyFontFamily,
                    fontWeight = if (selectedTab == PickFandomTab.Fandom) FontWeight.Medium else FontWeight.Light,
                    fontSize = 16.sp,
                    color = if (selectedTab == PickFandomTab.Fandom) mainGreen else Color(0xFFA4A4A6)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when {
                selectedTab == PickFandomTab.Picks && isLoadingPicks -> {
                    CircularProgressIndicator(
                        color = mainGreen,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                selectedTab == PickFandomTab.Fandom && isLoadingFans -> {
                    CircularProgressIndicator(
                        color = mainGreen,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                currentList.isEmpty() -> {
                    Text(
                        text = when (selectedTab) {
                            PickFandomTab.Picks -> "픽 목록이 없습니다"
                            PickFandomTab.Fandom -> "팬덤이 없습니다"
                        },
                        fontFamily = PaperlogyFontFamily,
                        fontSize = 14.sp,
                        color = Color(0xFFA4A4A6),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(currentList) { user ->
                            FriendItemCard(
                                user = user,
                                navController = navController,
                                currentUserId = currentUserId,
                                isPickTab = selectedTab == PickFandomTab.Picks
                            )
                        }
                    }
                }
            }
        }
    }
}
