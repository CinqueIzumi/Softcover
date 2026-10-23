package nl.rhaydus.softcover.feature.settings.presentation.screen

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.layout.DesktopScrollPage
import nl.rhaydus.softcover.core.component.topbar.EditorialTopBar
import nl.rhaydus.softcover.core.component.topbar.TopBarEvent
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel

/**
 * Standalone desktop About page, a fallback for a direct push; the primary desktop entry is the
 * Settings master–detail pane's `About` category.
 */
@Composable
internal actual fun AboutScreenLayout(
    topBar: TopBarUiModel,
    versionLabel: String,
    creditsHeader: SectionHeaderUiModel,
    sourceHeader: SectionHeaderUiModel,
    contactHeader: SectionHeaderUiModel,
    openUrl: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onWhatsNewClick: () -> Unit,
    onRoadmapClick: () -> Unit,
) {
    DesktopScrollPage(
        topBar = {
            EditorialTopBar(
                model = topBar,
                onEvent = { event ->
                    when (event) {
                        TopBarEvent.BackClicked -> onNavigateBack()
                    }
                },
            )
        },
    ) {
        AboutContent(
            versionLabel = versionLabel,
            creditsHeader = creditsHeader,
            sourceHeader = sourceHeader,
            contactHeader = contactHeader,
            openUrl = openUrl,
            onWhatsNewClick = onWhatsNewClick,
            onRoadmapClick = onRoadmapClick,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
