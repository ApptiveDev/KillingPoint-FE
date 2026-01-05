package com.killingpart.killingpoint.ui.screen.SocialScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.max
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

@Composable
fun FeedRunMusicBox(
    feedDiary: FeedDiary,
    navController: NavController,
    onLikeClick: (() -> Unit)? = null,
    onVideoEnd: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val diary = feedDiary.toDiary
    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    
    var isLiked by remember(feedDiary.diaryId) { mutableStateOf(feedDiary.isLiked) }
    var likeCount by remember(feedDiary.diaryId) { mutableStateOf(feedDiary.likeCount) }

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
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
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
                
                Box(
                    modifier = Modifier
                        .width(68.dp)
                        .background(
                            color = Color(0xFF262626),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            val encodedUsername = URLEncoder.encode(feedDiary.username, "UTF-8")
                            val encodedTag = URLEncoder.encode(feedDiary.tag, "UTF-8")
                            val encodedProfileImageUrl = URLEncoder.encode(feedDiary.profileImageUrl, "UTF-8")
                            navController.navigate(
                                "friend_profile" +
                                        "?userId=${feedDiary.userId}" +
                                        "&username=$encodedUsername" +
                                        "&tag=$encodedTag" +
                                        "&profileImageUrl=$encodedProfileImageUrl" +
                                        "&isMyPick=false"
                            )
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "프로필 방문",
                        fontFamily = PaperlogyFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 9.sp,
                        color = mainGreen,
                    )
                }
            }

            key(diary.videoUrl) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
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
                        YouTubePlayerBox(
                            diary,
                            startSeconds,
                            durationSeconds,
                            isPlayingState = null,
                            onVideoEnd = {
                                onVideoEnd?.invoke()
                            }
                        )
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

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

