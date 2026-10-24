package nl.rhaydus.softcover.core.component.chip

/** The typographic treatment of [ChipUiModel.label]: a formatting mark, or the editorial eyebrow. */
enum class ChipFace {
    Plain,
    Bold,
    Italic,

    /** Uppercase editorial eyebrow type; the chip uppercases the label itself. */
    Eyebrow,
}
