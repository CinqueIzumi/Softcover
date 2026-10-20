package nl.rhaydus.softcover.core.designsystem.preset

/** Where each type role goes, in the order the token file lists them. */
internal object TypeUsage {
    val editorial: Map<String, String> = linkedMapOf(
        "editorial-eyebrow" to "All-caps section label, a kicker over a headline, or an accent over a hero stat. " +
            "Wide letter-spacing, semibold. Pair with the 32 by 4 section bar.",
        "editorial-eyebrow-small" to "The same label packed inside a card or row. Tighter. Pair with the 20 by 1 " +
            "inline bar or none.",
        "editorial-page-title" to "The screen's primary title inside the scrolling page. The top bar slot uses " +
            "title-large instead.",
        "editorial-display" to "Section headline that follows an eyebrow. Italic and semibold.",
        "editorial-headline-medium" to "Subsection or modal headline. Italic and semibold.",
        "editorial-headline-small" to "A smaller subsection or modal headline. Italic and semibold.",
        "editorial-title-large" to "Card title and grouped row header. Roman and semibold.",
        "editorial-title-medium" to "List item title. Roman and semibold.",
        "editorial-title-small" to "Compact list item title. Roman and semibold.",
        "editorial-body-large" to "Running prose at reading size: descriptions and longer captions. Italic.",
        "editorial-body" to "Default running prose and short descriptions. Italic.",
        "editorial-body-small" to "Captions and small prose. Italic.",
        "editorial-review" to "Community review prose. Roman, so a reader's words are not styled as editorial copy.",
        "editorial-stat-hero" to "The single largest number on a screen. Italic, semibold and oversize.",
        "editorial-stat-large" to "Hero numbers, including input fields styled as stats. Italic and semibold.",
        "editorial-quote-glyph" to "Oversized quotation mark behind a quote or as an empty-state flourish. " +
            "Draw it in on-surface-variant at low alpha.",
    )

    val material: Map<String, String> = linkedMapOf(
        "display-large" to "Material display role. Reach for an editorial role first.",
        "display-medium" to "Material display role. Reach for an editorial role first.",
        "display-small" to "Material display role. Reach for an editorial role first.",
        "headline-large" to "Material headline role, set in the display face. Reach for an editorial role first.",
        "headline-medium" to "Material headline role, set in the display face.",
        "headline-small" to "Material headline role, set in the display face.",
        "title-large" to "The top bar title and Material component titles.",
        "title-medium" to "Material component titles, such as list item headlines.",
        "title-small" to "Compact Material component titles.",
        "body-large" to "Material component body copy.",
        "body-medium" to "Default Material body copy and component supporting text.",
        "body-small" to "Material component captions.",
        "label-large" to "Button labels.",
        "label-medium" to "Chip and tab labels, and utility labels.",
        "label-small" to "Small utility labels and badges.",
    )
}
