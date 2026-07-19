package nl.rhaydus.softcover.feature.onboarding.presentation.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import nl.rhaydus.designsystem.motion.playDecorativeMotion
import nl.rhaydus.designsystem.theme.StandardPreview
import nl.rhaydus.softcover.core.designsystem.presentation.component.SoftcoverLoadingSheet
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.feature.onboarding.presentation.action.OnboardingAction
import nl.rhaydus.softcover.feature.onboarding.presentation.model.IntroScreen
import nl.rhaydus.softcover.feature.onboarding.presentation.state.OnboardingUiState

private val PAGE_NUMBERS = listOf(1, 2, 3)

/** The persistent top bar's per-page eyebrow label, keyed on the pager's settled current page. */
private val IntroScreen.eyebrow: String
    get() = when (this) {
        IntroScreen.FIRST -> "Welcome"
        IntroScreen.SECOND -> "Connect"
        IntroScreen.THIRD -> "API key"
    }

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

    fun navigateToPage(index: Int) {
        scope.launch { pagerState.animateScrollToPage(page = index) }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding(),
        ) {
            // Persistent chrome, rendered once above the pager (not per-page) so it never shifts as
            // pages swipe — only the eyebrow label and the folio's active digit track the pager below.
            OnboardingTopBar(
                eyebrow = pages[pagerState.currentPage].eyebrow,
                activePage = pagerState.currentPage + 1,
                modifier = Modifier
                    .padding(horizontal = 30.dp)
                    .padding(top = 24.dp),
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) { page ->
                when (pages[page]) {
                    IntroScreen.FIRST -> {
                        FirstIntroScreen(
                            onBeginClick = { navigateToPage(index = 1) },
                            onSkipClick = { navigateToPage(index = 2) },
                        )
                    }

                    IntroScreen.SECOND -> {
                        SecondIntroScreen(
                            onContinueClick = { navigateToPage(index = 2) },
                            onBackClick = { navigateToPage(index = 0) },
                        )
                    }

                    IntroScreen.THIRD -> {
                        ThirdIntroScreen(
                            state = state,
                            runAction = runAction,
                            openUrl = openUrl,
                            getCopiedText = getCopiedText,
                            onBackClick = { navigateToPage(index = 1) },
                        )
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

/**
 * The persistent chrome above the pager: an accent-bar eyebrow on the left (the established
 * hand-composed pattern — `EditorialSectionHeader` has no trailing slot for the folio, see
 * `TagEditorHeader`/`AppUpdateSection` for the same convention elsewhere) and the "01 — 02 — 03" folio
 * indicator on the right. Rendered once above the pager rather than per-page, so its position never
 * shifts as pages swipe — only the [eyebrow] label cross-fades and the folio's active digit updates as
 * the pager settles on a new page. Back/Skip live only in [PrimaryActionFooterRow], never here.
 */
@Composable
private fun OnboardingTopBar(
    eyebrow: String,
    activePage: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(
                        width = 32.dp,
                        height = 4.dp,
                    )
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.primary),
            )

            Spacer(modifier = Modifier.width(12.dp))

            val playMotion = playDecorativeMotion()
            val fadeSpec = if (playMotion) tween<Float>(durationMillis = 220) else snap()

            AnimatedContent(
                targetState = eyebrow,
                transitionSpec = {
                    fadeIn(animationSpec = fadeSpec) togetherWith fadeOut(animationSpec = fadeSpec)
                },
                label = "OnboardingTopBarEyebrow",
            ) { label ->
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.editorialTypography.eyebrow,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        FolioIndicator(activePage = activePage)
    }
}

@Composable
private fun FolioIndicator(activePage: Int) {
    val dashColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.outlineVariant

    val folioText = buildAnnotatedString {
        PAGE_NUMBERS.forEachIndexed { index, page ->
            if (index > 0) {
                withStyle(style = SpanStyle(color = dashColor)) {
                    append(" — ")
                }
            }

            withStyle(style = SpanStyle(color = if (page == activePage) activeColor else inactiveColor)) {
                append("0$page")
            }
        }
    }

    Text(
        text = folioText,
        style = MaterialTheme.editorialTypography.eyebrow.copy(
            letterSpacing = 2.sp,
            fontFeatureSettings = "tnum",
        ),
    )
}

@Composable
private fun FirstIntroScreen(
    onBeginClick: () -> Unit,
    onSkipClick: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // The single decorative flourish for this page: a low-alpha primary quote glyph bleeding off
        // the top-left edge. Static (not `quoteGlyphSway()` — that sway is scoped to empty-state
        // glyphs), so it is marked decorative and dropped from the semantics tree.
        Text(
            text = "“",
            style = MaterialTheme.editorialTypography.quoteGlyph.copy(fontSize = 340.sp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
            modifier = Modifier
                .offset(x = (-18).dp, y = 96.dp)
                .clearAndSetSemantics {},
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 30.dp),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "Book\nsmart.",
                    style = MaterialTheme.editorialTypography.display.copy(
                        fontSize = 76.sp,
                        lineHeight = 72.sp,
                        letterSpacing = (-1.8).sp,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(26.dp))

                Text(
                    text = "Track every book you read, share the ones worth sharing — or " +
                        "don't — and find your next life-changing read.",
                    style = MaterialTheme.editorialTypography.bodyLarge.copy(
                        fontSize = 18.sp,
                        lineHeight = 28.sp,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.widthIn(max = 300.dp),
                )
            }

            PrimaryActionFooterRow(
                buttonLabel = "Begin",
                onButtonClick = onBeginClick,
                showTrailingArrow = true,
                trailingLabel = "Skip",
                onTrailingClick = onSkipClick,
                modifier = Modifier.padding(bottom = 16.dp),
            )
        }
    }
}

@Composable
private fun SecondIntroScreen(
    onContinueClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 30.dp),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Powered by Hardcover.",
                style = MaterialTheme.editorialTypography.display.copy(
                    fontSize = 54.sp,
                    lineHeight = 54.sp,
                    letterSpacing = (-1.2).sp,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(34.dp))

            WavyConnector()

            Spacer(modifier = Modifier.height(34.dp))

            Text(
                text = "To sync your reading, Softcover connects to your Hardcover account — " +
                    "the open book database your library actually lives in.",
                style = MaterialTheme.editorialTypography.bodyLarge.copy(
                    fontSize = 18.sp,
                    lineHeight = 28.sp,
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.widthIn(max = 308.dp),
            )
        }

        PrimaryActionFooterRow(
            buttonLabel = "Continue",
            onButtonClick = onContinueClick,
            showTrailingArrow = true,
            trailingLabel = "Back",
            onTrailingClick = onBackClick,
            modifier = Modifier.padding(bottom = 16.dp),
        )
    }
}

