package nl.rhaydus.softcover.feature.settings.data.model

import kotlinx.serialization.Serializable
import nl.rhaydus.softcover.feature.settings.domain.model.BottomBarStyle
import nl.rhaydus.softcover.feature.settings.domain.model.ThemeConfiguration

@Serializable
data class ThemeConfigurationEntity(
    val bottomBarStyle: BottomBarStyle = BottomBarStyle.FLOATING,
    val useDynamicColor: Boolean = false,
)

fun ThemeConfigurationEntity.toModel(): ThemeConfiguration {
    return ThemeConfiguration(
        bottomBarStyle = this.bottomBarStyle,
        useDynamicColor = this.useDynamicColor,
    )
}