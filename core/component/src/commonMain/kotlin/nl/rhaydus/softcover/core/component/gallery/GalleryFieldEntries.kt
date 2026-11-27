package nl.rhaydus.softcover.core.component.gallery

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.field.SelectField
import nl.rhaydus.softcover.core.component.field.SelectFieldUiModel
import nl.rhaydus.softcover.core.component.field.TextField
import nl.rhaydus.softcover.core.component.field.TextFieldUiModel

internal val fieldEntries: ImmutableList<GalleryEntry> = persistentListOf(
    galleryEntry(
        name = "TextField",
        family = GalleryFamily.FIELD,
        blurb = "A text input: a tonal pill with optional glyph and trailing action, the Material " +
            "outlined box, a multi-line box, a display-size hero, or a centred numeral.",
        previews = TextFieldUiModel,
        content = { model, modifier ->
            TextField(
                model = model,
                onEvent = {},
                modifier = modifier,
            )
        },
    ),
    galleryEntry(
        name = "SelectField",
        family = GalleryFamily.FIELD,
        blurb = "A tap target dressed as the outlined text field: the chosen value and a down caret, " +
            "opening a list of options elsewhere.",
        previews = SelectFieldUiModel,
        content = { model, modifier ->
            SelectField(
                model = model,
                onEvent = {},
                modifier = modifier,
            )
        },
    ),
)
