package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.UserTag
import nl.rhaydus.softcover.feature.book_detail.presentation.state.ShareCardVariant

internal data class ShareCardsSnapshot(
    val book: Book?,
    val edition: BookEdition?,
    val username: String?,
    val avatarUrl: String?,
    val userTags: List<UserTag>,
    val chosenVariant: ShareCardVariant?,
    val isSharing: Boolean,
    val isSavingToGallery: Boolean,
)
