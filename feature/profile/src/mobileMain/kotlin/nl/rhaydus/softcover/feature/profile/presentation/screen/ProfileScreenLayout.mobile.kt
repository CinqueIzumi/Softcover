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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.layout.cappedContentWidth
import nl.rhaydus.designsystem.theme.StandardPreview
import nl.rhaydus.softcover.core.component.bookcard.BookCardEvent
import nl.rhaydus.softcover.core.component.topbar.TopBar
import nl.rhaydus.softcover.core.component.topbar.TopBarEvent
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal actual fun ProfileScreenLayout(
    state: ProfileUiState,
    runAction: (ProfileAction) -> Unit,
    onNavigateUp: () -> Unit,
    onBookCardEvent: (BookCardEvent) -> Unit,
) {
    var showShareSheet by remember { mutableStateOf(false) }
    var showLogOutConfirm by remember { mutableStateOf(false) }

    Scaffold(
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
                        onClick = { showShareSheet = true },
                        enabled = state.readingLifeShareCard != null,
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
                .padding(horizontal = 24.dp),
        ) {
            ProfileIdentity(
                state = state,
                avatarSize = 168.dp,
                horizontalAlignment = Alignment.CenterHorizontally,
            )

            Spacer(modifier = Modifier.height(36.dp))

            ProfileReadingLife(
                state = state,
                runAction = runAction,
                onShareClick = { showShareSheet = true },
                onBookCardEvent = onBookCardEvent,
            )

            Spacer(modifier = Modifier.height(40.dp))

            AccountFootSection(
                onLogOutClick = { showLogOutConfirm = true },
                enabled = state.isLoading.not(),
                modifier = Modifier.padding(vertical = 24.dp),
            )
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

@StandardPreview
@Composable
private fun ProfileScreenLightPreview() {
    SoftcoverTheme(darkTheme = false) {
        ProfileScreenLayout(
            state = ProfileScreenPreviewState,
            runAction = {},
            onNavigateUp = {},
            onBookCardEvent = {},
        )
    }
}

@StandardPreview
@Composable
private fun ProfileScreenDarkPreview() {
    SoftcoverTheme(darkTheme = true) {
        ProfileScreenLayout(
            state = ProfileScreenPreviewState,
            runAction = {},
            onNavigateUp = {},
            onBookCardEvent = {},
        )
    }
}

private val PreviewReadingLife = profileReadingLifePreview()

private val ProfileScreenPreviewState = ProfileUiState(
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
