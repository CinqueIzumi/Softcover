package nl.rhaydus.softcover.feature.profile.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.layout.cappedContentWidth
import nl.rhaydus.designsystem.modifier.shimmer
import nl.rhaydus.designsystem.theme.StandardPreview
import nl.rhaydus.softcover.core.component.bookcard.BookCardEvent
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffold
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldEvent
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.component.topbar.TopBar
import nl.rhaydus.softcover.core.component.topbar.TopBarEvent
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.profile.domain.model.GenreBreakdown
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData
import nl.rhaydus.softcover.feature.profile.presentation.action.OnHideUntaggedAuthorsToggledAction
import nl.rhaydus.softcover.feature.profile.presentation.action.OnLogOutClickAction
import nl.rhaydus.softcover.feature.profile.presentation.action.OnYearMetricSelectedAction
import nl.rhaydus.softcover.feature.profile.presentation.action.ProfileAction
import nl.rhaydus.softcover.feature.profile.presentation.mapper.demographicChartUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.genderChartUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.genreRankingUiModel
import nl.rhaydus.softcover.feature.profile.presentation.screen.section.AccountFootSection
import nl.rhaydus.softcover.feature.profile.presentation.screen.section.AuthorRepresentationSection
import nl.rhaydus.softcover.feature.profile.presentation.screen.section.GenreRankingSection
import nl.rhaydus.softcover.feature.profile.presentation.screen.section.ProfileAvatar
import nl.rhaydus.softcover.feature.profile.presentation.screen.section.ProfileShareBottomSheet
import nl.rhaydus.softcover.feature.profile.presentation.screen.section.RatingsHistogramSection
import nl.rhaydus.softcover.feature.profile.presentation.screen.section.ReadingAtlasSection
import nl.rhaydus.softcover.feature.profile.presentation.screen.section.RecentlyLovedSection
import nl.rhaydus.softcover.feature.profile.presentation.screen.section.ShareEntryRow
import nl.rhaydus.softcover.feature.profile.presentation.screen.section.YearColumnHistorySection
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

    val shareContent = state.readingLifeShareCard

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
                    IconButton(
                        onClick = { showShareSheet = true },
                        enabled = shareContent != null,
                    ) {
                        val icon = drawableIconResource(
                            icon = SoftcoverIcon.Share,
                            contentDescription = "Share your reading year",
                        )

                        Icon(
                            painter = icon.getIconPainter(),
                            contentDescription = icon.contentDescription,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .cappedContentWidth(),
        ) {
            ProfileHeader(
                profileImageUrl = state.userProfileData?.profileImageUrl,
                name = state.userProfileData?.name.orEmpty(),
                bio = state.userProfileData?.bio.orEmpty(),
                isLoading = state.isLoading,
                header = state.theReaderHeader,
            )

            Spacer(modifier = Modifier.height(36.dp))

            ReadingAtlasSection(
                readingAtlas = state.readingAtlas,
                header = state.readingAtlasHeader,
                modifier = Modifier.padding(horizontal = 24.dp),
            )

            Spacer(modifier = Modifier.height(16.dp))

            ShareEntryRow(
                isLoading = state.isReadingLifeLoading,
                onClick = { showShareSheet = true },
                modifier = Modifier.padding(horizontal = 24.dp),
            )

            Spacer(modifier = Modifier.height(40.dp))

            YearColumnHistorySection(
                chart = state.yearHistoryChart,
                caption = state.yearHistoryCaption,
                isLoading = state.isReadingLifeLoading,
                header = state.yearColumnHistoryHeader,
                metricToggle = state.yearMetricToggle,
                onMetricSelected = { runAction(OnYearMetricSelectedAction(metric = it)) },
                modifier = Modifier.padding(horizontal = 24.dp),
            )

            Spacer(modifier = Modifier.height(40.dp))

            GenreRankingSection(
                genres = state.readingLife?.genres ?: GenreBreakdown(),
                genreRanking = state.genreRanking,
                isLoading = state.isReadingLifeLoading,
                header = state.genreRankingHeader,
                modifier = Modifier.padding(horizontal = 24.dp),
            )

            Spacer(modifier = Modifier.height(40.dp))

            AuthorRepresentationSection(
                authorDemographics = state.authorDemographics,
                genderChart = state.genderChart,
                bipocChart = state.bipocChart,
                lgbtqChart = state.lgbtqChart,
                hideUntaggedAuthors = state.hideUntaggedAuthors,
                hideUntaggedAuthorsToggle = state.hideUntaggedAuthorsToggle,
                canHideUntaggedAuthors = state.hasUntaggedAuthors,
                isLoading = state.isReadingLifeLoading,
                header = state.authorRepresentationHeader,
                onHideUntaggedAuthorsChange = { runAction(OnHideUntaggedAuthorsToggledAction(newValue = it)) },
                modifier = Modifier.padding(horizontal = 24.dp),
            )

            Spacer(modifier = Modifier.height(40.dp))

            RatingsHistogramSection(
                chart = state.ratingsChart,
                average = state.ratingsAverage,
                totalRatings = state.ratingsTotal,
                isLoading = state.isReadingLifeLoading,
                header = state.ratingsHistogramHeader,
                caption = state.ratingsHistogramCaption,
                modifier = Modifier.padding(horizontal = 24.dp),
            )

            Spacer(modifier = Modifier.height(40.dp))

            RecentlyLovedSection(
                cards = state.recentlyLovedCards,
                hasRecentlyLoved = state.hasRecentlyLoved,
                isLoading = state.isReadingLifeLoading,
                header = state.recentlyLovedHeader,
                onEvent = onBookCardEvent,
                modifier = Modifier.padding(horizontal = 24.dp),
            )

            Spacer(modifier = Modifier.height(40.dp))

            AccountFootSection(
                onLogOutClick = { showLogOutConfirm = true },
                enabled = state.isLoading.not(),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp),
            )
        }
    }

    if (showShareSheet && shareContent != null) {
        ProfileShareBottomSheet(
            model = shareContent,
            scaffold = state.shareSheetScaffold,
            onDismissRequest = { showShareSheet = false },
        )
    }

    if (showLogOutConfirm) {
        SheetScaffold(
            model = state.logOutSheetScaffold,
            onEvent = { event ->
                when (event) {
                    SheetScaffoldEvent.PrimaryClicked -> runAction(OnLogOutClickAction())

                    SheetScaffoldEvent.Dismissed -> showLogOutConfirm = false

                    SheetScaffoldEvent.SecondaryClicked,
                    SheetScaffoldEvent.HeaderActionClicked -> Unit
                }
            },
            content = {},
        )
    }
}

@Composable
private fun ProfileHeader(
    profileImageUrl: String?,
    name: String,
    bio: String,
    isLoading: Boolean,
    header: SectionHeaderUiModel,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ProfileAvatar(
            profileImageUrl = profileImageUrl,
            isLoading = isLoading,
            modifier = Modifier.size(168.dp),
        )

        Spacer(modifier = Modifier.height(20.dp))

        SectionHeader(model = header)

        Spacer(modifier = Modifier.height(10.dp))

        SoftcoverText(
            text = name,
            style = MaterialTheme.editorialTypography.display,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .shimmer(isLoading = isLoading),
        )

        if (bio.isNotBlank()) {
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .height(1.dp)
                        .width(28.dp)
                        .background(MaterialTheme.colorScheme.outline),
                )

                SoftcoverText(
                    text = "•",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(horizontal = 10.dp),
                )

                Box(
                    modifier = Modifier
                        .height(1.dp)
                        .width(28.dp)
                        .background(MaterialTheme.colorScheme.outline),
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            SoftcoverText(
                text = "“$bio”",
                style = MaterialTheme.editorialTypography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.shimmer(isLoading = isLoading),
            )
        }
    }
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
