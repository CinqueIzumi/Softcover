package nl.rhaydus.softcover.core.preferences.data.model

import kotlinx.serialization.Serializable
import nl.rhaydus.softcover.core.domain.model.BottomBarStyle
import nl.rhaydus.softcover.core.domain.model.ThemeConfiguration

@Serializable
internal data class ThemeConfigurationEntity(
    val bottomBarStyle: BottomBarStyle = BottomBarStyle.FLOATING,
    val useDynamicColor: Boolean = false,
)

internal fun ThemeConfigurationEntity.toModel(): ThemeConfiguration {
    return ThemeConfiguration(
        bottomBarStyle = this.bottomBarStyle,
        useDynamicColor = this.useDynamicColor,
    )
}