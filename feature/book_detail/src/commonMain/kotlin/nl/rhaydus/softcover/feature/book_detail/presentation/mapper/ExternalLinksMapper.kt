package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import nl.rhaydus.softcover.core.component.button.PillButtonContent
import nl.rhaydus.softcover.core.component.button.PillButtonTone
import nl.rhaydus.softcover.core.component.button.PillButtonUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.feature.book_detail.presentation.state.ExternalLinksUiModel

/** Null while the edition has no ISBN to search by. */
internal fun BookEdition?.toExternalLinksUiModel(): ExternalLinksUiModel? {
    val isbn = this?.let { edition ->
        edition.isbn13?.takeIf { it.isNotBlank() } ?: edition.isbn10?.takeIf { it.isNotBlank() }
    } ?: return null

    return ExternalLinksUiModel(
        pills = persistentListOf(
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
        ),
        urlByKey = persistentMapOf(
            BOOKSHOP_KEY to "https://bookshop.org/search?keywords=$isbn",
            AMAZON_KEY to "https://www.amazon.com/s?k=$isbn",
            OPEN_LIBRARY_KEY to "https://openlibrary.org/search?isbn=$isbn",
        ),
    )
}

private const val BOOKSHOP_KEY = "bookshop"
private const val AMAZON_KEY = "amazon"
private const val OPEN_LIBRARY_KEY = "open-library"

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
