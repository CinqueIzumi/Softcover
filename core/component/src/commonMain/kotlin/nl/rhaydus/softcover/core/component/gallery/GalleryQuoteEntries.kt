package nl.rhaydus.softcover.core.component.gallery

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.quote.QuoteCard
import nl.rhaydus.softcover.core.component.quote.QuoteCardUiModel
import nl.rhaydus.softcover.core.component.quote.QuoteSheetBody
import nl.rhaydus.softcover.core.component.quote.QuoteSheetUiModel

internal val quoteEntries: ImmutableList<GalleryEntry> = persistentListOf(
    galleryEntry(
        name = "QuoteCard",
        family = GalleryFamily.QUOTE,
        blurb = "One saved quote in the editorial quote pattern: an oversized opening mark behind the " +
            "italic passage, a byline beneath it, and a menu holding Delete.",
        previews = QuoteCardUiModel,
        content = { model, modifier ->
            QuoteCard(
                model = model,
                onEvent = {},
                modifier = modifier,
            )
        },
    ),
    galleryEntry(
        name = "QuoteSheet",
        family = GalleryFamily.QUOTE,
        blurb = "The add-a-quote sheet: a multi-line passage field, a page, percentage or time " +
            "position, an optional progress row, a privacy switcher and the backdating row. Save " +
            "stays disabled until there is a passage. Shown inline, without the modal container.",
        previews = QuoteSheetUiModel,
        content = { model, modifier ->
            GallerySheetFrame(
                scaffold = model.scaffold,
                modifier = modifier,
            ) {
                QuoteSheetBody(
                    model = model,
                    onEvent = {},
                )
            }
        },
    ),
)
