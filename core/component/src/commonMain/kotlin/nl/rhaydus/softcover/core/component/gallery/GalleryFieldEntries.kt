package nl.rhaydus.softcover.core.component.gallery

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.field.TextField
import nl.rhaydus.softcover.core.component.field.TextFieldPillSize
import nl.rhaydus.softcover.core.component.field.TextFieldUiModel
import nl.rhaydus.softcover.core.component.field.TextFieldVariant

internal val fieldEntries: ImmutableList<GalleryEntry> = persistentListOf(
    galleryEntry(
        name = "TextField",
        family = GalleryFamily.FIELD,
        blurb = "A text input: a tonal pill with optional glyph and trailing action, the Material " +
            "outlined box, a display-size hero, or a centred numeral.",
        previews = TextFieldUiModel,
        label = ::textFieldFixtureLabel,
        content = { model, modifier ->
            TextField(
                model = model,
                onEvent = {},
                modifier = modifier,
            )
        },
    ),
)

private fun textFieldFixtureLabel(model: TextFieldUiModel): String = when (val variant = model.variant) {
    is TextFieldVariant.Pill -> when {
        variant.trailing != null -> "Pill, trailing action"
        variant.size == TextFieldPillSize.Compact -> "Pill, compact"
        variant.size == TextFieldPillSize.Large -> "Pill, large"
        variant.leadingIcon != null -> "Pill, leading glyph"
        else -> "Pill"
    }

    is TextFieldVariant.Outlined -> when {
        model.enabled.not() -> "Outlined, disabled"
        variant.label == null -> "Outlined, no label"
        else -> "Outlined"
    }

    TextFieldVariant.Hero -> if (model.text.length > 20) "Hero, long value" else "Hero"

    is TextFieldVariant.Numeral -> "Numeral"
}
