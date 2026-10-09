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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.rhaydus.designsystem.component.DesktopVerticalScrollbar
import nl.rhaydus.softcover.core.component.divider.Divider
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.feature.onboarding.presentation.action.OnboardingAction
import nl.rhaydus.softcover.feature.onboarding.presentation.model.IntroScreen
import nl.rhaydus.softcover.feature.onboarding.presentation.screen.section.ApiKeyEntrySection
import nl.rhaydus.softcover.feature.onboarding.presentation.state.OnboardingUiState

/**
 * Desktop onboarding: the mobile three-page swipe flow collapses into one centered, scrolling panel,
 * the first page's hero above the shared [ApiKeyEntrySection], with no pager, folio or Back/Skip footer.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal actual fun OnboardingScreenLayout(
    state: OnboardingUiState,
    runAction: (action: OnboardingAction) -> Unit,
    openUrl: (String) -> Unit,
    getCopiedText: () -> String,
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
                        horizontal = Spacing.xl,
                        vertical = Spacing.x5l,
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Column(
                    modifier = Modifier.widthIn(max = 520.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    SoftcoverText(
                        text = state.pageHeaders.getValue(IntroScreen.FIRST).eyebrow.uppercase(),
                        style = MaterialTheme.editorialTypography.eyebrow,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(Spacing.sm))

                    SoftcoverText(
                        text = "Book smart.",
                        style = MaterialTheme.editorialTypography.display,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(Spacing.md))

                    SoftcoverText(
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

                    Spacer(modifier = Modifier.height(Spacing.xxxl))

                    Divider()

                    Spacer(modifier = Modifier.height(Spacing.xxxl))

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
                    .padding(vertical = Spacing.xxs),
            )
        }
    }
}
