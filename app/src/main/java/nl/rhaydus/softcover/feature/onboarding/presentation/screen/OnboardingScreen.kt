package nl.rhaydus.softcover.feature.onboarding.presentation.screen

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import nl.rhaydus.softcover.R
import nl.rhaydus.softcover.core.presentation.component.ClickableText
import nl.rhaydus.softcover.core.presentation.component.SoftcoverButton
import nl.rhaydus.softcover.core.presentation.component.SoftcoverLoadingDialog
import nl.rhaydus.softcover.core.presentation.model.ButtonSize
import nl.rhaydus.softcover.core.presentation.model.ButtonStyle
import nl.rhaydus.softcover.core.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.presentation.theme.StandardPreview
import nl.rhaydus.softcover.core.presentation.util.SnackBarManager
import nl.rhaydus.softcover.feature.onboarding.presentation.action.OnApiKeySaveClickAction
import nl.rhaydus.softcover.feature.onboarding.presentation.action.OnApiKeyValueChangeAction
import nl.rhaydus.softcover.feature.onboarding.presentation.action.OnboardingAction
import nl.rhaydus.softcover.feature.onboarding.presentation.model.IntroScreen
import nl.rhaydus.softcover.feature.onboarding.presentation.state.OnboardingUiState
import nl.rhaydus.softcover.feature.onboarding.presentation.screenmodel.OnboardingScreenModel

object OnboardingScreen : Screen {
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<OnboardingScreenModel>()

        val state by screenModel.state.collectAsStateWithLifecycle()

        val uriHandler = LocalUriHandler.current

        val clipboardManager = LocalClipboard.current

        val snackBarState by SnackBarManager.snackBarState.collectAsStateWithLifecycle()

        Screen(
            state = state,
            runAction = screenModel::runAction,
            openUrl = uriHandler::openUri,
            snackbarHostState = snackBarState,
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
            }
        )
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun Screen(
        state: OnboardingUiState,
        snackbarHostState: SnackbarHostState,
        runAction: (action: OnboardingAction) -> Unit,
        openUrl: (String) -> Unit,
        getCopiedText: () -> String,
    ) {
        val pages = IntroScreen.entries
        val pagerState = rememberPagerState { pages.size }

        val scope = rememberCoroutineScope()

        Scaffold(
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            },
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val currentIntroScreen: IntroScreen = pages[page]

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        verticalArrangement = Arrangement.Center
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
                                    }
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

            SoftcoverLoadingDialog(isLoading = state.isLoading)
        }
    }

    @Composable
    private fun ColumnScope.IntroScreen(
        @DrawableRes itemResource: Int,
        illustrationContentDescription: String,
        title: String,
        description: String,
        buttonLabel: String,
        buttonOnClick: () -> Unit,
    ) {
        Image(
            painter = painterResource(id = itemResource),
            contentDescription = illustrationContentDescription,
        )

        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
        ) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(32.dp))

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
            title = "Book Smart.",
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
            illustrationContentDescription = "Illustration containing someone reading a book.",
            title = "Hardcover",
            description = "To get started within Softcover, you'll need your Hardcover API key. This allows the app to sync your reading progress.",
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
            modifier = Modifier.padding(all = 16.dp),
        ) {
            Text(
                text = "API Key",
                style = MaterialTheme.typography.titleMedium,
            )

            Spacer(modifier = Modifier.height(8.dp))

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
                append("For syncing your reading progress. This key can be found ")

                pushStringAnnotation(
                    tag = "url",
                    annotation = "https://hardcover.app/account/api"
                )

                withStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append("here")
                }

                pop()
            }

            ClickableText(
                annotatedText = annotatedString,
                style = MaterialTheme.typography.labelSmall,
                handleUrlClick = openUrl,
            )

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider()

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
                    imageVector = Icons.Default.ContentPaste,
                    contentDescription = "Paste icon",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Paste from clipboard",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
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
            snackbarHostState = SnackbarHostState(),
        )
    }
}