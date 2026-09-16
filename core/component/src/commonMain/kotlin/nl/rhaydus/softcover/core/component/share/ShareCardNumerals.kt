package nl.rhaydus.softcover.core.component.share

/**
 * Tabular figures, for the numerals a share card sets at display size — the hero stat and the
 * reading-life footer stats. Without it a proportional `1` is narrower than a `0`, which is visible
 * the moment two stats sit in the same row.
 *
 * Shared across the family's body files rather than declared per file: a top-level `private` is
 * file-scoped, and two bodies need it.
 */
internal const val TABULAR_NUMS = "tnum"
