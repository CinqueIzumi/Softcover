package nl.rhaydus.softcover.core.preferences.data.model

import kotlinx.serialization.Serializable
import nl.rhaydus.softcover.core.domain.model.BottomBarStyle
import nl.rhaydus.softcover.core.domain.model.ThemeConfiguration
import nl.rhaydus.softcover.core.domain.model.ThemeMode

@Serializable
internal data class ThemeConfigurationEntity(
    val bottomBarStyle: BottomBarStyle = BottomBarStyle.FLOATING,
    val themeMode: ThemeMode = ThemeMode.DEFAULT,
    val useDynamicColor: Boolean = false,
)

internal fun ThemeConfigurationEntity.toModel(): ThemeConfiguration {
    return ThemeConfiguration(
        bottomBarStyle = this.bottomBarStyle,
        themeMode = this.themeMode,
        useDynamicColor = this.useDynamicColor,
    )
}
