package nl.rhaydus.softcover.core.component.gallery

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.badge.Badge
import nl.rhaydus.softcover.core.component.badge.BadgeUiModel
import nl.rhaydus.softcover.core.component.badge.CoverOverlay
import nl.rhaydus.softcover.core.component.badge.CoverOverlayUiModel
import nl.rhaydus.softcover.core.component.button.MorphingButton
import nl.rhaydus.softcover.core.component.button.MorphingButtonTone
import nl.rhaydus.softcover.core.component.button.PillButton
import nl.rhaydus.softcover.core.component.button.PillButtonUiModel
import nl.rhaydus.softcover.core.component.chip.Chip
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.component.cover.CoverlessTitleCover
import nl.rhaydus.softcover.core.component.deadline.DeadlineRow
import nl.rhaydus.softcover.core.component.deadline.DeadlineRowUiModel
import nl.rhaydus.softcover.core.component.deadline.DeadlineSummaryLine
import nl.rhaydus.softcover.core.component.deadline.DeadlineSummaryUiModel
import nl.rhaydus.softcover.core.component.header.MastheadRefreshAction
import nl.rhaydus.softcover.core.component.header.MastheadRefreshUiModel
import nl.rhaydus.softcover.core.component.header.PageMasthead
import nl.rhaydus.softcover.core.component.header.PageMastheadSize
import nl.rhaydus.softcover.core.component.header.PageMastheadUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.richtext.RichText
import nl.rhaydus.softcover.core.component.richtext.RichTextUiModel
import nl.rhaydus.softcover.core.component.selection.SelectionCheck
import nl.rhaydus.softcover.core.component.selection.SelectionMark
import nl.rhaydus.softcover.core.component.selection.SelectionMarkBackdrop

