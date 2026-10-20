package nl.rhaydus.softcover.core.designsystem.preset

/**
 * Where each spacing and radius step goes, keyed by member name and in the order the token file lists them.
 * The exporter fails when a member of `Spacing` or `CornerRadius` has no note here, or a note has no member.
 */
internal object SpacingUsage {
    val spacing: Map<String, String> = linkedMapOf(
        "xxxs" to "The hairline gap.",
        "xxs" to "Tight inline gap: icon to label, badge offsets.",
        "xs" to "Tight inline gap, one step up from xxs.",
        "sm" to "Padding within a component and the gap from a label to its control.",
        "md" to "Padding within a component, and the gap from a section header to its body.",
        "lg" to "The gap between a hero card's image and its text block.",
        "xl" to "The page gutter, and the gap between blocks inside a sheet.",
        "xxl" to "The gap between blocks inside a hero region.",
        "xxxl" to "The gap from a masthead to its first section.",
        "x4l" to "The gap between major page sections; the section step.",
        "x5l" to "The largest gap between major page sections.",
        "gutter" to "The page horizontal gutter, shared by every page on every platform.",
        "section" to "The gap between major page sections. Two adjacent eyebrow and headline pairs without it " +
            "is a layout bug.",
    )

    val radius: Map<String, String> = linkedMapOf(
        "xxs" to "The hairline corner.",
        "xs" to "The smallest rounded corner.",
        "sm" to "A small corner.",
        "md" to "A medium corner.",
        "lg" to "A large corner.",
        "xl" to "An extra-large corner.",
        "xxl" to "The largest corner. An off-scale radius snaps to the nearest step.",
    )
}
