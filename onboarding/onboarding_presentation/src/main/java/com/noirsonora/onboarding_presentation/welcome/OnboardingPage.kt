package com.noirsonora.onboarding_presentation.welcome

import androidx.annotation.DrawableRes


sealed class OnboardingPage(
    @DrawableRes
    val image: Int,
    val title: Int,
    val description: Int
) {
    object FirstPage : OnboardingPage(
        image = com.noirsonora.santoro_core_ui.R.drawable.onboarding_first_screen_icon,
        title = com.noirsonora.santoro_core.R.string.onboarding_first_screen_title,
        description = com.noirsonora.santoro_core.R.string.onboarding_first_screen_description
    )

    object SecondPage : OnboardingPage(
        image = com.noirsonora.santoro_core_ui.R.drawable.onboarding_second_screen_icon,
        title = com.noirsonora.santoro_core.R.string.onboarding_second_screen_title,
        description = com.noirsonora.santoro_core.R.string.onboarding_second_screen_description
    )

    object ThirdPage : OnboardingPage(
        image = com.noirsonora.santoro_core_ui.R.drawable.onboarding_third_screen_icon,
        title = com.noirsonora.santoro_core.R.string.onboarding_third_screen_title,
        description = com.noirsonora.santoro_core.R.string.onboarding_third_screen_description
    )

}