package com.killingpart.killingpoint.ui.screen.CommentScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.killingpart.killingpoint.R
import com.killingpart.killingpoint.data.model.Comment
import com.killingpart.killingpoint.data.model.CommentReference
import com.killingpart.killingpoint.ui.screen.SocialScreen.FeedMenuItem
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import com.killingpart.killingpoint.ui.theme.mainGreen

internal val CommentActionGray = Color(0xFFC1C1C1)
internal val CommentHighlight = Color(0x4DAAAAAA)
private val LinkBlue = Color(0xFF4DA3FF)
private const val COLLAPSED_MAX_LINES = 4

/** 댓글 하나에서 일어나는 사용자 동작 */
data class CommentActions(
    val onLike: (Comment) -> Unit,
    val onReply: (Comment) -> Unit,
    val onEdit: (Comment) -> Unit,
    val onDelete: (Comment) -> Unit,
    val onReport: (Comment) -> Unit,
    val onUnblock: (Comment) -> Unit
)

/**
 * 원댓글/답글 공통 행. 답글은 호출하는 쪽에서 왼쪽 들여쓰기(45dp)를 준다.
 */
@Composable
fun CommentRow(
    comment: Comment,
    actions: CommentActions,
    isHighlighted: Boolean,
    modifier: Modifier = Modifier,
    footer: @Composable () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(if (isHighlighted) CommentHighlight else Color.Transparent)
            .padding(horizontal = 32.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        when {
            comment.isDeleted -> MaskedBody(text = "삭제된 댓글 입니다.")
            comment.isBlocked -> MaskedBody(
                text = "차단한 유저의 댓글입니다.",
                trailing = {
                    Text(
                        text = "차단 해제",
                        color = CommentActionGray,
                        fontFamily = PaperlogyFontFamily,
                        fontSize = 10.sp,
                        modifier = Modifier.clickable { actions.onUnblock(comment) }
                    )
                }
            )
            else -> ActiveBody(
                avatarUrl = comment.author?.profileImageUrl,
                name = comment.author?.username.orEmpty(),
                timeLabel = buildTimeLabel(comment),
                text = comment.text.orEmpty(),
                references = comment.references
            )
        }

        if (!comment.isDeleted) {
            CommentActionRow(
                showLike = !comment.isBlocked,
                isLiked = comment.isLiked,
                likeCount = comment.likeCount,
                onLike = { actions.onLike(comment) },
                onReply = { actions.onReply(comment) },
                menu = { dismiss ->
                    if (comment.isMine) {
                        FeedMenuItem(text = "수정하기", iconRes = R.drawable.fixing) {
                            dismiss(); actions.onEdit(comment)
                        }
                        FeedMenuItem(text = "삭제하기", iconRes = R.drawable.deleting) {
                            dismiss(); actions.onDelete(comment)
                        }
                    } else if (!comment.isBlocked) {
                        FeedMenuItem(text = "신고하기", iconRes = R.drawable.ic_report) {
                            dismiss(); actions.onReport(comment)
                        }
                    }
                },
                hasMenu = comment.isMine || !comment.isBlocked
            )
        }

        footer()
    }
}

/** 서버 응답 전 화면에 먼저 보여주는 내 댓글 (투명도 50%) */
@Composable
fun PendingCommentRow(
    avatarUrl: String?,
    name: String,
    text: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .alpha(0.5f)
            .padding(horizontal = 32.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ActiveBody(
            avatarUrl = avatarUrl,
            name = name,
            timeLabel = "등록중...",
            text = text,
            references = emptyList()
        )
        CommentActionRow(
            showLike = true,
            isLiked = false,
            likeCount = 0,
            onLike = {},
            onReply = {},
            menu = {},
            hasMenu = false
        )
    }
}

private fun buildTimeLabel(comment: Comment): String {
    val relative = CommentText.relativeTime(comment.createDate)
    return if (comment.isEdited) "$relative · 수정됨" else relative
}

@Composable
private fun CommentAvatar(url: String?) {
    val placeholder = painterResource(id = R.drawable.default_profile)
    if (url.isNullOrBlank()) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFF9E9E9E))
        )
    } else {
        AsyncImage(
            model = url,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            placeholder = placeholder,
            error = placeholder,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
        )
    }
}

@Composable
private fun MaskedBody(text: String, trailing: @Composable () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CommentAvatar(url = null)
        Spacer(modifier = Modifier.width(9.dp))
        Text(
            text = text,
            color = Color.White,
            fontFamily = PaperlogyFontFamily,
            fontSize = 12.sp,
            modifier = Modifier.weight(1f)
        )
        trailing()
    }
}

