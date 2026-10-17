package nl.rhaydus.softcover.feature.library.presentation.state

import androidx.compose.runtime.Immutable

/** What the desktop right-click menu needs to know about one book, resolved when its card is built. */
@Immutable
internal data class LibraryBookMenuUiModel(
    val isRead: Boolean,
    val isPaused: Boolean,
)
