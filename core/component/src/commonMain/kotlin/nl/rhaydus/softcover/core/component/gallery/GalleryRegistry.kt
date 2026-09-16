package nl.rhaydus.softcover.core.component.gallery

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.badge.Badge
import nl.rhaydus.softcover.core.component.badge.BadgeTone
import nl.rhaydus.softcover.core.component.badge.BadgeUiModel
import nl.rhaydus.softcover.core.component.badge.BadgeVariant
import nl.rhaydus.softcover.core.component.badge.CoverOverlay
import nl.rhaydus.softcover.core.component.badge.CoverOverlayUiModel
import nl.rhaydus.softcover.core.component.badge.DeadlineSummaryLine
import nl.rhaydus.softcover.core.component.badge.DeadlineSummaryTone
import nl.rhaydus.softcover.core.component.badge.DeadlineSummaryUiModel
import nl.rhaydus.softcover.core.component.callout.Banner
import nl.rhaydus.softcover.core.component.callout.BannerUiModel
import nl.rhaydus.softcover.core.component.celebration.MarkAsReadBurst
import nl.rhaydus.softcover.core.component.celebration.MarkAsReadBurstUiModel
import nl.rhaydus.softcover.core.component.chip.Chip
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.control.ColorPalettePreviewTile
import nl.rhaydus.softcover.core.component.control.ColorPalettePreviewTileUiModel
import nl.rhaydus.softcover.core.component.control.ThemePreviewTile
import nl.rhaydus.softcover.core.component.control.ThemePreviewTileUiModel
import nl.rhaydus.softcover.core.component.cover.Cover
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.component.cover.CoverlessTitleCover
import nl.rhaydus.softcover.core.component.richtext.ClickableText
import nl.rhaydus.softcover.core.component.richtext.ClickableTextUiModel
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
import nl.rhaydus.softcover.core.component.state.EmptyState
import nl.rhaydus.softcover.core.component.state.EmptyStateUiModel
import nl.rhaydus.softcover.core.component.statistic.StatNumber
import nl.rhaydus.softcover.core.component.statistic.StatNumberFormat
import nl.rhaydus.softcover.core.component.statistic.StatNumberUiModel
import nl.rhaydus.softcover.core.component.topbar.SearchTopBar
import nl.rhaydus.softcover.core.component.topbar.SearchTopBarUiModel
import nl.rhaydus.softcover.core.component.topbar.TopBar
import nl.rhaydus.softcover.core.component.topbar.TopBarNavigation
import nl.rhaydus.softcover.core.component.topbar.TopBarSurface
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel

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
            name = "Chip",
            family = GalleryFamily.CHIP,
            blurb = "The pill-shaped chip: one label on a fully-rounded surface, optionally " +
                "selected, redacted as a spoiler, or read-only.",
            previews = ChipUiModel,
            label = ::chipFixtureLabel,
            content = { model, modifier ->
                Chip(
                    model = model,
                    modifier = modifier,
                )
            },
        ),
        galleryEntry(
            name = "Badge",
            family = GalleryFamily.BADGE,
            blurb = "A small pill reporting a status in one word or a short phrase — a reading " +
                "deadline's pace, or an edition's release date.",
            previews = BadgeUiModel,
            label = ::badgeFixtureLabel,
            content = { model, modifier ->
                Badge(
                    model = model,
                    modifier = modifier,
                )
            },
        ),
        galleryEntry(
            name = "CoverOverlay",
            family = GalleryFamily.BADGE,
            blurb = "A Badge pinned to a cover's top-end corner, with the cover itself optionally " +
                "desaturated — a reading deadline's status laid over its book.",
            previews = CoverOverlayUiModel,
            label = ::coverOverlayFixtureLabel,
            content = { model, modifier ->
                CoverOverlay(
                    model = model,
                    modifier = modifier.width(BADGE_COVER_FIXTURE_WIDTH),
                ) {
                    CoverlessTitleCover(
                        title = "Piranesi",
                        modifier = Modifier.width(BADGE_COVER_FIXTURE_WIDTH),
                    )
                }
            },
        ),
        galleryEntry(
            name = "DeadlineSummaryLine",
            family = GalleryFamily.BADGE,
            blurb = "The deadline date paired with the pace needed to still make it, or the status " +
                "label alone once the deadline has passed.",
            previews = DeadlineSummaryUiModel,
            label = ::deadlineSummaryLineFixtureLabel,
            content = { model, modifier ->
                DeadlineSummaryLine(
                    model = model,
                    modifier = modifier,
                )
            },
        ),
        galleryEntry(
            name = "TopBar",
            family = GalleryFamily.TOPBAR,
            blurb = "The page bar: an autosizing centred title, an optional subtitle, an optional " +
                "back affordance, and a slot for the screen's own actions.",
            previews = TopBarUiModel,
            label = ::topBarFixtureLabel,
            content = { model, modifier ->
                TopBar(
                    model = model,
                    onEvent = {},
                    modifier = modifier,
                )
            },
        ),
        galleryEntry(
            name = "SearchTopBar",
            family = GalleryFamily.TOPBAR,
            blurb = "The search chrome: a rounded pill holding the query beside a barcode-scan " +
                "button. Kept separate from TopBar (§ 7.6) — its focus contract has no counterpart there.",
            previews = SearchTopBarUiModel,
            label = ::searchTopBarFixtureLabel,
            content = { model, modifier ->
                SearchTopBar(
                    model = model,
                    onEvent = {},
                    modifier = modifier,
                )
            },
        ),
        galleryEntry(
            name = "Banner",
            family = GalleryFamily.CALLOUT,
            blurb = "A full-width notice that slides in above a screen's content — today the " +
                "offline report, raised once at the app root.",
            previews = BannerUiModel,
            label = ::bannerFixtureLabel,
            content = { model, modifier ->
                Banner(
                    model = model,
                    modifier = modifier,
                )
            },
        ),
        galleryEntry(
            name = "EmptyState",
            family = GalleryFamily.STATE,
            blurb = "The centred headline-and-body message a screen shows in place of content it " +
                "has nothing to draw.",
            previews = EmptyStateUiModel,
            label = { model -> model.title },
            content = { model, modifier ->
                EmptyState(
                    model = model,
                    modifier = modifier.height(EMPTY_STATE_FIXTURE_HEIGHT),
                )
            },
        ),
        galleryEntry(
            name = "StatNumber",
            family = GalleryFamily.STATISTIC,
            blurb = "A number bound to live state: it tweens between values, holds tabular figures " +
                "so digits don't jitter, and ticks a hairline on each integer crossing.",
            previews = StatNumberUiModel,
            label = ::statNumberFixtureLabel,
            content = { model, modifier ->
                StatNumber(
                    model = model,
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = modifier,
                )
            },
        ),
        galleryEntry(
            name = "ThemePreviewTile",
            family = GalleryFamily.CONTROL,
            blurb = "One choice in the theme picker: the app's own page in miniature, painted in " +
                "the scheme that choice would actually give.",
            previews = ThemePreviewTileUiModel,
            label = { model -> model.label },
            content = { model, modifier ->
                ThemePreviewTile(
                    model = model,
                    onEvent = {},
                    modifier = modifier.width(PREVIEW_TILE_FIXTURE_WIDTH),
                )
            },
        ),
        galleryEntry(
            name = "ColorPalettePreviewTile",
            family = GalleryFamily.CONTROL,
            blurb = "One choice in the spine-colour picker: the same miniature, painted in that " +
                "palette entire — its paper as well as its ink.",
            previews = ColorPalettePreviewTileUiModel,
            label = { model -> model.palette.label },
            content = { model, modifier ->
                ColorPalettePreviewTile(
                    model = model,
                    onEvent = {},
                    modifier = modifier.width(PREVIEW_TILE_FIXTURE_WIDTH),
                )
            },
        ),
        galleryEntry(
            name = "ClickableText",
            family = GalleryFamily.RICHTEXT,
            blurb = "Prose with tappable runs in it — the component resolves the link annotations " +
                "so a screen never wires its own tap detection.",
            previews = ClickableTextUiModel,
            label = { "With an inline link" },
            content = { model, modifier ->
                ClickableText(
                    model = model,
                    onEvent = {},
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = modifier,
                )
            },
        ),
        galleryEntry(
            name = "MarkAsReadBurst",
            family = GalleryFamily.CELEBRATION,
            blurb = "The radial particle burst played on a mark-as-read commit. A still fixture " +
                "cannot show it — the motion debug screen is where you watch one.",
            previews = MarkAsReadBurstUiModel,
            label = { "Burst" },
            content = { model, modifier ->
                MarkAsReadBurst(
                    model = model,
                    modifier = modifier.size(BURST_FIXTURE_SIZE),
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

/**
 * `EmptyState` fills whatever space it is given, and a gallery fixture tile has no height of its
 * own — so the tile lends it one rather than the component learning about the gallery (§ 7.3).
 */
private val EMPTY_STATE_FIXTURE_HEIGHT = 180.dp

/** The Appearance pickers size their tiles to the row they sit in; the gallery picks one width. */
private val PREVIEW_TILE_FIXTURE_WIDTH = 120.dp

/** The burst draws outward from the centre of whatever footprint it is given. */
private val BURST_FIXTURE_SIZE = 160.dp

/** `CoverOverlay`'s fixture cover — sized like the gallery's other thumbnail-ish jackets. */
private val BADGE_COVER_FIXTURE_WIDTH = 96.dp

/** Names what a [ChipUiModel] fixture demonstrates — its anatomy branch, not its words. */
private fun chipFixtureLabel(model: ChipUiModel): String = when {
    model.concealed -> "Concealed (spoiler)"
    model.selected -> "Selected"
    model.clickable.not() -> "Read-only"
    model.label.length > CHIP_LONG_LABEL_FLOOR -> "Long label, ellipsised"
    else -> "Idle"
}

private const val CHIP_LONG_LABEL_FLOOR = 30

/** Names what a [BadgeUiModel] fixture demonstrates — its tone, or its variant when that differs. */
private fun badgeFixtureLabel(model: BadgeUiModel): String = when {
    model.variant == BadgeVariant.FeaturedRelease -> "Release, featured (larger pad)"
    else -> when (model.tone) {
        BadgeTone.OnTrack -> "On track"
        BadgeTone.Behind -> "Behind"
        BadgeTone.Expired -> "Expired"
        BadgeTone.Release -> "Release"
    }
}

/** Names what a [CoverOverlayUiModel] fixture demonstrates — whether the cover desaturates. */
private fun coverOverlayFixtureLabel(model: CoverOverlayUiModel): String =
    if (model.grayscale) "Expired (grayscale cover)" else "On track"

/** Names what a [DeadlineSummaryUiModel] fixture demonstrates, derived from its anatomy branch. */
private fun deadlineSummaryLineFixtureLabel(model: DeadlineSummaryUiModel): String = when {
    model.tone == DeadlineSummaryTone.OnHeroBackdrop -> "On hero backdrop"
    model.paceText == "Expired" -> "Expired"
    model.paceText.contains("m/day") -> "Audio pace"
    model.paceText.startsWith("1149") -> "Extreme pace, ellipsised"
    else -> "Pages pace"
}

/** Names what a [TopBarUiModel] fixture demonstrates, derived from its anatomy branch. */
private fun topBarFixtureLabel(model: TopBarUiModel): String = when {
    model.surface == TopBarSurface.OVER_MEDIA -> "Over cover art"
    model.subtitle != null -> "With a subtitle"
    model.navigation == TopBarNavigation.None -> "Root surface"
    model.title.length > TOP_BAR_LONG_TITLE_FLOOR -> "Long title, autosized"
    else -> "With back"
}

private const val TOP_BAR_LONG_TITLE_FLOOR = 30

/** Names what a [SearchTopBarUiModel] fixture demonstrates. */
private fun searchTopBarFixtureLabel(model: SearchTopBarUiModel): String = when {
    model.isLoading -> "Searching"
    model.active -> "Focused, empty"
    else -> "Resting"
}

/** Names what a [BannerUiModel] fixture demonstrates — its tone is its anatomy. */
private fun bannerFixtureLabel(model: BannerUiModel): String =
    model.tone.name.lowercase().replaceFirstChar(Char::uppercase)

/** Names what a [StatNumberUiModel] fixture demonstrates — its format, which is its anatomy. */
private fun statNumberFixtureLabel(model: StatNumberUiModel): String = when (model.format) {
    StatNumberFormat.Grouped -> "Grouped thousands"
    StatNumberFormat.Plain -> "Plain integer"
    is StatNumberFormat.Decimal -> "One decimal"
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
