package nl.rhaydus.softcover.core.component.progress

import nl.rhaydus.softcover.core.component.field.TextFieldImeAction
import nl.rhaydus.softcover.core.component.field.TextFieldKeyboard
import nl.rhaydus.softcover.core.component.field.TextFieldUiModel
import nl.rhaydus.softcover.core.component.field.TextFieldVariant

/** The two-glyph numeral field for one unit of the time entry; its key is the unit's name. */
fun progressTimeField(
    unit: ProgressTimeUnit,
    text: String,
): TextFieldUiModel = TextFieldUiModel(
    key = unit.name,
    text = text,
    placeholder = "0",
    variant = TextFieldVariant.Numeral(charCount = 2),
    keyboard = TextFieldKeyboard.Number,
    imeAction = TextFieldImeAction.Done,
)
