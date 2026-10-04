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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.DesktopTooltip
import nl.rhaydus.designsystem.component.DesktopVerticalScrollbar
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.modifier.shimmer
import nl.rhaydus.designsystem.theme.StandardPreview
import nl.rhaydus.softcover.core.component.bookcard.BookCardEvent
import nl.rhaydus.softcover.core.component.divider.VerticalDivider
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.sheet.SheetScaffold
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldEvent
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.component.topbar.EditorialTopBar
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
import nl.rhaydus.softcover.feature.profile.presentation.state.YearMetric

/**
 * Desktop Profile. On a wide surface it is a fixed identity column (cookie-cut avatar, the reader's
 * name and bio, the account foot) beside a wider scrolling "reading life" stats column with a
 * persistent desktop scrollbar. On a narrow desktop window it collapses to a single centered scrolling
 * column. An editorial top bar carries the back control plus a share glyph; there is no scroll-collapsing
 * top bar. The whole surface paints an opaque [Surface] background so a pushed Profile never lets the
 * screen beneath it bleed through during the navigation transition. The avatar, section labels, and the
 * whole reading-life content block are shared shelf code ([ProfileAvatar],
 * [ReadingAtlasSection] and the other "reading life" sections in `section/`) — only the
 * arrangement is desktop-specific.
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

    val shareContent = state.readingLifeShareCard

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
                    IconButton(
                        onClick = { showShareSheet = true },
                        enabled = shareContent != null,
                        modifier = Modifier.pointerHandCursor(),
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
                    onShareClick = { showShareSheet = true },
                    onLogOutClick = { showLogOutConfirm = true },
                    onHideUntaggedAuthorsChange = { runAction(OnHideUntaggedAuthorsToggledAction(newValue = it)) },
                    onYearMetricSelected = { runAction(OnYearMetricSelectedAction(metric = it)) },
                    onBookCardEvent = onBookCardEvent,
                )
            } else {
                SingleColumnProfile(
                    state = state,
                    onShareClick = { showShareSheet = true },
                    onLogOutClick = { showLogOutConfirm = true },
                    onHideUntaggedAuthorsChange = { runAction(OnHideUntaggedAuthorsToggledAction(newValue = it)) },
                    onYearMetricSelected = { runAction(OnYearMetricSelectedAction(metric = it)) },
                    onBookCardEvent = onBookCardEvent,
                )
            }
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
private fun TwoColumnProfile(
    state: ProfileUiState,
    onShareClick: () -> Unit,
    onLogOutClick: () -> Unit,
    onHideUntaggedAuthorsChange: (Boolean) -> Unit,
    onYearMetricSelected: (YearMetric) -> Unit,
    onBookCardEvent: (BookCardEvent) -> Unit,
) {
    Row(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .width(320.dp)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 24.dp),
        ) {
            ReaderIdentity(
                state = state,
                horizontalAlignment = Alignment.Start,
            )

            Spacer(modifier = Modifier.height(28.dp))

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
                    .padding(start = 32.dp, end = 24.dp, top = 8.dp, bottom = 24.dp),
            ) {
                ReadingLifeContent(
                    state = state,
                    onShareClick = onShareClick,
                    onHideUntaggedAuthorsChange = onHideUntaggedAuthorsChange,
                    onYearMetricSelected = onYearMetricSelected,
                    onBookCardEvent = onBookCardEvent,
                    modifier = Modifier.widthIn(max = 640.dp),
                )
            }

            DesktopVerticalScrollbar(
                scrollState = scrollState,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .padding(vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun SingleColumnProfile(
    state: ProfileUiState,
    onShareClick: () -> Unit,
    onLogOutClick: () -> Unit,
    onHideUntaggedAuthorsChange: (Boolean) -> Unit,
    onYearMetricSelected: (YearMetric) -> Unit,
    onBookCardEvent: (BookCardEvent) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .widthIn(max = 640.dp)
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ReaderIdentity(
                state = state,
                horizontalAlignment = Alignment.CenterHorizontally,
            )

            Spacer(modifier = Modifier.height(32.dp))

            ReadingLifeContent(
                state = state,
                onShareClick = onShareClick,
                onHideUntaggedAuthorsChange = onHideUntaggedAuthorsChange,
                onYearMetricSelected = onYearMetricSelected,
                onBookCardEvent = onBookCardEvent,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(32.dp))

            AccountFootSection(
                onLogOutClick = onLogOutClick,
                enabled = state.isLoading.not(),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        DesktopVerticalScrollbar(
            scrollState = scrollState,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .padding(vertical = 4.dp),
        )
    }
}

/**
 * The whole "reading life" stack shared by both desktop column arrangements: the atlas hero, the share
 * entry row, and every new reading-life section, in the same top-to-bottom order the mobile layout
 * uses.
 */
