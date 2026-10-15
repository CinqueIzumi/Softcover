package nl.rhaydus.softcover.feature.settings.presentation.state

import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.softcover.core.component.header.PageMastheadUiModel
import nl.rhaydus.softcover.core.component.state.EmptyStateUiModel
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel
import nl.rhaydus.softcover.feature.settings.presentation.model.DocumentSection
import nl.rhaydus.toad.UiState

internal data class DocumentPageUiState(
    // Masked stand-ins until the first emission has loaded.
    val sections: ImmutableList<DocumentSection>,
    val bundledNote: String,
    val refreshEyebrow: String,
    val loadFailureMessage: String,
    val refreshFailureMessage: String,

    /** Shown when the page has loaded with no sections; `null` for a page that is never empty. */
    val emptyState: EmptyStateUiModel?,

    /** Desktop only: mobile's pushed page renders its own top bar instead. */
    val masthead: PageMastheadUiModel,
    val topBar: TopBarUiModel,
    val isBundled: Boolean = false,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null,

    /** The "Last updated" footer's date text; `null` exactly when the footer does not render. */
    val lastUpdatedText: String? = null,
) : UiState
