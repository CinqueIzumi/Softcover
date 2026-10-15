package nl.rhaydus.softcover.feature.settings.presentation.mapper

import nl.rhaydus.softcover.core.component.header.PageMastheadSubtitle
import nl.rhaydus.softcover.core.component.header.PageMastheadUiModel
import nl.rhaydus.softcover.core.component.state.EmptyStateUiModel
import nl.rhaydus.softcover.core.component.state.EmptyStateVariant
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel
import nl.rhaydus.softcover.feature.settings.presentation.model.DocumentPage
import nl.rhaydus.softcover.feature.settings.presentation.state.DocumentPageUiState

private const val ROADMAP_TITLE = "Roadmap"

private const val WHATS_NEW_TITLE = "What's new"

/** The page's chrome and copy, with masked stand-in sections until the first emission has loaded. */
internal fun DocumentPage.toInitialUiState(): DocumentPageUiState = when (this) {
    DocumentPage.ROADMAP -> DocumentPageUiState(
        sections = ROADMAP_STAND_IN_SECTIONS,
        bundledNote = "This is the roadmap that came with this version. Go online to see what's changed.",
        refreshEyebrow = ROADMAP_TITLE,
        loadFailureMessage = "Couldn't load the roadmap. Pull to refresh to try again.",
        refreshFailureMessage = "Couldn't reach the roadmap. Check your connection and try again.",
        emptyState = null,
        masthead = PageMastheadUiModel(
            eyebrow = ROADMAP_TITLE,
            title = ROADMAP_TITLE,
            subtitle = PageMastheadSubtitle.Text("What we're building next, and roughly when."),
        ),
        topBar = TopBarUiModel(title = ROADMAP_TITLE),
    )

    DocumentPage.WHATS_NEW -> DocumentPageUiState(
        sections = WHATS_NEW_STAND_IN_SECTIONS,
        bundledNote = "These are the notes that came with this version. Go online to see the latest.",
        refreshEyebrow = WHATS_NEW_TITLE,
        loadFailureMessage = "Couldn't load what's new. Pull to refresh to try again.",
        refreshFailureMessage = "Couldn't reach the release notes. Check your connection and try again.",
        emptyState = EmptyStateUiModel(
            title = "Nothing to report yet.",
            body = "Notes for each release will appear here.",
            variant = EmptyStateVariant.Inline,
        ),
        masthead = PageMastheadUiModel(
            eyebrow = WHATS_NEW_TITLE,
            title = WHATS_NEW_TITLE,
            subtitle = PageMastheadSubtitle.Text("What changed in each version, newest first."),
        ),
        topBar = TopBarUiModel(title = WHATS_NEW_TITLE),
    )
}
