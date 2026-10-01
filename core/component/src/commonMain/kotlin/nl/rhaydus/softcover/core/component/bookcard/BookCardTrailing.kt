package nl.rhaydus.softcover.core.component.bookcard

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.component.badge.BadgeUiModel

/**
 * [Badge] and [Restore] are read-only — a hidden row restores through the card's own tap. [Bookmark] and
 * [SetProgress] are their own tap targets and emit [BookCardEvent.TrailingClicked].
 */
@Immutable
sealed interface BookCardTrailing {
    data class Badge(val model: BadgeUiModel) : BookCardTrailing

    data class Restore(val label: String) : BookCardTrailing

    data class Bookmark(
        val saved: Boolean,
        val contentDescription: String,
    ) : BookCardTrailing

    data class SetProgress(val label: String) : BookCardTrailing
}
