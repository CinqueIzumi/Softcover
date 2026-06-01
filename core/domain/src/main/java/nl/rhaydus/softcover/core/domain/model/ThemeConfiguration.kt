package nl.rhaydus.softcover.core.domain.model

data class ThemeConfiguration(
    val bottomBarStyle: BottomBarStyle = BottomBarStyle.FLOATING,
    val useDynamicColor: Boolean = false,
)
