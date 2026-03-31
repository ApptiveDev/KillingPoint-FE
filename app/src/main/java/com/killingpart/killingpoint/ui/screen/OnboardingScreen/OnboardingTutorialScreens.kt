package com.killingpart.killingpoint.ui.screen.OnboardingScreen

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.killingpart.killingpoint.navigation.navigateToMainClearingStack
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily

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
                        contentColor = Color(0xFF17181B)
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
                        containerColor = Color(0xFF3A3B3E),
                        contentColor = Color(0xFF17181B)
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

/**
 * 홈 탭(내 컬렉션 / 뮤직 캘린더) 프리뷰 — 데모 UI, 실제 등록 데이터는 추후 연동 가능.
 */
@Composable
fun OnboardingHomePreviewScreen(navController: NavController) {
    var tab by remember { mutableIntStateOf(0) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "back",
                    tint = Color.White
                )
            }
            Text(
                text = "추가한 킬링파트는 여기서 다시 볼 수 있어요.",
                color = Color.White,
                fontFamily = PaperlogyFontFamily,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f),
                lineHeight = 20.sp
            )
            TextButton(onClick = { navController.navigateToMainClearingStack() }) {
                Text("건너뛰기", color = Color.White, fontFamily = PaperlogyFontFamily)
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf("내 컬렉션", "뮤직 캘린더").forEachIndexed { index, label ->
                val selected = tab == index
                Text(
                    text = label,
                    color = if (selected) Color.Black else Color.White,
                    fontFamily = PaperlogyFontFamily,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (selected) Color.White else Color(0xFF2A2B2E))
                        .clickable { tab = index }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                )
                if (index == 0) Spacer(modifier = Modifier.width(8.dp))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (tab == 0) {
                OnboardingCollectionDemoContent()
            } else {
                OnboardingCalendarDemoContent()
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

@Composable
private fun OnboardingCollectionDemoContent() {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(CardBg)
            )
            Spacer(modifier = Modifier.size(12.dp))
            Column {
                Text("홍길동", color = Color.White, fontFamily = PaperlogyFontFamily, fontSize = 15.sp)
                Text("@KILLERPART", color = CtaGreen, fontFamily = PaperlogyFontFamily, fontSize = 12.sp)
            }
        }
        Text(
            "0 명곡   0 PICKS   1 킬링파트",
            color = CtaGreen,
            fontFamily = PaperlogyFontFamily,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(CardBg)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text("Title", color = Color.White, fontFamily = PaperlogyFontFamily)
        Text("Artist Name", color = Color(0xFF9A9B9E), fontFamily = PaperlogyFontFamily, fontSize = 13.sp)
        Text("1999.12.12", color = Color(0xFF6A6B6C), fontFamily = PaperlogyFontFamily, fontSize = 12.sp)
    }
}

@Composable
private fun OnboardingCalendarDemoContent() {
    Column {
        Text(
            "2026년 3월",
            color = Color.White,
            fontFamily = PaperlogyFontFamily,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Text(
            "일 월 화 수 목 금 토",
            color = Color(0xFF9A9B9E),
            fontFamily = PaperlogyFontFamily,
            fontSize = 11.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "29일에 등록한 킬링파트가 여기 표시됩니다 (데모).",
            color = Color(0xFF9A9B9E),
            fontFamily = PaperlogyFontFamily,
            fontSize = 13.sp,
            lineHeight = 20.sp
        )
    }
}

/**
 * 피드/탐색 소개 — 카드 내부는 터치해도 동작하지 않음 (다음·건너뛰기만).
 */
@Composable
fun OnboardingFeedDemoScreen(navController: NavController) {
    val blockInteraction = remember { MutableInteractionSource() }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "back",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            TextButton(onClick = { navController.navigateToMainClearingStack() }) {
                Text("건너뛰기", color = Color.White, fontFamily = PaperlogyFontFamily)
            }
        }
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
                fontSize = 15.sp,
                lineHeight = 22.sp,
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
                    // 데모 카드: 클릭 소비만 하고 네비게이션 없음
                    .clickable(
                        interactionSource = blockInteraction,
                        indication = null,
                        onClick = { }
                    )
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFF444548))
                        )
                        Spacer(modifier = Modifier.size(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "킬링파트 @KILLINGPART",
                                color = Color.White,
                                fontFamily = PaperlogyFontFamily,
                                fontSize = 13.sp
                            )
                        }
                        Text(
                            "프로필 방문",
                            color = CtaGreen,
                            fontFamily = PaperlogyFontFamily,
                            fontSize = 11.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1A1A1A))
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "데모 게시글입니다. 다음을 눌러 계속하세요.",
                        color = Color(0xFFCCCCCC),
                        fontFamily = PaperlogyFontFamily,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
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
