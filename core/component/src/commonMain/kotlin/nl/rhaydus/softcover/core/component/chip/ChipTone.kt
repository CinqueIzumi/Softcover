package nl.rhaydus.softcover.core.component.chip

/** `component-contract.md` § 7.2 R2 — [ChipUiModel]'s colour axis, resolved with [ChipUiModel.selected]. */
enum class ChipTone {
    Tonal,
    Choice,
    Filled,
    Container,
    Outlined,
    Dashed,

    /** The tonal idle container under a dashed primary outline: a third state between off and selected.
     * [ChipUiModel.selected] is ignored for this tone. */
    TonalDashed,
    Spoiler,
    OverMedia,
}
