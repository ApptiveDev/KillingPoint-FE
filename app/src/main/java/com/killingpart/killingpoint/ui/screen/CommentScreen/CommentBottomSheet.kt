package com.killingpart.killingpoint.ui.screen.CommentScreen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.killingpart.killingpoint.data.model.Comment
import com.killingpart.killingpoint.ui.screen.SocialScreen.ReportDiaryModal
import com.killingpart.killingpoint.ui.screen.SocialScreen.ReportSuccessModal
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import com.killingpart.killingpoint.ui.theme.mainGreen
import com.killingpart.killingpoint.ui.viewmodel.CommentEvent
import com.killingpart.killingpoint.ui.viewmodel.CommentInputMode
import com.killingpart.killingpoint.ui.viewmodel.CommentLoadState
import com.killingpart.killingpoint.ui.viewmodel.CommentUiState
import com.killingpart.killingpoint.ui.viewmodel.CommentViewModel

private val HeaderGreen = Color(0xFF2B5223)

/**
 * 킬링파트 댓글 바텀시트. 닫힐 때 onDismiss(최신 댓글 수) 로 미리보기 갱신에 쓴다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentBottomSheet(
    diaryId: Long,
    onDismiss: (commentCount: Int) -> Unit
) {
    val viewModel: CommentViewModel = viewModel(key = "comments_$diaryId")
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val keyboard = LocalSoftwareKeyboardController.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val listState = rememberLazyListState()
    val focusRequester = remember { FocusRequester() }

    var deleteTarget by remember { mutableStateOf<Comment?>(null) }
    var reportTarget by remember { mutableStateOf<Comment?>(null) }
    var reportContent by remember { mutableStateOf("") }
    var isReporting by remember { mutableStateOf(false) }
    var showReportSuccess by remember { mutableStateOf(false) }
    var scrollTargetId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(diaryId) { viewModel.start(diaryId) }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is CommentEvent.Message -> Toast.makeText(context, event.text, Toast.LENGTH_SHORT).show()
                is CommentEvent.ScrollTo -> scrollTargetId = event.commentId
                CommentEvent.ReportSuccess -> {
                    reportTarget = null
                    reportContent = ""
                    showReportSuccess = true
                }
            }
        }
    }

    // 등록한 댓글 위치로 스크롤
    LaunchedEffect(scrollTargetId, state.comments, state.threads) {
        val target = scrollTargetId ?: return@LaunchedEffect
        val index = listKeys(state).indexOf(target)
        if (index >= 0) listState.animateScrollToItem(index)
        scrollTargetId = null
    }

    // 답글/수정 시작 시 입력창 포커스
    LaunchedEffect(state.inputMode) {
        if (state.inputMode != CommentInputMode.Normal) {
            runCatching { focusRequester.requestFocus() }
            keyboard?.show()
        }
    }

    // 끝에 가까워지면 다음 페이지
    val shouldLoadMore by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            info.totalItemsCount > 0 && last >= info.totalItemsCount - 3
        }
    }
    LaunchedEffect(shouldLoadMore) { if (shouldLoadMore) viewModel.loadMore() }

    val actions = remember(viewModel) {
        CommentActions(
            onLike = viewModel::toggleLike,
            onReply = viewModel::startReply,
            onEdit = viewModel::startEdit,
            onDelete = { deleteTarget = it },
            onReport = { reportTarget = it },
            onUnblock = viewModel::unblock
        )
    }

    ModalBottomSheet(
        onDismissRequest = { onDismiss(state.commentCount) },
        sheetState = sheetState,
        containerColor = Color.Black,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .imePadding()
                .navigationBarsPadding()
        ) {
            CommentSheetHeader(commentCount = state.commentCount)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (state.loadState) {
                    CommentLoadState.LOADING -> CircularProgressIndicator(
                        color = mainGreen,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    CommentLoadState.ERROR -> ErrorState(onRetry = viewModel::refresh)
                    CommentLoadState.LOADED -> if (state.comments.isEmpty() && state.pending.isEmpty()) {
                        EmptyState()
                    } else {
                        LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                            commentListItems(state, actions, viewModel)
                        }
                    }
                }
            }

            CommentInputBar(
                value = state.input,
                mentionSpans = state.mentionSpans,
                inputMode = state.inputMode,
                mentionSuggest = state.mentionSuggest,
                canSubmit = state.canSubmit,
                focusRequester = focusRequester,
                onValueChange = viewModel::onInputChange,
                onSubmit = viewModel::submit,
                onCancelMode = viewModel::cancelInputMode,
                onSelectMention = viewModel::selectMention
            )
        }
    }

    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = {
                Text(
                    text = "댓글 삭제",
                    fontFamily = PaperlogyFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White
                )
            },
            text = {
                Text(
                    text = "댓글을 삭제하시겠습니까?",
                    fontFamily = PaperlogyFontFamily,
                    fontSize = 14.sp,
                    color = Color.White
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(target)
                    deleteTarget = null
                }) {
                    Text(text = "삭제", fontFamily = PaperlogyFontFamily, color = Color(0xFFFF4444), fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text(text = "취소", fontFamily = PaperlogyFontFamily, color = Color(0xFFAAAAAA))
                }
            },
            containerColor = Color(0xFF1A1A1A),
            titleContentColor = Color.White,
            textContentColor = Color.White
        )
    }

    reportTarget?.let { target ->
        ReportDiaryModal(
            diaryId = target.commentId,
            reportContent = reportContent,
            onContentChange = { reportContent = it.take(200) },
            onDismiss = {
                reportTarget = null
                reportContent = ""
            },
            onSubmit = {
                if (reportContent.isBlank() || isReporting) return@ReportDiaryModal
                isReporting = true
                viewModel.report(target, reportContent.trim()) { isReporting = false }
            },
            isLoading = isReporting
        )
    }

    if (showReportSuccess) {
        ReportSuccessModal(onDismiss = { showReportSuccess = false })
    }
}

/** LazyColumn 아이템 key 순서와 같은 순서의 댓글 id 목록 (스크롤 위치 계산용) */
private fun listKeys(state: CommentUiState): List<Long> = buildList {
    state.pending.filter { it.parentCommentId == null }.forEach { add(it.tempId) }
    state.comments.forEach { comment ->
        add(comment.commentId)
        val thread = state.threads[comment.commentId]
        if (thread?.isExpanded == true) {
            thread.replies.forEach { add(it.commentId) }
            state.pending.filter { it.parentCommentId == comment.commentId }.forEach { add(it.tempId) }
            if (thread.hasMore) add(MORE_REPLIES_KEY_OFFSET - comment.commentId)
        }
    }
}

