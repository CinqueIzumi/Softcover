package nl.rhaydus.softcover.feature.profile.presentation.screen

import nl.rhaydus.designsystem.component.DesktopBackStrip
import nl.rhaydus.designsystem.component.RhaydusButton
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.designsystem.modifier.shimmer
import nl.rhaydus.softcover.core.designsystem.presentation.component.DesktopVerticalScrollbar
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
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
 * name and bio, the log-out action) beside a wider scrolling "Reading atlas" stats column with a
 * persistent desktop scrollbar. On a narrow desktop window it collapses to a single centered scrolling
 * column. A static top strip carries the back control; there is no scroll-collapsing top bar. The whole
 * surface paints an opaque [Surface] background so a pushed Profile never lets the screen beneath it
 * bleed through during the navigation transition. The avatar, section labels, and the entire stat block
 * are shared shelf code ([ProfileAvatar], [SectionLabel], [ReadingAtlasSection]) — only the arrangement
 * is desktop-specific.
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
        Column(modifier = Modifier.fillMaxSize()) {
            DesktopBackStrip(
                onNavigateBack = onNavigateUp,
                backIcon = drawableIconResource(
                    contentDescription = "Navigate back icon",
                    icon = SoftcoverIcon.ArrowBack,
                ),
            )

            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                if (maxWidth >= TWO_COLUMN_MIN_WIDTH) {
                    TwoColumnProfile(
                        state = state,
                        runAction = runAction,
                    )
                } else {
                    SingleColumnProfile(
                        state = state,
                        runAction = runAction,
                    )
                }
            }
        }
    }
}

@Composable
private fun TwoColumnProfile(
    state: ProfileUiState,
    runAction: (ProfileAction) -> Unit,
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

            RhaydusButton(
                label = "Log out",
                onClick = { runAction(OnLogOutClickAction()) },
                style = ButtonStyle.TONAL,
                modifier = Modifier.fillMaxWidth(),
                enabled = state.isLoading.not(),
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
                ReadingAtlasSection(
                    userProfileData = state.userProfileData,
                    isLoading = state.isLoading,
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
    runAction: (ProfileAction) -> Unit,
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

            ReadingAtlasSection(
                userProfileData = state.userProfileData,
                isLoading = state.isLoading,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(32.dp))

            RhaydusButton(
                label = "Log out",
                onClick = { runAction(OnLogOutClickAction()) },
                style = ButtonStyle.TONAL,
                modifier = Modifier.fillMaxWidth(),
                enabled = state.isLoading.not(),
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
