package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import nl.rhaydus.softcover.core.component.button.PillButtonContent
import nl.rhaydus.softcover.core.component.button.PillButtonTone
import nl.rhaydus.softcover.core.component.button.PillButtonUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.domain.model.BookEdition

/** The first non-blank ISBN, preferring ISBN-13; null while the edition has none to search by. */
internal val BookEdition.searchIsbn: String?
    get() = isbn13?.takeIf { it.isNotBlank() } ?: isbn10?.takeIf { it.isNotBlank() }

/**
 * The title plus first author; null without a title. Kobo's search finds only its own ebook ISBNs, never
 * print ones, so Kobo is searched by title rather than [searchIsbn].
 */
internal val BookEdition.koboQuery: String?
    get() {
        val searchTitle: String = title?.takeIf { it.isNotBlank() } ?: return null
        val firstAuthor: String? = authors.firstOrNull()?.name?.takeIf { it.isNotBlank() }

        return listOfNotNull(searchTitle, firstAuthor).joinToString(separator = " ")
    }

internal fun koboSearchUrl(query: String): String = "https://www.kobo.com/search?query=${query.percentEncoded()}"

internal fun toExternalLinkPills(
    hasIsbn: Boolean,
    hasKoboQuery: Boolean,
): ImmutableList<PillButtonUiModel>? {
    val pills: List<PillButtonUiModel> = buildList {
        if (hasIsbn) {
            add(
                externalLinkPill(
                    key = BOOKSHOP_KEY,
                    label = "Bookshop",
                    icon = SoftcoverIcon.Storefront,
                ),
            )
            add(
                externalLinkPill(
                    key = AMAZON_KEY,
                    label = "Amazon",
                    icon = SoftcoverIcon.ShoppingBag,
                ),
            )
        }
        if (hasKoboQuery) {
            add(
                externalLinkPill(
                    key = KOBO_KEY,
                    label = "Kobo",
                    icon = SoftcoverIcon.MenuBook,
                ),
            )
        }
        if (hasIsbn) {
            add(
                externalLinkPill(
                    key = OPEN_LIBRARY_KEY,
                    label = "OpenLibrary",
                    icon = SoftcoverIcon.LibraryBooks,
                ),
            )
        }
    }

    return pills.takeIf { it.isNotEmpty() }?.toPersistentList()
}

internal const val BOOKSHOP_KEY = "bookshop"
internal const val AMAZON_KEY = "amazon"
internal const val OPEN_LIBRARY_KEY = "open-library"
internal const val KOBO_KEY = "kobo"

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

private fun String.percentEncoded(): String = buildString {
    for (byte in encodeToByteArray()) {
        val value: Int = byte.toInt() and 0xFF
        val char: Char = value.toChar()
        if (char in 'A'..'Z' || char in 'a'..'z' || char in '0'..'9' || char in "-._~") {
            append(char)
        } else {
            append('%')
            append(HEX_DIGITS[value shr 4])
            append(HEX_DIGITS[value and 0x0F])
        }
    }
}

private const val HEX_DIGITS = "0123456789ABCDEF"
