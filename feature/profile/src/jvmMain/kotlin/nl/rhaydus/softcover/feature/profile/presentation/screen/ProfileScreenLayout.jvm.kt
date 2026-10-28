package nl.rhaydus.softcover.feature.profile.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.DesktopTooltip
import nl.rhaydus.designsystem.component.DesktopVerticalScrollbar
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.theme.StandardPreview
import nl.rhaydus.softcover.core.component.divider.VerticalDivider
import nl.rhaydus.softcover.core.component.topbar.EditorialTopBar
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

/**
 * Desktop Profile: a fixed identity column beside a scrolling share-and-atlas column on a wide window,
 * one scrolling column on a narrow one. The opaque [Surface] keeps the screen beneath a pushed Profile
 * from bleeding through during the navigation transition.
 */
@Composable
internal actual fun ProfileScreenLayout(
    state: ProfileUiState,
    runAction: (ProfileAction) -> Unit,
    onNavigateUp: () -> Unit,
    onOpenStatsAtlas: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        ProfileContent(
            state = state,
            runAction = runAction,
            onNavigateUp = onNavigateUp,
            onOpenStatsAtlas = onOpenStatsAtlas,
        )
    }
}

@Composable
private fun ProfileContent(
    state: ProfileUiState,
    runAction: (ProfileAction) -> Unit,
    onNavigateUp: () -> Unit,
    onOpenStatsAtlas: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        EditorialTopBar(
            model = state.topBar,
            onEvent = { event ->
                when (event) {
                    TopBarEvent.BackClicked -> onNavigateUp()
                }
            },
            actions = {
                DesktopTooltip(text = "Share your profile") {
                    ProfileShareButton(
                        onClick = { runAction(OnShareSheetOpenAction()) },
                        enabled = state.canShare,
                        modifier = Modifier.pointerHandCursor(),
                    )
                }
            },
        )

        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            if (maxWidth >= 720.dp) {
                TwoColumnProfile(
                    state = state,
                    onShareClick = { runAction(OnShareSheetOpenAction()) },
                    onLogOutClick = { runAction(OnLogOutSheetOpenAction()) },
                    onOpenStatsAtlas = onOpenStatsAtlas,
                )
            } else {
                SingleColumnProfile(
                    state = state,
                    onShareClick = { runAction(OnShareSheetOpenAction()) },
                    onLogOutClick = { runAction(OnLogOutSheetOpenAction()) },
                    onOpenStatsAtlas = onOpenStatsAtlas,
                )
            }
        }
    }

    ProfileSheets(
        state = state,
        runAction = runAction,
    )
}

@Composable
private fun SummaryAndShareRows(
    state: ProfileUiState,
    onShareClick: () -> Unit,
    onOpenStatsAtlas: () -> Unit,
) {
    ReadingSummaryCard(
        summary = state.readingSummary,
        onClick = onOpenStatsAtlas,
    )

    Spacer(modifier = Modifier.height(Spacing.md))

    ShareEntryRow(
        isLoading = state.isReadingLifeLoading,
        enabled = state.canShare,
        onClick = onShareClick,
    )
}

@Composable
private fun TwoColumnProfile(
    state: ProfileUiState,
    onShareClick: () -> Unit,
    onLogOutClick: () -> Unit,
    onOpenStatsAtlas: () -> Unit,
) {
    Row(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .width(320.dp)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(start = Spacing.gutter, end = Spacing.gutter, top = Spacing.xs, bottom = Spacing.xl),
        ) {
            ProfileIdentity(
                state = state,
                avatarSize = 140.dp,
                horizontalAlignment = Alignment.Start,
            )

            Spacer(modifier = Modifier.height(Spacing.section))

            AccountFootSection(
                onLogOutClick = onLogOutClick,
                enabled = state.isLoading.not(),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        VerticalDivider()

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        ) {
            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(start = Spacing.gutter, end = Spacing.gutter, top = Spacing.xs, bottom = Spacing.xl),
            ) {
                Column(modifier = Modifier.widthIn(max = 640.dp)) {
                    SummaryAndShareRows(
                        state = state,
                        onShareClick = onShareClick,
                        onOpenStatsAtlas = onOpenStatsAtlas,
                    )
                }
            }

            DesktopVerticalScrollbar(
                scrollState = scrollState,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .padding(vertical = Spacing.xxs),
            )
        }
    }
}

@Composable
private fun SingleColumnProfile(
    state: ProfileUiState,
    onShareClick: () -> Unit,
    onLogOutClick: () -> Unit,
    onOpenStatsAtlas: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = Spacing.gutter, vertical = Spacing.xs),
        ) {
            ProfileIdentity(
                state = state,
                avatarSize = 140.dp,
                horizontalAlignment = Alignment.CenterHorizontally,
            )

            Spacer(modifier = Modifier.height(Spacing.section))

            SummaryAndShareRows(
                state = state,
                onShareClick = onShareClick,
                onOpenStatsAtlas = onOpenStatsAtlas,
            )

            Spacer(modifier = Modifier.height(Spacing.section))

            AccountFootSection(
                onLogOutClick = onLogOutClick,
                enabled = state.isLoading.not(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Spacing.xl),
            )
        }

        DesktopVerticalScrollbar(
            scrollState = scrollState,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .padding(vertical = Spacing.xxs),
        )
    }
}

@StandardPreview
@Composable
private fun ProfileScreenDesktopLightPreview() {
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
private fun ProfileScreenDesktopDarkPreview() {
    SoftcoverTheme(brightness = SchemeBrightness.DARK) {
        ProfileScreenLayout(
            state = ProfileScreenPreviewState,
            runAction = {},
            onNavigateUp = {},
            onOpenStatsAtlas = {},
        )
    }
}
