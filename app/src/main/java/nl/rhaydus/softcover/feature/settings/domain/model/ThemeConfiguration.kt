package nl.rhaydus.softcover.feature.settings.domain.model

data class ThemeConfiguration(
    val bottomBarStyle: BottomBarStyle = BottomBarStyle.FLOATING,
    val useDynamicColor: Boolean = false,
)