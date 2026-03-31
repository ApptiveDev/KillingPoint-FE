package com.killingpart.killingpoint.data.model

data class UserInitSettingsResponse(
    val app: AppUpdateInfo,
    val needsPolicyAgreement: Boolean,
    val needsTagSetup: Boolean,
    val policies: List<PolicyStatus>
)

data class AppUpdateInfo(
    val needsForceUpdate: Boolean,
    val needsOptionalUpdate: Boolean
)

data class PolicyStatus(
    val policyType: String,
    val required: Boolean,
    val agreed: Boolean,
    val currentRevision: Long,
    val latestRevision: Long
)

data class PolicyAgreementRequest(
    val agreements: List<PolicyAgreementItem>
)

data class PolicyAgreementItem(
    val policyType: String,
    val agreed: Boolean
)
