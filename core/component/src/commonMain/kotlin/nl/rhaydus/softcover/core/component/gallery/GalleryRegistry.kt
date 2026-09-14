package nl.rhaydus.softcover.core.component.gallery

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.cover.Cover
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.component.richtext.RichText
import nl.rhaydus.softcover.core.component.richtext.RichTextUiModel
import nl.rhaydus.softcover.core.component.share.BookShareCardUiModel
import nl.rhaydus.softcover.core.component.share.QuoteShareCardUiModel
import nl.rhaydus.softcover.core.component.share.ReadingLifeShareCardUiModel
import nl.rhaydus.softcover.core.component.share.ReadingUpdateKind
import nl.rhaydus.softcover.core.component.share.ReadingUpdateShareCardUiModel
import nl.rhaydus.softcover.core.component.share.ShareCard
import nl.rhaydus.softcover.core.component.share.ShareCardUiModel
import nl.rhaydus.softcover.core.component.share.StatShareCardUiModel
import nl.rhaydus.softcover.core.component.share.YearRecapShareCardUiModel

/**
 * The Component Gallery's data: every component paired with its family and its preview fixtures
 * (`component-contract.md` § 7.5). This object is pure data — it holds no navigation, no DI, no
 * Compose render body. The *screen* that walks this registry lives in `feature:settings`, because
 * screens belong to features and `:core:component` is banned from Voyager.
 *
 * Each migration stage appends its family's entries to [entries] in the same change as the
 * component itself. Adding a component to the gallery is one entry here, in the same change as
 * the component.
 */
object GalleryRegistry {
    val entries: ImmutableList<GalleryEntry> = persistentListOf(
        galleryEntry(
            name = "RichText",
            family = GalleryFamily.RICHTEXT,
            blurb = "Read-only formatted prose with inline bold, italic, and tap-to-reveal spoiler marks.",
            previews = RichTextUiModel,
            label = ::richTextFixtureLabel,
            content = { model, modifier ->
                RichText(
                    model = model,
                    modifier = modifier,
                )
            },
        ),
        galleryEntry(
            name = "Cover",
            family = GalleryFamily.COVER,
            blurb = "A book/edition cover jacket — loaded art, or a monogram fallback that degrades " +
                "to a single initial on a thumbnail-sized tile. No fixture here carries a live network " +
                "URL, matching every other family in this gallery.",
            previews = CoverUiModel,
            label = ::coverFixtureLabel,
            content = { model, modifier ->
                val width = if (model.variant == CoverVariant.EditionListRow) 48.dp else 140.dp

                Cover(
                    model = model,
                    modifier = modifier.width(width),
                )
            },
        ),
        galleryEntry(
            name = "ShareCard",
            family = GalleryFamily.SHARE,
            blurb = "The exportable share-card family — book, reading update, stat, quote, year recap, and reading life.",
            previews = ShareCardUiModel,
            label = ::shareCardFixtureLabel,
            content = { model, modifier ->
                // A share card sets its own fixed export width (`ShareCardDimensions`, 300-420dp),
                // wider than the gallery's fixture tile on a phone. Render it at its true size and
                // let it be panned rather than clipping or squashing an artifact meant for export.
                Box(
                    modifier = modifier.horizontalScroll(rememberScrollState()),
                ) {
                    ShareCard(
                        content = model,
                    )
                }
            },
        ),
    )

    val families: ImmutableList<GalleryFamily> = GalleryFamily.entries
        .filter { family -> entries.any { it.family == family } }
        .toImmutableList()

    fun entriesIn(family: GalleryFamily): ImmutableList<GalleryEntry> = entries
        .filter { it.family == family }
        .toImmutableList()
}

/** Names what a [RichTextUiModel] fixture demonstrates, derived from the marks it actually carries. */
private fun richTextFixtureLabel(model: RichTextUiModel): String {
    if (model.paragraphs.size > 1) return "Two paragraphs"

    val marks = model.paragraphs
        .flatMap { it.runs }
        .flatMap { run ->
            listOfNotNull(
                "bold".takeIf { run.bold },
                "italic".takeIf { run.italic },
                "spoiler".takeIf { run.spoiler },
            )
        }
        .toSet()

    return when {
        marks.isEmpty() -> "Plain prose"
        marks == setOf("spoiler") -> "A concealed spoiler"
        marks.size == 3 -> "All three marks"
        else -> marks.joinToString(separator = " + ") { it.replaceFirstChar(Char::uppercase) }
    }
}

/** Names what a [CoverUiModel] fixture demonstrates, derived from its anatomy branch. */
private fun coverFixtureLabel(model: CoverUiModel): String = when {
    model.isLoading -> "Loading"
    model.source != null -> "With cover art"
    model.variant == CoverVariant.EditionListRow -> "Coverless, thumbnail-sized"
    model.coverlessTitle.orEmpty().length > COVERLESS_LONG_TITLE_LABEL_FLOOR -> "Coverless, long title"
    else -> "Coverless, short title"
}

private const val COVERLESS_LONG_TITLE_LABEL_FLOOR = 30

/** Names which [ShareCardUiModel] variant a fixture is, distinguishing the two anatomy outliers. */
private fun shareCardFixtureLabel(model: ShareCardUiModel): String = when (model) {
    is BookShareCardUiModel -> if (model.description != null) "Book" else "Book (minimal)"
    is ReadingUpdateShareCardUiModel -> when (model.kind) {
        ReadingUpdateKind.FINISHED -> "Reading update (finished)"
        ReadingUpdateKind.READING -> "Reading update (in progress)"
    }
    is StatShareCardUiModel -> "Stat"
    is QuoteShareCardUiModel -> "Quote"
    is YearRecapShareCardUiModel -> "Year recap"
    is ReadingLifeShareCardUiModel -> "Reading life"
}
