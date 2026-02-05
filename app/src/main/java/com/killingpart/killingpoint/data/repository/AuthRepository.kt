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
import com.killingpart.killingpoint.data.model.SubscribeResponse
import com.killingpart.killingpoint.data.model.FeedResponse
import com.killingpart.killingpoint.data.model.FeedDiary
import com.killingpart.killingpoint.data.model.LikeResponse
import com.killingpart.killingpoint.data.model.StoreResponse
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
import com.killingpart.killingpoint.data.model.ReportDiaryRequest
import com.killingpart.killingpoint.data.model.DiaryOrderRequest

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
    
    /**
     * JWT 토큰에서 userId를 추출
     */
    suspend fun getUserIdFromToken(): Long? = withContext(Dispatchers.IO) {
        try {
            val accessToken = getAccessToken() ?: return@withContext null
            // "Bearer " 제거
            val token = accessToken.removePrefix("Bearer ").trim()
            
            // JWT는 header.payload.signature 형식
            val parts = token.split(".")
            if (parts.size < 2) return@withContext null
            
            // payload 디코딩
            val payload = parts[1]
            // Base64 URL 디코딩 (패딩 추가)
            val paddedPayload = payload + "=".repeat((4 - payload.length % 4) % 4)
            val decodedBytes = android.util.Base64.decode(paddedPayload, android.util.Base64.URL_SAFE)
            val decodedString = String(decodedBytes, Charsets.UTF_8)
            
            // JSON 파싱
            val json = org.json.JSONObject(decodedString)
            
            // userId 또는 sub (subject) 필드에서 추출
            when {
                json.has("userId") -> json.getLong("userId")
                json.has("sub") -> json.getString("sub").toLongOrNull()
                json.has("id") -> json.getLong("id")
                else -> null
            }
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "JWT 디코딩 실패: ${e.message}")
            null
        }
    }

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
                
                android.util.Log.d("AuthRepository", "토큰 갱신 시도: refreshToken 존재=${refreshToken.isNotBlank()}")
                
                val response = api.refreshAccessToken(refreshToken)
                
                android.util.Log.d("AuthRepository", "토큰 갱신 성공: isNew=${response.isNew}, 새 accessToken 길이=${response.accessToken.length}, 새 refreshToken 길이=${response.refreshToken.length}")
                
                tokenStore.save(response.accessToken, response.refreshToken)
                
                val savedAccessToken = tokenStore.getAccessToken()
                val savedRefreshToken = tokenStore.getRefreshToken()
                android.util.Log.d("AuthRepository", "토큰 저장 확인: accessToken 저장됨=${savedAccessToken != null}, refreshToken 저장됨=${savedRefreshToken != null}")
                
                response.isNew // isNew 반환
            }.recoverCatching { e ->
                android.util.Log.e("AuthRepository", "토큰 갱신 실패: ${e.message}")
                if (e is HttpException) {
                    val code = e.code()
                    val msg = e.response()?.errorBody()?.string().orEmpty()
                    android.util.Log.e("AuthRepository", "토큰 갱신 실패 상세: code=$code, message=$msg")
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


    suspend fun getMyDiaries(page: Int = 0, size: Int): MyDiaries =
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

    suspend fun getFeeds(page: Int = 0, size: Int): FeedResponse =
        withContext(Dispatchers.IO) {
            try {
                val accessToken = getAccessToken()
                    ?: throw IllegalStateException("액세스 토큰이 없습니다")
                val result = api.getFeeds("Bearer $accessToken", page, size)
                
                android.util.Log.d("AuthRepository", "피드 조회 성공: page=$page, size=$size, totalElements=${result.page.totalElements}")
                result.content.forEachIndexed { index, feed ->
                    android.util.Log.d("AuthRepository", "Feed[$index]: diaryId=${feed.diaryId}, userId=${feed.userId}, username=${feed.username}, tag=${feed.tag}, musicTitle=${feed.musicTitle}, artist=${feed.artist}")
                }
                
                result
            } catch (e: HttpException) {
                val code = e.code()
                val msg = e.response()?.errorBody()?.string().orEmpty()
                throw IllegalStateException("피드 조회 실패 ($code): $msg")
            }
        }

    suspend fun getRandomDiaries(): List<FeedDiary> =
        withContext(Dispatchers.IO) {
            try {
                val accessToken = getAccessToken() ?: throw IllegalStateException("액세스 토큰이 없습니다")
                val result = api.getRandomDiaries("Bearer $accessToken")
                val diaries: List<FeedDiary> = result.content
                
                android.util.Log.d("AuthRepository", "무작위 일기 조회 성공: count=${result.pageSize}")
                diaries.forEachIndexed { index, diary ->
                    android.util.Log.d("AuthRepository", "RandomDiary[$index]: diaryId=${diary.diaryId}, userId=${diary.userId}, username=${diary.username}, tag=${diary.tag}, musicTitle=${diary.musicTitle}, artist=${diary.artist}")
                }
                diaries
            } catch (e: HttpException) {
                val code = e.code()
                val msg = e.response()?.errorBody()?.string().orEmpty()
                throw IllegalStateException("무작위 일기 조회 실패 ($code): $msg")
            }
        }

    suspend fun toggleLike(diaryId: Long): Result<LikeResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val accessToken = getAccessToken()
                ?: throw IllegalStateException("액세스 토큰이 없습니다")
            val result = api.toggleLike("Bearer $accessToken", diaryId)
            android.util.Log.d("AuthRepository", "좋아요 토글 성공: diaryId=$diaryId, isLiked=${result.isLiked}")
            result
        }.recoverCatching { e ->
            if (e is HttpException) {
                val code = e.code()
                val msg = e.response()?.errorBody()?.string().orEmpty()
                throw IllegalStateException("좋아요 토글 실패 ($code): $msg")
            } else {
                throw e
            }
        }
    }

    suspend fun toggleStore(diaryId: Long): Result<StoreResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val accessToken = getAccessToken()
                ?: throw IllegalStateException("액세스 토큰이 없습니다")
            val result = api.toggleStore("Bearer $accessToken", diaryId)
            android.util.Log.d("AuthRepository", "보관 토글 성공: diaryId=$diaryId, isStored=${result.isStored}")
            result
        }.recoverCatching { e ->
            if (e is HttpException) {
                val code = e.code()
                val msg = e.response()?.errorBody()?.string().orEmpty()
                throw IllegalStateException("보관 토글 실패 ($code): $msg")
            } else {
                throw e
            }
        }
    }

    suspend fun getStoredDiariesPage(page: Int = 0, size: Int = 20): Result<FeedResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val accessToken = getAccessToken()
                ?: throw IllegalStateException("액세스 토큰이 없습니다")
            api.getStoredDiaries("Bearer $accessToken", page = page, size = size)
        }.recoverCatching { e ->
            if (e is HttpException) {
                val code = e.code()
                val msg = e.response()?.errorBody()?.string().orEmpty()
                throw IllegalStateException("보관 일기 조회 실패 ($code): $msg")
            } else {
                throw e
            }
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

    /**
     * 구독 목록 조회 (나의 픽)
     */
    suspend fun getSubscribes(userId: Long, pick_total: Int, page: Int = 0): Result<SubscribeResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val accessToken = getAccessToken()
                ?: throw IllegalStateException("액세스 토큰이 없습니다")
            api.getSubscribes("Bearer $accessToken", userId, page, pick_total)
        }.recoverCatching { e ->
            if (e is HttpException) {
                val code = e.code()
                val msg = e.response()?.errorBody()?.string().orEmpty()
                throw IllegalStateException("구독 목록 조회 실패 ($code): $msg")
            } else {
                throw e
            }
        }
    }

    /**
     * 팬덤 목록 조회 (나의 팬덤)
     */
    suspend fun getFans(userId: Long, fan_total: Int, page: Int = 0): Result<SubscribeResponse> = withContext(Dispatchers.IO) {
        runCatching {
            var accessToken = getAccessToken()
                ?: throw IllegalStateException("액세스 토큰이 없습니다")

            try {
                val result = api.getFans("Bearer $accessToken", userId, page, fan_total)
                android.util.Log.d("AuthRepository", "팬덤 목록 조회 성공: totalElements=${result.page.totalElements}")
                result
            } catch (e: HttpException) {
                val code = e.code()
                val msg = e.response()?.errorBody()?.string().orEmpty()

                if (code == 401) {
                    android.util.Log.d("AuthRepository", "토큰 갱신 시도 (401)")
                    refreshAccessToken().getOrThrow()
                    accessToken = getAccessToken()
                        ?: throw IllegalStateException("토큰 갱신 후 액세스 토큰을 가져올 수 없습니다")
                    android.util.Log.d("AuthRepository", "토큰 갱신 성공, 재시도")
                    api.getFans("Bearer $accessToken", userId, page, fan_total)
                } else {
                    throw IllegalStateException("팬덤 목록 조회 실패 ($code): $msg")
                }
            }
        }.recoverCatching { e ->
            if (e is HttpException) {
                val code = e.code()
                val msg = e.response()?.errorBody()?.string().orEmpty()
                throw IllegalStateException("팬덤 목록 조회 실패 ($code): $msg")
            } else {
                throw e
            }
        }
    }

    /**
     * 구독 추가 (나의 픽으로 추가)
     */
    suspend fun addSubscribe(subscribeToUserId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val accessToken = getAccessToken()
                ?: throw IllegalStateException("액세스 토큰이 없습니다")
            val response = api.addSubscribe("Bearer $accessToken", subscribeToUserId)
            if (!response.isSuccessful) {
                throw IllegalStateException("구독 추가 실패 (${response.code()}): ${response.message()}")
            }
        }.recoverCatching { e ->
            if (e is HttpException) {
                val code = e.code()
                val msg = e.response()?.errorBody()?.string().orEmpty()
                throw IllegalStateException("구독 추가 실패 ($code): $msg")
            } else {
                throw e
            }
        }
    }

    /**
     * 구독 취소 (나의 픽에서 제거)
     */
    suspend fun removeSubscribe(subscribeToUserId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val accessToken = getAccessToken()
                ?: throw IllegalStateException("액세스 토큰이 없습니다")
            val response = api.removeSubscribe("Bearer $accessToken", subscribeToUserId)
            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string().orEmpty()
                throw IllegalStateException("구독 취소 실패 (${response.code()}): $errorBody")
            }
        }.recoverCatching { e ->
            if (e is HttpException) {
                val code = e.code()
                val msg = e.response()?.errorBody()?.string().orEmpty()
                throw IllegalStateException("구독 취소 실패 ($code): $msg")
            } else {
                throw e
            }
        }
    }

    /**
     * 회원 검색
     */
    suspend fun searchUsers(
        searchCond: String? = null,
        page: Int = 0,
        size: Int
    ): Result<SubscribeResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val accessToken = getAccessToken()
                ?: throw IllegalStateException("액세스 토큰이 없습니다")
            api.searchUsers("Bearer $accessToken", searchCond, page, size)
        }.recoverCatching { e ->
            if (e is HttpException) {
                val code = e.code()
                val msg = e.response()?.errorBody()?.string().orEmpty()
                throw IllegalStateException("회원 검색 실패 ($code): $msg")
            } else {
                throw e
            }
        }
    }

    /**
     * 특정 유저의 일기 조회
     */
    suspend fun getUserDiaries(
        userId: Long,
        page: Int = 0,
        size: Int
    ): Result<MyDiaries> = withContext(Dispatchers.IO) {
        runCatching {
            val accessToken = getAccessToken()
                ?: throw IllegalStateException("액세스 토큰이 없습니다")
            api.getUserDiaries("Bearer $accessToken", userId, page, size)
        }.recoverCatching { e ->
            if (e is HttpException) {
                val code = e.code()
                val msg = e.response()?.errorBody()?.string().orEmpty()
                throw IllegalStateException("유저 일기 조회 실패 ($code): $msg")
            } else {
                throw e
            }
        }
    }

    /**
     * 회원 통계 조회 (팬덤, 픽, 킬링파트 개수)
     */
    suspend fun getUserStatistics(userId: Long): Result<com.killingpart.killingpoint.data.model.UserStatistics> = 
        withContext(Dispatchers.IO) {
            runCatching {
                val accessToken = getAccessToken()
                    ?: throw IllegalStateException("액세스 토큰이 없습니다")
                api.getUserStatistics("Bearer $accessToken", userId)
            }.recoverCatching { e ->
                if (e is HttpException) {
                    val code = e.code()
                    val msg = e.response()?.errorBody()?.string().orEmpty()
                    throw IllegalStateException("회원 통계 조회 실패 ($code): $msg")
                } else {
                    throw e
                }
            }
        }

    /**
     * 게시글 신고
     */
    suspend fun reportDiary(diaryId: Long, content: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val accessToken = getAccessToken()
                ?: throw IllegalStateException("액세스 토큰이 없습니다")
            val response = api.reportDiary("Bearer $accessToken", diaryId, ReportDiaryRequest(content))
            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string().orEmpty()
                throw IllegalStateException("게시글 신고 실패 (${response.code()}): $errorBody")
            }
        }.recoverCatching { e ->
            if (e is HttpException) {
                val code = e.code()
                val msg = e.response()?.errorBody()?.string().orEmpty()
                throw IllegalStateException("게시글 신고 실패 ($code): $msg")
            } else {
                throw e
            }
        }
    }

    /**
     * 플레이리스트 순서 변경
     */
    suspend fun reorderDiaryOrder(diaryIds: List<Long>): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val accessToken = getAccessToken()
                ?: throw IllegalStateException("액세스 토큰이 없습니다")
            val response = api.reorderDiaries("Bearer $accessToken", DiaryOrderRequest(diaryIds))
            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string().orEmpty()
                throw IllegalStateException("순서 변경 실패 (${response.code()}): $errorBody")
            }
        }.recoverCatching { e ->
            if (e is HttpException) {
                val code = e.code()
                val msg = e.response()?.errorBody()?.string().orEmpty()
                throw IllegalStateException("순서 변경 실패 ($code): $msg")
            } else {
                throw e
            }
        }
    }
}
