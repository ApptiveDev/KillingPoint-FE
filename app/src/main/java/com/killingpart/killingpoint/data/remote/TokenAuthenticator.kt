package com.killingpart.killingpoint.data.remote

import android.content.Context
import com.killingpart.killingpoint.data.local.TokenStore
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import org.json.JSONObject

class TokenAuthenticator(
    private val context: Context
) : Authenticator {
    private val tokenStore = TokenStore(context)

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) {
            return null
        }

        val refreshToken = runBlocking { tokenStore.getRefreshToken() }
            ?: return null

        val refreshUrl = RetrofitClient.BASE_URL + "jwt/exchange"

        val client = OkHttpClient()

        val refreshRequest = Request.Builder()
            .url(refreshUrl)
            .header("X-Refresh-Token", refreshToken)
            .post("".toRequestBody())
            .build()

        val refreshResponse = try {
            client.newCall(refreshRequest).execute()
        } catch (e: Exception) {
            return null
        }

        if (!refreshResponse.isSuccessful) {
            runBlocking { tokenStore.clear() }
            return null
        }

        val body = refreshResponse.body?.string() ?: return null
        val json = JSONObject(body)

        val newAccess = json.getString("accessToken")
        val newRefresh = json.getString("refreshToken")

        runBlocking { tokenStore.save(newAccess, newRefresh) }

        return response.request.newBuilder()
            .header("Authorization", "Bearer $newAccess")
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