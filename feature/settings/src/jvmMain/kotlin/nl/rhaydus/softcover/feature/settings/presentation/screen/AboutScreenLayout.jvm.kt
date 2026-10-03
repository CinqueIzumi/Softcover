package nl.rhaydus.softcover.feature.settings.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.DesktopVerticalScrollbar
import nl.rhaydus.designsystem.layout.cappedContentWidth
import nl.rhaydus.softcover.core.component.divider.DividerUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.topbar.EditorialTopBar
import nl.rhaydus.softcover.core.component.topbar.EditorialTopBarEvent
import nl.rhaydus.softcover.core.component.topbar.EditorialTopBarUiModel
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel

/**
 * Standalone desktop About page — a fallback for a direct push (the primary desktop entry is the
 * Settings master–detail pane's `About` category). Static back bar over the shared [AboutContent],
 * capped to the reading measure, with a persistent desktop scrollbar.
 */
@Composable
internal actual fun AboutScreenLayout(
    topBar: TopBarUiModel,
    editorialTopBar: EditorialTopBarUiModel,
    versionFooterDivider: DividerUiModel,
    creditsHeader: SectionHeaderUiModel,
    sourceHeader: SectionHeaderUiModel,
    contactHeader: SectionHeaderUiModel,
    openUrl: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onRoadmapClick: () -> Unit,
    onComponentGalleryUnlocked: () -> Unit,
) {
    val scrollState = rememberScrollState()

    Column(modifier = Modifier.fillMaxSize()) {
        EditorialTopBar(
            model = editorialTopBar,
            onEvent = { event ->
                when (event) {
                    EditorialTopBarEvent.BackClicked -> onNavigateBack()
                }
            },
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            AboutContent(
                versionFooterDivider = versionFooterDivider,
                creditsHeader = creditsHeader,
                sourceHeader = sourceHeader,
                contactHeader = contactHeader,
                openUrl = openUrl,
                onRoadmapClick = onRoadmapClick,
                onComponentGalleryUnlocked = onComponentGalleryUnlocked,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .cappedContentWidth()
                    .padding(
                        horizontal = 32.dp,
                        vertical = 16.dp,
                    ),
            )

            DesktopVerticalScrollbar(
                scrollState = scrollState,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .padding(vertical = 4.dp),
            )
        }
    }
}
