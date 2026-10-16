package nl.rhaydus.softcover.feature.settings.presentation.model

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon

/** [gloss] is the instance's configuration in words, or a fallback line when the widget has none to show. */
@Immutable
internal data class PlacedWidgetRowUiModel(
    val widgetId: Int,
    val title: String,
    val gloss: String,
    val icon: SoftcoverIcon,
    val hasSettings: Boolean,
)
