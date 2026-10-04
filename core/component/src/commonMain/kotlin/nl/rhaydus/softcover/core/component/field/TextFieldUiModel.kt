package nl.rhaydus.softcover.core.component.field

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.button.PillButtonContent
import nl.rhaydus.softcover.core.component.button.PillButtonTone
import nl.rhaydus.softcover.core.component.button.PillButtonUiModel
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon

/**
 * Everything [TextField] renders: the user's current [text] and how the field is dressed.
 *
 * @property placeholder Shown while [text] is empty, and the field's accessibility label.
 * [TextFieldVariant.Outlined] shows it once the label has floated.
 * @property autoFocus Requests focus when the field enters composition and whenever it flips from false to true.
 * There is no loading state: a field holds user input, so a field that must not be edited yet is built with `enabled = false`.
 */
@Immutable
data class TextFieldUiModel(
    val key: String,
    val text: String,
    val placeholder: String,
    val variant: TextFieldVariant,
    val enabled: Boolean = true,
    val autoFocus: Boolean = false,
) {
    companion object : UiModelPreviews<TextFieldUiModel> {
        override val previews: ImmutableList<TextFieldUiModel> = persistentListOf(
            TextFieldUiModel(
                key = "pill-empty",
                text = "",
                placeholder = "Name a tag…",
                variant = TextFieldVariant.Pill(leadingIcon = SoftcoverIcon.Edit),
            ),
            TextFieldUiModel(
                key = "pill-compact",
                text = "",
                placeholder = "Search your tags",
                variant = TextFieldVariant.Pill(
                    leadingIcon = SoftcoverIcon.Search,
                    size = TextFieldPillSize.Compact,
                    tone = TextFieldPillTone.Container,
                ),
            ),
            TextFieldUiModel(
                key = "pill-with-trailing",
                text = "slow burn",
                placeholder = "Name a tag…",
                variant = TextFieldVariant.Pill(
                    leadingIcon = SoftcoverIcon.Edit,
                    trailing = PillButtonUiModel(
                        key = "add",
                        content = PillButtonContent.Label(text = "Add"),
                        tone = PillButtonTone.Filled,
                    ),
                ),
            ),
            TextFieldUiModel(
                key = "pill-plain",
                text = "",
                placeholder = "Enter your key",
                variant = TextFieldVariant.Pill(size = TextFieldPillSize.Large),
            ),
            TextFieldUiModel(
                key = "outlined",
                text = "9780374710781",
                placeholder = "e.g. 9780374710781",
                variant = TextFieldVariant.Outlined(
                    label = "ISBN",
                    keyboard = TextFieldKeyboard.Number,
                ),
            ),
            TextFieldUiModel(
                key = "outlined-unlabelled",
                text = "",
                placeholder = "e.g. 9780374710781",
                variant = TextFieldVariant.Outlined(keyboard = TextFieldKeyboard.Number),
            ),
            TextFieldUiModel(
                key = "outlined-disabled",
                text = "9780374710781",
                placeholder = "e.g. 9780374710781",
                variant = TextFieldVariant.Outlined(
                    label = "ISBN",
                    keyboard = TextFieldKeyboard.Number,
                ),
                enabled = false,
            ),
            TextFieldUiModel(
                key = "hero-empty",
                text = "",
                placeholder = "Summer reads",
                variant = TextFieldVariant.Hero(),
            ),
            TextFieldUiModel(
                key = "hero-long",
                text = "Books I keep meaning to reread before the year ends",
                placeholder = "Summer reads",
                variant = TextFieldVariant.Hero(),
            ),
            TextFieldUiModel(
                key = "pill-disabled",
                text = "slow burn",
                placeholder = "Name a tag…",
                variant = TextFieldVariant.Pill(leadingIcon = SoftcoverIcon.Edit),
                enabled = false,
            ),
            TextFieldUiModel(
                key = "hero-disabled",
                text = "",
                placeholder = "Summer reads",
                variant = TextFieldVariant.Hero(),
                enabled = false,
            ),
            TextFieldUiModel(
                key = "numeral-disabled",
                text = "42",
                placeholder = "0",
                variant = TextFieldVariant.Numeral(charCount = 2),
                enabled = false,
            ),
            TextFieldUiModel(
                key = "numeral",
                text = "42",
                placeholder = "0",
                variant = TextFieldVariant.Numeral(charCount = 2),
            ),
        )
    }
}
