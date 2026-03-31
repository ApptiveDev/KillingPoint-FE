package com.killingpart.killingpoint.ui.screen.SocialScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.runtime.saveable.rememberSaveable
import com.killingpart.killingpoint.ui.component.AppBackground
import com.killingpart.killingpoint.ui.component.BottomBar
import com.killingpart.killingpoint.ui.screen.MainScreen.TopPillTabs

enum class SocialTab {
    FEED, FRIEND
}

@Composable
fun SocialScreen(navController: NavController, initialTab: String = "feed") {
    var selectedTab by rememberSaveable(initialTab) { 
        mutableStateOf(
            when (initialTab) {
                "friend" -> SocialTab.FRIEND
                else -> SocialTab.FEED
            }
        )
    }

    AppBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            // 친구 탭일 때만 단색 배경 적용
            if (selectedTab == SocialTab.FRIEND) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF1D1E20))
                )
            }
            
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(35.dp))

                TopPillTabs(
                    options = listOf("피드", "친구"),
                    selectedIndex = when (selectedTab) {
                        SocialTab.FEED -> 0
                        SocialTab.FRIEND -> 1
                    },
                    onSelected = { idx ->
                        selectedTab = when (idx) {
                            0 -> SocialTab.FEED
                            else -> SocialTab.FRIEND
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 30.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    when (selectedTab) {
                        SocialTab.FEED -> FeedScreen(navController)
                        SocialTab.FRIEND -> FriendScreen(navController)
                    }
                }

                BottomBar(navController = navController)
            }
        }
    }
}
