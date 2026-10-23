package nl.rhaydus.softcover.feature.settings.presentation.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import nl.rhaydus.designsystem.layout.cappedContentWidth
import nl.rhaydus.designsystem.theme.StandardPreview
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.topbar.TopBar
import nl.rhaydus.softcover.core.component.topbar.TopBarEvent
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState

@OptIn(ExperimentalMaterial3Api::class)
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
    Scaffold(
        modifier = Modifier.testTag(SettingsTestTags.ABOUT_PAGE),
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
            onWhatsNewClick = onWhatsNewClick,
            onRoadmapClick = onRoadmapClick,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .cappedContentWidth()
                .padding(
                    horizontal = Spacing.gutter,
                    vertical = Spacing.md,
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
            versionLabel = "Version 1.0.0 (1)",
            creditsHeader = state.aboutCreditsHeader,
            sourceHeader = state.aboutSourceHeader,
            contactHeader = state.aboutContactHeader,
            openUrl = {},
            onNavigateBack = {},
            onWhatsNewClick = {},
            onRoadmapClick = {},
        )
    }
}
