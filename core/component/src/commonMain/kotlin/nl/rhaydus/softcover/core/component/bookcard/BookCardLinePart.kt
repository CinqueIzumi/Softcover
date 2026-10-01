package nl.rhaydus.softcover.core.component.bookcard

import androidx.compose.runtime.Immutable

@Immutable
sealed interface BookCardLinePart {
    data class Text(val text: String) : BookCardLinePart

    data class Rating(val label: String) : BookCardLinePart
}
