package nl.rhaydus.softcover.feature.settings.presentation.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.layout.cappedContentWidth
import nl.rhaydus.designsystem.theme.StandardPreview
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.topbar.EditorialTopBarUiModel
import nl.rhaydus.softcover.core.component.topbar.TopBar
import nl.rhaydus.softcover.core.component.topbar.TopBarEvent
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal actual fun AboutScreenLayout(
    topBar: TopBarUiModel,
    editorialTopBar: EditorialTopBarUiModel,
    versionLabel: String,
    creditsHeader: SectionHeaderUiModel,
    sourceHeader: SectionHeaderUiModel,
    contactHeader: SectionHeaderUiModel,
    openUrl: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onRoadmapClick: () -> Unit,
    onComponentGalleryUnlocked: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopBar(
                model = topBar,
                onEvent = { event ->
                    when (event) {
                        TopBarEvent.BackClicked -> onNavigateBack()
                    }
                },
            )
        },
    ) { innerPadding ->
        AboutContent(
            versionLabel = versionLabel,
            creditsHeader = creditsHeader,
            sourceHeader = sourceHeader,
            contactHeader = contactHeader,
            openUrl = openUrl,
            onRoadmapClick = onRoadmapClick,
            onComponentGalleryUnlocked = onComponentGalleryUnlocked,
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .cappedContentWidth()
                .padding(
                    horizontal = 24.dp,
                    vertical = 16.dp,
                ),
        )
    }
}

@StandardPreview
@Composable
private fun AboutScreenPreview() {
    val state = SettingsScreenUiState()

    SoftcoverTheme {
        AboutScreenLayout(
            topBar = state.aboutTopBar,
            editorialTopBar = state.aboutEditorialTopBar,
            versionLabel = "Version 1.0.0 (1)",
            creditsHeader = state.aboutCreditsHeader,
            sourceHeader = state.aboutSourceHeader,
            contactHeader = state.aboutContactHeader,
            openUrl = {},
            onNavigateBack = {},
            onRoadmapClick = {},
            onComponentGalleryUnlocked = {},
        )
    }
}
