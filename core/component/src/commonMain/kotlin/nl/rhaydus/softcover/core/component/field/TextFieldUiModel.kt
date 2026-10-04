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
 * @property placeholder Shown while [text] is empty. [TextFieldVariant.Outlined] shows it once the label has floated.
 * @property autoFocus Requests focus once when the field enters composition.
 * There is no loading state: a field holds user input, so a field that must not be edited yet is built with `enabled = false`.
 */
@Immutable
data class TextFieldUiModel(
    val key: String,
    val text: String,
    val placeholder: String,
    val variant: TextFieldVariant,
    val keyboard: TextFieldKeyboard = TextFieldKeyboard.Text,
    val imeAction: TextFieldImeAction = TextFieldImeAction.Default,
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
                variant = TextFieldVariant.Outlined(label = "ISBN"),
                keyboard = TextFieldKeyboard.Number,
            ),
            TextFieldUiModel(
                key = "outlined-unlabelled",
                text = "",
                placeholder = "e.g. 9780374710781",
                variant = TextFieldVariant.Outlined(),
                keyboard = TextFieldKeyboard.Number,
            ),
            TextFieldUiModel(
                key = "outlined-disabled",
                text = "9780374710781",
                placeholder = "e.g. 9780374710781",
                variant = TextFieldVariant.Outlined(label = "ISBN"),
                keyboard = TextFieldKeyboard.Number,
                enabled = false,
            ),
            TextFieldUiModel(
                key = "hero-empty",
                text = "",
                placeholder = "Summer reads",
                variant = TextFieldVariant.Hero,
            ),
            TextFieldUiModel(
                key = "hero-long",
                text = "Books I keep meaning to reread before the year ends",
                placeholder = "Summer reads",
                variant = TextFieldVariant.Hero,
            ),
            TextFieldUiModel(
                key = "numeral",
                text = "42",
                placeholder = "0",
                variant = TextFieldVariant.Numeral(charCount = 2),
                keyboard = TextFieldKeyboard.Number,
            ),
        )
    }
}
