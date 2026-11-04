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
import com.killingpart.killingpoint.data.model.DiaryDetail
import com.killingpart.killingpoint.data.model.FeedDiary
import com.killingpart.killingpoint.data.model.UserStatistics
import com.killingpart.killingpoint.data.model.LikeResponse
import com.killingpart.killingpoint.data.model.StoreResponse
import com.killingpart.killingpoint.data.model.SurveyRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import com.killingpart.killingpoint.data.model.ReportDiaryRequest
import com.killingpart.killingpoint.data.model.DiaryOrderRequest
import com.killingpart.killingpoint.data.model.RandomDiariesResponse
import com.killingpart.killingpoint.data.model.StoredDiariesResponse
import com.killingpart.killingpoint.data.model.DiaryLikesResponse
import com.killingpart.killingpoint.data.model.PolicyAgreementRequest
import com.killingpart.killingpoint.data.model.UpdateUsernameRequest
import com.killingpart.killingpoint.data.model.UserInitSettingsResponse
import com.killingpart.killingpoint.data.model.AlarmResponse
import com.killingpart.killingpoint.data.model.AlarmEnabledRequest
import com.killingpart.killingpoint.data.model.AlarmEnabledResponse
import com.killingpart.killingpoint.data.model.FcmTokenRequest
import com.killingpart.killingpoint.data.model.Comment
import com.killingpart.killingpoint.data.model.CommentLikeResponse
import com.killingpart.killingpoint.data.model.CommentListResponse
import com.killingpart.killingpoint.data.model.CommentPage
import com.killingpart.killingpoint.data.model.CommentReportRequest
import com.killingpart.killingpoint.data.model.CreateCommentRequest
import com.killingpart.killingpoint.data.model.MentionCandidate
import com.killingpart.killingpoint.data.model.UpdateCommentRequest

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

    @GET("users/init-settings")
    suspend fun getUserInitSettings(
        @Header("Authorization") accessToken: String,
        @Query("clientVersion") clientVersion: String,
        @Query("clientType") clientType: String
    ): UserInitSettingsResponse

    @POST("users/policy-agreement")
    suspend fun agreePolicies(
        @Header("Authorization") accessToken: String,
        @Body body: PolicyAgreementRequest
    ): retrofit2.Response<Unit>

    @POST("surveys")
    suspend fun submitSurvey(
        @Header("Authorization") accessToken: String,
        @Body body: SurveyRequest
    ): retrofit2.Response<Unit>

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

    /** GET /api/diaries/{diaryId} — 일기 단건 조회 */
    @GET("diaries/{diaryId}")
    suspend fun getDiaryDetail(
        @Header("Authorization") accessToken: String,
        @Path("diaryId") diaryId: Long
    ): DiaryDetail

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

    @PATCH("users/my/names")
    suspend fun updateUsername(
        @Header("Authorization") accessToken: String,
        @Body body: UpdateUsernameRequest
    ): UserInfo

    @GET("presigned-url")
    suspend fun getPresignedUrl(
        @Header("Authorization") accessToken: String
    ): PresignedUrlResponse

    @PATCH("users/my/profile-image")
    suspend fun updateProfileImage(
        @Header("Authorization") accessToken: String,
        @Body body: UpdateProfileImageRequest
    ): UserInfo

    @DELETE("users/my/profile-image")
    suspend fun deleteProfileImage(
        @Header("Authorization") accessToken: String
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

    @GET("diaries/{diaryId}/like")
    suspend fun getDiaryLikes(
        @Header("Authorization") accessToken: String,
        @Path("diaryId") diaryId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int,
        @Query("searchCond") searchCond: String? = null
    ): DiaryLikesResponse

    @POST("diaries/{diaryId}/stores")
    suspend fun toggleStore(
        @Header("Authorization") accessToken: String,
        @Path("diaryId") diaryId: Long
    ): StoreResponse

    @POST("diaries/{diaryId}/reports")
    suspend fun reportDiary(
        @Header("Authorization") accessToken: String,
        @Path("diaryId") diaryId: Long,
        @Body body: ReportDiaryRequest
    ): retrofit2.Response<Unit>

    @POST("users/{blockedId}/blocks")
    suspend fun blockUser(
        @Header("Authorization") accessToken: String,
        @Path("blockedId") blockedId: Long
    ): retrofit2.Response<Unit>

    @GET("users/blocks")
    suspend fun getBlockedUsers(
        @Header("Authorization") accessToken: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 5
    ): SubscribeResponse

    @DELETE("users/{blockedId}/blocks")
    suspend fun unblockUser(
        @Header("Authorization") accessToken: String,
        @Path("blockedId") blockedId: Long
    ): retrofit2.Response<Unit>

    @GET("diaries/randoms")
    suspend fun getRandomDiaries(
        @Header("Authorization") accessToken: String
    ): RandomDiariesResponse


    @GET("diaries/stores")
    suspend fun getStoredDiaries(
        @Header("Authorization") accessToken: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): StoredDiariesResponse

    @PATCH("diaries/order")
    suspend fun reorderDiaries(
        @Header("Authorization") accessToken: String,
        @Body body: DiaryOrderRequest
    ): retrofit2.Response<Unit>

    @POST("fcm/tokens")
    suspend fun addDeviceToken(
        @Header("Authorization") accessToken: String,
        @Body body: FcmTokenRequest
    ): retrofit2.Response<Unit>

    @DELETE("fcm/tokens")
    suspend fun deleteDeviceToken(
        @Header("Authorization") accessToken: String
    ): retrofit2.Response<Unit>

    @PATCH("users/my/notification-settings")
    suspend fun updateAlarmEnabled(
        @Header("Authorization") accessToken: String,
        @Body body: AlarmEnabledRequest
    ): retrofit2.Response<Unit>

    @GET("users/my/notification-settings")
    suspend fun getAlarmEnabled(
        @Header("Authorization") accessToken: String
    ): AlarmEnabledResponse

    @GET("alarms")
    suspend fun getAlarms(
        @Header("Authorization") accessToken: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): AlarmResponse

    // ===== 댓글 =====

    @GET("diaries/{diaryId}/comments")
    suspend fun getComments(
        @Header("Authorization") accessToken: String,
        @Path("diaryId") diaryId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): CommentListResponse

    @POST("diaries/{diaryId}/comments")
    suspend fun createComment(
        @Header("Authorization") accessToken: String,
        @Path("diaryId") diaryId: Long,
        @Body body: CreateCommentRequest
    ): Comment

    @GET("comments/{commentId}/replies")
    suspend fun getReplies(
        @Header("Authorization") accessToken: String,
        @Path("commentId") commentId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): CommentPage

    @PUT("comments/{commentId}")
    suspend fun updateComment(
        @Header("Authorization") accessToken: String,
        @Path("commentId") commentId: Long,
        @Body body: UpdateCommentRequest
    ): Comment

    @DELETE("comments/{commentId}")
    suspend fun deleteComment(
        @Header("Authorization") accessToken: String,
        @Path("commentId") commentId: Long
    ): retrofit2.Response<Unit>

    @POST("comments/{commentId}/like")
    suspend fun toggleCommentLike(
        @Header("Authorization") accessToken: String,
        @Path("commentId") commentId: Long
    ): CommentLikeResponse

    @POST("comments/{commentId}/reports")
    suspend fun reportComment(
        @Header("Authorization") accessToken: String,
        @Path("commentId") commentId: Long,
        @Body body: CommentReportRequest
    ): retrofit2.Response<Unit>

    @GET("comments/mention-candidates")
    suspend fun getMentionCandidates(
        @Header("Authorization") accessToken: String,
        @Query("keyword") keyword: String? = null,
        @Query("size") size: Int = 10
    ): List<MentionCandidate>
}