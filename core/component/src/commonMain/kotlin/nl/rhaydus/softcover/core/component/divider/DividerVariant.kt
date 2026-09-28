package nl.rhaydus.softcover.core.component.divider

sealed interface DividerVariant {
    data object DebugRoutesRow : DividerVariant

    data object ReadingLifeShareCard : DividerVariant

    data object SettingsRoadmap : DividerVariant

    data object CreateListQuote : DividerVariant

    data class OnboardingOrTypeIt(val label: String) : DividerVariant
}
