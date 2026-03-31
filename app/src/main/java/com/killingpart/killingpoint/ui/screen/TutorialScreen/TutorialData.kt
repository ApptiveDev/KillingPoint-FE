package com.killingpart.killingpoint.ui.screen.TutorialScreen

import com.killingpart.killingpoint.R

data class TutorialStep(
    val step: Int,
    val description: String,
    val image: Int,
)

val tutorialSteps = listOf(
    TutorialStep(
        step = 1,
        description = "좌우로 밀어서 킬링파트 근처로 이동하고\n양 옆의 핸들로 킬링파트를 지정해 보세요!",
        image = R.drawable.tutorial1
    ),

    TutorialStep(
        step = 2,
        description = "킬링파트에 당신만의 코멘트를 기록해 보세요.",
        image = R.drawable.tutorial2
    ),

    TutorialStep(
        step = 3,
        description = "프로필을 보면 킬링파트들을 다시 확인할 수 있어요.\n물론 코멘트도 다시 읽을 수 있답니다.",
        image = R.drawable.tutorial3
    ),

    TutorialStep(
        step = 4,
        description = "내 컬렉션 뿐만 아니라 캘린더에도 저장해놨어요.\n킬링파트를 모아 나만의 캘린더를 완성해 보세요.",
        image = R.drawable.tutorial4
    ),

    TutorialStep(
        step = 5,
        description = "그날의 감정이 담긴 코멘트를 읽으며\n킬링파트를 더 깊이 있게 감상할 수 있어요.",
        image = R.drawable.tutorial5
    ),
)