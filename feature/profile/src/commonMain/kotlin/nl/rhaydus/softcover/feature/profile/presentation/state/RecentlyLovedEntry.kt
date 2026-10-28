package nl.rhaydus.softcover.feature.profile.presentation.state

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.profile.domain.model.RecentlyLovedBook

/** A loved book paired with the library [Book] that supplies its cover, title and authors. */
internal data class RecentlyLovedEntry(
    val loved: RecentlyLovedBook,
    val libraryBook: Book,
)
