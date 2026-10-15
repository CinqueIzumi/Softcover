package nl.rhaydus.softcover.feature.settings.presentation.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.designsystem.component.InlineErrorState
import nl.rhaydus.softcover.core.component.document.MarkdownDocument
import nl.rhaydus.softcover.core.component.document.MarkdownDocumentEvent
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.state.EmptyState
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.feature.settings.presentation.action.DocumentPageAction
import nl.rhaydus.softcover.feature.settings.presentation.action.RefreshDocumentPageAction
import nl.rhaydus.softcover.feature.settings.presentation.model.DocumentSection
import nl.rhaydus.softcover.feature.settings.presentation.state.DocumentPageUiState

/**
 * The document page body, shared by the mobile [DocumentPageScreen], the desktop standalone fallback, and
 * the desktop Settings master-detail pane's Roadmap and What's new categories. Renders
 * [DocumentPageUiState.sections] as an editorial spread under the page's own chrome: a bundled-copy note
 * and a last-updated footer. A [DocumentPageUiState.error] renders above sections that are already on
 * screen (a stale-but-readable page beats a blank error page) and stands alone only when none have loaded
 * yet.
 */
@Composable
internal fun DocumentPageContent(
    state: DocumentPageUiState,
    runAction: (DocumentPageAction) -> Unit,
    openUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasLoadedSections = state.sections.none { it.body.isLoading }

    Column(modifier = modifier.fillMaxWidth()) {
        if (hasLoadedSections || state.isLoading) {
            if (state.error != null) {
                DocumentPageErrorBanner(
                    message = state.error,
                    onRetry = { runAction(RefreshDocumentPageAction()) },
                )

                Spacer(modifier = Modifier.height(Spacing.xxl))
            }

            DocumentPageBody(
                state = state,
                openUrl = openUrl,
            )
        } else if (state.error != null) {
            DocumentPageErrorBanner(
                message = state.error,
                onRetry = { runAction(RefreshDocumentPageAction()) },
            )
        }
    }
}

@Composable
private fun DocumentPageErrorBanner(
    message: String,
    onRetry: () -> Unit,
) {
    InlineErrorState(
        message = message,
        onRetry = onRetry,
        modifier = Modifier.fillMaxWidth(),
        textStyle = MaterialTheme.editorialTypography.bodySmall,
    )
}

@Composable
private fun DocumentPageBody(
    state: DocumentPageUiState,
    openUrl: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (state.isBundled) {
            SoftcoverText(
                text = state.bundledNote,
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(Spacing.xl))
        }

        if (state.emptyState != null && state.isLoading.not() && state.sections.isEmpty()) {
            EmptyState(
                model = state.emptyState,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        state.sections.forEachIndexed { index, section ->
            if (index > 0) {
                Spacer(modifier = Modifier.height(Spacing.section))
            }

            DocumentPageSection(
                section = section,
                openUrl = openUrl,
            )
        }

        if (state.lastUpdatedText != null) {
            Spacer(modifier = Modifier.height(Spacing.xxxl))

            SoftcoverText(
                text = "Last updated ${state.lastUpdatedText}.",
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DocumentPageSection(
    section: DocumentSection,
    openUrl: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (section.header != null) {
            SectionHeader(model = section.header)
        }

        if (section.dateText != null) {
            Spacer(modifier = Modifier.height(Spacing.xs))

            SoftcoverText(
                text = section.dateText,
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (section.header != null || section.dateText != null) {
            Spacer(modifier = Modifier.height(Spacing.md))
        }

        MarkdownDocument(
            model = section.body,
            onEvent = { event ->
                when (event) {
                    is MarkdownDocumentEvent.LinkClicked -> openUrl(event.url)
                }
            },
        )
    }
}
