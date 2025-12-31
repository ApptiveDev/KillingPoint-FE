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
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily

@Composable
fun FriendScreen(navController: NavController) {
    var searchText by remember { mutableStateOf("") }

    // TODO: 실제 데이터로 교체 필요
    val friends = remember {
        listOf(
            Friend(
                name = "홍길동",
                username = "@KILLERPART",
                profileImageUrl = ""
            ),
            Friend(
                name = "김소리",
                username = "@SORRHUNG",
                profileImageUrl = ""
            ),
            Friend(
                name = "웅찬혁",
                username = "@CHANCHAN2",
                profileImageUrl = ""
            ),
            Friend(
                name = "설예지",
                username = "@JI_0214",
                profileImageUrl = ""
            ),
            Friend(
                name = "닥터페퍼",
                username = "@DR_PEPPER",
                profileImageUrl = ""
            ),
            Friend(
                name = "모수린",
                username = "@MOSUUULIN",
                profileImageUrl = ""
            ),
            Friend(
                name = "개나리",
                username = "@SPRINGISHERE",
                profileImageUrl = ""
            ),
            Friend(
                name = "나우주",
                username = "@MENIVERSE",
                profileImageUrl = ""
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // 검색 바
        TextField(
            value = searchText,
            onValueChange = { searchText = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(45.dp),
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
                focusedContainerColor = Color(0xFF2A2A2A),
                unfocusedContainerColor = Color(0xFF2A2A2A),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    // TODO: 검색 로직 구현
                }
            ),
            // TODO: 검색 아이콘 리소스 추가 필요
        )

        // 카테고리 라벨
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "나의 픽 8",
                fontFamily = PaperlogyFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = Color(0xFFA4A4A6)
            )
            Text(
                text = "나의 팬덤 16",
                fontFamily = PaperlogyFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = Color(0xFFA4A4A6)
            )
        }

        // 친구 목록
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(friends) { friend ->
                FriendItemCard(friend = friend, navController = navController)
            }
        }
    }
}

data class Friend(
    val name: String,
    val username: String,
    val profileImageUrl: String
)

@Composable
fun FriendItemCard(friend: Friend, navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFF2A2A2A),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
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
                if (friend.profileImageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = friend.profileImageUrl,
                        contentDescription = "프로필 이미지",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Column {
                Text(
                    text = friend.name,
                    fontFamily = PaperlogyFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = Color.White
                )
                Text(
                    text = friend.username,
                    fontFamily = PaperlogyFontFamily,
                    fontWeight = FontWeight.Light,
                    fontSize = 12.sp,
                    color = Color(0xFFA4A4A6)
                )
            }
        }

        // 프로필 방문 버튼
        Text(
            text = "프로필 방문",
            fontFamily = PaperlogyFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = Color(0xFF7B7B7B),
            modifier = Modifier.clickable {
                // TODO: 프로필 화면으로 이동
            }
        )
    }
}
