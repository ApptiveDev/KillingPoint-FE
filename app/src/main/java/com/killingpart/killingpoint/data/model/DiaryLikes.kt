package com.killingpart.killingpoint.data.model

import com.google.gson.annotations.SerializedName

data class DiaryLikeUser(
    @SerializedName("userId")
    val userId: Long,
    val username: String,
    val tag: String,
    val identifier: String,
    val profileImageUrl: String
)

data class DiaryLikesResponse(
    val content: List<DiaryLikeUser>,
    val page: DiaryPage
)

