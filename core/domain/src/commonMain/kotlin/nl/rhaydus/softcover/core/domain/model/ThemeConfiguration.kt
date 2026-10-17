package nl.rhaydus.softcover.core.domain.model

data class ThemeConfiguration(
    val bottomBarStyle: BottomBarStyle = BottomBarStyle.FLOATING,
    val themeMode: ThemeMode = ThemeMode.DEFAULT,
    val colorPalette: ColorPalette = ColorPalette.DEFAULT,
    // Kept alongside [colorPalette] rather than folded into it: while dynamic colour is on it
    // replaces the palette outright, but the reader's palette choice is remembered underneath and
    // comes back the moment they switch dynamic colour off again.
    val useDynamicColor: Boolean = false,
)
