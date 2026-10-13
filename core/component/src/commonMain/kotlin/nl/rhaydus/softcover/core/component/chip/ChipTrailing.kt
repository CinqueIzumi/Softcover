package nl.rhaydus.softcover.core.component.chip

import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon

/** `component-contract.md` § 7.2 R2 — [ChipUiModel]'s trailing slot, independent of [ChipTone]. */
sealed interface ChipTrailing {
    data class Icon(
        val icon: SoftcoverIcon,
        val description: String? = null,
    ) : ChipTrailing

    /** Its own tap target reporting [ChipEvent.Dismissed], described by [label]. */
    data class Dismiss(val label: String) : ChipTrailing
}