@Composable
private fun ActiveBody(
    avatarUrl: String?,
    name: String,
    timeLabel: String,
    text: String,
    references: List<CommentReference>
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CommentAvatar(url = avatarUrl)
        Spacer(modifier = Modifier.width(9.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Text(
                    text = name,
                    color = Color.White,
                    fontFamily = PaperlogyFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = timeLabel,
                    color = Color.White,
                    fontFamily = PaperlogyFontFamily,
                    fontWeight = FontWeight.Light,
                    fontSize = 8.sp
                )
            }
            ExpandableCommentText(text = text, references = references)
        }
    }
}

@Composable
private fun ExpandableCommentText(text: String, references: List<CommentReference>) {
    var expanded by rememberSaveable(text) { mutableStateOf(false) }
    var overflowed by remember(text) { mutableStateOf(false) }
    val annotated = remember(text, references) { buildCommentText(text, references) }
    val bodyStyle = TextStyle(
        color = Color.White,
        fontFamily = PaperlogyFontFamily,
        fontSize = 12.sp
    )

    Column {
        Text(
            text = annotated,
            style = bodyStyle,
            maxLines = if (expanded) Int.MAX_VALUE else COLLAPSED_MAX_LINES,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { result -> if (!expanded) overflowed = result.hasVisualOverflow }
        )
        if (overflowed || expanded) {
            Text(
                text = if (expanded) "접기" else "더보기",
                color = CommentActionGray,
                fontFamily = PaperlogyFontFamily,
                fontSize = 10.sp,
                modifier = Modifier
                    .padding(top = 2.dp)
                    .clickable { expanded = !expanded }
            )
        }
    }
}

private fun buildCommentText(text: String, references: List<CommentReference>): AnnotatedString =
    buildAnnotatedString {
        CommentText.segments(text, references).forEach { segment ->
            when (segment) {
                is CommentSegment.Plain -> append(segment.text)
                is CommentSegment.Mention -> withStyle(SpanStyle(color = mainGreen)) { append(segment.text) }
                is CommentSegment.Link -> withLink(
                    LinkAnnotation.Url(
                        url = segment.url,
                        styles = TextLinkStyles(
                            style = SpanStyle(color = LinkBlue, textDecoration = TextDecoration.Underline)
                        )
                    )
                ) { append(segment.text) }
            }
        }
    }

@Composable
private fun CommentActionRow(
    showLike: Boolean,
    isLiked: Boolean,
    likeCount: Int,
    onLike: () -> Unit,
    onReply: () -> Unit,
    menu: @Composable (dismiss: () -> Unit) -> Unit,
    hasMenu: Boolean
) {
    var showMenu by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.padding(start = 45.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (showLike) {
            Row(
                modifier = Modifier.clickable(onClick = onLike),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Icon(
                    imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "좋아요",
                    tint = if (isLiked) mainGreen else Color.White,
                    modifier = Modifier.size(9.dp)
                )
                Text(
                    text = likeCount.toString(),
                    color = Color.White,
                    fontFamily = PaperlogyFontFamily,
                    fontWeight = FontWeight.Light,
                    fontSize = 8.sp
                )
            }
        }
        Text(
            text = "답글 달기",
            color = CommentActionGray,
            fontFamily = PaperlogyFontFamily,
            fontWeight = FontWeight.Light,
            fontSize = 8.sp,
            modifier = Modifier.clickable(onClick = onReply)
        )
        if (hasMenu) {
            Box {
                Icon(
                    imageVector = Icons.Filled.MoreHoriz,
                    contentDescription = "메뉴",
                    tint = CommentActionGray,
                    modifier = Modifier
                        .size(14.dp)
                        .clickable { showMenu = true }
                )
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier
                        .width(78.dp)
                        .background(color = Color(0xFF101010), shape = RoundedCornerShape(4.dp))
                ) {
                    menu { showMenu = false }
                }
            }
        }
    }
}

/** "── 답글 n개 보기" / "── 답글 숨기기" */
@Composable
fun ReplyToggle(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(start = 45.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .width(26.dp)
                .height(1.dp)
                .background(CommentActionGray)
        )
        Text(
            text = label,
            color = CommentActionGray,
            fontFamily = PaperlogyFontFamily,
            fontWeight = FontWeight.Light,
            fontSize = 8.sp
        )
    }
}
