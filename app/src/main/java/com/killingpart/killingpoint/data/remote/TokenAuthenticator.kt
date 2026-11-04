package com.killingpart.killingpoint.data.remote

import android.content.Context
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

/**
 * 401 응답 시 액세스 토큰을 재발급하고 요청을 한 번 다시 보낸다.
 * 재발급 자체는 [TokenRefresher] 가 직렬로 처리한다.
 */
class TokenAuthenticator(
    context: Context
) : Authenticator {
    private val refresher = TokenRefresher.get(context)

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) return null
        if (response.request.url.encodedPath.endsWith("/jwt/exchange")) return null

        val failedAccessToken = response.request.header("Authorization")
            ?.removePrefix("Bearer")
            ?.trim()
            ?.takeIf { it.isNotEmpty() }

        return when (val result = refresher.refresh(failedAccessToken)) {
            is RefreshResult.Success -> response.request.newBuilder()
                .header("Authorization", "Bearer ${result.accessToken}")
                .build()
            RefreshResult.InvalidRefreshToken,
            is RefreshResult.TemporaryFailure -> null
        }
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
