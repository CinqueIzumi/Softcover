package nl.rhaydus.softcover.core.presentation.transition

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.compositionLocalOf

@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

val LocalNavAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

fun bookCoverTransitionKey(editionId: Int?, bookId: Int?): String? = when {
    editionId != null -> "book-cover-edition-$editionId"
    bookId != null -> "book-cover-book-$bookId"
    else -> null
}
