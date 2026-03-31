package com.killingpart.killingpoint.data.model

import com.google.gson.annotations.SerializedName

enum class Scope {
    PUBLIC,
    PRIVATE,
    KILLING_PART
}

data class Diary(
    @SerializedName("diaryId")
    val id: Long? = null,
    val artist: String,
    @SerializedName("musicTitle")
    val musicTitle: String,
    @SerializedName("albumImageUrl")
    val albumImageUrl: String,
    val content: String,
    @SerializedName("videoUrl")
    val videoUrl: String,
    val scope: Scope,
    var duration: String,
    var start: String,
    var end: String,
    @SerializedName("totalDuration")
    val totalDuration: Int? = null, // YouTube 비디오 전체 길이 (초 단위)
    @SerializedName("createDate")
    val createDate: String,
    @SerializedName("updateDate")
    val updateDate: String,
    val isLiked: Boolean = false,
    val likeCount: Int = 0
)

data class DiaryPage(
    val size: Int,
    val number: Int,
    val totalElements: Int,
    val totalPages: Int
)

data class MyDiaries(
    val content: List<Diary>,
    val page: DiaryPage
)
