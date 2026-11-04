package com.killingpart.killingpoint.ui.screen.CommentScreen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.killingpart.killingpoint.R
import com.killingpart.killingpoint.data.model.CommentPreview
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import kotlinx.coroutines.delay

private val PreviewBackground = Color(0xFF162A12)
private const val ROTATE_INTERVAL_MS = 2_000L
private val AvatarSize = 28.dp
private val AvatarOverlap = 21.5.dp

/**
 * 상세 화면 일기 아래 댓글 미리보기 (1안).
 * 아바타 최대 3개(같은 유저 중복 제거) + 최신 댓글 2초마다 전환 + 댓글 수. 누르면 댓글 시트를 연다.
 */
@Composable
fun CommentPreviewBar(
    preview: CommentPreview,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val comments = preview.comments
    val avatars = remember(comments) {
        comments.distinctBy { it.userId }.take(3).map { it.profileImageUrl }
    }
    var index by remember(comments) { mutableIntStateOf(0) }

    // 댓글이 1개 이하면 전환 없음
    LaunchedEffect(comments) {
        if (comments.size <= 1) return@LaunchedEffect
        while (true) {
            delay(ROTATE_INTERVAL_MS)
            index = (index + 1) % comments.size
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(PreviewBackground)
            .clickable(onClick = onClick)
            .padding(start = 9.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (avatars.isNotEmpty()) {
            AvatarStack(urls = avatars)
            Spacer(modifier = Modifier.width(19.dp))
        } else {
            Spacer(modifier = Modifier.width(6.dp))
        }

        Box(modifier = Modifier.weight(1f)) {
            if (comments.isEmpty()) {
                PreviewText(text = "첫 반응을 남겨주세요!")
            } else {
                AnimatedContent(
                    targetState = index.coerceIn(0, comments.lastIndex),
                    transitionSpec = {
                        (slideInVertically(tween(300)) { it } togetherWith slideOutVertically(tween(300)) { -it })
                    },
                    label = "commentPreview"
                ) { i ->
                    PreviewText(text = CommentText.previewText(comments[i].content))
                }
            }
        }

        Text(
            text = CommentText.countLabel(preview.commentCount),
            color = Color.White,
            fontFamily = PaperlogyFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
private fun PreviewText(text: String) {
    Text(
        text = text,
        color = Color.White,
        fontFamily = PaperlogyFontFamily,
        fontSize = 14.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun AvatarStack(urls: List<String?>) {
    val placeholder = painterResource(id = R.drawable.default_profile)
    val width = AvatarSize + AvatarOverlap * (urls.size - 1)
    Box(modifier = Modifier.width(width).height(AvatarSize)) {
        urls.forEachIndexed { i, url ->
            AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                placeholder = placeholder,
                error = placeholder,
                modifier = Modifier
                    .offset(x = AvatarOverlap * i)
                    .size(AvatarSize)
                    .clip(CircleShape)
                    .border(1.dp, PreviewBackground, CircleShape)
            )
        }
    }
}
