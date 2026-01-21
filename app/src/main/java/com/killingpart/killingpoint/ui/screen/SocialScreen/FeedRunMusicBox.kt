package com.killingpart.killingpoint.ui.screen.SocialScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.killingpart.killingpoint.R
import com.killingpart.killingpoint.data.model.Diary
import com.killingpart.killingpoint.data.model.FeedDiary
import com.killingpart.killingpoint.ui.screen.MainScreen.DiaryBox
import com.killingpart.killingpoint.ui.screen.MainScreen.YouTubePlayerBox
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import com.killingpart.killingpoint.ui.theme.mainGreen
import java.net.URLEncoder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import com.killingpart.killingpoint.data.repository.AuthRepository
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@Composable
fun FeedRunMusicBox(
    feedDiary: FeedDiary,
    navController: NavController,
    isActive: Boolean = true,
    onLikeClick: (() -> Unit)? = null,
    onVideoEnd: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val diary = feedDiary.toDiary
    val coroutineScope = rememberCoroutineScope()
    val authRepository = remember(context) { AuthRepository(context) }


    var isLiked by remember(feedDiary.diaryId) { mutableStateOf(feedDiary.isLiked) }
    var likeCount by remember(feedDiary.diaryId) { mutableStateOf(feedDiary.likeCount) }
    var showMenu by remember { mutableStateOf(false) }
    var showReportModal by remember { mutableStateOf(false) }
    var reportContent by remember { mutableStateOf("") }
    var isReporting by remember { mutableStateOf(false) }

    LaunchedEffect(feedDiary.isLiked, feedDiary.likeCount) {
        isLiked = feedDiary.isLiked
        likeCount = feedDiary.likeCount
    }

    LaunchedEffect(diary.videoUrl) {
        android.util.Log.d("FeedRunMusicBox", "FeedRunMusicBox 렌더링: diaryId=${diary.id}, videoUrl=${diary.videoUrl}")
        android.util.Log.d("FeedRunMusicBox", "Start: ${diary.start}, Duration: ${diary.duration}")
        android.util.Log.d("FeedRunMusicBox", "Music: ${diary.musicTitle} - ${diary.artist}")
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 15.dp, end = 17.dp, top = 8.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AsyncImage(
                        model = feedDiary.profileImageUrl,
                        contentDescription = "프로필 사진",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .border(3.dp, mainGreen, CircleShape),
                        placeholder = painterResource(id = R.drawable.default_profile),
                        error = painterResource(id = R.drawable.default_profile)
                    )

                    Column {
                        Text(
                            text = feedDiary.username,
                            fontFamily = PaperlogyFontFamily,
                            fontWeight = FontWeight.W400,
                            fontSize = 12.sp,
                            color = mainGreen
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "@${feedDiary.tag}",
                            fontFamily = PaperlogyFontFamily,
                            fontWeight = FontWeight.W400,
                            fontSize = 11.sp,
                            color = mainGreen,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Box {
                    Icon(
                        imageVector = Icons.Filled.MoreHoriz,
                        contentDescription = "메뉴",
                        tint = Color(0xFF4E4E4E),
                        modifier = Modifier
                            .size(30.dp)
                            .clickable { showMenu = true }
                    )

                    Box {
                        Icon(
                            imageVector = Icons.Filled.MoreHoriz,
                            contentDescription = "메뉴",
                            tint = Color(0xFF4E4E4E),
                            modifier = Modifier
                                .size(30.dp)
                                .clickable { showMenu = true }
                        )

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier
                                .width(78.dp)
                                .background(
                                    color = Color(0xFF101010),
                                    shape = RoundedCornerShape(4.dp)
                                )
                        ) {
                            FeedMenuItem(
                                text = "차단하기",
                                iconRes = R.drawable.ic_block
                            ) {
                                showMenu = false
                                // TODO 차단하기
                            }

                            FeedMenuItem(
                                text = "신고하기",
                                iconRes = R.drawable.ic_report
                            ) {
                                showMenu = false
                                showReportModal = true
                            }
                        }
                    }
                }
            }

            key(diary.videoUrl) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val startSeconds = diary.start.toFloatOrNull() ?: 0f
                    val endSeconds = diary.end.toFloatOrNull() ?: 0f
                    val durationSeconds = if (endSeconds > 0f && startSeconds > 0f) {
                        endSeconds - startSeconds
                    } else {
                        diary.duration.toFloatOrNull() ?: 0f
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isActive) {
                            YouTubePlayerBox(
                                diary,
                                startSeconds,
                                durationSeconds,
                                isPlayingState = null,
                                onVideoEnd = {
                                    onVideoEnd?.invoke()
                                }
                            )
                        } else {
                            // 비활성 아이템은 플레이스홀더만 표시
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .background(Color.Black.copy(alpha = 0.3f))
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.clickable {
                                onLikeClick?.invoke()
                            }
                                .size(49.dp, 24.dp)
                                .background(color = if (isLiked) mainGreen else Color(0xFF2C2C2C),
                                    RoundedCornerShape(8.dp)),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Favorite,
                                contentDescription = "좋아요",
                                tint = if (isLiked) Color.Black else mainGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = likeCount.toString(),
                                fontFamily = PaperlogyFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                color = if (isLiked) Color.Black else Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        DiaryBox(diary)
                    }

                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }

        if (showReportModal) {
            ReportDiaryModal(
                diaryId = feedDiary.diaryId,
                reportContent = reportContent,
                onContentChange = { reportContent = it },
                onDismiss = {
                    showReportModal = false
                    reportContent = ""
                },
                onSubmit = {
                    coroutineScope.launch {
                        isReporting = true
                        try {
                            authRepository.reportDiary(feedDiary.diaryId, reportContent).getOrThrow()
                            showReportModal = false
                            reportContent = ""
                        } catch (e: Exception) {
                            android.util.Log.e("FeedRunMusicBox", "게시글 신고 실패: ${e.message}")
                        } finally {
                            isReporting = false
                        }
                    }
                },
                isLoading = isReporting
            )
        }
    }
}

