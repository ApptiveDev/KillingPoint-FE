package com.killingpart.killingpoint.data.model

data class KakaoAuthRequest(val accessToken: String)

data class KakaoAuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val isNew: Boolean,
)

data class TestAuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val isNew: Boolean,
)
