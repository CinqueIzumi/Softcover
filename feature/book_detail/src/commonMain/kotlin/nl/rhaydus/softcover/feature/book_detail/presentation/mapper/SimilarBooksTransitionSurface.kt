package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

/** Scopes the covers and titles of the Similar books rail of [sourceBookId], so they never collide with another surface's. */
internal fun similarBooksTransitionSurface(sourceBookId: Int): String = "similar:$sourceBookId"
