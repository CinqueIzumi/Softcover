package nl.rhaydus.softcover.core.designsystem.preset

private const val UNUSED = "Not used by Softcover; the value is the Material default, not a brand colour. Do not use."

/**
 * What each colour is for, in the order the token file lists them. The exporter fails when a role is in the
 * scheme and not here, so a Material role added by an upgrade cannot reach the artifact without a note.
 */
internal object ColorUsage {
    val schemeRoles: Map<String, String> = linkedMapOf(
        "primary" to "The single accent: section marks, active toggles and filters, progress, the filled " +
            "primary button. Also the fill of at most one stat hero card per scrolling region, never any other " +
            "background. Reads on surface, background and the surface containers.",
        "on-primary" to "Text and icons on primary: the filled button label and everything inside the stat hero. " +
            "Demoted text there is on-primary at about 75% alpha.",
        "primary-container" to "Soft accent fill behind a highlighted control or badge.",
        "on-primary-container" to "Text and icons on primary-container.",
        "inverse-primary" to "Small attention markers on chrome, such as a notification dot.",
        "secondary" to "A quieter accent beside primary, for supporting emphasis.",
        "on-secondary" to "Text and icons on secondary.",
        "secondary-container" to "The selected state of pill chips and facet toggles. Idle chips sit on " +
            "surface-container-high.",
        "on-secondary-container" to "Text and icons on secondary-container, such as a selected chip label.",
        "tertiary" to "A third accent for rare, distinct emphasis.",
        "on-tertiary" to "Text and icons on tertiary.",
        "tertiary-container" to "Soft fill behind a tertiary highlight.",
        "on-tertiary-container" to "Text and icons on tertiary-container.",
        "error" to "Destructive actions and validation messages only. Reads on surface and background.",
        "on-error" to "Text and icons on error.",
        "error-container" to "Fill behind an error banner or destructive confirmation.",
        "on-error-container" to "Text and icons on error-container.",
        "background" to "The plain page canvas.",
        "on-background" to "Default readable copy on the page background.",
        "surface" to "The plain surface under cards and content; matches background.",
        "on-surface" to "Default readable copy on surface and every surface-container.",
        "surface-variant" to "A tinted fill for a grouped region that is not a card.",
        "on-surface-variant" to "Demoted copy on surface and the surface containers: metadata, captions, " +
            "secondary lines, inactive labels.",
        "surface-tint" to "The overlay tint Material uses for tonal elevation. Softcover is flat, so it is " +
            "rarely visible; do not paint with it.",
        "inverse-surface" to "The fill of a snackbar or tooltip that must stand out from the page.",
        "inverse-on-surface" to "Text and icons on inverse-surface.",
        "outline" to "The border of the rare bordered control, and strong dividers.",
        "outline-variant" to "Hairline dividers. Prefer whitespace or a tone change to a dense rule.",
        "scrim" to "The black veil behind a modal sheet or dialog.",
        "surface-bright" to "A surface that stays bright in dark themes, for elevated chrome.",
        "surface-dim" to "A surface that stays dim in light themes, for recessed regions.",
        "surface-container-lowest" to "Modal sheets and overlays sitting above the page.",
        "surface-container-low" to "The quietest card or tile fill. Text on it is on-surface.",
        "surface-container" to "The default card or grouped section fill. Text on it is on-surface.",
        "surface-container-high" to "Elevated tiles and idle chips; step up from surface-container to express " +
            "grouping or focus. Text on it is on-surface.",
        "surface-container-highest" to "The strongest grouping fill, for a focused card or input. Text on it is " +
            "on-surface.",
        "primary-fixed" to UNUSED,
        "primary-fixed-dim" to UNUSED,
        "on-primary-fixed" to UNUSED,
        "on-primary-fixed-variant" to UNUSED,
        "secondary-fixed" to UNUSED,
        "secondary-fixed-dim" to UNUSED,
        "on-secondary-fixed" to UNUSED,
        "on-secondary-fixed-variant" to UNUSED,
        "tertiary-fixed" to UNUSED,
        "tertiary-fixed-dim" to UNUSED,
        "on-tertiary-fixed" to UNUSED,
        "on-tertiary-fixed-variant" to UNUSED,
    )

    val fixedInks: Map<String, String> = linkedMapOf(
        "rating-gold" to "Rating star glyphs only, read-only and interactive. Identical in every theme and palette.",
        "over-media-scrim" to "Black scrim behind content laid over imagery: share-card glass, over-media chips " +
            "and icon buttons.",
        "over-media-ink" to "White text and icons on over-media-scrim. Identical in every theme.",
        "mood-ink-cosy-background" to "Fill of the Cosy mood tile in Browse by mood.",
        "mood-ink-cosy-foreground" to "Text and icons on mood-ink-cosy-background.",
        "mood-ink-dread-background" to "Fill of the Dread mood tile in Browse by mood.",
        "mood-ink-dread-foreground" to "Title text on mood-ink-dread-background.",
        "mood-ink-dread-eyebrow" to "Eyebrow text on mood-ink-dread-background.",
        "mood-ink-heart-wrench-background" to "Fill of the heart-wrench mood tile in Browse by mood.",
        "mood-ink-heart-wrench-foreground" to "Text and icons on mood-ink-heart-wrench-background.",
        "monogram-cover-ink" to "The dark fill of the coverless-book monogram cover, in every theme.",
        "monogram-cover-foreground" to "The initials on monogram-cover-ink.",
    )

    const val spoilerCover: String = "The near-solid block over a spoiler run on display, until the reader taps " +
        "it. Derived from on-surface-variant at 90% alpha. Sits over body text on surface."

    const val spoilerEditorHighlight: String = "A translucent wash over a spoiler run in the review editor, so the " +
        "author can still read it. Derived from on-surface-variant at 20% alpha."
}
