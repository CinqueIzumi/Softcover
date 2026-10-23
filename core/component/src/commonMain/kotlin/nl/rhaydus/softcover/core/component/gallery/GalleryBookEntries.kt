package nl.rhaydus.softcover.core.component.gallery

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.bookcard.BookCard
import nl.rhaydus.softcover.core.component.bookcard.BookCardRail
import nl.rhaydus.softcover.core.component.bookcard.BookCardRailUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardVariant
import nl.rhaydus.softcover.core.component.cover.Cover
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.component.mood.MoodTile
import nl.rhaydus.softcover.core.component.mood.MoodTileUiModel
import nl.rhaydus.softcover.core.component.share.ShareCard
import nl.rhaydus.softcover.core.component.share.ShareCardUiModel
import nl.rhaydus.softcover.core.component.verdict.VerdictBlock
import nl.rhaydus.softcover.core.component.verdict.VerdictBlockUiModel

internal val bookEntries: ImmutableList<GalleryEntry> = persistentListOf(
    galleryEntry(
        name = "Cover",
        family = GalleryFamily.COVER,
        blurb = "A book/edition cover jacket — loaded art, or a monogram fallback that degrades " +
            "to a single initial on a thumbnail-sized tile. No fixture here carries a live network " +
            "URL, matching every other family in this gallery.",
        previews = CoverUiModel,
        content = { model, modifier ->
            val width = if (model.variant == CoverVariant.Flat) 48.dp else 140.dp

            Cover(
                model = model,
                modifier = modifier.width(width),
            )
        },
    ),
    galleryEntry(
        name = "BookCard",
        family = GalleryFamily.BOOKCARD,
        blurb = "The one book or series card: a bare cover, a cover over its text, or a row in three sizes — " +
            "every library, explore, reading, profile and edition surface.",
        previews = BookCardUiModel,
        content = { model, modifier ->
            Box(modifier = modifier.then(bookCardGallerySize(model = model))) {
                BookCard(
                    model = model,
                    onEvent = {},
                )
            }
        },
    ),
    galleryEntry(
        name = "BookCardRail",
        family = GalleryFamily.BOOKCARD,
        blurb = "A horizontal rail of byline cards: loaded, loading stand-ins, and a trailing progress card " +
            "while the next page is fetched.",
        previews = BookCardRailUiModel,
        content = { model, modifier ->
            BookCardRail(
                model = model,
                onEvent = {},
                listState = rememberLazyListState(),
                modifier = modifier,
            )
        },
    ),
    galleryEntry(
        name = "MoodTile",
        family = GalleryFamily.MOOD,
        blurb = "A tappable mood tile in one of four inks, opening a mood-filtered search; loading masks " +
            "its text and icon and drops the press.",
        previews = MoodTileUiModel,
        content = { model, modifier ->
            MoodTile(
                model = model,
                onEvent = {},
                modifier = modifier.width(160.dp),
            )
        },
    ),
    galleryEntry(
        name = "VerdictBlock",
        family = GalleryFamily.VERDICT,
        blurb = "The book page's read-only \"Your verdict\" card: an invitation when empty, otherwise " +
            "the gold star row and score, the review, and a note when it holds spoilers.",
        previews = VerdictBlockUiModel,
        content = { model, modifier ->
            VerdictBlock(
                model = model,
                onEvent = {},
                modifier = modifier,
            )
        },
    ),
    galleryEntry(
        name = "ShareCard",
        family = GalleryFamily.SHARE,
        blurb = "The exportable share-card family — book, reading update, stat, quote, year recap, and reading life.",
        previews = ShareCardUiModel,
        content = { model, modifier ->
            // A share card sets its own fixed export size (up to 360dp wide),
            // wider than the gallery's fixture tile on a phone. Render it at its true size and
            // let it be panned rather than clipping or squashing an artifact meant for export.
            Box(
                modifier = modifier.horizontalScroll(rememberScrollState()),
            ) {
                ShareCard(model = model)
            }
        },
    ),
)

private fun bookCardGallerySize(model: BookCardUiModel): Modifier = if (model.variant is BookCardVariant.Row) {
    Modifier.fillMaxWidth()
} else {
    Modifier.width(140.dp)
}
