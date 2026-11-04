package com.killingpart.killingpoint.data.model

import com.google.gson.annotations.SerializedName

/**
 * GET /api/diaries/{diaryId} 단건 조회 응답
 */
data class DiaryDetail(
    @SerializedName("diaryId")
    val diaryId: Long,
    val artist: String,
    @SerializedName("musicTitle")
    val musicTitle: String,
    @SerializedName("albumImageUrl")
    val albumImageUrl: String,
    val content: String,
    @SerializedName("videoUrl")
    val videoUrl: String,
    val scope: Scope,
    val duration: String,
    @SerializedName("totalDuration")
    val totalDuration: String?,
    val start: String,
    val end: String,
    @SerializedName("createDate")
    val createDate: String,
    @SerializedName("updateDate")
    val updateDate: String,
    val isLiked: Boolean,
    @SerializedName("isStored")
    val isStored: Boolean,
    val likeCount: Int,
    val userId: Long,
    val username: String,
    val tag: String,
    val profileImageUrl: String,
    // 댓글 미리보기. 서버는 항상 내려주지만 구버전 응답 대비 nullable
    val commentPreview: CommentPreview? = null
) {
    fun toDiary(): Diary = Diary(
        id = diaryId,
        artist = artist,
        musicTitle = musicTitle,
        albumImageUrl = albumImageUrl,
        content = content,
        videoUrl = videoUrl,
        scope = scope,
        duration = duration,
        start = start,
        end = end,
        totalDuration = totalDuration?.toIntOrNull(),
        createDate = createDate,
        updateDate = updateDate,
        isLiked = isLiked,
        likeCount = likeCount
    )
}
