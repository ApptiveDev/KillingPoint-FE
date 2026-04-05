package com.killingpart.killingpoint.ui.screen.OnboardingScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.killingpart.killingpoint.R
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.killingpart.killingpoint.data.model.Diary
import com.killingpart.killingpoint.data.repository.AuthRepository
import com.killingpart.killingpoint.navigation.navigateToMainClearingStack
import com.killingpart.killingpoint.ui.screen.ArchiveScreen.OuterBox
import com.killingpart.killingpoint.ui.screen.MainScreen.TopPillTabs
import com.killingpart.killingpoint.ui.screen.MusicCalendarScreen.MusicCalendarScreen
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import java.time.LocalDate

private val CtaGreen = Color(0xFFC4F236)
private val CardBg = Color(0xFF232427)

/**
 * 태그 설정 직후: 첫 킬링파트 추가 유도.
 */
@Composable
fun OnboardingKpIntroScreen(navController: NavController) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 24.dp)
    ) {
        val topInset = maxHeight / 3.5f
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = topInset, bottom = 28.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "첫번째 킬링파트를\n추가해볼까요?",
                color = Color.White,
                fontFamily = PaperlogyFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                lineHeight = 30.sp,
                textAlign = TextAlign.Center
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        navController.navigate("add_music?tutorial=true")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(100),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CtaGreen,
                        contentColor = Color(0xFF000000)
                    )
                ) {
                    Text(
                        text = "추가하기",
                        fontFamily = PaperlogyFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Button(
                    onClick = { navController.navigateToMainClearingStack() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(100),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF9C9C9C),
                        contentColor = Color(0xFF000000)
                    )
                ) {
                    Text(
                        text = "건너뛰기",
                        fontFamily = PaperlogyFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun OnboardingHomePreviewScreen(navController: NavController) {
    val context = LocalContext.current
    val repo = remember { AuthRepository(context) }
    var tab by remember { mutableIntStateOf(0) }
    var diaries by remember { mutableStateOf<List<Diary>>(emptyList()) }

    LaunchedEffect(Unit) {
        diaries = repo.getMyDiaries(size = 30).content
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        OnboardingFlowTopBar(
            onBack = { navController.popBackStack() },
            onSkip = { navController.navigateToMainClearingStack() }
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "추가한 킬링파트는\n여기서 다시 볼 수 있어요.",
            color = Color.White,
            fontFamily = PaperlogyFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            lineHeight = 34.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        TopPillTabs(
            options = listOf("내 컬렉션", "뮤직 캘린더"),
            selectedIndex = tab,
            onSelected = { tab = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            height = 56.dp,
            textSize = 16.sp
        )
        Spacer(modifier = Modifier.height(15.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (tab == 0) {
                OuterBox(
                    diaries = diaries,
                    navController = navController,
                    onProfileClick = { },
                    showProfileEditButton = false,
                    showStoredTab = false,
                    showDiaryTypeTabs = false,
                    interactionsEnabled = false,
                    diaryCardScale = 0.86f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .fillMaxHeight()
                )
            } else {
                MusicCalendarScreen(
                    diaries = diaries,
                    navController = navController,
                    initialSelectedDate = LocalDate.now().toString(),
                    interactionsEnabled = false
                )
            }
        }
        Button(
            onClick = { navController.navigate("onboarding_feed_demo") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .height(52.dp),
            shape = RoundedCornerShape(100),
            colors = ButtonDefaults.buttonColors(
                containerColor = CtaGreen,
                contentColor = Color(0xFF17181B)
            )
        ) {
            Text("다음으로 →", fontFamily = PaperlogyFontFamily, fontWeight = FontWeight.Bold)
        }
    }
}

/**
 * 피드/탐색 데모 — 시안과 동일한 정적 UI. 하단 [다음으로]만 동작.
 */
@Composable
fun OnboardingFeedDemoScreen(navController: NavController) {
    val blockInteraction = remember { MutableInteractionSource() }
    val mutedGreen = Color(0xFF9EB85C)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        OnboardingFlowTopBar(
            onBack = { navController.popBackStack() },
            onSkip = { navController.navigateToMainClearingStack() }
        )
        Spacer(modifier = Modifier.height(14.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "피드에서 친구의 킬링파트를,\n탐색에서 다양한 킬링파트를 감상해보세요",
                color = Color.White,
                fontFamily = PaperlogyFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                lineHeight = 30.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 20.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFF333438), RoundedCornerShape(16.dp))
                    .background(CardBg)
                    .padding(16.dp)
                    .clickable(
                        interactionSource = blockInteraction,
                        indication = null,
                        onClick = { }
                    )
            ) {
                Column {
                    // 프로필 헤더
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(R.drawable.default_profile),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(55.dp)
                                .clip(CircleShape)
                                .border(4.dp, CtaGreen, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "킬링파트",
                                color = CtaGreen,
                                fontFamily = PaperlogyFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "@KILLINGPART",
                                color = mutedGreen,
                                fontFamily = PaperlogyFontFamily,
                                fontSize = 11.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(
                                    color = Color(0xFF262626),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "프로필 방문",
                                color = CtaGreen,
                                fontFamily = PaperlogyFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    // 영상 썸네일
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        Image(
                            painter = painterResource(R.drawable.example_video),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, Color.White.copy(alpha = 0.9f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Info,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(56.dp)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFF0000)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))

                    // 곡 정보
                    Text(
                        text = "Death Sonnet von Dat",
                        color = Color.White,
                        fontFamily = PaperlogyFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Davinci Leo",
                        color = Color(0xFF8E8E93),
                        fontFamily = PaperlogyFontFamily,
                        fontSize = 15.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(30.dp))

                    // 킬링파트 일기 + 좋아요
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                        content = { // content 파라미터 명시
                            Text(
                                text = "킬링파트 일기",
                                color = Color(0xFF8E8E93),
                                fontFamily = PaperlogyFontFamily,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .clip(RoundedCornerShape(30))
                                    .background(CtaGreen)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Favorite,
                                    contentDescription = null,
                                    tint = Color(0xFF17181B),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "26",
                                    color = Color(0xFF17181B),
                                    fontFamily = PaperlogyFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "오늘 하루는 유난히 공허하게 흘러간 것 같다. 해야 할 일은 분명 있었지만 마음은 잘 따라주지 않았고, 생각들은 제자리를 맴돌았다. 그러다 문득, 특별한 이유 없이 음악을 들어야겠다는 충동이 찾아왔다. 그래서 큰 고민도 없이 '아무 노래'를 골랐다...",
                        color = Color.White,
                        fontFamily = PaperlogyFontFamily,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(26.dp))

                    // 킬링파트 라벨 + 정지 아이콘
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "킬링파트",
                            color = Color(0xFF8E8E93),
                            fontFamily = PaperlogyFontFamily,
                            fontSize = 12.sp
                        )
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(CtaGreen)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    // 재생바: 얇은 흰 전체 라인 + 킬링파트 구간 두꺼운 초록 바
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                    ) {
                        // 전체 얇은 흰 라인
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .align(Alignment.Center)
                                .background(Color.White.copy(alpha = 0.5f))
                        )
                        // 킬링파트 구간 초록 바 (0:51 ~ 1:43 / 전체 2:00 기준)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.Center)
                        ) {
                            Spacer(modifier = Modifier.weight(0.425f))   // 0:51 / 2:00
                            Box(
                                modifier = Modifier
                                    .weight(0.433f)                       // (1:43 - 0:51) / 2:00
                                    .height(7.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(CtaGreen)
                            )
                            Spacer(modifier = Modifier.weight(0.142f))   // 나머지
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))

                    // 타임스탬프 + 대시 행
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "0",
                            color = Color(0xFF5C5C5E),
                            fontFamily = PaperlogyFontFamily,
                            fontSize = 12.sp
                        )
                        Row(
                            modifier = Modifier.weight(0.85f),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(9) {
                                Text("-", color = Color(0xFF5C5C5E), fontFamily = PaperlogyFontFamily, fontSize = 11.sp)
                            }
                            Text("1:42", color = Color(0xFF5C5C5E), fontFamily = PaperlogyFontFamily, fontSize = 11.sp)
                            repeat(4) {
                                Text("-", color = Color(0xFF5C5C5E), fontFamily = PaperlogyFontFamily, fontSize = 11.sp)
                            }
                            Text("2:00", color = Color(0xFF5C5C5E), fontFamily = PaperlogyFontFamily, fontSize = 11.sp)
                            repeat(3) {
                                Text("-", color = Color(0xFF5C5C5E), fontFamily = PaperlogyFontFamily, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
        Button(
            onClick = { navController.navigate("onboarding_finish") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .height(52.dp),
            shape = RoundedCornerShape(100),
            colors = ButtonDefaults.buttonColors(
                containerColor = CtaGreen,
                contentColor = Color(0xFF17181B)
            )
        ) {
            Text("다음으로 →", fontFamily = PaperlogyFontFamily, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun OnboardingFinishScreen(navController: NavController) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 24.dp)
    ) {
        val topInset = maxHeight / 3.5f
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = topInset, bottom = 28.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "이제 킬링파트를\n시작해보세요.",
                color = Color.White,
                fontFamily = PaperlogyFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                lineHeight = 32.sp,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = { navController.navigateToMainClearingStack() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(100),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CtaGreen,
                    contentColor = Color(0xFF17181B)
                )
            ) {
                Text(
                    text = "시작하기",
                    fontFamily = PaperlogyFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}