private const val MORE_REPLIES_KEY_OFFSET = Long.MIN_VALUE / 2

private fun LazyListScope.commentListItems(
    state: CommentUiState,
    actions: CommentActions,
    viewModel: CommentViewModel
) {
    val highlightedId = when (val mode = state.inputMode) {
        is CommentInputMode.Reply -> mode.targetCommentId
        is CommentInputMode.Edit -> mode.comment.commentId
        CommentInputMode.Normal -> null
    }

    items(state.pending.filter { it.parentCommentId == null }, key = { it.tempId }) { pending ->
        PendingCommentRow(
            avatarUrl = pending.author?.profileImageUrl,
            name = pending.author?.username.orEmpty(),
            text = pending.text
        )
    }

    state.comments.forEach { comment ->
        val thread = state.threads[comment.commentId]
        val expanded = thread?.isExpanded == true

        item(key = comment.commentId) {
            CommentRow(
                comment = comment,
                actions = actions,
                isHighlighted = highlightedId == comment.commentId,
                footer = {
                    if (comment.replyCount > 0) {
                        ReplyToggle(
                            label = if (expanded) "답글 숨기기" else "답글 ${comment.replyCount}개 보기",
                            onClick = { viewModel.toggleReplies(comment.commentId) }
                        )
                    }
                }
            )
        }

        if (expanded) {
            items(thread!!.replies, key = { it.commentId }) { reply ->
                CommentRow(
                    comment = reply,
                    actions = actions,
                    isHighlighted = highlightedId == reply.commentId,
                    modifier = Modifier.padding(start = 45.dp)
                )
            }
            items(state.pending.filter { it.parentCommentId == comment.commentId }, key = { it.tempId }) { pending ->
                PendingCommentRow(
                    avatarUrl = pending.author?.profileImageUrl,
                    name = pending.author?.username.orEmpty(),
                    text = pending.text,
                    modifier = Modifier.padding(start = 45.dp)
                )
            }
            if (thread.isLoading) {
                item(key = "loading_${comment.commentId}") {
                    Box(modifier = Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = mainGreen, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    }
                }
            } else if (thread.hasMore) {
                item(key = MORE_REPLIES_KEY_OFFSET - comment.commentId) {
                    Box(modifier = Modifier.padding(start = 32.dp, bottom = 12.dp)) {
                        ReplyToggle(
                            label = "답글 더 보기",
                            onClick = { viewModel.loadReplies(comment.commentId) }
                        )
                    }
                }
            }
        }
    }

    if (state.isLoadingMore) {
        item(key = "loading_more") {
            Box(modifier = Modifier.fillMaxWidth().padding(12.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = mainGreen, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            }
        }
    }
}

@Composable
private fun CommentSheetHeader(commentCount: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(63.dp)
            .background(HeaderGreen)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp)
                .width(56.dp)
                .height(4.dp)
                .background(Color.White, RoundedCornerShape(2.dp))
        )
        Text(
            text = buildAnnotatedString {
                append("댓글 ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(commentCount.toString()) }
            },
            color = mainGreen,
            fontFamily = PaperlogyFontFamily,
            fontSize = 16.sp,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 33.dp, bottom = 13.dp)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(1.dp)
                .background(mainGreen.copy(alpha = 0.5f))
        )
    }
}

@Composable
private fun EmptyState() {
    Text(
        text = "아직 댓글이 없어요.\n첫 반응을 남겨주세요!",
        color = Color.White,
        fontFamily = PaperlogyFontFamily,
        fontSize = 12.sp,
        lineHeight = 23.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 102.dp)
    )
}

@Composable
private fun ErrorState(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 121.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        Text(
            text = "댓글을 불러오지 못했어요",
            color = Color.White,
            fontFamily = PaperlogyFontFamily,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(53.dp))
        Box(
            modifier = Modifier
                .background(mainGreen, RoundedCornerShape(4.dp))
                .clickable(onClick = onRetry)
                .padding(horizontal = 12.dp, vertical = 9.dp)
        ) {
            Text(text = "다시 시도", color = Color.Black, fontFamily = PaperlogyFontFamily, fontSize = 14.sp)
        }
    }
}
