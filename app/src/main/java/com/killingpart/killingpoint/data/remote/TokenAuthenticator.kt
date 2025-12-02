package com.killingpart.killingpoint.data.remote

import android.content.Context
import com.killingpart.killingpoint.data.local.TokenStore
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    private val context: Context
) : Authenticator {
    private val tokenStore = TokenStore(context)
    private val api = RetrofitClient.getApi(context)

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) {
            return null
        }

        val refreshToken = runBlocking { tokenStore.getRefreshToken() }
            ?: return null

        val newTokens = try {
            runBlocking {
                api.refreshAccessToken(refreshToken)
            }
        } catch (e: Exception) {
            runBlocking { tokenStore.clear() }
            return null
        }

        runBlocking {
            tokenStore.save(
                newTokens.accessToken,
                newTokens.refreshToken
            )
        }

        val newAccessToken = newTokens.accessToken

        return response.request.newBuilder()
            .header("Authorization", "Bearer $newAccessToken")
            .build()
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var res = response.priorResponse
        while (res != null) {
            count++
            res = res.priorResponse
        }
        return count
    }
}