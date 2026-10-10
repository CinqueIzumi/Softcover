package nl.rhaydus.softcover.feature.explore.presentation.model

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon

/** One dismiss choice in the up-next menu sheet: what it says and which [kind] of dismissal picking it asks for. */
@Immutable
internal data class ContinueSeriesMenuOptionUiModel(
    val icon: SoftcoverIcon,
    val label: String,
    val description: String,
    val kind: ContinueSeriesDismissalKind,
)