@Composable
private fun ReadingLifeContent(
    state: ProfileUiState,
    onShareClick: () -> Unit,
    onHideUntaggedAuthorsChange: (Boolean) -> Unit,
    onYearMetricSelected: (YearMetric) -> Unit,
    onBookCardEvent: (BookCardEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        ReadingAtlasSection(
            readingAtlas = state.readingAtlas,
            header = state.readingAtlasHeader,
        )

        Spacer(modifier = Modifier.height(16.dp))

        ShareEntryRow(
            isLoading = state.isReadingLifeLoading,
            onClick = onShareClick,
        )

        Spacer(modifier = Modifier.height(40.dp))

        YearColumnHistorySection(
            chart = state.yearHistoryChart,
            caption = state.yearHistoryCaption,
            isLoading = state.isReadingLifeLoading,
            header = state.yearColumnHistoryHeader,
            metricToggle = state.yearMetricToggle,
            onMetricSelected = onYearMetricSelected,
        )

        Spacer(modifier = Modifier.height(40.dp))

        GenreRankingSection(
            genres = state.readingLife?.genres ?: GenreBreakdown(),
            genreRanking = state.genreRanking,
            isLoading = state.isReadingLifeLoading,
            header = state.genreRankingHeader,
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
            onHideUntaggedAuthorsChange = onHideUntaggedAuthorsChange,
        )

        Spacer(modifier = Modifier.height(40.dp))

        RatingsHistogramSection(
            chart = state.ratingsChart,
            average = state.ratingsAverage,
            totalRatings = state.ratingsTotal,
            isLoading = state.isReadingLifeLoading,
            header = state.ratingsHistogramHeader,
            caption = state.ratingsHistogramCaption,
        )

        Spacer(modifier = Modifier.height(40.dp))

        RecentlyLovedSection(
            cards = state.recentlyLoved.cards,
            hasRecentlyLoved = state.hasRecentlyLoved,
            isLoading = state.isReadingLifeLoading,
            header = state.recentlyLovedHeader,
            onEvent = onBookCardEvent,
        )
    }
}

@Composable
private fun ReaderIdentity(
    state: ProfileUiState,
    horizontalAlignment: Alignment.Horizontal,
    modifier: Modifier = Modifier,
) {
    val data = state.userProfileData

    val textAlign =
        if (horizontalAlignment == Alignment.CenterHorizontally) TextAlign.Center else TextAlign.Start

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = horizontalAlignment,
    ) {
        ProfileAvatar(
            profileImageUrl = data?.profileImageUrl,
            isLoading = state.isLoading,
            modifier = Modifier.size(140.dp),
        )

        Spacer(modifier = Modifier.height(20.dp))

        SectionHeader(model = state.theReaderHeader)

        Spacer(modifier = Modifier.height(10.dp))

        SoftcoverText(
            text = data?.name.orEmpty(),
            style = MaterialTheme.editorialTypography.display,
            color = MaterialTheme.colorScheme.primary,
            textAlign = textAlign,
            modifier = Modifier
                .fillMaxWidth()
                .shimmer(isLoading = state.isLoading),
        )

        val bio = data?.bio.orEmpty()

        if (bio.isNotBlank()) {
            Spacer(modifier = Modifier.height(12.dp))

            SoftcoverText(
                text = "“$bio”",
                style = MaterialTheme.editorialTypography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = textAlign,
                modifier = Modifier
                    .fillMaxWidth()
                    .shimmer(isLoading = state.isLoading),
            )
        }
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
