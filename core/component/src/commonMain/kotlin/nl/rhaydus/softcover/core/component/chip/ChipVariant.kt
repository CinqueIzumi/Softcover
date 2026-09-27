package nl.rhaydus.softcover.core.component.chip

/** `component-contract.md` § 7.2 R2 — the chip's anatomy and colours, resolved by [Chip]. */
sealed interface ChipVariant {
    data class Tonal(val selected: Boolean = false) : ChipVariant

    /**
     * A spoiler redaction: the label draws transparent under a solid cover fill, reserving its
     * width so revealing it (swapping to [Tonal]) never reflows the row.
     */
    data object Spoiler : ChipVariant

    /** A primary-filled "+ label" pill. */
    data object Add : ChipVariant

    /** An outlined "+ label" pill, [Add]'s quieter sibling. */
    data object AddOutlined : ChipVariant

    /** A primary-container pill whose trailing ✕ is display-only — it never reports [ChipEvent.Dismissed]. */
    data class Remove(val removeLabel: String) : ChipVariant

    /** A low-emphasis surface-container pill; [selected] swaps it onto the secondary container. */
    data class Quiet(val selected: Boolean = false) : ChipVariant

    /**
     * A rich-text formatting toggle previewing the mark [face] applies; [active] reflects whether
     * the current selection already carries it. Renders through [Chip] from S8-4 onward —
     * `RichTextFormattingToolbar`'s own `FormatChip` still owns this look until then.
     */
    data class Format(
        val active: Boolean,
        val face: ChipFace,
    ) : ChipVariant
}
