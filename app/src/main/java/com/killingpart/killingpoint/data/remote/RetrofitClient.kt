package com.killingpart.killingpoint.data.remote

import android.content.Context
import com.google.gson.internal.GsonBuildConfig
import com.killingpart.killingpoint.data.local.TokenStore
import com.killingpart.killingpoint.ui.screen.MainScreen.YouTubePlayerBox
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    const val BASE_URL = "https://music.jinwon.click/api/"
    const val PLAYER_URL = "https://api.jinwon.click/api/"
    
    private var _api: ApiService? = null
    
    fun getApi(context: Context): ApiService {
        if (_api == null) {
            val tokenStore = TokenStore(context.applicationContext)
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            val client = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(tokenStore))
                .authenticator(TokenAuthenticator(context))
                .addInterceptor(logging)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()

            _api = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
        return _api!!
    }

    fun getYoutubeApi() : ApiService {
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        val youtubeApi = Retrofit.Builder()
            .baseUrl(PLAYER_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)

        return youtubeApi
    }

    @Deprecated("Use getApi(context) instead")
    val api: ApiService by lazy {
        throw IllegalStateException("Context is required. Use getApi(context) instead.")
    }
}

