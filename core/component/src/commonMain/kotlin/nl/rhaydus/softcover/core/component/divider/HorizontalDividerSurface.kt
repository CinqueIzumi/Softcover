package nl.rhaydus.softcover.core.component.divider

sealed interface HorizontalDividerSurface {
    data object DebugRoutesRow : HorizontalDividerSurface

    data object ReadingLifeShareCard : HorizontalDividerSurface

    data object SettingsRoadmap : HorizontalDividerSurface

    data object CreateListQuote : HorizontalDividerSurface

    data object OnboardingOrTypeIt : HorizontalDividerSurface
}
