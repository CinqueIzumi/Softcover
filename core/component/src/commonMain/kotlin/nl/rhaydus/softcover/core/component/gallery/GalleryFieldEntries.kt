package nl.rhaydus.softcover.core.component.gallery

import androidx.compose.foundation.layout.size
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.field.TextField
import nl.rhaydus.softcover.core.component.field.TextFieldUiModel

internal val fieldEntries: ImmutableList<GalleryEntry> = persistentListOf(
    galleryEntry(
        name = "TextField",
        family = GalleryFamily.FIELD,
        blurb = "A text input: a tonal pill with optional glyph and trailing action, the Material " +
            "outlined box, a display-size hero, or a centred numeral.",
        previews = TextFieldUiModel,
        content = { model, modifier ->
            TextField(
                model = model,
                onEvent = {},
                modifier = modifier,
            )
        },
    ),
)
