package nl.rhaydus.softcover.core.component.cover

/**
 * The distinct cover treatments, each named after its look: shadow depth, corner radius, shadow tint
 * and decode cap. [Flat] is the default for any surface that wants no shadow and a 4 dp corner,
 * including the backdrop and full-screen viewer surfaces that render through
 * [rememberCoverImageRequest] rather than [Cover].
 */
sealed interface CoverVariant {
    /** Library grid cover: 6 dp shadow, 10 dp corner, decoded at no more than 600 px. */
    data object Shelf : CoverVariant

    /** 6 dp shadow, 4 dp corner. */
    data object Raised : CoverVariant

    /** 12 dp shadow in a dark tint, 4 dp corner. */
    data object Floating : CoverVariant

    /** 4 dp shadow, 8 dp corner. */
    data object Tile : CoverVariant

    /** No shadow, 16 dp corner. */
    data object Rounded : CoverVariant

    /** 4 dp shadow in a dark tint, 4 dp corner. */
    data object Jacket : CoverVariant

    /** 3 dp shadow in a lighter dark tint, 4 dp corner, for covers stacked on each other. */
    data object StackedJacket : CoverVariant

    /** 20 dp shadow in a dark tint, 8 dp corner. */
    data object Spotlight : CoverVariant

    /** No shadow, 4 dp corner. */
    data object Flat : CoverVariant
}
