package nl.rhaydus.softcover.core.component.button

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon

/** What [PillButtonUiModel] shows: a text label, or a lone glyph on a circle. */
@Immutable
sealed interface PillButtonContent {
    data class Label(
        val text: String,
        val leadingIcon: SoftcoverIcon? = null,
    ) : PillButtonContent

    /** [description] is both the tooltip and the accessibility label, since the pill has no text. */
    data class IconOnly(
        val icon: SoftcoverIcon,
        val description: String,
    ) : PillButtonContent
}
