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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.layout.cappedContentWidth
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.designsystem.modifier.shimmer
import nl.rhaydus.designsystem.theme.StandardPreview
import nl.rhaydus.softcover.core.designsystem.presentation.component.SoftcoverTopBar
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData
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
    Scaffold(
        topBar = {
            SoftcoverTopBar(
                title = "",
                onNavigateBack = onNavigateUp,
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

            Spacer(modifier = Modifier.height(40.dp))

            RhaydusButton(
                label = "Log out",
                onClick = { runAction(OnLogOutClickAction()) },
                style = ButtonStyle.TONAL,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 24.dp,
                        vertical = 24.dp,
                    ),
                enabled = state.isLoading.not(),
            )
        }
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
private fun ProfileScreenPreview() {
    SoftcoverTheme {
        ProfileScreenLayout(
            state = ProfileUiState(
                isLoading = false,
                userProfileData = UserProfileData(
                    profileImageUrl = "",
                    name = "Cinque",
                    username = "cinque",
                    bio = "Lover of classic literature and sci-fi.",
                    booksRead = 20,
                    totalPagesRead = 5_432,
                    averageRating = 4.2,
                    readingStreak = 7,
                ),
            ),
            runAction = {},
            onNavigateUp = {},
        )
    }
}
