package nl.rhaydus.softcover.core.component.state

sealed interface EmptyStateVariant {
    data object Centered : EmptyStateVariant

    data object Inline : EmptyStateVariant
}
