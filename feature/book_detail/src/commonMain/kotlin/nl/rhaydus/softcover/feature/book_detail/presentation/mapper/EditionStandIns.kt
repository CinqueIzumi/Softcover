package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.bookcard.BookCardContent
import nl.rhaydus.softcover.core.component.bookcard.BookCardCover
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.BookCardLead
import nl.rhaydus.softcover.core.component.bookcard.BookCardLinePart
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardVariant
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.CoverVariant

private const val EDITION_STAND_IN_COUNT = 4

/** Rows sized like real edition rows, drawn masked while the edition list loads. */
internal val EDITION_STAND_IN_CARDS: ImmutableList<BookCardUiModel> = List(EDITION_STAND_IN_COUNT) { index ->
    BookCardUiModel(
        key = BookCardKey.Edition(editionId = -(index + 1)),
        variant = BookCardVariant.Row(
            size = BookCardVariant.Row.Size.Standard,
            lead = BookCardLead.Cover(
                cover = BookCardCover(
                    model = CoverUiModel(
                        source = null,
                        coverlessTitle = null,
                        variant = CoverVariant.Flat,
                        isLoading = true,
                    ),
                ),
            ),
            text = BookCardContent(
                title = "Hardcover",
                supportingLines = persistentListOf(
                    persistentListOf(BookCardLinePart.Text(text = "William Morrow · 2025 · 896 pp")),
                    persistentListOf(BookCardLinePart.Text(text = "ISBN 9780062200600")),
                ),
            ),
        ),
        isLoading = true,
    )
}.toImmutableList()
