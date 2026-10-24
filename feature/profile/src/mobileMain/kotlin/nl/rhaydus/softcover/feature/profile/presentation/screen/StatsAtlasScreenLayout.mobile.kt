package nl.rhaydus.softcover.feature.profile.presentation.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import nl.rhaydus.softcover.core.component.bookcard.BookCardEvent
import nl.rhaydus.softcover.core.component.topbar.TopBar
import nl.rhaydus.softcover.core.component.topbar.TopBarEvent
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SchemeBrightness
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.feature.profile.presentation.action.StatsAtlasAction
import nl.rhaydus.softcover.feature.profile.presentation.screen.section.StatsAtlasBody
import nl.rhaydus.softcover.feature.profile.presentation.screen.section.rememberAtlasChapterJumper
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal actual fun StatsAtlasScreenLayout(
    state: StatsAtlasUiState,
    runAction: (StatsAtlasAction) -> Unit,
    onNavigateUp: () -> Unit,
    onBookCardEvent: (BookCardEvent) -> Unit,
) {
    val scrollState = rememberScrollState()

    val jumper = rememberAtlasChapterJumper(scrollState)

    Scaffold(
        modifier = Modifier.testTag(ProfileTestTags.STATS_ATLAS_SCREEN),
        topBar = {
            TopBar(
                model = state.topBar,
                onEvent = { event ->
                    when (event) {
                        TopBarEvent.BackClicked -> onNavigateUp()
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .then(jumper.viewport())
                .verticalScroll(scrollState)
                .cappedContentWidth()
                .padding(horizontal = Spacing.gutter),
        ) {
            Spacer(modifier = Modifier.height(Spacing.xs))

            StatsAtlasBody(
                state = state,
                jumper = jumper,
                runAction = runAction,
                onBookCardEvent = onBookCardEvent,
            )

            Spacer(modifier = Modifier.height(Spacing.xl))
        }
    }
}

@StandardPreview
@Composable
private fun StatsAtlasScreenLightPreview() {
    SoftcoverTheme(brightness = SchemeBrightness.LIGHT) {
        StatsAtlasScreenLayout(
            state = StatsAtlasPreviewState,
            runAction = {},
            onNavigateUp = {},
            onBookCardEvent = {},
        )
    }
}

@StandardPreview
@Composable
private fun StatsAtlasScreenDarkPreview() {
    SoftcoverTheme(brightness = SchemeBrightness.DARK) {
        StatsAtlasScreenLayout(
            state = StatsAtlasPreviewState,
            runAction = {},
            onNavigateUp = {},
            onBookCardEvent = {},
        )
    }
}
