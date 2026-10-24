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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.layout.cappedContentWidth
import nl.rhaydus.designsystem.theme.StandardPreview
import nl.rhaydus.softcover.core.component.topbar.TopBar
import nl.rhaydus.softcover.core.component.topbar.TopBarEvent
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SchemeBrightness
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.feature.profile.presentation.action.OnLogOutSheetOpenAction
import nl.rhaydus.softcover.feature.profile.presentation.action.OnShareSheetOpenAction
import nl.rhaydus.softcover.feature.profile.presentation.action.ProfileAction
import nl.rhaydus.softcover.feature.profile.presentation.screen.section.AccountFootSection
import nl.rhaydus.softcover.feature.profile.presentation.screen.section.ProfileIdentity
import nl.rhaydus.softcover.feature.profile.presentation.screen.section.ProfileShareButton
import nl.rhaydus.softcover.feature.profile.presentation.screen.section.ProfileSheets
import nl.rhaydus.softcover.feature.profile.presentation.screen.section.ReadingSummaryCard
import nl.rhaydus.softcover.feature.profile.presentation.screen.section.ShareEntryRow
import nl.rhaydus.softcover.feature.profile.presentation.state.ProfileUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal actual fun ProfileScreenLayout(
    state: ProfileUiState,
    runAction: (ProfileAction) -> Unit,
    onNavigateUp: () -> Unit,
    onOpenStatsAtlas: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.testTag(ProfileTestTags.PROFILE_SCREEN),
        topBar = {
            TopBar(
                model = state.topBar,
                onEvent = { event ->
                    when (event) {
                        TopBarEvent.BackClicked -> onNavigateUp()
                    }
                },
                actions = {
                    ProfileShareButton(
                        onClick = { runAction(OnShareSheetOpenAction()) },
                        enabled = state.canShare,
                    )
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .cappedContentWidth()
                .padding(horizontal = Spacing.gutter),
        ) {
            ProfileIdentity(
                state = state,
                avatarSize = 168.dp,
                horizontalAlignment = Alignment.CenterHorizontally,
            )

            Spacer(modifier = Modifier.height(Spacing.section))

            ReadingSummaryCard(
                summary = state.readingSummary,
                onClick = onOpenStatsAtlas,
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            ShareEntryRow(
                isLoading = state.isReadingLifeLoading,
                enabled = state.canShare,
                onClick = { runAction(OnShareSheetOpenAction()) },
            )

            Spacer(modifier = Modifier.height(Spacing.section))

            AccountFootSection(
                onLogOutClick = { runAction(OnLogOutSheetOpenAction()) },
                enabled = state.isLoading.not(),
                modifier = Modifier.padding(bottom = Spacing.xl),
            )
        }
    }

    ProfileSheets(
        state = state,
        runAction = runAction,
    )
}

@StandardPreview
@Composable
private fun ProfileScreenLightPreview() {
    SoftcoverTheme(brightness = SchemeBrightness.LIGHT) {
        ProfileScreenLayout(
            state = ProfileScreenPreviewState,
            runAction = {},
            onNavigateUp = {},
            onOpenStatsAtlas = {},
        )
    }
}

@StandardPreview
@Composable
private fun ProfileScreenDarkPreview() {
    SoftcoverTheme(brightness = SchemeBrightness.DARK) {
        ProfileScreenLayout(
            state = ProfileScreenPreviewState,
            runAction = {},
            onNavigateUp = {},
            onOpenStatsAtlas = {},
        )
    }
}
