package nl.rhaydus.softcover.core.designsystem.presentation.transition

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.compositionLocalOf

@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

val LocalNavAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

// Prefer bookId so the key stays stable across a navigation when the destination
// resolves a different currentEdition than the source row was using. Pass a non-null
// surface when the same book can appear in multiple visible carousels on one screen,
// so each card registers a distinct key in the SharedTransitionScope.
fun bookTransitionKey(
    element: BookTransitionElement,
    editionId: Int?,
    bookId: Int?,
    surface: String? = null,
): String? {
    val base = when {
        bookId != null -> "book-${element.keyPart}-book-$bookId"
        editionId != null -> "book-${element.keyPart}-edition-$editionId"
        else -> return null
    }

    return if (surface != null) "$base-$surface" else base
}
