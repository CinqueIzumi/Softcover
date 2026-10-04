package nl.rhaydus.softcover.feature.lists.presentation.state

import nl.rhaydus.softcover.core.component.field.TextFieldUiModel
import nl.rhaydus.softcover.core.component.field.TextFieldVariant

/** A real title rather than "List name" — it shows the reader what a good list name looks like. */
internal fun nameFieldFor(
    name: String,
    enabled: Boolean,
): TextFieldUiModel = TextFieldUiModel(
    key = "list-name",
    text = name,
    placeholder = "Summer reads",
    variant = TextFieldVariant.Hero(submittable = true),
    enabled = enabled,
    autoFocus = true,
)
