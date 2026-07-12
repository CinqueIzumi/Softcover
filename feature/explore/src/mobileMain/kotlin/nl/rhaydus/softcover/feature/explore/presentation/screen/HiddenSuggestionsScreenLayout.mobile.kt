package nl.rhaydus.softcover.feature.explore.presentation.screen

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.designsystem.presentation.component.SoftcoverTopBar
import nl.rhaydus.softcover.feature.explore.presentation.action.HiddenSuggestionsAction
import nl.rhaydus.softcover.feature.explore.presentation.state.HiddenSuggestionsUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal actual fun HiddenSuggestionsScreenLayout(
    state: HiddenSuggestionsUiState,
    runAction: (HiddenSuggestionsAction) -> Unit,
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            SoftcoverTopBar(
                title = "Hidden suggestions",
                subTitle = "From \"Up next in your series\"",
                onNavigateBack = onNavigateBack,
            )
        },
        contentWindowInsets = WindowInsets.statusBars,
    ) { innerPadding ->
        HiddenSuggestionsContent(
            state = state,
            runAction = runAction,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        )
    }
}
