package com.killingpart.killingpoint.data.model

data class YouTubeVideo(
    val id: String,
    val title: String,
    val duration: Int,
)

data class YoutubeVideoRequest(
    val title: String,
    val artist: String
)