package nl.rhaydus.softcover.core.component.gallery

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.bookcard.BookCard
import nl.rhaydus.softcover.core.component.bookcard.BookCardLead
import nl.rhaydus.softcover.core.component.bookcard.BookCardSelection
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardVariant
import nl.rhaydus.softcover.core.component.cover.Cover
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.component.mood.MoodTile
import nl.rhaydus.softcover.core.component.mood.MoodTileUiModel
import nl.rhaydus.softcover.core.component.share.BookShareCardUiModel
import nl.rhaydus.softcover.core.component.share.QuoteShareCardUiModel
import nl.rhaydus.softcover.core.component.share.ReadingLifeShareCardUiModel
import nl.rhaydus.softcover.core.component.share.ReadingUpdateKind
import nl.rhaydus.softcover.core.component.share.ReadingUpdateShareCardUiModel
import nl.rhaydus.softcover.core.component.share.ShareCard
import nl.rhaydus.softcover.core.component.share.ShareCardUiModel
import nl.rhaydus.softcover.core.component.share.StatShareCardUiModel
import nl.rhaydus.softcover.core.component.share.YearRecapShareCardUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetHeaderTrailing
import nl.rhaydus.softcover.core.component.sheet.SheetLayout
import nl.rhaydus.softcover.core.component.sheet.SheetRow
import nl.rhaydus.softcover.core.component.sheet.SheetRowUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldContent
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel
import nl.rhaydus.softcover.core.component.sidebar.Sidebar
import nl.rhaydus.softcover.core.component.sidebar.SidebarUiModel
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.component.verdict.VerdictBlock
import nl.rhaydus.softcover.core.component.verdict.VerdictBlockUiModel

