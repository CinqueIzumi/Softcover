package nl.rhaydus.softcover.core.component.gallery

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.badge.Badge
import nl.rhaydus.softcover.core.component.badge.BadgeSize
import nl.rhaydus.softcover.core.component.badge.BadgeTone
import nl.rhaydus.softcover.core.component.badge.BadgeUiModel
import nl.rhaydus.softcover.core.component.badge.CoverOverlay
import nl.rhaydus.softcover.core.component.badge.CoverOverlayCorner
import nl.rhaydus.softcover.core.component.badge.CoverOverlayUiModel
import nl.rhaydus.softcover.core.component.button.PillButton
import nl.rhaydus.softcover.core.component.button.PillButtonContent
import nl.rhaydus.softcover.core.component.button.PillButtonInteraction
import nl.rhaydus.softcover.core.component.button.PillButtonSize
import nl.rhaydus.softcover.core.component.button.PillButtonTone
import nl.rhaydus.softcover.core.component.button.PillButtonUiModel
import nl.rhaydus.softcover.core.component.chip.Chip
import nl.rhaydus.softcover.core.component.chip.ChipFace
import nl.rhaydus.softcover.core.component.chip.ChipInteraction
import nl.rhaydus.softcover.core.component.chip.ChipLeading
import nl.rhaydus.softcover.core.component.chip.ChipTone
import nl.rhaydus.softcover.core.component.chip.ChipTrailing
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.cover.CoverlessTitleCover
import nl.rhaydus.softcover.core.component.deadline.DeadlineSummaryLine
import nl.rhaydus.softcover.core.component.deadline.DeadlineSummaryTone
import nl.rhaydus.softcover.core.component.deadline.DeadlineSummaryUiModel
import nl.rhaydus.softcover.core.component.header.HeaderTextSegment
import nl.rhaydus.softcover.core.component.header.MastheadRefreshAction
import nl.rhaydus.softcover.core.component.header.MastheadRefreshUiModel
import nl.rhaydus.softcover.core.component.header.PageMasthead
import nl.rhaydus.softcover.core.component.header.PageMastheadSize
import nl.rhaydus.softcover.core.component.header.PageMastheadUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.richtext.RichText
import nl.rhaydus.softcover.core.component.richtext.RichTextUiModel
import nl.rhaydus.softcover.core.component.selection.SelectionMark
import nl.rhaydus.softcover.core.component.selection.SelectionMarkBackdrop
import nl.rhaydus.softcover.core.component.selection.SelectionMarkUiModel

