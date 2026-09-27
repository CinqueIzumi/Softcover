package nl.rhaydus.softcover.core.component.chip

import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon

/** `component-contract.md` § 7.2 R2 — [ChipUiModel]'s leading slot, independent of [ChipTone]. */
sealed interface ChipLeading {
    data class Icon(
        val icon: SoftcoverIcon,
        val description: String? = null,
    ) : ChipLeading

    /** [marked] swaps the eye glyph and reports [ChipEvent.SpoilerToggled], never [ChipEvent.Clicked]. */
    data class SpoilerToggle(
        val marked: Boolean,
        val label: String,
    ) : ChipLeading
}