@Composable
fun FeedMenuItem(
    text: String,
    iconRes: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(start = 8.dp, end = 0.dp ,top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = text,
            fontFamily = PaperlogyFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            color = Color(0xFFB1B1B1)
        )

        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(14.dp)
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDiaryModal(
    diaryId: Long,
    reportContent: String,
    onContentChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit,
    isLoading: Boolean
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF111111),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
            , horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "불편하거나, 부적절한 콘텐츠를 발견하셨나요?",
                fontFamily = PaperlogyFontFamily,
                fontSize = 13.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "아래에 신고 사유를 작성해주세요.",
                fontFamily = PaperlogyFontFamily,
                fontSize = 13.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(Color(0xFF2A2A2A), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                BasicTextField(
                    value = reportContent,
                    onValueChange = onContentChange,
                    modifier = Modifier.fillMaxSize(),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = Color.White,
                        fontSize = 13.sp,
                        fontFamily = PaperlogyFontFamily
                    ),
                    decorationBox = { innerTextField ->
                        if (reportContent.isEmpty()) {
                            Text(
                                text = "신고 사유...",
                                color = Color(0xFF777777),
                                fontSize = 13.sp,
                                fontFamily = PaperlogyFontFamily,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                        innerTextField()
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ReportButton(
                    text = "돌아가기",
                    background = Color.White,
                    textColor = Color.Black,
                    modifier = Modifier.weight(1f)
                ) {
                    onDismiss()
                }

                ReportButton(
                    text = "신고하기",
                    background = Color(0xFFFF5A5A),
                    textColor = Color.White,
                    modifier = Modifier.weight(1f),
                    enabled = reportContent.isNotBlank() && !isLoading
                ) {
                    onSubmit()
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun ReportButton(
    text: String,
    background: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .background(
                color = if (enabled) background else background.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontFamily = PaperlogyFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = textColor
        )
    }
}