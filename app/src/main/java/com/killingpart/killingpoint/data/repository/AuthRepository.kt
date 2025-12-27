package com.killingpart.killingpoint.data.repository

import android.R
import android.content.Context
import com.killingpart.killingpoint.data.local.TokenStore
import com.killingpart.killingpoint.data.model.KakaoAuthRequest
import com.killingpart.killingpoint.data.model.KakaoAuthResponse
import com.killingpart.killingpoint.data.model.TestAuthResponse
import com.killingpart.killingpoint.data.model.MyDiaries
import com.killingpart.killingpoint.data.model.UserInfo
import com.killingpart.killingpoint.data.model.YouTubeVideo
import com.killingpart.killingpoint.data.model.CreateDiaryRequest
import com.killingpart.killingpoint.data.model.Diary
import com.killingpart.killingpoint.data.model.UpdateTagRequest
import com.killingpart.killingpoint.data.model.PresignedUrlResponse
import com.killingpart.killingpoint.data.model.UpdateProfileImageRequest
import com.killingpart.killingpoint.data.model.YoutubeVideoRequest
import com.killingpart.killingpoint.data.remote.RetrofitClient
import com.killingpart.killingpoint.data.remote.ApiService
import com.killingpart.killingpoint.ui.screen.MainScreen.YouTubePlayerBox
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Retrofit
import java.io.File

