package com.killingpart.killingpoint

import com.killingpart.killingpoint.data.remote.ExchangeResponse
import com.killingpart.killingpoint.data.remote.RefreshResult
import com.killingpart.killingpoint.data.remote.TokenRefresher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class TokenRefresherTest {

    /** 서버처럼 재발급마다 리프레시 토큰을 교체하고, 이전 리프레시 토큰은 거부한다 */
    private class FakeServerAndStore(var access: String? = "at0", var refresh: String? = "rt0") {
        val exchangeCount = AtomicInteger()
        var validRefresh = "rt0"
        var nextError: Int? = null
        var throwIo = false

        fun refresher() = TokenRefresher(
            loadAccessToken = { access },
            loadRefreshToken = { refresh },
            saveTokens = { a, r -> access = a; refresh = r },
            clearTokens = { access = null; refresh = null },
            exchange = exchange@{ rt ->
                Thread.sleep(20)
                if (throwIo) throw IOException("timeout")
                nextError?.let { return@exchange ExchangeResponse.HttpError(it, "") }
                if (rt != validRefresh) return@exchange ExchangeResponse.HttpError(401, "invalid")
                val n = exchangeCount.incrementAndGet()
                validRefresh = "rt$n"
                ExchangeResponse.Ok("at$n", "rt$n", isNew = false)
            }
        )
    }

    @Test
    fun concurrent401s_refreshOnce_andKeepTokens() {
        val fake = FakeServerAndStore()
        val refresher = fake.refresher()
        val pool = Executors.newFixedThreadPool(5)
        val start = CountDownLatch(1)
        val results = (1..5).map {
            pool.submit<RefreshResult> {
                start.await()
                refresher.refresh(failedAccessToken = "at0")
            }
        }
        start.countDown()
        val all = results.map { it.get(5, TimeUnit.SECONDS) }
        pool.shutdown()

        assertEquals(1, fake.exchangeCount.get())
        assertTrue(all.all { it is RefreshResult.Success && it.accessToken == "at1" })
        assertEquals("at1", fake.access)
        assertEquals("rt1", fake.refresh)
    }

    @Test
    fun serverError_keepsTokens() {
        val fake = FakeServerAndStore().apply { nextError = 502 }
        val result = fake.refresher().refresh(failedAccessToken = "at0")
        assertTrue(result is RefreshResult.TemporaryFailure)
        assertEquals("at0", fake.access)
        assertEquals("rt0", fake.refresh)
    }

    @Test
    fun networkError_keepsTokens() {
        val fake = FakeServerAndStore().apply { throwIo = true }
        val result = fake.refresher().refresh(failedAccessToken = null)
        assertTrue(result is RefreshResult.TemporaryFailure)
        assertEquals("rt0", fake.refresh)
    }

    @Test
    fun rejectedRefreshToken_clearsTokens() {
        val fake = FakeServerAndStore().apply { nextError = 401 }
        val result = fake.refresher().refresh(failedAccessToken = null)
        assertEquals(RefreshResult.InvalidRefreshToken, result)
        assertNull(fake.access)
        assertNull(fake.refresh)
    }

    @Test
    fun noRefreshToken_isInvalid() {
        val fake = FakeServerAndStore(access = null, refresh = null)
        assertEquals(RefreshResult.InvalidRefreshToken, fake.refresher().refresh(null))
        assertEquals(0, fake.exchangeCount.get())
    }

    @Test
    fun launchRefresh_alwaysExchanges_andReturnsIsNew() {
        val fake = FakeServerAndStore()
        val result = fake.refresher().refresh(failedAccessToken = null)
        assertEquals(RefreshResult.Success("at1", isNew = false), result)
    }
}
