package com.killingpart.killingpoint.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object KakaoLoginClient {
    private fun Context.activity(): Activity? {
        var c = this
        while (c is ContextWrapper) {
            if (c is Activity) return c
            c = c.baseContext
        }
        return null
    }

    private suspend fun loginInternal(context: Context): String =
        suspendCancellableCoroutine { cont ->
            val act = context.activity() ?: run {
                cont.resumeWithException(IllegalStateException("No Activity context"))
                return@suspendCancellableCoroutine
            }
            val callback: (com.kakao.sdk.auth.model.OAuthToken?, Throwable?) -> Unit = { token, err ->
                when {
                    err != null -> {
                        if (err is ClientError && err.reason == ClientErrorCause.Cancelled) cont.cancel()
                        else cont.resumeWithException(err)
                    }
                    token != null -> cont.resume(token.accessToken)
                    else -> cont.resumeWithException(IllegalStateException("Token null"))
                }
            }
            if (UserApiClient.instance.isKakaoTalkLoginAvailable(act)) {
                UserApiClient.instance.loginWithKakaoTalk(act) { token, error ->
                    if (error != null) {
                        if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                            cont.cancel()
                        } else {
                            UserApiClient.instance.loginWithKakaoAccount(act, callback = callback)
                        }
                    } else {
                        callback(token, null)
                    }
                }
            } else {
                UserApiClient.instance.loginWithKakaoAccount(act, callback = callback)
            }
        }

    suspend fun getAccessToken(context: Context): String = loginInternal(context)
}