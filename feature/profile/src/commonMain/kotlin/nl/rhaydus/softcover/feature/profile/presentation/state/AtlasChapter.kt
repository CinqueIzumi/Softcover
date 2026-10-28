package nl.rhaydus.softcover.feature.profile.presentation.state

/**
 * The Atlas chapters in reading order, which the contents list follows. The chapter numbers are fixed
 * per chapter rather than positional: they follow the design's chapter list, and the other chapters land
 * with later 3.3.0 issues.
 */
internal enum class AtlasChapter {
    YEAR_AT_A_GLANCE,
    SEASONS_AND_RECORDS,
    GENRES_AND_HOW_YOU_READ,

    /** Numbered 99, because it goes when the real chapters replace it. */
    TEMPORARY,
}
