package nl.rhaydus.softcover.core.component.header

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

/** Everything [MastheadRefreshAction] renders: its label, and whether it can be tapped. */
@Immutable
data class MastheadRefreshUiModel(
    val label: String,
    val enabled: Boolean,
) {
    companion object : UiModelPreviews<MastheadRefreshUiModel> {
        /** Idle and mid-refresh (disabled) fixtures. */
        override val previews: ImmutableList<MastheadRefreshUiModel> = persistentListOf(
            MastheadRefreshUiModel(
                label = "Refresh",
                enabled = true,
            ),
            MastheadRefreshUiModel(
                label = "Refreshing…",
                enabled = false,
            ),
        )
    }
}
