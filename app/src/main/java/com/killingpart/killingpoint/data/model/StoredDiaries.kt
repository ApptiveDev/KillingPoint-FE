package com.killingpart.killingpoint.data.model

import com.google.gson.annotations.SerializedName


data class StoredDiary(
    @SerializedName("diaryId")
    val diaryId: Long,
    val artist: String,
    @SerializedName("musicTitle")
    val musicTitle: String,
    @SerializedName("albumImageUrl")
    val albumImageUrl: String,
    @SerializedName("videoUrl")
    val videoUrl: String,
    val duration: String,
    @SerializedName("totalDuration")
    val totalDuration: String?,
    val start: String,
    val end: String,
    @SerializedName("originalAuthorTag")
    val originalAuthorTag: String?
) {
    val toDiary: Diary
        get() = Diary(
            id = diaryId,
            artist = artist,
            musicTitle = musicTitle,
            albumImageUrl = albumImageUrl,
            content = "",
            videoUrl = videoUrl,
            scope = Scope.KILLING_PART,
            duration = duration,
            start = start,
            end = end,
            totalDuration = totalDuration?.toIntOrNull(),
            createDate = "",
            updateDate = ""
        )
}

data class StoredDiariesResponse(
    val content: List<StoredDiary>,
    val page: DiaryPage
)

