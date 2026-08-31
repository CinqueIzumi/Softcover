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
import androidx.compose.material3.Text
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
import nl.rhaydus.softcover.core.designsystem.presentation.component.SoftcoverTopBar
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.profile.domain.model.GenreBreakdown
import nl.rhaydus.softcover.core.profile.domain.model.RatingsDistribution
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData
import nl.rhaydus.softcover.feature.profile.presentation.action.OnHideUntaggedAuthorsToggledAction
import nl.rhaydus.softcover.feature.profile.presentation.action.OnLogOutClickAction
import nl.rhaydus.softcover.feature.profile.presentation.action.ProfileAction
import nl.rhaydus.softcover.feature.profile.presentation.state.ProfileUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal actual fun ProfileScreenLayout(
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

    // ReadingAtlasSection reads state.isLoading directly (it is driven by userProfileData alone),
    // but every reading-life section below also needs readingLife to have arrived — the two land
    // via separate collectors, so gating on isLoading alone risks a section briefly rendering its
    // permanent "nothing yet" copy before readingLife has actually resolved.
    val readingLifeLoading = state.isLoading || state.readingLife == null

    Scaffold(
        topBar = {
            SoftcoverTopBar(
                title = "",
                onNavigateBack = onNavigateUp,
                additionalActions = {
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
            )

            Spacer(modifier = Modifier.height(36.dp))

            ReadingAtlasSection(
                userProfileData = state.userProfileData,
                isLoading = state.isLoading,
                modifier = Modifier.padding(horizontal = 24.dp),
            )

            Spacer(modifier = Modifier.height(16.dp))

            ShareEntryRow(
                isLoading = readingLifeLoading,
                onClick = { showShareSheet = true },
                modifier = Modifier.padding(horizontal = 24.dp),
            )

            Spacer(modifier = Modifier.height(40.dp))

            YearColumnHistorySection(
                readingLife = state.readingLife,
                isLoading = readingLifeLoading,
                modifier = Modifier.padding(horizontal = 24.dp),
            )

            Spacer(modifier = Modifier.height(40.dp))

            GenreRankingSection(
                genres = state.readingLife?.genres ?: GenreBreakdown(),
                isLoading = readingLifeLoading,
                modifier = Modifier.padding(horizontal = 24.dp),
            )

            Spacer(modifier = Modifier.height(40.dp))

            AuthorRepresentationSection(
                authorDemographics = state.authorDemographics,
                hideUntaggedAuthors = state.hideUntaggedAuthors,
                canHideUntaggedAuthors = state.hasUntaggedAuthors,
                isLoading = readingLifeLoading,
                onHideUntaggedAuthorsChange = { runAction(OnHideUntaggedAuthorsToggledAction(newValue = it)) },
                modifier = Modifier.padding(horizontal = 24.dp),
            )

            Spacer(modifier = Modifier.height(40.dp))

            RatingsHistogramSection(
                ratings = state.readingLife?.ratings ?: RatingsDistribution(),
                isLoading = readingLifeLoading,
                modifier = Modifier.padding(horizontal = 24.dp),
            )

            Spacer(modifier = Modifier.height(40.dp))

            RecentlyLovedSection(
                books = state.readingLife?.recentlyLoved.orEmpty(),
                isLoading = readingLifeLoading,
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
private fun ProfileHeader(
    profileImageUrl: String?,
    name: String,
    bio: String,
    isLoading: Boolean,
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

        SectionLabel(text = "The reader")

        Spacer(modifier = Modifier.height(10.dp))

        Text(
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

                Text(
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

            Text(
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
        )
    }
}

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
    readingLife = profileReadingLifePreview(),
)