class AuthRepository(
    private val context: Context,
    private val api: ApiService = RetrofitClient.getApi(context),
    private val youtubeApi: ApiService = RetrofitClient.getYoutubeApi(),
    private val tokenStore: TokenStore = TokenStore(context.applicationContext)
) {
    /**
     *  카카오 accessToken을 받아서:
     *   1) 우리 서버 /auth/kakao 로 교환
     *   2) 우리 서버 access/refresh 토큰 저장
     */
    suspend fun exchangeKakaoAccessToken(kakaoAccessToken: String): Result<Boolean> = // isNew 반환
        withContext(Dispatchers.IO) {
            runCatching {
                val res: KakaoAuthResponse = api.loginWithKakao(KakaoAuthRequest(kakaoAccessToken))
                tokenStore.save(res.accessToken, res.refreshToken)
                res.isNew // isNew 반환
            }.recoverCatching { e ->
                if (e is HttpException) {
                    val code = e.code()
                    val msg = e.response()?.errorBody()?.string().orEmpty()
                    throw IllegalStateException("로그인 실패 ($code): $msg")
                } else {
                    throw e
                }
            }
        }

    /**
     * 테스터 로그인:
     *   1) 우리 서버 /oauth2/test 호출
     *   2) 우리 서버 access/refresh 토큰 저장
     */
    suspend fun loginWithTest(): Result<Boolean> = // isNew 반환
        withContext(Dispatchers.IO) {
            runCatching {
                val res: TestAuthResponse = api.loginWithTest()
                tokenStore.save(res.accessToken, res.refreshToken)
                res.isNew // isNew 반환
            }.recoverCatching { e ->
                if (e is HttpException) {
                    val code = e.code()
                    val msg = e.response()?.errorBody()?.string().orEmpty()
                    throw IllegalStateException("테스터 로그인 실패 ($code): $msg")
                } else {
                    throw e
                }
            }
        }

    suspend fun getAccessToken(): String? = tokenStore.getAccessToken()
    suspend fun getRefreshToken(): String? = tokenStore.getRefreshToken()
    suspend fun clearTokens() = tokenStore.clear()

    suspend fun getUserInfo(): Result<UserInfo> =
        withContext(Dispatchers.IO) {
            runCatching {
                val accessToken = getAccessToken() 
                    ?: throw IllegalStateException("액세스 토큰이 없습니다")
                api.getUserInfo("Bearer $accessToken")
            }.recoverCatching { e ->
                if (e is HttpException) {
                    val code = e.code()
                    val msg = e.response()?.errorBody()?.string().orEmpty()
                    throw IllegalStateException("사용자 정보 조회 실패 ($code): $msg")
                } else {
                    throw e
                }
            }
        }

    suspend fun refreshAccessToken(): Result<Boolean> = // isNew 반환
        withContext(Dispatchers.IO) {
            runCatching {
                val refreshToken = getRefreshToken()
                    ?: throw IllegalStateException("리프레시 토큰이 없습니다")
                val response = api.refreshAccessToken(refreshToken)
                tokenStore.save(response.accessToken, response.refreshToken)
                response.isNew // isNew 반환
            }.recoverCatching { e ->
                if (e is HttpException) {
                    val code = e.code()
                    val msg = e.response()?.errorBody()?.string().orEmpty()
                    // 토큰 갱신 실패 시 토큰 삭제
                    clearTokens()
                    throw IllegalStateException("토큰 갱신 실패 ($code): $msg")
                } else {
                    throw e
                }
            }
        }

    suspend fun searchVideos(title: String, artist: String): List<YouTubeVideo> =
        withContext(Dispatchers.IO) {
            try {
                val result = youtubeApi.searchVideos(YoutubeVideoRequest(title, artist))
                result.forEachIndexed { index, video ->
                    android.util.Log.d("AuthRepository", "  비디오[$index]: id=${video.id}, duration=${video.duration}")
                }
                result
            } catch (e: HttpException) {
                val code = e.code()
                val msg = e.response()?.errorBody()?.string().orEmpty()
                throw IllegalStateException("비디오 검색 실패 ($code): $msg")
            }
        }


    suspend fun getMyDiaries(page: Int = 0, size: Int = 10): MyDiaries =
        withContext(Dispatchers.IO) {
            try {
                val accessToken = getAccessToken() 
                    ?: throw IllegalStateException("액세스 토큰이 없습니다")
                val result = api.getMyDiaries("Bearer $accessToken", page, size)

                result.content.forEachIndexed { index, diary ->
                    android.util.Log.d("AuthRepository", "Diary[$index]: id=${diary.id}, title=${diary.musicTitle}, artist=${diary.artist}, totalDuration=${diary.totalDuration}")
                }
                
                result
            } catch (e: HttpException) {
                val code = e.code()
                val msg = e.response()?.errorBody()?.string().orEmpty()
                throw IllegalStateException("다이어리 조회 실패 ($code): $msg")
            }
        }

    suspend fun createDiary(body: CreateDiaryRequest) = withContext(Dispatchers.IO) {
        try {
            val accessToken = getAccessToken() ?: throw IllegalStateException("액세스 토큰이 없습니다")
            val response = api.createDiary("Bearer $accessToken", body)
            if (!response.isSuccessful) {
                throw IllegalStateException("일기 작성 실패 (${response.code()}): ${response.message()}")
            }
        } catch (e: HttpException) {
            val code = e.code()
            val msg = e.response()?.errorBody()?.string().orEmpty()
            throw IllegalStateException("일기 작성 실패 ($code): $msg")
        }
    }

    suspend fun updateDiary(diaryId: Long, body: CreateDiaryRequest) = withContext(Dispatchers.IO) {
        try {
            val accessToken = getAccessToken() ?: throw IllegalStateException("액세스 토큰이 없습니다")
            val response = api.updateDiary("Bearer $accessToken", diaryId, body)
            if (!response.isSuccessful) {
                throw IllegalStateException("일기 수정 실패 (${response.code()}): ${response.message()}")
            }
        } catch (e: HttpException) {
            val code = e.code()
            val msg = e.response()?.errorBody()?.string().orEmpty()
            throw IllegalStateException("일기 수정 실패 ($code): $msg")
        }
    }

    suspend fun deleteDiary(diaryId: Long) = withContext(Dispatchers.IO) {
        try {
            val accessToken = getAccessToken() ?: throw IllegalStateException("액세스 토큰이 없습니다")
            val response = api.deleteDiary("Bearer $accessToken", diaryId)
            if (!response.isSuccessful) {
                throw IllegalStateException("일기 삭제 실패 (${response.code()}): ${response.message()}")
            }
        } catch (e: HttpException) {
            val code = e.code()
            val msg = e.response()?.errorBody()?.string().orEmpty()
            throw IllegalStateException("일기 삭제 실패 ($code): $msg")
        }
    }

    suspend fun updateTag(tag: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val accessToken = getAccessToken() 
                ?: throw IllegalStateException("액세스 토큰이 없습니다")
            val response = api.updateTag("Bearer $accessToken", UpdateTagRequest(tag))
            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string().orEmpty()
                throw IllegalStateException("태그 업데이트 실패 (${response.code()}): $errorBody")
            }
        }.recoverCatching { e ->
            if (e is HttpException) {
                val code = e.code()
                val msg = e.response()?.errorBody()?.string().orEmpty()
                throw IllegalStateException("태그 업데이트 실패 ($code): $msg")
            } else {
                throw e
            }
        }
    }

    /**
     * PresignedUrl 발급
     */
    suspend fun getPresignedUrl(): Result<PresignedUrlResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val accessToken = getAccessToken()
                ?: throw IllegalStateException("액세스 토큰이 없습니다")
            api.getPresignedUrl("Bearer $accessToken")
        }.recoverCatching { e ->
            if (e is HttpException) {
                val code = e.code()
                val msg = e.response()?.errorBody()?.string().orEmpty()
                throw IllegalStateException("PresignedUrl 발급 실패 ($code): $msg")
            } else {
                throw e
            }
        }
    }

    /**
     * S3에 이미지 업로드 (PUT 요청)
     * @param presignedUrl 쿼리파라미터 포함된 presignedUrl
     * @param imageFile 업로드할 이미지 파일
     */
    suspend fun uploadImageToS3(presignedUrl: String, imageFile: File): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val client = OkHttpClient()
            val mediaType = "image/*".toMediaType()
            val requestBody = imageFile.asRequestBody(mediaType)
            
            val request = Request.Builder()
                .url(presignedUrl)
                .put(requestBody)
                .build()
            
            val response = client.newCall(request).execute()
            
            if (!response.isSuccessful) {
                throw IllegalStateException("S3 업로드 실패 (${response.code}): ${response.message}")
            }
        }
    }

    /**
     * 프로필 이미지 변경
     * @param id PresignedUrl 발급 시 받은 TemporalFile의 PK
     * @param presignedUrl 쿼리파라미터를 제거한 presignedUrl
     */
    suspend fun updateProfileImage(id: Long, presignedUrl: String): Result<UserInfo> = withContext(Dispatchers.IO) {
        runCatching {
            val accessToken = getAccessToken()
                ?: throw IllegalStateException("액세스 토큰이 없습니다")
            api.updateProfileImage("Bearer $accessToken", UpdateProfileImageRequest(id, presignedUrl))
        }.recoverCatching { e ->
            if (e is HttpException) {
                val code = e.code()
                val msg = e.response()?.errorBody()?.string().orEmpty()
                throw IllegalStateException("프로필 이미지 변경 실패 ($code): $msg")
            } else {
                throw e
            }
        }
    }

    /**
     * 로그아웃
     * 백엔드에서 리프래시 토큰 삭제 후, 프론트에서 액세스 토큰 삭제
     * SocketException이나 IOException이 발생해도 서버가 연결을 닫았을 수 있으므로 토큰은 삭제
     */
    suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val accessToken = getAccessToken()
                ?: throw IllegalStateException("액세스 토큰이 없습니다")
            
            try {
                val response = api.logout("Bearer $accessToken")
                if (!response.isSuccessful) {
                    val errorBody = response.errorBody()?.string().orEmpty()
                    throw IllegalStateException("로그아웃 실패 (${response.code()}): $errorBody")
                }
            } catch (e: java.net.SocketException) {
                // 서버가 연결을 닫았을 수 있음 (로그아웃 성공 가능성)
                android.util.Log.w("AuthRepository", "로그아웃 중 SocketException 발생 (서버가 연결을 닫았을 수 있음): ${e.message}")
            } catch (e: java.io.IOException) {
                // 네트워크 에러 (로그아웃 성공 가능성)
                android.util.Log.w("AuthRepository", "로그아웃 중 IOException 발생: ${e.message}")
            } catch (e: HttpException) {
                val code = e.code()
                val msg = e.response()?.errorBody()?.string().orEmpty()
                throw IllegalStateException("로그아웃 실패 ($code): $msg")
            }
            
            // 프론트에서 액세스 토큰 삭제 (에러가 발생해도 토큰은 삭제)
            tokenStore.clear()
        }
    }

    /**
     * 회원탈퇴
     * 백엔드에서 회원 정보 삭제 후, 프론트에서 토큰 삭제
     * SocketException이나 IOException이 발생해도 서버가 연결을 닫았을 수 있으므로 토큰은 삭제
     */
    suspend fun unregister(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val accessToken = getAccessToken()
                ?: throw IllegalStateException("액세스 토큰이 없습니다")
            
            try {
                val response = api.unregister("Bearer $accessToken")
                if (!response.isSuccessful) {
                    val errorBody = response.errorBody()?.string().orEmpty()
                    throw IllegalStateException("회원탈퇴 실패 (${response.code()}): $errorBody")
                }
            } catch (e: java.net.SocketException) {
                // 서버가 연결을 닫았을 수 있음 (회원탈퇴 성공 가능성)
                android.util.Log.w("AuthRepository", "회원탈퇴 중 SocketException 발생 (서버가 연결을 닫았을 수 있음): ${e.message}")
            } catch (e: java.io.IOException) {
                // 네트워크 에러 (회원탈퇴 성공 가능성)
                android.util.Log.w("AuthRepository", "회원탈퇴 중 IOException 발생: ${e.message}")
            } catch (e: HttpException) {
                val code = e.code()
                val msg = e.response()?.errorBody()?.string().orEmpty()
                throw IllegalStateException("회원탈퇴 실패 ($code): $msg")
            }
            
            // 프론트에서 토큰 삭제 (에러가 발생해도 토큰은 삭제)
            tokenStore.clear()
        }
    }
}
