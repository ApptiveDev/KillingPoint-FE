package com.killingpart.killingpoint.ui.viewmodel

import android.app.Application
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.killingpart.killingpoint.data.model.Comment
import com.killingpart.killingpoint.data.model.CommentAuthor
import com.killingpart.killingpoint.data.model.CommentStatus
import com.killingpart.killingpoint.data.model.MAX_COMMENT_LENGTH
import com.killingpart.killingpoint.data.model.MAX_MENTION_COUNT
import com.killingpart.killingpoint.data.model.MentionCandidate
import com.killingpart.killingpoint.data.repository.AuthRepository
import com.killingpart.killingpoint.data.repository.CommentRepository
import com.killingpart.killingpoint.ui.screen.CommentScreen.CommentText
import com.killingpart.killingpoint.ui.screen.CommentScreen.MentionQuery
import com.killingpart.killingpoint.ui.screen.CommentScreen.MentionSpan
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class CommentLoadState { LOADING, ERROR, LOADED }

/** 답글 펼침 상태 (원댓글 id 기준) */
data class ReplyThread(
    val replies: List<Comment> = emptyList(),
    val isExpanded: Boolean = false,
    val isLoading: Boolean = false,
    val currentPage: Int = -1,
    val totalPages: Int = 0
) {
    val hasMore: Boolean get() = currentPage + 1 < totalPages
}

/** 서버 응답 전 화면에 먼저 보여주는 댓글 (투명도 50% + "등록중...") */
data class PendingComment(
    val tempId: Long,
    val parentCommentId: Long?,
    val text: String,
    val author: CommentAuthor?
)

sealed interface CommentInputMode {
    data object Normal : CommentInputMode
    data class Reply(val parentCommentId: Long, val targetCommentId: Long, val targetName: String?) : CommentInputMode
    data class Edit(val comment: Comment) : CommentInputMode
}

data class MentionSuggestState(
    val query: MentionQuery,
    val candidates: List<MentionCandidate> = emptyList(),
    val isLoading: Boolean = true,
    val isLimitReached: Boolean = false
)

data class CommentUiState(
    val loadState: CommentLoadState = CommentLoadState.LOADING,
    val commentCount: Int = 0,
    val comments: List<Comment> = emptyList(),
    val currentPage: Int = -1,
    val totalPages: Int = 0,
    val isLoadingMore: Boolean = false,
    val threads: Map<Long, ReplyThread> = emptyMap(),
    val pending: List<PendingComment> = emptyList(),
    val input: TextFieldValue = TextFieldValue(""),
    val mentionSpans: List<MentionSpan> = emptyList(),
    val inputMode: CommentInputMode = CommentInputMode.Normal,
    val mentionSuggest: MentionSuggestState? = null,
    val isSubmitting: Boolean = false
) {
    val hasMore: Boolean get() = currentPage + 1 < totalPages
    val canSubmit: Boolean get() = input.text.isNotBlank() && !isSubmitting
}

sealed interface CommentEvent {
    data class Message(val text: String) : CommentEvent
    /** 등록 후 해당 댓글로 스크롤 */
    data class ScrollTo(val commentId: Long) : CommentEvent
    data object ReportSuccess : CommentEvent
}

class CommentViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = CommentRepository(application)
    private val authRepo = AuthRepository(application)

    private val _state = MutableStateFlow(CommentUiState())
    val state: StateFlow<CommentUiState> = _state

    private val _events = MutableSharedFlow<CommentEvent>(extraBufferCapacity = 8)
    val events: SharedFlow<CommentEvent> = _events

    private var diaryId: Long? = null
    private var myAuthor: CommentAuthor? = null
    private var mentionJob: Job? = null
    private var tempIdSeq = -1L

    fun start(diaryId: Long) {
        if (this.diaryId == diaryId && _state.value.loadState == CommentLoadState.LOADED) return
        this.diaryId = diaryId
        loadMyProfile()
        refresh()
    }

    private fun loadMyProfile() {
        if (myAuthor != null) return
        viewModelScope.launch {
            val userId = authRepo.getUserIdFromToken() ?: return@launch
            authRepo.getUserInfo().onSuccess { user ->
                myAuthor = CommentAuthor(userId, user.username, user.tag, user.profileImageUrl)
            }
        }
    }

    fun refresh() {
        val id = diaryId ?: return
        _state.update { it.copy(loadState = CommentLoadState.LOADING) }
        viewModelScope.launch {
            repo.getComments(id, page = 0)
                .onSuccess { response ->
                    _state.update {
                        it.copy(
                            loadState = CommentLoadState.LOADED,
                            commentCount = response.commentCount,
                            comments = response.comments.content,
                            currentPage = 0,
                            totalPages = response.comments.page.totalPages,
                            threads = emptyMap()
                        )
                    }
                }
                .onFailure { _state.update { it.copy(loadState = CommentLoadState.ERROR) } }
        }
    }

    fun loadMore() {
        val id = diaryId ?: return
        val current = _state.value
        if (!current.hasMore || current.isLoadingMore || current.loadState != CommentLoadState.LOADED) return
        val nextPage = current.currentPage + 1
        _state.update { it.copy(isLoadingMore = true) }
        viewModelScope.launch {
            repo.getComments(id, page = nextPage)
                .onSuccess { response ->
                    _state.update { s ->
                        // 인기순이라 페이지 사이에 순서가 바뀔 수 있어 중복 제거
                        val known = s.comments.map { it.commentId }.toSet()
                        s.copy(
                            commentCount = response.commentCount,
                            comments = s.comments + response.comments.content.filter { it.commentId !in known },
                            currentPage = nextPage,
                            totalPages = response.comments.page.totalPages
                        )
                    }
                }
            _state.update { it.copy(isLoadingMore = false) }
        }
    }

    // ===== 답글 =====

    fun toggleReplies(parentCommentId: Long) {
        val thread = _state.value.threads[parentCommentId] ?: ReplyThread()
        if (thread.isExpanded) {
            updateThread(parentCommentId) { it.copy(isExpanded = false) }
            return
        }
        updateThread(parentCommentId) { it.copy(isExpanded = true) }
        if (thread.currentPage < 0) loadReplies(parentCommentId)
    }

    fun loadReplies(parentCommentId: Long) {
        val thread = _state.value.threads[parentCommentId] ?: ReplyThread()
        if (thread.isLoading) return
        val nextPage = thread.currentPage + 1
        updateThread(parentCommentId) { it.copy(isLoading = true) }
        viewModelScope.launch {
            repo.getReplies(parentCommentId, page = nextPage)
                .onSuccess { page ->
                    updateThread(parentCommentId) { t ->
                        val known = t.replies.map { it.commentId }.toSet()
                        t.copy(
                            replies = t.replies + page.content.filter { it.commentId !in known },
                            currentPage = nextPage,
                            totalPages = page.page.totalPages
                        )
                    }
                }
                .onFailure { emitMessage(it) }
            updateThread(parentCommentId) { it.copy(isLoading = false) }
        }
    }

    private fun updateThread(parentCommentId: Long, block: (ReplyThread) -> ReplyThread) {
        _state.update { s ->
            val thread = s.threads[parentCommentId] ?: ReplyThread()
            s.copy(threads = s.threads + (parentCommentId to block(thread)))
        }
    }

    // ===== 입력 =====

    fun onInputChange(value: TextFieldValue) {
        val limited = if (value.text.length > MAX_COMMENT_LENGTH) {
            val text = value.text.take(MAX_COMMENT_LENGTH)
            value.copy(text = text, selection = TextRange(minOf(value.selection.start, text.length)))
        } else value
        _state.update { s ->
            s.copy(
                input = limited,
                mentionSpans = CommentText.shiftSpans(s.input.text, limited.text, s.mentionSpans)
            )
        }
        updateMentionQuery()
    }

    private fun updateMentionQuery() {
        val s = _state.value
        val selection = s.input.selection
        val query = if (selection.collapsed) {
            CommentText.mentionQueryAt(s.input.text, selection.start, s.mentionSpans)
        } else null

        if (query == null) {
            mentionJob?.cancel()
            _state.update { it.copy(mentionSuggest = null) }
            return
        }
        if (s.mentionSuggest?.query == query) return

        val limitReached = CommentText.mentionedUserCount(s.mentionSpans) >= MAX_MENTION_COUNT
        _state.update {
            it.copy(
                mentionSuggest = MentionSuggestState(
                    query = query,
                    candidates = it.mentionSuggest?.candidates.orEmpty(),
                    isLoading = !limitReached,
                    isLimitReached = limitReached
                )
            )
        }
        if (limitReached) return

        mentionJob?.cancel()
        mentionJob = viewModelScope.launch {
            delay(MENTION_DEBOUNCE_MS)
            repo.getMentionCandidates(query.keyword)
                .onSuccess { candidates ->
                    _state.update { st ->
                        if (st.mentionSuggest?.query != query) st
                        else st.copy(mentionSuggest = st.mentionSuggest.copy(candidates = candidates, isLoading = false))
                    }
                }
                .onFailure {
                    _state.update { st ->
                        if (st.mentionSuggest?.query != query) st
                        else st.copy(mentionSuggest = st.mentionSuggest.copy(candidates = emptyList(), isLoading = false))
                    }
                }
        }
    }

    fun selectMention(candidate: MentionCandidate) {
        val s = _state.value
        val query = s.mentionSuggest?.query ?: return
        val alreadyMentioned = s.mentionSpans.any { it.userId == candidate.userId }
        if (!alreadyMentioned && CommentText.mentionedUserCount(s.mentionSpans) >= MAX_MENTION_COUNT) {
            _state.update { it.copy(mentionSuggest = it.mentionSuggest?.copy(isLimitReached = true)) }
            return
        }
        val (text, spans, cursor) = CommentText.applyMention(
            text = s.input.text,
            query = query,
            cursor = s.input.selection.start,
            spans = s.mentionSpans,
            userId = candidate.userId,
            username = candidate.username
        )
        mentionJob?.cancel()
        _state.update {
            it.copy(
                input = TextFieldValue(text, TextRange(cursor)),
                mentionSpans = spans,
                mentionSuggest = null
            )
        }
    }

    fun dismissMentionSuggest() {
        mentionJob?.cancel()
        _state.update { it.copy(mentionSuggest = null) }
    }

    /** 답글 달기. 답글에 다는 경우에도 원댓글 아래에 달리고, 대상 작성자를 자동 멘션한다 */
    fun startReply(target: Comment) {
        if (target.isDeleted && !target.isReply) return
        val parentId = target.parentCommentId ?: target.commentId
        val author = target.author
        val name = author?.username?.takeIf { !target.isBlocked && it.isNotBlank() }
        val (text, spans) = if (name != null && author.userId != myAuthor?.userId) {
            val prefix = "@$name "
            prefix to listOf(MentionSpan(0, name.length + 1, author.userId, name))
        } else {
            "" to emptyList()
        }
        _state.update {
            it.copy(
                inputMode = CommentInputMode.Reply(parentId, target.commentId, name),
                input = TextFieldValue(text, TextRange(text.length)),
                mentionSpans = spans,
                mentionSuggest = null
            )
        }
        if (!(_state.value.threads[parentId]?.isExpanded ?: false)) toggleReplies(parentId)
    }

    fun startEdit(comment: Comment) {
        val text = comment.text ?: return
        _state.update {
            it.copy(
                inputMode = CommentInputMode.Edit(comment),
                input = TextFieldValue(text, TextRange(text.length)),
                mentionSpans = CommentText.spansFromReferences(text, comment.references),
                mentionSuggest = null
            )
        }
    }

    fun cancelInputMode() {
        _state.update {
            it.copy(
                inputMode = CommentInputMode.Normal,
                input = TextFieldValue(""),
                mentionSpans = emptyList(),
                mentionSuggest = null
            )
        }
    }

    fun submit() {
        val id = diaryId ?: return
        val s = _state.value
        if (!s.canSubmit) return
        val (text, spans) = CommentText.trimWithSpans(s.input.text, s.mentionSpans)
        if (CommentText.mentionedUserCount(spans) > MAX_MENTION_COUNT) {
            _events.tryEmit(CommentEvent.Message("한 댓글에 최대 ${MAX_MENTION_COUNT}명까지 언급할 수 있어요."))
            return
        }
        val references = CommentText.toReferences(text, spans)

        when (val mode = s.inputMode) {
            is CommentInputMode.Edit -> submitEdit(mode.comment, text, spans, references)
            else -> submitCreate(id, mode, text, spans, references)
        }
    }

    private fun submitCreate(
        diaryId: Long,
        mode: CommentInputMode,
        text: String,
        spans: List<MentionSpan>,
        references: List<com.killingpart.killingpoint.data.model.MentionReferenceRequest>
    ) {
        val parentId = (mode as? CommentInputMode.Reply)?.parentCommentId
        val pending = PendingComment(tempIdSeq--, parentId, text, myAuthor)
        val previousInput = _state.value.input
        _state.update {
            it.copy(
                pending = it.pending + pending,
                isSubmitting = true,
                input = TextFieldValue(""),
                mentionSpans = emptyList(),
                mentionSuggest = null,
                inputMode = CommentInputMode.Normal
            )
        }
        viewModelScope.launch {
            repo.createComment(diaryId, text, parentId, references)
                .onSuccess { created ->
                    _state.update { st ->
                        val withoutPending = st.copy(pending = st.pending - pending)
                        if (parentId == null) {
                            withoutPending.copy(
                                comments = listOf(created) + st.comments,
                                commentCount = st.commentCount + 1
                            )
                        } else {
                            val thread = st.threads[parentId] ?: ReplyThread(isExpanded = true)
                            withoutPending.copy(
                                comments = st.comments.map { c ->
                                    if (c.commentId == parentId) c.copy(replyCount = c.replyCount + 1) else c
                                },
                                threads = st.threads + (parentId to thread.copy(
                                    replies = thread.replies + created,
                                    isExpanded = true
                                ))
                            )
                        }
                    }
                    _events.tryEmit(CommentEvent.ScrollTo(created.commentId))
                }
                .onFailure { e ->
                    // 실패하면 입력 내용을 되돌려 다시 보낼 수 있게 한다
                    _state.update { st ->
                        st.copy(
                            pending = st.pending - pending,
                            input = if (st.input.text.isEmpty()) previousInput else st.input,
                            mentionSpans = if (st.input.text.isEmpty()) spans else st.mentionSpans,
                            inputMode = if (st.input.text.isEmpty()) mode else st.inputMode
                        )
                    }
                    emitMessage(e)
                }
            _state.update { it.copy(isSubmitting = false) }
        }
    }

    private fun submitEdit(
        target: Comment,
        text: String,
        spans: List<MentionSpan>,
        references: List<com.killingpart.killingpoint.data.model.MentionReferenceRequest>
    ) {
        _state.update { it.copy(isSubmitting = true) }
        viewModelScope.launch {
            repo.updateComment(target.commentId, text, references)
                .onSuccess { updated ->
                    replaceComment(updated)
                    _state.update {
                        it.copy(
                            inputMode = CommentInputMode.Normal,
                            input = TextFieldValue(""),
                            mentionSpans = emptyList(),
                            mentionSuggest = null
                        )
                    }
                }
                .onFailure { emitMessage(it) }
            _state.update { it.copy(isSubmitting = false) }
        }
    }

    // ===== 댓글 액션 =====

    fun toggleLike(comment: Comment) {
        if (comment.isDeleted || comment.isBlocked) return
        val optimistic = comment.copy(
            isLiked = !comment.isLiked,
            likeCount = (comment.likeCount + if (comment.isLiked) -1 else 1).coerceAtLeast(0)
        )
        replaceComment(optimistic)
        viewModelScope.launch {
            repo.toggleLike(comment.commentId)
                .onSuccess { r -> replaceComment(optimistic.copy(isLiked = r.isLiked, likeCount = r.likeCount)) }
                .onFailure { e ->
                    replaceComment(comment)
                    emitMessage(e)
                }
        }
    }

    fun delete(comment: Comment) {
        viewModelScope.launch {
            repo.deleteComment(comment.commentId)
                .onSuccess {
                    _state.update { s ->
                        if (comment.isReply) {
                            val parentId = comment.parentCommentId!!
                            val thread = s.threads[parentId]
                            val comments = s.comments.mapNotNull { c ->
                                if (c.commentId != parentId) return@mapNotNull c
                                val replyCount = (c.replyCount - 1).coerceAtLeast(0)
                                // 답글이 모두 사라진 삭제 원댓글은 목록에서 빠진다
                                if (c.isDeleted && replyCount == 0) null else c.copy(replyCount = replyCount)
                            }
                            s.copy(
                                comments = comments,
                                threads = if (thread == null) s.threads else s.threads + (parentId to thread.copy(
                                    replies = thread.replies.filterNot { it.commentId == comment.commentId }
                                ))
                            )
                        } else {
                            val comments = if (comment.replyCount > 0) {
                                s.comments.map { c ->
                                    if (c.commentId == comment.commentId) c.asDeletedPlaceholder() else c
                                }
                            } else {
                                s.comments.filterNot { it.commentId == comment.commentId }
                            }
                            val wasCounted = !comment.isDeleted
                            s.copy(
                                comments = comments,
                                commentCount = if (wasCounted) (s.commentCount - 1).coerceAtLeast(0) else s.commentCount
                            )
                        }
                    }
                    val mode = _state.value.inputMode
                    if (mode is CommentInputMode.Edit && mode.comment.commentId == comment.commentId) cancelInputMode()
                }
                .onFailure { emitMessage(it) }
        }
    }

    fun report(comment: Comment, content: String, onDone: () -> Unit) {
        viewModelScope.launch {
            repo.reportComment(comment.commentId, content)
                .onSuccess { _events.tryEmit(CommentEvent.ReportSuccess) }
                .onFailure { emitMessage(it) }
            onDone()
        }
    }

    fun unblock(comment: Comment) {
        val userId = comment.author?.userId ?: return
        viewModelScope.launch {
            authRepo.unblockUser(userId)
                .onSuccess {
                    _events.tryEmit(CommentEvent.Message("차단을 해제했어요."))
                    refresh()
                }
                .onFailure { _events.tryEmit(CommentEvent.Message("차단 해제에 실패했어요.")) }
        }
    }

    private fun replaceComment(updated: Comment) {
        _state.update { s ->
            if (updated.parentCommentId == null) {
                s.copy(comments = s.comments.map { if (it.commentId == updated.commentId) updated else it })
            } else {
                val parentId = updated.parentCommentId
                val thread = s.threads[parentId] ?: return@update s
                s.copy(threads = s.threads + (parentId to thread.copy(
                    replies = thread.replies.map { if (it.commentId == updated.commentId) updated else it }
                )))
            }
        }
    }

    private fun Comment.asDeletedPlaceholder(): Comment = copy(
        status = CommentStatus.DELETED,
        text = null,
        attachment = null,
        author = null,
        isMine = false,
        isLiked = false,
        likeCount = 0
    )

    private fun emitMessage(e: Throwable) {
        _events.tryEmit(CommentEvent.Message(e.message ?: "요청을 처리하지 못했어요."))
    }

    companion object {
        private const val MENTION_DEBOUNCE_MS = 200L
    }
}
