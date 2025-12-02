package com.killingpart.killingpoint.data.remote

import android.content.Context
import com.killingpart.killingpoint.data.local.TokenStore
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8080/api/"
    
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
    
    // 기존 호환성을 위한 프로퍼티 (deprecated)
    @Deprecated("Use getApi(context) instead")
    val api: ApiService by lazy {
        throw IllegalStateException("Context is required. Use getApi(context) instead.")
    }
}