internal val primitiveEntries: ImmutableList<GalleryEntry> = persistentListOf(
    galleryEntry(
        name = "RichText",
        family = GalleryFamily.RICHTEXT,
        blurb = "Read-only formatted prose with inline bold, italic, and tap-to-reveal spoiler marks.",
        previews = RichTextUiModel,
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
        blurb = "The pill-shaped chip family: tonal, choice, filled, container, outlined, " +
            "dashed and spoiler tones, with optional leading and trailing icons.",
        previews = ChipUiModel,
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
        content = { model, modifier ->
            PillButton(
                model = model,
                onEvent = {},
                modifier = modifier,
            )
        },
    ),
    GalleryEntry(
        name = "MorphingButton",
        family = GalleryFamily.BUTTON,
        blurb = "A text button that animates between the filled and tonal styles: colour, label " +
            "crossfade and width. Tap the third fixture to see the morph.",
        fixtures = persistentListOf(
            GalleryFixture(
                label = "Filled",
                render = { modifier ->
                    MorphingButton(
                        label = "Want to read",
                        tone = MorphingButtonTone.Filled,
                        onClick = {},
                        modifier = modifier,
                    )
                },
            ),
            GalleryFixture(
                label = "Tonal",
                render = { modifier ->
                    MorphingButton(
                        label = "Remove from library",
                        tone = MorphingButtonTone.Tonal,
                        onClick = {},
                        modifier = modifier,
                    )
                },
            ),
            GalleryFixture(
                label = "Tap to morph",
                render = { modifier ->
                    var inLibrary by remember { mutableStateOf(false) }

                    MorphingButton(
                        label = if (inLibrary) "Remove from library" else "Want to read",
                        tone = if (inLibrary) MorphingButtonTone.Tonal else MorphingButtonTone.Filled,
                        onClick = { inLibrary = inLibrary.not() },
                        modifier = modifier,
                    )
                },
            ),
        ),
    ),
    galleryEntry(
        name = "Badge",
        family = GalleryFamily.BADGE,
        blurb = "A small pill reporting a status in one word or a short phrase — a reading " +
            "deadline's pace, or an edition's release date.",
        previews = BadgeUiModel,
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
        content = { model, modifier ->
            CoverOverlay(
                model = model,
                modifier = modifier.width(96.dp),
            ) {
                CoverlessTitleCover(
                    model = CoverUiModel(
                        source = null,
                        coverlessTitle = "Piranesi",
                        variant = CoverVariant.Shelf,
                    ),
                    modifier = Modifier.width(96.dp),
                )
            }
        },
    ),
    GalleryEntry(
        name = "SelectionMark",
        family = GalleryFamily.SELECTION,
        blurb = "A circular selection mark — filled with a check when selected, an empty ring " +
            "otherwise — for a library grid cover, a row's leading indicator, or an edition card.",
        fixtures = persistentListOf(
            selectionMarkFixture(
                backdrop = SelectionMarkBackdrop.Image,
                selected = true,
            ),
            selectionMarkFixture(
                backdrop = SelectionMarkBackdrop.Image,
                selected = false,
            ),
            selectionMarkFixture(
                backdrop = SelectionMarkBackdrop.Surface,
                selected = true,
            ),
            selectionMarkFixture(
                backdrop = SelectionMarkBackdrop.Surface,
                selected = false,
            ),
        ),
    ),
    GalleryEntry(
        name = "SelectionCheck",
        family = GalleryFamily.SELECTION,
        blurb = "An accent check that pops in when selected and keeps its space when not, " +
            "for a sheet row or a settings choice row.",
        fixtures = persistentListOf(
            GalleryFixture(
                label = "Selected",
                render = { modifier ->
                    SelectionCheck(
                        selected = true,
                        modifier = modifier,
                    )
                },
            ),
            GalleryFixture(
                label = "Unselected",
                render = { modifier ->
                    SelectionCheck(
                        selected = false,
                        modifier = modifier,
                    )
                },
            ),
        ),
    ),
    galleryEntry(
        name = "DeadlineSummaryLine",
        family = GalleryFamily.DEADLINE,
        blurb = "The deadline date paired with the pace needed to still make it, or the status " +
            "label alone once the deadline has passed.",
        previews = DeadlineSummaryUiModel,
        content = { model, modifier ->
            DeadlineSummaryLine(
                model = model,
                modifier = modifier,
            )
        },
    ),
    galleryEntry(
        name = "DeadlineRow",
        family = GalleryFamily.DEADLINE,
        blurb = "Book detail's deadline card: the finish-by date and status badge over the goal " +
            "sentence.",
        previews = DeadlineRowUiModel,
        content = { model, modifier ->
            DeadlineRow(
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
        content = { model, modifier ->
            SectionHeader(
                model = model,
                modifier = modifier,
            )
        },
    ),
    galleryEntry(
        name = "PageMasthead",
        family = GalleryFamily.HEADER,
        blurb = "Names the page, once per page: an optional eyebrow, the title, and an optional " +
            "subtitle, sized for the surface that hosts it.",
        previews = PageMastheadUiModel,
        content = { model, modifier ->
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
    ),
    galleryEntry(
        name = "MastheadRefreshAction",
        family = GalleryFamily.HEADER,
        blurb = "A masthead's trailing text action — the desktop headers' refresh control.",
        previews = MastheadRefreshUiModel,
        content = { model, modifier ->
            MastheadRefreshAction(
                model = model,
                onEvent = {},
                modifier = modifier,
            )
        },
    ),
)

private fun selectionMarkFixture(
    backdrop: SelectionMarkBackdrop,
    selected: Boolean,
): GalleryFixture {
    val backdropLabel = when (backdrop) {
        SelectionMarkBackdrop.Image -> "On image"
        SelectionMarkBackdrop.Surface -> "On surface"
    }

    return GalleryFixture(
        label = if (selected) "$backdropLabel, selected" else "$backdropLabel, unselected",
        render = { modifier ->
            SelectionMark(
                selected = selected,
                modifier = modifier,
                backdrop = backdrop,
            )
        },
    )
}
