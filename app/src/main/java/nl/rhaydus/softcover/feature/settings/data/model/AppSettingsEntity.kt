package nl.rhaydus.softcover.feature.settings.data.model

import kotlinx.serialization.Serializable
import nl.rhaydus.softcover.feature.settings.domain.model.DateStyle

@Serializable
data class AppSettingsEntity(
    val apiKey: String = "",
    val userId: Int = -1,
    val themeConfig: ThemeConfigurationEntity = ThemeConfigurationEntity(),
    val dateStyle: DateStyle = DateStyle.DAY_MONTH_YEAR,
)