package com.killingpart.killingpoint.navigation

import android.content.Context

/**
 * 신규 온보딩 중 "킬링파트 튜토리얼 진행 상태"를 로컬에 저장한다.
 * - true: 태그 설정은 끝났지만 튜토리얼(곡/구간/일기/프리뷰)이 아직 끝나지 않음
 * - false: 튜토리얼 완료 또는 건너뛰기 처리됨
 */
object OnboardingProgressStore {
    private const val PREF_NAME = "onboarding_progress"
    private const val KEY_TUTORIAL_IN_PROGRESS = "tutorial_in_progress"

    fun markTutorialInProgress(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_TUTORIAL_IN_PROGRESS, true)
            .apply()
    }

    fun clearTutorialInProgress(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_TUTORIAL_IN_PROGRESS, false)
            .apply()
    }

    fun isTutorialInProgress(context: Context): Boolean {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_TUTORIAL_IN_PROGRESS, false)
    }
}
