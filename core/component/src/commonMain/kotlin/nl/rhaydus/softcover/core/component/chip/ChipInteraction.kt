package nl.rhaydus.softcover.core.component.chip

/**
 * `component-contract.md` § 7.2 R1 — the chip's tap affordance, independent of [ChipVariant].
 * [Disabled] dims the chip to signal a withheld tap; [Inert] never had one, so it renders at full
 * opacity with no ripple and no click role.
 */
enum class ChipInteraction {
    Clickable,
    Disabled,
    Inert,
}
