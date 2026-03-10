package com.killingpart.killingpoint.data.spotify

/**
 * iTunes Search API 응답 모델
 * https://itunes.apple.com/search
 */
data class ItunesSearchResponse(
    val resultCount: Int,
    val results: List<ItunesTrack>
)

data class ItunesTrack(
    val trackId: Long,
    val trackName: String,
    val artistName: String,
    val artworkUrl100: String?,
    val collectionId: Long?
)

/**
 * UI에서 공통으로 쓰는 단순화된 트랙 모델
 */
data class SimpleTrack(
    val id: String,
    val title: String,
    val artist: String,
    val albumImageUrl: String?,
    val albumId: String
)

