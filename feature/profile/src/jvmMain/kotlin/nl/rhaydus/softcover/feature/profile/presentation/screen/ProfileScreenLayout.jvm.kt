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
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.DesktopBackStrip
import nl.rhaydus.designsystem.component.DesktopTooltip
import nl.rhaydus.designsystem.component.DesktopVerticalScrollbar
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.modifier.shimmer
import nl.rhaydus.designsystem.theme.StandardPreview
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.profile.domain.model.AuthorDemographics
import nl.rhaydus.softcover.core.profile.domain.model.RatingsDistribution
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData
import nl.rhaydus.softcover.feature.profile.presentation.action.OnLogOutClickAction
import nl.rhaydus.softcover.feature.profile.presentation.action.ProfileAction
import nl.rhaydus.softcover.feature.profile.presentation.state.ProfileUiState

private val IDENTITY_COLUMN_WIDTH = 320.dp

// Keeps the stats column from stretching the hero card and tiles to absurd widths on a maximized
// monitor — past this the content stays left-anchored at an editorial measure.
private val STATS_CONTENT_MAX_WIDTH = 640.dp

// Below this available width the identity column plus a readable stats column no longer fit side by
// side, so the layout collapses to a single centered scrolling column (a narrow desktop window).
private val TWO_COLUMN_MIN_WIDTH = 720.dp

/**
 * Desktop Profile. On a wide surface it is a fixed identity column (cookie-cut avatar, the reader's
 * name and bio, the account foot) beside a wider scrolling "reading life" stats column with a
 * persistent desktop scrollbar. On a narrow desktop window it collapses to a single centered scrolling
 * column. A static top strip carries the back control plus a share glyph; there is no scroll-collapsing
 * top bar. The whole surface paints an opaque [Surface] background so a pushed Profile never lets the
 * screen beneath it bleed through during the navigation transition. The avatar, section labels, and the
 * whole reading-life content block are shared shelf code ([ProfileAvatar], [SectionLabel],
 * [ReadingAtlasSection] and the new "reading life" sections in `ProfileShelf.kt`) — only the
 * arrangement is desktop-specific.
 */
@Composable
internal actual fun ProfileScreenLayout(
    state: ProfileUiState,
    runAction: (ProfileAction) -> Unit,
    onNavigateUp: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        ProfileContent(
            state = state,
            runAction = runAction,
            onNavigateUp = onNavigateUp,
        )
    }
}

@Composable
private fun ProfileContent(
    state: ProfileUiState,
    runAction: (ProfileAction) -> Unit,
    onNavigateUp: () -> Unit,
) {
    var showShareSheet by remember { mutableStateOf(false) }
    var showLogOutConfirm by remember { mutableStateOf(false) }

    val shareContent = remember(state.readingLife, state.userProfileData) {
        val life = state.readingLife
        val profile = state.userProfileData

        if (life != null && profile != null) life.toShareContent(profile) else null
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            DesktopBackStrip(
                onNavigateBack = onNavigateUp,
                backIcon = drawableIconResource(
                    contentDescription = "Navigate back icon",
                    icon = SoftcoverIcon.ArrowBack,
                ),
                modifier = Modifier.weight(1f),
            )

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

            Spacer(modifier = Modifier.width(8.dp))
        }

        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            if (maxWidth >= TWO_COLUMN_MIN_WIDTH) {
                TwoColumnProfile(
                    state = state,
                    onShareClick = { showShareSheet = true },
                    onLogOutClick = { showLogOutConfirm = true },
                )
            } else {
                SingleColumnProfile(
                    state = state,
                    onShareClick = { showShareSheet = true },
                    onLogOutClick = { showLogOutConfirm = true },
                )
            }
        }
    }

    if (showShareSheet && shareContent != null) {
        ProfileShareBottomSheet(
            content = shareContent,
            onDismissRequest = { showShareSheet = false },
        )
    }

    if (showLogOutConfirm) {
        LogOutConfirmBottomSheet(
            onLogOutClick = { runAction(OnLogOutClickAction()) },
            onDismissRequest = { showLogOutConfirm = false },
        )
    }
}

@Composable
private fun TwoColumnProfile(
    state: ProfileUiState,
    onShareClick: () -> Unit,
    onLogOutClick: () -> Unit,
) {
    Row(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .width(IDENTITY_COLUMN_WIDTH)
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

        VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)

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
                    modifier = Modifier.widthIn(max = STATS_CONTENT_MAX_WIDTH),
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
) {
    Box(modifier = Modifier.fillMaxSize()) {
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .widthIn(max = STATS_CONTENT_MAX_WIDTH)
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
    modifier: Modifier = Modifier,
) {
    // ReadingAtlasSection reads state.isLoading directly (it is driven by userProfileData alone),
    // but every section below also needs readingLife to have arrived — the two land via separate
    // collectors, so gating on isLoading alone risks a section briefly rendering its permanent
    // "nothing yet" copy before readingLife has actually resolved.
    val readingLifeLoading = state.isLoading || state.readingLife == null

    Column(modifier = modifier) {
        ReadingAtlasSection(
            userProfileData = state.userProfileData,
            isLoading = state.isLoading,
        )

        Spacer(modifier = Modifier.height(16.dp))

        ShareEntryRow(
            isLoading = readingLifeLoading,
            onClick = onShareClick,
        )

        Spacer(modifier = Modifier.height(40.dp))

        YearColumnHistorySection(
            readingLife = state.readingLife,
            isLoading = readingLifeLoading,
        )

        Spacer(modifier = Modifier.height(40.dp))

        GenreStackSection(
            genres = state.readingLife?.genres.orEmpty(),
            isLoading = readingLifeLoading,
        )

        Spacer(modifier = Modifier.height(40.dp))

        AuthorRepresentationSection(
            authorDemographics = state.readingLife?.authorDemographics ?: AuthorDemographics(),
            isLoading = readingLifeLoading,
        )

        Spacer(modifier = Modifier.height(40.dp))

        RatingsHistogramSection(
            ratings = state.readingLife?.ratings ?: RatingsDistribution(),
            isLoading = readingLifeLoading,
        )

        Spacer(modifier = Modifier.height(40.dp))

        RecentlyLovedSection(
            books = state.readingLife?.recentlyLoved.orEmpty(),
            isLoading = readingLifeLoading,
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

        SectionLabel(text = "The reader")

        Spacer(modifier = Modifier.height(10.dp))

        Text(
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

            Text(
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
        )
    }
}

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
    readingLife = profileReadingLifePreview(),
)
