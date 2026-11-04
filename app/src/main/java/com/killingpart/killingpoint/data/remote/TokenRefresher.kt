package com.killingpart.killingpoint.data.remote

import android.content.Context
import com.killingpart.killingpoint.data.local.TokenStore
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

sealed interface RefreshResult {
    /** isNew 는 실제로 jwt/exchange 를 호출했을 때만 채워진다 */
    data class Success(val accessToken: String, val isNew: Boolean?) : RefreshResult

    /** 리프레시 토큰이 없거나 서버가 거부(401/403) → 재로그인 필요. 토큰은 삭제됨 */
    data object InvalidRefreshToken : RefreshResult

    /** 네트워크 오류, 5xx 등 일시적 실패 → 토큰은 그대로 둔다 */
    data class TemporaryFailure(val reason: String) : RefreshResult
}

sealed interface ExchangeResponse {
    data class Ok(val accessToken: String, val refreshToken: String, val isNew: Boolean?) : ExchangeResponse
    data class HttpError(val code: Int, val body: String) : ExchangeResponse
}

/** 리프레시 토큰이 무효라 재로그인이 필요할 때 */
class RefreshTokenInvalidException : IllegalStateException("리프레시 토큰이 만료되었거나 유효하지 않습니다")

/**
 * 토큰 재발급을 한 번에 하나씩만 실행한다.
 *
 * 서버는 jwt/exchange 때마다 리프레시 토큰을 새로 발급하므로, 여러 요청이 동시에 401 을 받고
 * 같은 리프레시 토큰으로 각자 재발급하면 뒤늦은 쪽이 실패하면서 토큰을 지워 로그아웃되던 문제가 있었다.
 * - 재발급은 lock 안에서 직렬로 실행
 * - 실패한 요청의 액세스 토큰과 저장된 토큰이 다르면 이미 다른 요청이 재발급한 것이므로 저장된 토큰을 그대로 사용
 * - 토큰 삭제는 서버가 리프레시 토큰을 거부(401/403)했을 때만
 */
class TokenRefresher(
    private val loadAccessToken: () -> String?,
    private val loadRefreshToken: () -> String?,
    private val saveTokens: (accessToken: String, refreshToken: String) -> Unit,
    private val clearTokens: () -> Unit,
    private val exchange: (refreshToken: String) -> ExchangeResponse
) {
    private val lock = Any()

    /**
     * @param failedAccessToken 401 을 받은 요청이 보낸 액세스 토큰. null 이면 무조건 재발급한다 (앱 실행 시).
     */
    fun refresh(failedAccessToken: String?): RefreshResult = synchronized(lock) {
        if (failedAccessToken != null) {
            val current = loadAccessToken()
            if (current != null && current != failedAccessToken) {
                return RefreshResult.Success(current, isNew = null)
            }
        }

        val refreshToken = loadRefreshToken()?.takeIf { it.isNotBlank() }
            ?: return RefreshResult.InvalidRefreshToken

        val response = try {
            exchange(refreshToken)
        } catch (e: Exception) {
            return RefreshResult.TemporaryFailure(e.message ?: e.javaClass.simpleName)
        }

        when (response) {
            is ExchangeResponse.Ok -> {
                saveTokens(response.accessToken, response.refreshToken)
                RefreshResult.Success(response.accessToken, response.isNew)
            }
            is ExchangeResponse.HttpError -> {
                if (response.code == 401 || response.code == 403) {
                    // 그 사이 로그인 등으로 토큰이 바뀌었다면 새 토큰은 지우지 않는다
                    if (loadRefreshToken() == refreshToken) clearTokens()
                    RefreshResult.InvalidRefreshToken
                } else {
                    RefreshResult.TemporaryFailure("토큰 갱신 실패 (${response.code}): ${response.body}")
                }
            }
        }
    }

    companion object {
        @Volatile
        private var instance: TokenRefresher? = null

        fun get(context: Context): TokenRefresher =
            instance ?: synchronized(this) {
                instance ?: create(TokenStore(context.applicationContext)).also { instance = it }
            }

        private fun create(tokenStore: TokenStore): TokenRefresher {
            val client = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()
            return TokenRefresher(
                loadAccessToken = { tokenStore.getAccessTokenSync() },
                loadRefreshToken = { tokenStore.getRefreshTokenSync() },
                saveTokens = { access, refresh -> kotlinx.coroutines.runBlocking { tokenStore.save(access, refresh) } },
                clearTokens = { tokenStore.clearSync() },
                exchange = { refreshToken ->
                    val request = Request.Builder()
                        .url(RetrofitClient.BASE_URL + "jwt/exchange")
                        .header("X-Refresh-Token", refreshToken)
                        .post("".toRequestBody())
                        .build()
                    client.newCall(request).execute().use { res ->
                        val body = res.body?.string().orEmpty()
                        if (res.isSuccessful) {
                            val json = JSONObject(body)
                            ExchangeResponse.Ok(
                                accessToken = json.getString("accessToken"),
                                refreshToken = json.getString("refreshToken"),
                                isNew = if (json.has("isNew")) json.optBoolean("isNew") else null
                            )
                        } else {
                            ExchangeResponse.HttpError(res.code, body)
                        }
                    }
                }
            )
        }
    }
}
