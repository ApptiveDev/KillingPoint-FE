package com.killingpart.killingpoint.data.repository

import android.content.Context
import com.killingpart.killingpoint.data.local.TokenStore
import com.killingpart.killingpoint.data.model.Comment
import com.killingpart.killingpoint.data.model.CommentLikeResponse
import com.killingpart.killingpoint.data.model.CommentListResponse
import com.killingpart.killingpoint.data.model.CommentPage
import com.killingpart.killingpoint.data.model.CommentReportRequest
import com.killingpart.killingpoint.data.model.CreateCommentRequest
import com.killingpart.killingpoint.data.model.MentionCandidate
import com.killingpart.killingpoint.data.model.MentionReferenceRequest
import com.killingpart.killingpoint.data.model.UpdateCommentRequest
import com.killingpart.killingpoint.data.remote.ApiService
import com.killingpart.killingpoint.data.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.HttpException

/**
 * 댓글 API 실패. message 는 서버가 내려준 사용자용 문구 (예: "이미 신고한 댓글입니다.")
 */
class CommentApiException(
    val code: Int,
    override val message: String
) : Exception(message)

class CommentRepository(
    private val context: Context,
    private val api: ApiService = RetrofitClient.getApi(context),
    private val tokenStore: TokenStore = TokenStore(context.applicationContext)
) {

    suspend fun getComments(diaryId: Long, page: Int = 0, size: Int = PAGE_SIZE): Result<CommentListResponse> =
        call("댓글 목록 조회") { token -> api.getComments(token, diaryId, page, size) }

    suspend fun getReplies(commentId: Long, page: Int = 0, size: Int = PAGE_SIZE): Result<CommentPage> =
        call("답글 목록 조회") { token -> api.getReplies(token, commentId, page, size) }

    suspend fun createComment(
        diaryId: Long,
        text: String,
        parentCommentId: Long? = null,
        references: List<MentionReferenceRequest> = emptyList()
    ): Result<Comment> = call("댓글 작성") { token ->
        api.createComment(
            token,
            diaryId,
            CreateCommentRequest(
                text = text,
                parentCommentId = parentCommentId,
                references = references.ifEmpty { null }
            )
        )
    }

    /** references 는 전체 교체. 멘션을 모두 지우려면 빈 리스트 */
    suspend fun updateComment(
        commentId: Long,
        text: String,
        references: List<MentionReferenceRequest> = emptyList()
    ): Result<Comment> = call("댓글 수정") { token ->
        api.updateComment(token, commentId, UpdateCommentRequest(text = text, references = references))
    }

    suspend fun deleteComment(commentId: Long): Result<Unit> = call("댓글 삭제") { token ->
        val response = api.deleteComment(token, commentId)
        if (!response.isSuccessful) throw toApiException(response.code(), response.errorBody()?.string())
    }

    suspend fun toggleLike(commentId: Long): Result<CommentLikeResponse> =
        call("댓글 좋아요") { token -> api.toggleCommentLike(token, commentId) }

    suspend fun reportComment(commentId: Long, content: String): Result<Unit> = call("댓글 신고") { token ->
        val response = api.reportComment(token, commentId, CommentReportRequest(content))
        if (!response.isSuccessful) throw toApiException(response.code(), response.errorBody()?.string())
    }

    suspend fun getMentionCandidates(keyword: String?, size: Int = 10): Result<List<MentionCandidate>> =
        call("멘션 후보 조회") { token ->
            api.getMentionCandidates(token, keyword?.takeIf { it.isNotBlank() }, size)
        }

    private suspend fun <T> call(label: String, block: suspend (String) -> T): Result<T> =
        withContext(Dispatchers.IO) {
            runCatching {
                val accessToken = tokenStore.getAccessToken()
                    ?: throw IllegalStateException("액세스 토큰이 없습니다")
                block("Bearer $accessToken")
            }.recoverCatching { e ->
                android.util.Log.e("CommentRepository", "$label 실패: ${e.message}")
                if (e is HttpException) {
                    throw toApiException(e.code(), e.response()?.errorBody()?.string())
                } else {
                    throw e
                }
            }
        }

    /**
     * 에러 본문은 {"message": "..."} 형식이고, 바디 유효성 실패(400)만
     * {"fieldErrors": [{"text": "..."}]} 형식이다.
     */
    private fun toApiException(code: Int, body: String?): CommentApiException {
        val parsed = runCatching {
            val json = JSONObject(body.orEmpty())
            json.optString("message").takeIf { it.isNotBlank() }
                ?: json.optJSONArray("fieldErrors")?.optJSONObject(0)?.let { fieldError ->
                    fieldError.keys().asSequence().firstOrNull()?.let { key -> fieldError.optString(key) }
                }
        }.getOrNull()
        return CommentApiException(code, parsed?.takeIf { it.isNotBlank() } ?: "요청을 처리하지 못했어요. ($code)")
    }

    companion object {
        const val PAGE_SIZE = 20
    }
}
