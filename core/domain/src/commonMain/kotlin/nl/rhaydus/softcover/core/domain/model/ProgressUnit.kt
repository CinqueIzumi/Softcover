package nl.rhaydus.softcover.core.domain.model

/**
 * The unit a reader entered their last progress update in. Persisted so the progress sheet can
 * pre-select the same unit next time. The presentation layer maps this to its `ProgressSheetTab`.
 */
enum class ProgressUnit {
    PAGE,
    TIME,
    PERCENTAGE,
}