internal val headerEntries: ImmutableList<GalleryEntry> = persistentListOf(
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
        blurb = "The pill-shaped chip family: tonal, spoiler, add, add-outlined, remove, " +
            "quiet, format, choice, dashed, and editable variants.",
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
        name = "PillButton",
        family = GalleryFamily.BUTTON,
        blurb = "A fixed-height action pill — label or icon-only circle — in tonal, accent, " +
            "filled, outlined and quiet tones, with an optional active dot.",
        previews = PillButtonUiModel,
        label = ::pillButtonFixtureLabel,
        content = { model, modifier ->
            PillButton(
                model = model,
                onEvent = {},
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
        blurb = "A Badge pinned to one of a cover's corners, with the cover itself optionally " +
            "desaturated — a reading deadline's status, or a countdown, laid over its book.",
        previews = CoverOverlayUiModel,
        label = ::coverOverlayFixtureLabel,
        content = { model, modifier ->
            CoverOverlay(
                model = model,
                modifier = modifier.width(96.dp),
            ) {
                CoverlessTitleCover(
                    title = "Piranesi",
                    modifier = Modifier.width(96.dp),
                )
            }
        },
    ),
    galleryEntry(
        name = "SelectionMark",
        family = GalleryFamily.SELECTION,
        blurb = "A circular selection mark — filled with a check when selected, an empty ring " +
            "otherwise — for a library grid cover, a row's leading indicator, or an edition card.",
        previews = SelectionMarkUiModel,
        label = ::selectionMarkFixtureLabel,
        content = { model, modifier ->
            SelectionMark(
                model = model,
                modifier = modifier,
            )
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
        name = "SectionHeader",
        family = GalleryFamily.HEADER,
        blurb = "Opens a region within a page or sheet: an accent bar in one of three weights, " +
            "an uppercased eyebrow, and — at its heaviest — an optional headline and description.",
        previews = SectionHeaderUiModel,
        label = ::sectionHeaderFixtureLabel,
        content = { model, modifier ->
            SectionHeader(
                model = model,
                modifier = modifier,
            )
        },
    ),
    GalleryEntry(
        name = "PageMasthead",
        family = GalleryFamily.HEADER,
        blurb = "Names the page, once per page: an optional eyebrow, the title, and an optional " +
            "subtitle, sized for the surface that hosts it.",
        fixtures = (
            PageMastheadUiModel.previews.map { model ->
                GalleryFixture(
                    label = pageMastheadFixtureLabel(model),
                    render = { modifier ->
                        PageMasthead(
                            model = model,
                            modifier = modifier,
                        ) {
                            if (model.eyebrow == null && model.size == PageMastheadSize.Regular) {
                                MastheadRefreshAction(
                                    model = MastheadRefreshUiModel(
                                        label = "Refresh",
                                        enabled = true,
                                    ),
                                    onEvent = {},
                                )
                            }
                        }
                    },
                )
            } + GalleryFixture(
                label = "Loading subtitle",
                render = { modifier ->
                    PageMasthead(
                        model = PageMastheadUiModel(
                            title = "Want to read",
                            eyebrow = "Your collection",
                            subtitle = "128 books · 34,000 pages",
                        ),
                        modifier = modifier,
                        isLoading = true,
                    )
                },
            )
            ).toImmutableList(),
    ),
    galleryEntry(
        name = "MastheadRefreshAction",
        family = GalleryFamily.HEADER,
        blurb = "A masthead's trailing text action — the desktop headers' refresh control.",
        previews = MastheadRefreshUiModel,
        label = { model -> if (model.enabled) "Idle" else "Refreshing" },
        content = { model, modifier ->
            MastheadRefreshAction(
                model = model,
                onEvent = {},
                modifier = modifier,
            )
        },
    ),
)

private fun chipFixtureLabel(model: ChipUiModel): String = when {
    model.leading is ChipLeading.SpoilerToggle && model.leading.marked -> "Spoiler toggle, marked"
    model.leading is ChipLeading.SpoilerToggle -> "Spoiler toggle"
    model.tone == ChipTone.Spoiler -> "Spoiler"
    model.tone == ChipTone.Dashed -> "Dashed"
    model.tone == ChipTone.Choice && model.selected -> "Choice, selected"
    model.tone == ChipTone.Choice -> "Choice"
    model.tone == ChipTone.Filled -> "Filled"
    model.tone == ChipTone.Container -> "Container"
    model.tone == ChipTone.Outlined -> "Outlined"
    model.trailing is ChipTrailing.Dismiss -> "Dismissible"
    model.tone == ChipTone.Tonal && model.selected -> "Selected"
    model.interaction == ChipInteraction.Disabled -> "Disabled"
    model.interaction == ChipInteraction.Inert -> "Read-only"
    model.face != ChipFace.Plain -> "Format"
    model.leading != null -> "Leading icon"
    model.label.length > CHIP_LONG_LABEL_FLOOR -> "Long label, ellipsised"
    else -> "Idle"
}

private const val CHIP_LONG_LABEL_FLOOR = 30

private fun pillButtonFixtureLabel(model: PillButtonUiModel): String = when {
    model.interaction == PillButtonInteraction.Disabled -> "Disabled"
    model.content is PillButtonContent.IconOnly -> "Icon only, ${model.tone.name}"
    (model.content as? PillButtonContent.Label)?.indicator == true -> "${model.tone.name}, with indicator"
    model.size == PillButtonSize.Large -> "Large ${model.tone.name}"
    else -> "Regular ${model.tone.name}"
}

/** Names what a [BadgeUiModel] fixture demonstrates — its icon or size when that differs, else its tone. */
private fun badgeFixtureLabel(model: BadgeUiModel): String = when {
    model.icon != null -> "${model.label}, with icon"
    model.size == BadgeSize.Regular -> "${model.label}, regular"
    else -> when (model.tone) {
        BadgeTone.PrimaryContainer -> "On track"
        BadgeTone.ErrorContainer -> "Behind"
        BadgeTone.Neutral -> "Expired"
        BadgeTone.Primary -> "Release"
        BadgeTone.Scrim -> model.label
    }
}

/** Names what a [SectionHeaderUiModel] fixture demonstrates, derived from its register. */
private fun sectionHeaderFixtureLabel(model: SectionHeaderUiModel): String = when (model) {
    is SectionHeaderUiModel.Section -> when {
        (model.headline.orEmpty() + model.description.orEmpty())
            .any { it is HeaderTextSegment.Emphasised } -> "Section, with emphasis"
        model.headline != null -> "Section, with headline"
        else -> "Section, eyebrow only"
    }
    is SectionHeaderUiModel.Inline -> "Inline"
    is SectionHeaderUiModel.Label -> "Label"
}

/** Names what a [PageMastheadUiModel] fixture demonstrates — its size, or its eyebrow when present. */
private fun pageMastheadFixtureLabel(model: PageMastheadUiModel): String = when {
    model.size == PageMastheadSize.Compact -> "Compact"
    model.eyebrow != null -> "Regular, with eyebrow"
    else -> "Regular"
}

/** Names what a [CoverOverlayUiModel] fixture demonstrates — its corner, or grayscale when that differs. */
private fun coverOverlayFixtureLabel(model: CoverOverlayUiModel): String = when {
    model.corner == CoverOverlayCorner.TopStart -> "Library countdown (top-start)"
    model.grayscale -> "Expired (grayscale cover)"
    else -> "On track"
}

/** Names what a [SelectionMarkUiModel] fixture demonstrates — its backdrop and selected state. */
private fun selectionMarkFixtureLabel(model: SelectionMarkUiModel): String {
    val backdrop = when (model.backdrop) {
        SelectionMarkBackdrop.Image -> "On image"
        SelectionMarkBackdrop.Surface -> "On surface"
    }

    return if (model.selected) "$backdrop, selected" else "$backdrop, unselected"
}

/** Names what a [DeadlineSummaryUiModel] fixture demonstrates, derived from its anatomy branch. */
private fun deadlineSummaryLineFixtureLabel(model: DeadlineSummaryUiModel): String = when {
    model.tone == DeadlineSummaryTone.OnHeroBackdrop -> "On hero backdrop"
    model.paceText == "Expired" -> "Expired"
    model.paceText.contains("m/day") -> "Audio pace"
    model.paceText.startsWith("1149") -> "Extreme pace, ellipsised"
    else -> "Pages pace"
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
