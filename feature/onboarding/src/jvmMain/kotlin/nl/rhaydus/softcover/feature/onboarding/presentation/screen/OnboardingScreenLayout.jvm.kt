package nl.rhaydus.softcover.feature.onboarding.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.rhaydus.designsystem.component.DesktopVerticalScrollbar
import nl.rhaydus.softcover.core.designsystem.presentation.component.SoftcoverLoadingSheet
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.feature.onboarding.presentation.action.OnboardingAction
import nl.rhaydus.softcover.feature.onboarding.presentation.state.OnboardingUiState

// Keeps the onboarding panel at a readable editorial measure rather than stretching the intro copy and
// the key field across a maximized window.
private val PANEL_MAX_WIDTH = 520.dp

/**
 * Desktop onboarding. The mobile three-page swipe flow collapses into one centered, scrolling
 * editorial panel: the same type-led "Welcome" hero used on mobile page 1 (sized down for the narrower,
 * centered panel — `editorialTypography.display`'s baked-in 45sp/44sp rather than mobile's oversized
 * 76sp override, since a full-bleed hero size reads as oversized once it is centered in a 520dp column
 * instead of bleeding to a phone's edges) above the shared [ApiKeyEntrySection] — no pager, no folio, no
 * top-bar chrome (those are mobile-pager-only concerns) and no separate Back/Skip footer: desktop keeps
 * a single "Save API key" button flow via the shared section. The whole surface paints an opaque
 * [Surface] background, and the [SoftcoverLoadingSheet] overlay still covers the setup progress.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal actual fun OnboardingScreenLayout(
    state: OnboardingUiState,
    runAction: (action: OnboardingAction) -> Unit,
    openUrl: (String) -> Unit,
    getCopiedText: () -> String,
    onInitializingComplete: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(
                        horizontal = 24.dp,
                        vertical = 48.dp,
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Column(
                    modifier = Modifier.widthIn(max = PANEL_MAX_WIDTH),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Welcome".uppercase(),
                        style = MaterialTheme.editorialTypography.eyebrow,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Book smart.",
                        style = MaterialTheme.editorialTypography.display,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Track every book you read, share the ones worth sharing — or " +
                            "don't — and find your next life-changing read.",
                        style = MaterialTheme.editorialTypography.bodyLarge.copy(
                            fontSize = 18.sp,
                            lineHeight = 28.sp,
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    Spacer(modifier = Modifier.height(32.dp))

                    ApiKeyEntrySection(
                        state = state,
                        runAction = runAction,
                        openUrl = openUrl,
                        getCopiedText = getCopiedText,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            DesktopVerticalScrollbar(
                scrollState = scrollState,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .padding(vertical = 4.dp),
            )
        }

        SoftcoverLoadingSheet(
            isLoading = state.isLoading,
            progress = state.progress,
            onLoaderFinished = onInitializingComplete,
            eyebrow = "Setting up",
            headline = "Pulling your library together.",
            description = "Depending on its size, this might take a moment.",
        )
    }
}
