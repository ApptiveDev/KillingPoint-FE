package com.killingpart.killingpoint.data.remote

import com.killingpart.killingpoint.data.local.TokenStore
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class AuthInterceptor(
    private val tokenStore: TokenStore
) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.toString()

        if(url.contains("/oauth2/kakao")
            || url.contains("/oauth2/test")
            || url.contains("/jwt/exchange"))
        {
            return chain.proceed(request)
        }

        val accessToken = tokenStore.getAccessTokenSync()
        val requestWithAuth = if (accessToken != null) {
            request.newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .build()
        } else {
            request
        }

        val response = chain.proceed(requestWithAuth)

        return response
    }

}
