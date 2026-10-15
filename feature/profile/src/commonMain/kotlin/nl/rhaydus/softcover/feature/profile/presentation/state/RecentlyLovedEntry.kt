package nl.rhaydus.softcover.feature.profile.presentation.state

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.profile.domain.model.LovedBook

/** A loved book paired with its cached library [Book], which is null while the book is not in the library. */
internal data class RecentlyLovedEntry(
    val loved: LovedBook,
    val libraryBook: Book?,
)