internal val bookAndSheetEntries: ImmutableList<GalleryEntry> = persistentListOf(
    galleryEntry(
        name = "Cover",
        family = GalleryFamily.COVER,
        blurb = "A book/edition cover jacket — loaded art, or a monogram fallback that degrades " +
            "to a single initial on a thumbnail-sized tile. No fixture here carries a live network " +
            "URL, matching every other family in this gallery.",
        previews = CoverUiModel,
        label = ::coverFixtureLabel,
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
        label = ::bookCardFixtureLabel,
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
        name = "MoodTile",
        family = GalleryFamily.MOOD,
        blurb = "A tappable mood tile in one of four inks, opening a mood-filtered search; loading masks " +
            "its text and icon and drops the press.",
        previews = MoodTileUiModel,
        label = { model -> if (model.isLoading) "Loading" else "${model.ink.name} ink" },
        content = { model, modifier ->
            MoodTile(
                model = model,
                onEvent = {},
                modifier = modifier.width(160.dp),
            )
        },
    ),
    galleryEntry(
        name = "Sidebar",
        family = GalleryFamily.ROW,
        blurb = "A desktop source list: sections of rows, each with an optional leading icon, " +
            "a selected tint, a trailing arrow, or a long press.",
        previews = SidebarUiModel,
        label = ::sidebarFixtureLabel,
        content = { model, modifier ->
            Sidebar(
                model = model,
                onEvent = {},
                modifier = modifier,
            )
        },
    ),
    galleryEntry(
        name = "SheetScaffold",
        family = GalleryFamily.SHEET,
        blurb = "The chrome every modal sheet shares — dismiss label, editorial header with an optional " +
            "trailing jacket, jacket stack or action, and a footer. Scrolling lets the whole sheet " +
            "scroll; Pinned fixes the header and footer around a body that scrolls itself. Shown " +
            "inline, without the modal container.",
        previews = SheetScaffoldUiModel,
        label = ::sheetScaffoldFixtureLabel,
        content = { model, modifier ->
            SheetScaffoldContent(
                model = model,
                onEvent = {},
                modifier = if (model.layout != SheetLayout.Scrolling) {
                    modifier.height(420.dp)
                } else {
                    modifier.heightIn(max = 420.dp)
                },
            ) {
                SoftcoverText(
                    text = "The sheet's own body goes here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    ),
    galleryEntry(
        name = "SheetRow",
        family = GalleryFamily.SHEET,
        blurb = "One ruled row of a single-choice sheet list: italic label, optional leading icon " +
            "and count, and a check that marks the selection.",
        previews = SheetRowUiModel,
        label = ::sheetRowFixtureLabel,
        content = { model, modifier ->
            SheetRow(
                model = model,
                onEvent = {},
                modifier = modifier,
            )
        },
    ),
    galleryEntry(
        name = "VerdictBlock",
        family = GalleryFamily.VERDICT,
        blurb = "The book page's read-only \"Your verdict\" card: an invitation when empty, otherwise " +
            "the gold star row and score, the review, and a note when it holds spoilers.",
        previews = VerdictBlockUiModel,
        label = ::verdictBlockFixtureLabel,
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

/** Names what a [CoverUiModel] fixture demonstrates, derived from its anatomy branch. */
private fun coverFixtureLabel(model: CoverUiModel): String = when {
    model.isLoading -> "Loading"
    model.source != null -> "With cover art"
    model.variant == CoverVariant.Flat -> "Coverless, thumbnail-sized"
    model.coverlessTitle.orEmpty().length > COVERLESS_LONG_TITLE_LABEL_FLOOR -> "Coverless, long title"
    else -> "Coverless, short title"
}

private const val COVERLESS_LONG_TITLE_LABEL_FLOOR = 30

private fun sheetScaffoldFixtureLabel(model: SheetScaffoldUiModel): String = when {
    model.dismissible.not() -> "Not dismissible"
    model.busy -> "Busy"
    model.dismissLabel != null -> "Scrolling, dismiss label and jacket"
    model.layout == SheetLayout.Scrolling -> "Scrolling, footer"
    model.header.trailing is SheetHeaderTrailing.Action -> "Pinned, header action"
    else -> "Pinned, jacket stack and inline footer"
}

private fun sheetRowFixtureLabel(model: SheetRowUiModel<String>): String = when {
    model.leadingIcon != null && model.selected -> "Selected, icon and count"
    model.leadingIcon != null -> "Icon and count"
    model.selected -> "Selected"
    else -> "Unselected"
}

private fun sidebarFixtureLabel(model: SidebarUiModel<String>): String =
    if (model.sections.firstOrNull()?.label != null) "With label" else "No label"

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

private fun verdictBlockFixtureLabel(model: VerdictBlockUiModel): String = when {
    model.rating == null && model.review == null -> "Empty"
    model.review == null -> "Rating only"
    model.rating == null -> "Review only"
    else -> "Rating and review"
}

private fun bookCardGallerySize(model: BookCardUiModel): Modifier = if (model.variant is BookCardVariant.Row) {
    Modifier.fillMaxWidth()
} else {
    Modifier.width(140.dp)
}

/** Names a [BookCardUiModel] fixture by its layout and the decorations that change its anatomy. */
private fun bookCardFixtureLabel(model: BookCardUiModel): String {
    val layout = when (val variant = model.variant) {
        is BookCardVariant.CoverOnly -> "Cover only"
        is BookCardVariant.Column -> "Column"
        is BookCardVariant.Row -> "Row ${variant.size.name}"
    }

    val row = model.variant as? BookCardVariant.Row
    val decorations = model.decorations
    val selection = decorations.selection
    val badged = when (val variant = model.variant) {
        is BookCardVariant.CoverOnly -> variant.cover.badge != null
        is BookCardVariant.Column -> variant.cover?.badge != null
        is BookCardVariant.Row -> (variant.lead as? BookCardLead.Cover)?.cover?.badge != null
    }

    val details = listOfNotNull(
        "loading".takeIf { model.isLoading },
        "single choice".takeIf { selection is BookCardSelection.Single },
        "selected".takeIf { selection?.selected == true },
        "unselected".takeIf { selection?.selected == false },
        "progress".takeIf { decorations.progress != null },
        "cover badge".takeIf { badged },
        "deadline".takeIf { row?.deadline != null },
        "series stack".takeIf { row?.lead is BookCardLead.SeriesStack },
        "coverless".takeIf { row != null && row.lead == null },
        row?.trailing?.let { it::class.simpleName },
    )

    return (listOf(layout) + details).joinToString(separator = ", ")
}
