package nl.rhaydus.softcover.feature.settings.presentation.model

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.presentation.widget.HomeScreenWidgetKind

@Immutable
internal data class AddWidgetRowUiModel(
    val kind: HomeScreenWidgetKind,
    val title: String,
    val gloss: String,
    val icon: SoftcoverIcon,
)
