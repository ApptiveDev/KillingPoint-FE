package com.killingpart.killingpoint.data.model

import androidx.compose.foundation.pager.PageSize
import com.google.gson.annotations.SerializedName

data class FeedDiary(
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
    val profileImageUrl: String
) {
    val toDiary: Diary
        get() = Diary(
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

data class FeedResponse(
    val content: List<FeedDiary>,
    val page: DiaryPage
)

data class RandomDiariesResponse(
    val content: List<FeedDiary>,
    val pageSize: Int
)