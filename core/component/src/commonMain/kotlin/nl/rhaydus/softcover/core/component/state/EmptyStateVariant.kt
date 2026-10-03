package nl.rhaydus.softcover.core.component.state

sealed interface EmptyStateVariant {
    data class Centered(val button: EmptyStateButtonUiModel? = null) : EmptyStateVariant

    data object Inline : EmptyStateVariant
}
