package nl.rhaydus.softcover.core.component.field

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.component.button.PillButtonUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon

@Immutable
sealed interface TextFieldVariant {
    /**
     * A tonal, fully-rounded single line. [trailing] fades and scales in and out as it comes and goes.
     * A [TextFieldPillSize.Compact] pill sits on a lighter tone than the others.
     */
    data class Pill(
        val leadingIcon: SoftcoverIcon? = null,
        val trailing: PillButtonUiModel? = null,
        val size: TextFieldPillSize = TextFieldPillSize.Regular,
    ) : TextFieldVariant

    /** A non-null [label] floats above the value. Only this variant can ask for a [numeric] keyboard. */
    data class Outlined(
        val label: String? = null,
        val numeric: Boolean = false,
    ) : TextFieldVariant

    /** Borderless display-face text, centred and shrunk until a long value fits one line. */
    data object Hero : TextFieldVariant

    /**
     * A centred numeric entry sized for [charCount] glyphs. Input is limited to digits and its text is
     * selected when it gains focus. It always uses the number keyboard, and Done dismisses focus and
     * submits whatever [TextFieldUiModel.submittable] says. Its placeholder shows while empty.
     */
    data class Numeral(val charCount: Int) : TextFieldVariant
}
