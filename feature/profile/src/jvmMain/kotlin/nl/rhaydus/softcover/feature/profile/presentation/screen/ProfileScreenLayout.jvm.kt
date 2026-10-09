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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.DesktopTooltip
import nl.rhaydus.designsystem.component.DesktopVerticalScrollbar
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.theme.StandardPreview
import nl.rhaydus.softcover.core.component.bookcard.BookCardEvent
import nl.rhaydus.softcover.core.component.divider.VerticalDivider
import nl.rhaydus.softcover.core.component.topbar.EditorialTopBar
import nl.rhaydus.softcover.core.component.topbar.TopBarEvent
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData
import nl.rhaydus.softcover.feature.profile.presentation.action.ProfileAction
import nl.rhaydus.softcover.feature.profile.presentation.mapper.demographicChartUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.genderChartUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.genreRankingUiModel
import nl.rhaydus.softcover.feature.profile.presentation.screen.section.AccountFootSection
import nl.rhaydus.softcover.feature.profile.presentation.screen.section.ProfileIdentity
import nl.rhaydus.softcover.feature.profile.presentation.screen.section.ProfileReadingLife
import nl.rhaydus.softcover.feature.profile.presentation.screen.section.ProfileShareButton
import nl.rhaydus.softcover.feature.profile.presentation.screen.section.ProfileSheets
import nl.rhaydus.softcover.feature.profile.presentation.screen.section.profileReadingLifePreview
import nl.rhaydus.softcover.feature.profile.presentation.state.ProfileUiState

/**
 * Desktop Profile: a fixed identity column beside a scrolling reading-life column on a wide window,
 * one scrolling column on a narrow one. The opaque [Surface] keeps the screen beneath a pushed Profile
 * from bleeding through during the navigation transition.
 */
@Composable
internal actual fun ProfileScreenLayout(
    state: ProfileUiState,
    runAction: (ProfileAction) -> Unit,
    onNavigateUp: () -> Unit,
    onBookCardEvent: (BookCardEvent) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        ProfileContent(
            state = state,
            runAction = runAction,
            onNavigateUp = onNavigateUp,
            onBookCardEvent = onBookCardEvent,
        )
    }
}

@Composable
private fun ProfileContent(
    state: ProfileUiState,
    runAction: (ProfileAction) -> Unit,
    onNavigateUp: () -> Unit,
    onBookCardEvent: (BookCardEvent) -> Unit,
) {
    var showShareSheet by remember { mutableStateOf(false) }
    var showLogOutConfirm by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        EditorialTopBar(
            model = state.topBar,
            onEvent = { event ->
                when (event) {
                    TopBarEvent.BackClicked -> onNavigateUp()
                }
            },
            actions = {
                DesktopTooltip(text = "Share your reading year") {
                    ProfileShareButton(
                        onClick = { showShareSheet = true },
                        enabled = state.readingLifeShareCard != null,
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
                    runAction = runAction,
                    onShareClick = { showShareSheet = true },
                    onLogOutClick = { showLogOutConfirm = true },
                    onBookCardEvent = onBookCardEvent,
                )
            } else {
                SingleColumnProfile(
                    state = state,
                    runAction = runAction,
                    onShareClick = { showShareSheet = true },
                    onLogOutClick = { showLogOutConfirm = true },
                    onBookCardEvent = onBookCardEvent,
                )
            }
        }
    }

    ProfileSheets(
        state = state,
        showShareSheet = showShareSheet,
        showLogOutConfirm = showLogOutConfirm,
        onShareDismiss = { showShareSheet = false },
        onLogOutDismiss = { showLogOutConfirm = false },
        runAction = runAction,
    )
}

@Composable
private fun TwoColumnProfile(
    state: ProfileUiState,
    runAction: (ProfileAction) -> Unit,
    onShareClick: () -> Unit,
    onLogOutClick: () -> Unit,
    onBookCardEvent: (BookCardEvent) -> Unit,
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
                    ProfileReadingLife(
                        state = state,
                        runAction = runAction,
                        onShareClick = onShareClick,
                        onBookCardEvent = onBookCardEvent,
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
    runAction: (ProfileAction) -> Unit,
    onShareClick: () -> Unit,
    onLogOutClick: () -> Unit,
    onBookCardEvent: (BookCardEvent) -> Unit,
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

            ProfileReadingLife(
                state = state,
                runAction = runAction,
                onShareClick = onShareClick,
                onBookCardEvent = onBookCardEvent,
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
    SoftcoverTheme(darkTheme = false) {
        ProfileScreenLayout(
            state = ProfileScreenDesktopPreviewState,
            runAction = {},
            onNavigateUp = {},
            onBookCardEvent = {},
        )
    }
}

@StandardPreview
@Composable
private fun ProfileScreenDesktopDarkPreview() {
    SoftcoverTheme(darkTheme = true) {
        ProfileScreenLayout(
            state = ProfileScreenDesktopPreviewState,
            runAction = {},
            onNavigateUp = {},
            onBookCardEvent = {},
        )
    }
}

private val PreviewReadingLife = profileReadingLifePreview()

private val ProfileScreenDesktopPreviewState = ProfileUiState(
    isLoading = false,
    userProfileData = UserProfileData(
        profileImageUrl = "",
        name = "Elena Marchetti",
        username = "elenam",
        bio = "Mostly literary fiction, the occasional doorstop fantasy.",
        booksRead = 214,
        totalPagesRead = 128_406,
        averageRating = 4.2,
        readingStreak = 31,
    ),
    readingLife = PreviewReadingLife,
    authorDemographics = PreviewReadingLife.authorDemographics,
    genreRanking = genreRankingUiModel(PreviewReadingLife.genres),
    genderChart = genderChartUiModel(PreviewReadingLife.authorDemographics.genderSlices),
    bipocChart = demographicChartUiModel(PreviewReadingLife.authorDemographics.bipocBreakdown),
    lgbtqChart = demographicChartUiModel(PreviewReadingLife.authorDemographics.lgbtqBreakdown),
)
