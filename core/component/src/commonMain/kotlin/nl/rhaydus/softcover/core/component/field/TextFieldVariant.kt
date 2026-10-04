package nl.rhaydus.softcover.core.component.field

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.component.button.PillButtonUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon

@Immutable
sealed interface TextFieldVariant {
    /**
     * A tonal, fully-rounded single line, filled with [tone]. [trailing] fades and scales in and out as it
     * comes and goes. A [submittable] pill offers Done and sends [TextFieldEvent.Submitted].
     */
    data class Pill(
        val leadingIcon: SoftcoverIcon? = null,
        val trailing: PillButtonUiModel? = null,
        val size: TextFieldPillSize = TextFieldPillSize.Regular,
        val tone: TextFieldPillTone = TextFieldPillTone.ContainerHigh,
        val submittable: Boolean = false,
    ) : TextFieldVariant

    /** A non-null [label] floats above the value. Only this variant can ask for a [keyboard] other than text. */
    data class Outlined(
        val label: String? = null,
        val keyboard: TextFieldKeyboard = TextFieldKeyboard.Text,
        val submittable: Boolean = false,
    ) : TextFieldVariant

    /** Borderless display-face text, centred and shrunk until a long value fits one line. */
    data class Hero(val submittable: Boolean = false) : TextFieldVariant

    /**
     * A centred numeric entry sized for [charCount] glyphs. Input is limited to digits and its text is
     * selected when it gains focus. It always uses the number keyboard, and Done dismisses focus and
     * submits. Its placeholder shows while empty.
     */
    data class Numeral(val charCount: Int) : TextFieldVariant
}
