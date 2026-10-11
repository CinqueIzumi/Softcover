package nl.rhaydus.softcover.core.component.bookcard

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.component.transition.BookTransitionKey

@Immutable
sealed interface BookCardLinePart {
    data class Text(val text: String) : BookCardLinePart

    data class Rating(
        val label: String,
        val contentDescription: String,
        val transitionKey: BookTransitionKey? = null,
    ) : BookCardLinePart
}
