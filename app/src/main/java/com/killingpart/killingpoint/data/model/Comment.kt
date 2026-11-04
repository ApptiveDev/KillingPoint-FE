package com.killingpart.killingpoint.data.model

/**
 * 댓글 객체 (CommentResponse)
 * 목록 조회, 답글 목록 조회, 작성, 수정 응답에 공통으로 쓰인다.
 * status 가 DELETED 이거나 isBlocked 가 true 면 text/author 가 마스킹되어 내려온다.
 */
data class Comment(
    val commentId: Long,
    val diaryId: Long,
    val parentCommentId: Long? = null,
    val status: CommentStatus = CommentStatus.ACTIVE,
    val isBlocked: Boolean = false,
    val text: String? = null,
    val attachment: CommentAttachment? = null,
    val author: CommentAuthor? = null,
    val isMine: Boolean = false,
    val isLiked: Boolean = false,
    val likeCount: Int = 0,
    val replyCount: Int = 0,
    val createDate: String,
    val isEdited: Boolean = false
) {
    val isDeleted: Boolean get() = status == CommentStatus.DELETED
    val isReply: Boolean get() = parentCommentId != null
    val references: List<CommentReference> get() = attachment?.references.orEmpty()
}

enum class CommentStatus {
    ACTIVE,
    DELETED
}

data class CommentAttachment(
    val references: List<CommentReference>? = null
)

data class CommentReference(
    val targetType: String = MENTION_TARGET_USER,
    val targetId: Long,
    val atOrders: List<Int>,
    val len: Int,
    val display: CommentAuthorDisplay? = null
)

data class CommentAuthorDisplay(
    val username: String? = null,
    val tag: String? = null,
    val profileImageUrl: String? = null
)

data class CommentAuthor(
    val userId: Long,
    val username: String? = null,
    val tag: String? = null,
    val profileImageUrl: String? = null
)

data class CommentPage(
    val content: List<Comment>,
    val page: DiaryPage
)

/**
 * GET /api/diaries/{diaryId}/comments
 * 상단 "댓글 N" 은 page.totalElements 가 아니라 commentCount 를 쓴다 (삭제 플레이스홀더 제외 값).
 */
data class CommentListResponse(
    val commentCount: Int,
    val comments: CommentPage
)

/** 멘션 요청용. 응답의 CommentReference 와 달리 display 가 없다. */
data class MentionReferenceRequest(
    val targetType: String = MENTION_TARGET_USER,
    val targetId: Long,
    val atOrders: List<Int>,
    val len: Int
)

data class CreateCommentRequest(
    val text: String,
    val parentCommentId: Long? = null,
    val references: List<MentionReferenceRequest>? = null
)

data class UpdateCommentRequest(
    val text: String,
    val references: List<MentionReferenceRequest>? = null
)

data class CommentLikeResponse(
    val isLiked: Boolean,
    val likeCount: Int
)

data class CommentReportRequest(
    val content: String
)

/** GET /api/comments/mention-candidates 응답 항목 (내가 픽한 유저 중 검색) */
data class MentionCandidate(
    val userId: Long,
    val username: String,
    val tag: String,
    val profileImageUrl: String? = null
)

/**
 * 일기 조회 응답에 추가된 댓글 미리보기
 * comments 는 최신순 상위 3개 원댓글이며 content 는 서버에서 30자로 잘려서 온다.
 */
data class CommentPreview(
    val commentCount: Int = 0,
    val comments: List<CommentPreviewItem> = emptyList()
)

data class CommentPreviewItem(
    val commentId: Long,
    val userId: Long,
    val profileImageUrl: String? = null,
    val content: String
)

const val MENTION_TARGET_USER = "USER"
const val MAX_MENTION_COUNT = 5
const val MAX_COMMENT_LENGTH = 500
