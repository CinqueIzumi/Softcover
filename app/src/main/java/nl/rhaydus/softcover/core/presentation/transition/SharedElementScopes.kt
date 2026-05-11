package nl.rhaydus.softcover.core.presentation.transition

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.compositionLocalOf

@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

val LocalNavAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

// Prefer bookId so the key stays stable across a navigation when the destination
// resolves a different currentEdition than the source row was using.
fun bookCoverTransitionKey(editionId: Int?, bookId: Int?): String? = when {
    bookId != null -> "book-cover-book-$bookId"
    editionId != null -> "book-cover-edition-$editionId"
    else -> null
}
