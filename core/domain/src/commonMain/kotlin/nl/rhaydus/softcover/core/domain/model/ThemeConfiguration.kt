package nl.rhaydus.softcover.core.domain.model

data class ThemeConfiguration(
    val bottomBarStyle: BottomBarStyle = BottomBarStyle.FLOATING,
    val themeMode: ThemeMode = ThemeMode.DEFAULT,
    val useDynamicColor: Boolean = false,
)