/**
 * The mandated sine motif that literally links the two wordmarks: a static `outlineVariant` track with
 * a `primary` wave drawn over it. No reusable static wavy-line primitive exists in the foundation or
 * this app — the only wavy component is Material3's `LinearWavyProgressIndicator`, which is tied to
 * animated progress state (used unchanged in `SoftcoverLoadingSheet`) and not a fit for a static
 * decorative connector — so this is a small bespoke `Canvas`, scoped to this single screen.
 */
@Composable
private fun WavyConnector() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "SOFTCOVER",
            style = MaterialTheme.editorialTypography.eyebrowSmall.copy(
                letterSpacing = 1.8.sp,
                lineHeight = 14.sp,
            ),
            color = MaterialTheme.colorScheme.onSurface,
        )

        WavySineLine(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 10.dp)
                .height(14.dp),
        )

        Text(
            text = "HARDCOVER",
            style = MaterialTheme.editorialTypography.eyebrowSmall.copy(
                letterSpacing = 1.8.sp,
                lineHeight = 14.sp,
            ),
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun WavySineLine(modifier: Modifier = Modifier) {
    val waveColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.outlineVariant

    Canvas(modifier = modifier.fillMaxWidth()) {
        val trackY = size.height / 2

        drawLine(
            color = trackColor,
            start = Offset(
                0f,
                trackY,
            ),
            end = Offset(
                size.width,
                trackY,
            ),
            strokeWidth = 1.5.dp.toPx(),
        )

        val amplitude = size.height * 0.35f
        val waveCount = 3
        val segmentWidth = size.width / (waveCount * 2)

        val wavePath = Path().apply {
            moveTo(
                0f,
                trackY,
            )

            for (segment in 0 until waveCount * 2) {
                val nextX = (segment + 1) * segmentWidth
                val controlX = nextX - segmentWidth / 2
                val direction = if (segment % 2 == 0) -1f else 1f

                quadraticTo(
                    x1 = controlX,
                    y1 = trackY + direction * amplitude,
                    x2 = nextX,
                    y2 = trackY,
                )
            }
        }

        drawPath(
            path = wavePath,
            color = waveColor,
            style = Stroke(
                width = 1.5.dp.toPx(),
                cap = StrokeCap.Round,
            ),
        )
    }
}

@Composable
private fun ThirdIntroScreen(
    state: OnboardingUiState,
    runAction: (action: OnboardingAction) -> Unit,
    openUrl: (String) -> Unit,
    getCopiedText: () -> String,
    onBackClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 30.dp),
    ) {
        // Scrollable content only — the commit footer is a pinned sibling below, outside this scroll
        // region, so "Save API key" is always visible rather than scrolling off-screen with the
        // explainer (the bug this page previously had, unlike pages 1–2's already-pinned footers).
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Add your\nkey.",
                style = MaterialTheme.editorialTypography.display.copy(
                    fontSize = 46.sp,
                    lineHeight = 46.sp,
                    letterSpacing = (-1).sp,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "One key, once. It only syncs your reading progress — never your password.",
                style = MaterialTheme.editorialTypography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.widthIn(max = 300.dp),
            )

            Spacer(modifier = Modifier.height(30.dp))

            ApiKeyEntryContent(
                state = state,
                runAction = runAction,
                openUrl = openUrl,
                getCopiedText = getCopiedText,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        ApiKeyEntryFooter(
            state = state,
            runAction = runAction,
            footerTrailingLabel = "Back",
            onFooterTrailingLabelClick = onBackClick,
            modifier = Modifier.padding(bottom = 16.dp),
        )
    }
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
