package nl.rhaydus.softcover.core.component.state

import nl.rhaydus.softcover.core.component.button.PillButtonUiModel

sealed interface EmptyStateVariant {
    /** A quote glyph over a headline, with an optional [button] beneath. */
    data class Centered(val button: PillButtonUiModel? = null) : EmptyStateVariant

    /** A start-aligned title and body, for an empty section of a larger page. */
    data object Inline : EmptyStateVariant
}
