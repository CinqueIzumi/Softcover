package nl.rhaydus.softcover.core.component.chip

/**
 * `component-contract.md` § 7.2 R1 — the chip's tap affordance, independent of [ChipTone].
 * [Disabled] dims the chip to signal a withheld tap; [Inert] renders at full opacity with no click
 * role — every chip uses a press-scale affordance rather than a ripple, [Clickable] included.
 */
enum class ChipInteraction {
    Clickable,
    Disabled,
    Inert,
}
