package com.killingpart.killingpoint.data.remote

import com.killingpart.killingpoint.data.model.KakaoAuthRequest
import com.killingpart.killingpoint.data.model.KakaoAuthResponse
import com.killingpart.killingpoint.data.model.MyDiaries
import com.killingpart.killingpoint.data.model.UserInfo
import com.killingpart.killingpoint.data.model.YouTubeVideo
import com.killingpart.killingpoint.data.model.CreateDiaryRequest
import com.killingpart.killingpoint.data.model.Diary
import com.killingpart.killingpoint.data.model.UpdateTagRequest
import com.killingpart.killingpoint.data.model.PresignedUrlResponse
import com.killingpart.killingpoint.data.model.TestAuthResponse
import com.killingpart.killingpoint.data.model.UpdateProfileImageRequest
import com.killingpart.killingpoint.data.model.YoutubeVideoRequest
import com.killingpart.killingpoint.data.model.SubscribeResponse
import com.killingpart.killingpoint.data.model.FeedResponse
import com.killingpart.killingpoint.data.model.UserStatistics
import com.killingpart.killingpoint.data.model.LikeResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("youtube/search")
    suspend fun searchVideos(
        @Body body: YoutubeVideoRequest
    ): List<YouTubeVideo>

    @GET("oauth2/test")
    suspend fun loginWithTest() : TestAuthResponse

    @POST("oauth2/kakao")
    suspend fun loginWithKakao(
        @Body body: KakaoAuthRequest
    ): KakaoAuthResponse

    @GET("users/my")
    suspend fun getUserInfo(
        @Header("Authorization") accessToken: String
    ): UserInfo

    @POST("jwt/exchange")
    suspend fun refreshAccessToken(
        @Header("X-Refresh-Token") refreshToken: String
    ): KakaoAuthResponse

    @GET("diaries/my")
    suspend fun getMyDiaries(
        @Header("Authorization") accessToken: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int
    ): MyDiaries

    @POST("diaries")
    suspend fun createDiary(
        @Header("Authorization") accessToken: String,
        @Body body: CreateDiaryRequest
    ): retrofit2.Response<Unit>

    @PUT("diaries/{diaryId}")
    suspend fun updateDiary(
        @Header("Authorization") accessToken: String,
        @Path("diaryId") diaryId: Long,
        @Body body: CreateDiaryRequest
    ): retrofit2.Response<Unit>

    @DELETE("diaries/{diaryId}")
    suspend fun deleteDiary(
        @Header("Authorization") accessToken: String,
        @Path("diaryId") diaryId: Long
    ): retrofit2.Response<Unit>

    @PATCH("users/my/tags")
    suspend fun updateTag(
        @Header("Authorization") accessToken: String,
        @Body body: UpdateTagRequest
    ): retrofit2.Response<Unit>

    @GET("presigned-url")
    suspend fun getPresignedUrl(
        @Header("Authorization") accessToken: String
    ): PresignedUrlResponse

    @PATCH("users/my/profile-image")
    suspend fun updateProfileImage(
        @Header("Authorization") accessToken: String,
        @Body body: UpdateProfileImageRequest
    ): UserInfo

    @POST("users/logout")
    suspend fun logout(
        @Header("Authorization") accessToken: String
    ): retrofit2.Response<Unit>

    @DELETE("users/my")
    suspend fun unregister(
        @Header("Authorization") accessToken: String
    ): retrofit2.Response<Unit>

    @GET("subscribes/{userId}")
    suspend fun getSubscribes(
        @Header("Authorization") accessToken: String,
        @Path("userId") userId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int
    ): SubscribeResponse

    @GET("subscribes/{userId}/fans")
    suspend fun getFans(
        @Header("Authorization") accessToken: String,
        @Path("userId") userId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int
    ): SubscribeResponse

    @POST("subscribes/{subscribeToUserId}")
    suspend fun addSubscribe(
        @Header("Authorization") accessToken: String,
        @Path("subscribeToUserId") subscribeToUserId: Long
    ): retrofit2.Response<Unit>

    @DELETE("subscribes/{subscribeToUserId}")
    suspend fun removeSubscribe(
        @Header("Authorization") accessToken: String,
        @Path("subscribeToUserId") subscribeToUserId: Long
    ): retrofit2.Response<Unit>

    @GET("users")
    suspend fun searchUsers(
        @Header("Authorization") accessToken: String,
        @Query("searchCond") searchCond: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int
    ): SubscribeResponse

    @GET("diaries/user/{userId}")
    suspend fun getUserDiaries(
        @Header("Authorization") accessToken: String,
        @Path("userId") userId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int
    ): MyDiaries

    @GET("diaries/my/feeds")
    suspend fun getFeeds(
        @Header("Authorization") accessToken: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int
    ): FeedResponse
    @GET("users/{userId}/statics")
    suspend fun getUserStatistics(
        @Header("Authorization") accessToken: String,
        @Path("userId") userId: Long
    ): UserStatistics

    @POST("diaries/{diaryId}/like")
    suspend fun toggleLike(
        @Header("Authorization") accessToken: String,
        @Path("diaryId") diaryId: Long
    ): LikeResponse
}