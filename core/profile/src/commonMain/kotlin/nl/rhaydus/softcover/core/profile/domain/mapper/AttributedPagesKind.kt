package nl.rhaydus.softcover.core.profile.domain.mapper

/** What a credit of pages comes from. */
internal sealed interface AttributedPagesKind {
    /** A logged progress update. */
    data object Update : AttributedPagesKind

    /** The unlogged final stretch of a finished book, up to [endPage]. */
    data class FinishRemainder(val endPage: Int) : AttributedPagesKind

    /** The unlogged stretch of a Did not finish book up to its stop point at [endPage]. */
    data class StopRemainder(val endPage: Int) : AttributedPagesKind
}
