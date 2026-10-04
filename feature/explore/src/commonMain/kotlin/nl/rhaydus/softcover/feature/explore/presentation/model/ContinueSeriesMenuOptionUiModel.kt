package nl.rhaydus.softcover.feature.explore.presentation.model

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.feature.explore.presentation.action.ExploreAction

/** One dismiss choice in the up-next menu sheet: what it says and the [dismissAction] picking it runs. */
@Immutable
internal data class ContinueSeriesMenuOptionUiModel(
    val icon: SoftcoverIcon,
    val label: String,
    val description: String,
    val dismissAction: ExploreAction,
)
