package com.killingpart.killingpoint.data.remote

import com.killingpart.killingpoint.data.spotify.ItunesSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * iTunes Search API
 * https://itunes.apple.com/search
 */
interface SpotifyService {

    @GET("search")
    suspend fun searchTracks(
        @Query("term") term: String,
        @Query("country") country: String = "KR",
        @Query("entity") entity: String = "song",
        @Query("limit") limit: Int = 5
    ): ItunesSearchResponse
}
