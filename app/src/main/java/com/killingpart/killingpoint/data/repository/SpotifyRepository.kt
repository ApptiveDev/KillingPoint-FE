package com.killingpart.killingpoint.data.repository

import com.killingpart.killingpoint.data.remote.SpotifyService
import com.killingpart.killingpoint.data.spotify.ItunesSearchResponse
import com.killingpart.killingpoint.data.spotify.SimpleTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SpotifyRepository(
    private val service: SpotifyService
) {

    suspend fun searchTracks(query: String, market: String = "KR", limit: Int = 5): List<SimpleTrack> =
        withContext(Dispatchers.IO) {
            val res: ItunesSearchResponse = service.searchTracks(
                term = query,
                country = "KR",
                entity = "song",
                limit = limit
            )
            res.results.map { item ->
                SimpleTrack(
                    id = item.trackId.toString(),
                    title = item.trackName,
                    artist = item.artistName,
                    albumImageUrl = item.artworkUrl100,
                    albumId = item.collectionId?.toString() ?: ""
                )
            }
        }

    companion object {

        fun create(): SpotifyRepository {
            val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl("https://itunes.apple.com/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val service = retrofit.create(SpotifyService::class.java)
            return SpotifyRepository(service)
        }
    }
}
