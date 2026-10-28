package nl.rhaydus.softcover.feature.profile.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import nl.rhaydus.designsystem.theme.StandardPreview
import nl.rhaydus.softcover.core.component.bookcard.BookCardEvent
import nl.rhaydus.softcover.core.component.layout.DesktopScrollPage
import nl.rhaydus.softcover.core.component.topbar.EditorialTopBar
import nl.rhaydus.softcover.core.component.topbar.TopBarEvent
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SchemeBrightness
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.feature.profile.presentation.action.StatsAtlasAction
import nl.rhaydus.softcover.feature.profile.presentation.screen.section.DayDetailSheet
import nl.rhaydus.softcover.feature.profile.presentation.screen.section.StatsAtlasBody
import nl.rhaydus.softcover.feature.profile.presentation.screen.section.rememberAtlasChapterJumper
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasUiState

/**
 * Desktop Stats Atlas: one scrolling reading column. The opaque [Surface] keeps the screen beneath
 * the pushed Atlas from bleeding through during the navigation transition.
 */
@Composable
internal actual fun StatsAtlasScreenLayout(
    state: StatsAtlasUiState,
    runAction: (StatsAtlasAction) -> Unit,
    onNavigateUp: () -> Unit,
    onBookCardEvent: (BookCardEvent) -> Unit,
) {
    val scrollState = rememberScrollState()

    val jumper = rememberAtlasChapterJumper(scrollState)

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag(ProfileTestTags.STATS_ATLAS_SCREEN),
        color = MaterialTheme.colorScheme.background,
    ) {
        DesktopScrollPage(
            scrollState = scrollState,
            topBar = {
                Box(modifier = jumper.aboveViewport()) {
                    EditorialTopBar(
                        model = state.topBar,
                        onEvent = { event ->
                            when (event) {
                                TopBarEvent.BackClicked -> onNavigateUp()
                            }
                        },
                    )
                }
            },
        ) {
            StatsAtlasBody(
                state = state,
                jumper = jumper,
                runAction = runAction,
                onBookCardEvent = onBookCardEvent,
            )
        }
    }

    DayDetailSheet(
        session = state.dayDetailSheet,
        runAction = runAction,
    )
}

@StandardPreview
@Composable
private fun StatsAtlasScreenDesktopLightPreview() {
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
private fun StatsAtlasScreenDesktopDarkPreview() {
    SoftcoverTheme(brightness = SchemeBrightness.DARK) {
        StatsAtlasScreenLayout(
            state = StatsAtlasPreviewState,
            runAction = {},
            onNavigateUp = {},
            onBookCardEvent = {},
        )
    }
}
