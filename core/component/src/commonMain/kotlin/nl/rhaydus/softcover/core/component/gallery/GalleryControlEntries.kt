package nl.rhaydus.softcover.core.component.gallery

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.control.SegmentedControl
import nl.rhaydus.softcover.core.component.control.SegmentedControlUiModel
import nl.rhaydus.softcover.core.component.control.SegmentedControlVariant
import nl.rhaydus.softcover.core.component.control.Toggle
import nl.rhaydus.softcover.core.component.control.ToggleIconStyle
import nl.rhaydus.softcover.core.component.control.ToggleUiModel
import nl.rhaydus.softcover.core.component.control.ToggleVariant
import nl.rhaydus.softcover.core.component.richtext.RichTextFormattingToolbar
import nl.rhaydus.softcover.core.component.richtext.RichTextFormattingToolbarUiModel

internal val controlEntries: ImmutableList<GalleryEntry> = persistentListOf(
    galleryEntry(
        name = "SegmentedControl",
        family = GalleryFamily.CONTROL,
        blurb = "A one-of-N choice: a full-width pill track, or the options as words in a sentence.",
        previews = SegmentedControlUiModel,
        label = ::segmentedControlFixtureLabel,
        content = { model, modifier ->
            SegmentedControl(
                model = model,
                onEvent = {},
                modifier = modifier,
            )
        },
    ),
    galleryEntry(
        name = "Toggle",
        family = GalleryFamily.CONTROL,
        blurb = "A two-state control: a filled or bare icon that swaps glyphs, or the platform switch.",
        previews = ToggleUiModel,
        label = ::toggleFixtureLabel,
        content = { model, modifier ->
            Toggle(
                model = model,
                onEvent = {},
                modifier = modifier,
            )
        },
    ),
    galleryEntry(
        name = "RichTextFormattingToolbar",
        family = GalleryFamily.CONTROL,
        blurb = "The review editor's bold, italic and spoiler chips, selected while the whole " +
            "selection carries the mark.",
        previews = RichTextFormattingToolbarUiModel,
        label = { model ->
            when {
                model.enabled.not() -> "No selection"
                model.activeMarks.isEmpty() -> "Nothing marked"
                else -> "Marked selection"
            }
        },
        content = { model, modifier ->
            RichTextFormattingToolbar(
                model = model,
                onEvent = {},
                modifier = modifier,
            )
        },
    ),
)

private fun segmentedControlFixtureLabel(model: SegmentedControlUiModel<String>): String = when {
    model.variant is SegmentedControlVariant.Prose -> "Prose"
    model.options.any { it.enabled.not() } -> "Track, one option disabled"
    else -> "Track"
}

private fun toggleFixtureLabel(model: ToggleUiModel): String = when {
    model.enabled.not() && model.variant is ToggleVariant.Icon -> "Filled icon, disabled"
    model.enabled.not() -> "Switch, disabled"
    model.variant == ToggleVariant.Switch -> "Switch"
    model.variant == ToggleVariant.DisplaySwitch -> "Display-only switch"
    (model.variant as? ToggleVariant.Icon)?.style == ToggleIconStyle.Bare && model.checked -> "Bare icon, on"
    (model.variant as? ToggleVariant.Icon)?.style == ToggleIconStyle.Bare -> "Bare icon, off"
    model.checked -> "Filled icon, on"
    else -> "Filled icon, off"
}
