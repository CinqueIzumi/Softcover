package nl.rhaydus.softcover.core.component.bookcard

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
data class BookCardLine(
    val parts: ImmutableList<BookCardLinePart>,
)
