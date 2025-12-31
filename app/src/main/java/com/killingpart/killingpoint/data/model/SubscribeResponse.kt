package com.killingpart.killingpoint.data.model

data class SubscribeUser(
    val userId: Long,
    val username: String,
    val tag: String,
    val identifier: String,
    val profileImageUrl: String,
    val userRoleType: String,
    val socialType: String,
    val isMyPick: Boolean = false // 내가 구독한 사람인지의 여부
)

data class SubscribePage(
    val size: Int,
    val number: Int,
    val totalElements: Int,
    val totalPages: Int
)

data class SubscribeResponse(
    val content: List<SubscribeUser>,
    val page: SubscribePage
)

