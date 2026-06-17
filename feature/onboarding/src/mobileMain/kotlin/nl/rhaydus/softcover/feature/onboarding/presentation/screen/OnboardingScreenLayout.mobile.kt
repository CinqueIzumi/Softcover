package nl.rhaydus.softcover.feature.onboarding.presentation.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import nl.rhaydus.designsystem.theme.StandardPreview
import nl.rhaydus.softcover.core.designsystem.presentation.component.SoftcoverButton
import nl.rhaydus.softcover.core.designsystem.presentation.component.SoftcoverLoadingSheet
import nl.rhaydus.softcover.core.designsystem.presentation.illustration.SoftcoverIllustration
import nl.rhaydus.softcover.core.designsystem.presentation.illustration.painter
import nl.rhaydus.softcover.core.designsystem.presentation.model.ButtonSize
import nl.rhaydus.softcover.core.designsystem.presentation.model.ButtonStyle
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.feature.onboarding.presentation.action.OnboardingAction
import nl.rhaydus.softcover.feature.onboarding.presentation.model.IntroScreen
import nl.rhaydus.softcover.feature.onboarding.presentation.state.OnboardingUiState

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal actual fun OnboardingScreenLayout(
    state: OnboardingUiState,
    runAction: (action: OnboardingAction) -> Unit,
    openUrl: (String) -> Unit,
    getCopiedText: () -> String,
    onInitializingComplete: () -> Unit,
) {
    val pages = IntroScreen.entries
    val pagerState = rememberPagerState { pages.size }

    val scope = rememberCoroutineScope()

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding(),
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                val currentIntroScreen: IntroScreen = pages[page]

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Center,
                ) {
                    when (currentIntroScreen) {
                        IntroScreen.FIRST -> {
                            FirstIntroScreen(
                                onContinueClick = {
                                    scope.launch {
                                        pagerState.animateScrollToPage(page = page + 1)
                                    }
                                },
                            )
                        }

                        IntroScreen.SECOND -> {
                            SecondIntroScreen(
                                onContinueClick = {
                                    scope.launch {
                                        pagerState.animateScrollToPage(page = page + 1)
                                    }
                                },
                            )
                        }

                        IntroScreen.THIRD -> {
                            ThirdIntroScreen(
                                state = state,
                                runAction = runAction,
                                openUrl = openUrl,
                                getCopiedText = getCopiedText,
                            )
                        }
                    }
                }
            }
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

@Composable
private fun ColumnScope.IntroScreenPage(
    illustration: SoftcoverIllustration,
    illustrationContentDescription: String,
    eyebrow: String,
    headline: String,
    description: String,
    buttonLabel: String,
    buttonOnClick: () -> Unit,
) {
    Image(
        painter = illustration.painter(),
        contentDescription = illustrationContentDescription,
    )

    Spacer(modifier = Modifier.height(32.dp))

    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Text(
            text = eyebrow.uppercase(),
            style = MaterialTheme.editorialTypography.eyebrow,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = headline,
            style = MaterialTheme.editorialTypography.display,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = description,
            style = MaterialTheme.editorialTypography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(40.dp))

        SoftcoverButton(
            label = buttonLabel,
            style = ButtonStyle.FILLED,
            size = ButtonSize.M,
            modifier = Modifier.fillMaxWidth(),
            onClick = buttonOnClick,
        )
    }
}

@Composable
private fun ColumnScope.FirstIntroScreen(
    onContinueClick: () -> Unit,
) {
    IntroScreenPage(
        illustration = SoftcoverIllustration.Writing,
        illustrationContentDescription = "Illustration containing someone reading a book.",
        eyebrow = "Welcome",
        headline = "Book smart.",
        description = "Track every book, share them with the world (or don't) and find new life changing reads.",
        buttonLabel = "Continue",
        buttonOnClick = onContinueClick,
    )
}

@Composable
private fun ColumnScope.SecondIntroScreen(
    onContinueClick: () -> Unit,
) {
    IntroScreenPage(
        illustration = SoftcoverIllustration.SignUp,
        illustrationContentDescription = "Illustration containing someone signing up for an account.",
        eyebrow = "Connect",
        headline = "Powered by Hardcover.",
        description = "To get started with Softcover, you'll need a Hardcover account to sync your reading progress.",
        buttonLabel = "Continue",
        buttonOnClick = onContinueClick,
    )
}

@Composable
private fun ThirdIntroScreen(
    state: OnboardingUiState,
    runAction: (action: OnboardingAction) -> Unit,
    openUrl: (String) -> Unit,
    getCopiedText: () -> String,
) {
    ApiKeyEntrySection(
        state = state,
        runAction = runAction,
        openUrl = openUrl,
        getCopiedText = getCopiedText,
        modifier = Modifier.padding(
            horizontal = 24.dp,
            vertical = 16.dp,
        ),
    )
}

@StandardPreview
@Composable
private fun FirstIntroScreenPreview() {
    SoftcoverTheme {
        OnboardingScreenLayout(
            state = OnboardingUiState(),
            runAction = {},
            getCopiedText = { "" },
            openUrl = {},
            onInitializingComplete = {},
        )
    }
}

@StandardPreview
@Composable
private fun LoadingDialogIntroScreenPreview() {
    SoftcoverTheme {
        OnboardingScreenLayout(
            state = OnboardingUiState(
                isLoading = true,
                progress = 0.2f,
            ),
            runAction = {},
            getCopiedText = { "" },
            openUrl = {},
            onInitializingComplete = {},
        )
    }
}
