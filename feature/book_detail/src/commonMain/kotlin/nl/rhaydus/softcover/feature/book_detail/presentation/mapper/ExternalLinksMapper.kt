package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.button.PillButtonContent
import nl.rhaydus.softcover.core.component.button.PillButtonTone
import nl.rhaydus.softcover.core.component.button.PillButtonUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.domain.model.BookEdition

/** The first non-blank ISBN, preferring ISBN-13; null while the edition has none to search by. */
internal val BookEdition.searchIsbn: String?
    get() = isbn13?.takeIf { it.isNotBlank() } ?: isbn10?.takeIf { it.isNotBlank() }

/** Null while the edition has no ISBN to search by. */
internal fun BookEdition?.toExternalLinkPills(): ImmutableList<PillButtonUiModel>? {
    this?.searchIsbn ?: return null

    return persistentListOf(
        externalLinkPill(
            key = BOOKSHOP_KEY,
            label = "Bookshop",
            icon = SoftcoverIcon.Storefront,
        ),
        externalLinkPill(
            key = AMAZON_KEY,
            label = "Amazon",
            icon = SoftcoverIcon.ShoppingBag,
        ),
        externalLinkPill(
            key = OPEN_LIBRARY_KEY,
            label = "OpenLibrary",
            icon = SoftcoverIcon.LibraryBooks,
        ),
    )
}

internal const val BOOKSHOP_KEY = "bookshop"
internal const val AMAZON_KEY = "amazon"
internal const val OPEN_LIBRARY_KEY = "open-library"

private fun externalLinkPill(
    key: String,
    label: String,
    icon: SoftcoverIcon,
): PillButtonUiModel = PillButtonUiModel(
    key = key,
    content = PillButtonContent.Label(
        text = label,
        leadingIcon = icon,
    ),
    tone = PillButtonTone.Outlined,
)
