package nl.rhaydus.softcover.core.component.header

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

/** Everything [MastheadRefreshAction] renders: its label, and whether it can be tapped. */
@Immutable
data class MastheadRefreshUiModel(
    val label: String,
    val enabled: Boolean,
) {
    companion object : UiModelPreviews<MastheadRefreshUiModel> {
        /** Idle and mid-refresh (disabled) fixtures. */
        override val previews: ImmutableMap<String, MastheadRefreshUiModel> = persistentMapOf(
            "Idle" to MastheadRefreshUiModel(
                label = "Refresh",
                enabled = true,
            ),
            "Refreshing" to MastheadRefreshUiModel(
                label = "Refreshing…",
                enabled = false,
            ),
        )
    }
}
