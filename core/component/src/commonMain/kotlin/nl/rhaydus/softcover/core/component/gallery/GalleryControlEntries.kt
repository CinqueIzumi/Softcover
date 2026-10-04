package nl.rhaydus.softcover.core.component.gallery

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.control.SegmentedControl
import nl.rhaydus.softcover.core.component.control.SegmentedControlUiModel
import nl.rhaydus.softcover.core.component.control.SegmentedControlVariant
import nl.rhaydus.softcover.core.component.control.Toggle
import nl.rhaydus.softcover.core.component.control.ToggleRow
import nl.rhaydus.softcover.core.component.control.ToggleRowUiModel
import nl.rhaydus.softcover.core.component.control.ToggleUiModel
import nl.rhaydus.softcover.core.component.control.ToggleVariant

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
        name = "ToggleRow",
        family = GalleryFamily.CONTROL,
        blurb = "A labelled switch whose whole row is the tap target; the switch only displays the state.",
        previews = ToggleRowUiModel,
        label = ::toggleRowFixtureLabel,
        content = { model, modifier ->
            ToggleRow(
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

private fun toggleFixtureLabel(model: ToggleUiModel): String {
    val look = if (model.variant == ToggleVariant.Filled) "Filled icon" else "Bare icon"

    return when {
        model.enabled.not() -> "$look, disabled"
        model.checked -> "$look, on"
        else -> "$look, off"
    }
}

private fun toggleRowFixtureLabel(model: ToggleRowUiModel<String>): String = when {
    model.enabled.not() -> "Disabled"
    model.gloss == null -> "Title only"
    else -> "Title and gloss"
}
