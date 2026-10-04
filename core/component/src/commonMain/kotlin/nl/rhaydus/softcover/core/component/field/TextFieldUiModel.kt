package nl.rhaydus.softcover.core.component.field

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
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
 * @property submittable Offers Done on the keyboard and sends [TextFieldEvent.Submitted] when it is pressed.
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
    val submittable: Boolean = false,
) {
    companion object : UiModelPreviews<TextFieldUiModel> {
        override val previews: ImmutableMap<String, TextFieldUiModel> = persistentMapOf(
            "Pill, leading glyph" to TextFieldUiModel(
                key = "pill-empty",
                text = "",
                placeholder = "Name a tag…",
                variant = TextFieldVariant.Pill(leadingIcon = SoftcoverIcon.Edit),
            ),
            "Pill, compact" to TextFieldUiModel(
                key = "pill-compact",
                text = "",
                placeholder = "Search your tags",
                variant = TextFieldVariant.Pill(
                    leadingIcon = SoftcoverIcon.Search,
                    size = TextFieldPillSize.Compact,
                ),
            ),
            "Pill, trailing action" to TextFieldUiModel(
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
            "Pill, large" to TextFieldUiModel(
                key = "pill-plain",
                text = "",
                placeholder = "Enter your key",
                variant = TextFieldVariant.Pill(size = TextFieldPillSize.Large),
            ),
            "Outlined" to TextFieldUiModel(
                key = "outlined",
                text = "9780374710781",
                placeholder = "e.g. 9780374710781",
                variant = TextFieldVariant.Outlined(
                    label = "ISBN",
                    numeric = true,
                ),
            ),
            "Outlined, no label" to TextFieldUiModel(
                key = "outlined-unlabelled",
                text = "",
                placeholder = "e.g. 9780374710781",
                variant = TextFieldVariant.Outlined(numeric = true),
            ),
            "Outlined, disabled" to TextFieldUiModel(
                key = "outlined-disabled",
                text = "9780374710781",
                placeholder = "e.g. 9780374710781",
                variant = TextFieldVariant.Outlined(
                    label = "ISBN",
                    numeric = true,
                ),
                enabled = false,
            ),
            "Hero" to TextFieldUiModel(
                key = "hero-empty",
                text = "",
                placeholder = "Summer reads",
                variant = TextFieldVariant.Hero,
            ),
            "Pill, disabled" to TextFieldUiModel(
                key = "pill-disabled",
                text = "slow burn",
                placeholder = "Name a tag…",
                variant = TextFieldVariant.Pill(leadingIcon = SoftcoverIcon.Edit),
                enabled = false,
            ),
            "Hero, disabled" to TextFieldUiModel(
                key = "hero-disabled",
                text = "",
                placeholder = "Summer reads",
                variant = TextFieldVariant.Hero,
                enabled = false,
            ),
            "Numeral, disabled" to TextFieldUiModel(
                key = "numeral-disabled",
                text = "42",
                placeholder = "0",
                variant = TextFieldVariant.Numeral(charCount = 2),
                enabled = false,
            ),
            "Numeral" to TextFieldUiModel(
                key = "numeral",
                text = "42",
                placeholder = "0",
                variant = TextFieldVariant.Numeral(charCount = 2),
            ),
        )
    }
}
