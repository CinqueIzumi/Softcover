package nl.rhaydus.softcover.core.component.field

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.component.button.PillButtonUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon

@Immutable
sealed interface TextFieldVariant {
    /** A tonal, fully-rounded single line. [trailing] fades and scales in and out as it comes and goes. */
    data class Pill(
        val leadingIcon: SoftcoverIcon? = null,
        val trailing: PillButtonUiModel? = null,
        val size: TextFieldPillSize = TextFieldPillSize.Regular,
    ) : TextFieldVariant

    /** A non-null [label] floats above the value. */
    data class Outlined(val label: String? = null) : TextFieldVariant

    /** Borderless display-face text, centred and shrunk until a long value fits one line. */
    data object Hero : TextFieldVariant

    /**
     * A centred numeric entry sized for [charCount] glyphs. Input is limited to digits, its text is
     * selected when it gains focus, and Done dismisses focus.
     */
    data class Numeral(val charCount: Int) : TextFieldVariant
}
