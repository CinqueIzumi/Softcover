package nl.rhaydus.softcover.core.component.bookcard

import androidx.compose.runtime.Immutable

/** @property fraction In `0f..1f`. */
@Immutable
data class BookCardProgress(
    val fraction: Float,
    val label: String,
)
