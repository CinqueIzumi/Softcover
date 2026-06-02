package nl.rhaydus.softcover.feature.onboarding.presentation.screen

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import nl.rhaydus.softcover.core.designsystem.R
import nl.rhaydus.softcover.core.designsystem.presentation.component.ClickableText
import nl.rhaydus.softcover.core.designsystem.presentation.component.EditorialSectionHeader
import nl.rhaydus.softcover.core.designsystem.presentation.component.SoftcoverButton
import nl.rhaydus.softcover.core.designsystem.presentation.component.SoftcoverLoadingSheet
import nl.rhaydus.softcover.core.designsystem.presentation.model.ButtonSize
import nl.rhaydus.softcover.core.designsystem.presentation.model.ButtonStyle
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.StandardPreview
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.designsystem.presentation.viewmodel.MainActivityViewModel
import nl.rhaydus.softcover.feature.onboarding.presentation.action.OnApiKeySaveClickAction
import nl.rhaydus.softcover.feature.onboarding.presentation.action.OnApiKeyValueChangeAction
import nl.rhaydus.softcover.feature.onboarding.presentation.action.OnboardingAction
import nl.rhaydus.softcover.feature.onboarding.presentation.model.IntroScreen
import nl.rhaydus.softcover.feature.onboarding.presentation.screenmodel.OnboardingScreenScreenModel
import nl.rhaydus.softcover.feature.onboarding.presentation.state.OnboardingUiState

object OnboardingScreen : Screen {
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<OnboardingScreenScreenModel>()
        val mainVm = koinViewModel<MainActivityViewModel>()

        val state by screenModel.state.collectAsStateWithLifecycle()

        val uriHandler = LocalUriHandler.current

        val clipboardManager = LocalClipboard.current

        Screen(
            state = state,
            runAction = screenModel::runAction,
            openUrl = uriHandler::openUri,
            onInitializingComplete = {
                mainVm.setUserAuthenticated(authenticated = true)
            },
            getCopiedText = {
                val text: String = try {
                    clipboardManager
                        .nativeClipboard
                        .primaryClip
                        ?.getItemAt(0)
                        ?.text
                        ?.toString() ?: ""
                } catch (_: Exception) {
                    ""
                }

                text
            },
        )
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    internal fun Screen(
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
    private fun ColumnScope.IntroScreen(
        @DrawableRes itemResource: Int,
        illustrationContentDescription: String,
        eyebrow: String,
        headline: String,
        description: String,
        buttonLabel: String,
        buttonOnClick: () -> Unit,
    ) {
        Image(
            painter = painterResource(id = itemResource),
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
        IntroScreen(
            itemResource = R.drawable.illu_writing,
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
        IntroScreen(
            itemResource = R.drawable.illu_sign_up,
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
        Column(
            modifier = Modifier.padding(
                horizontal = 24.dp,
                vertical = 16.dp,
            ),
        ) {
            EditorialSectionHeader(
                eyebrow = "Api key",
                headline = "Add your key.",
                description = "Softcover uses your Hardcover API key to sync reading progress.",
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = state.apiKeyValue,
                onValueChange = {
                    runAction(OnApiKeyValueChangeAction(newValue = it))
                },
                shape = RoundedCornerShape(8.dp),
                placeholder = { Text(text = "Enter your key") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors().copy(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                ),
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(16.dp))

            val annotatedString = buildAnnotatedString {
                append("Find your key on the Hardcover website ")

                pushStringAnnotation(
                    tag = "url",
                    annotation = "https://hardcover.app/account/api",
                )

                withStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline,
                    ),
                ) {
                    append("here")
                }

                append(".")

                pop()
            }

            ClickableText(
                annotatedText = annotatedString,
                style = MaterialTheme.editorialTypography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                handleUrlClick = openUrl,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "If you already have the Hardcover app installed, this key can also be found " +
                    "there: Profile → Settings → Hardcover API.",
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(20.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val newValue = getCopiedText()

                        runAction(OnApiKeyValueChangeAction(newValue = newValue))
                    }
                    .padding(all = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_content_paste),
                    contentDescription = "Paste icon",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Paste from clipboard",
                    style = MaterialTheme.editorialTypography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            SoftcoverButton(
                label = "Save API key",
                onClick = { runAction(OnApiKeySaveClickAction()) },
                modifier = Modifier.fillMaxWidth(),
                style = ButtonStyle.FILLED,
                size = ButtonSize.M,
                enabled = state.saveApiKeyButtonEnabled,
            )
        }
    }
}

@StandardPreview
@Composable
private fun FirstIntroScreenPreview() {
    SoftcoverTheme {
        OnboardingScreen.Screen(
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
        OnboardingScreen.Screen(
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
