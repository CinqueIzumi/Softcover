package nl.rhaydus.softcover.debug

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch
import nl.rhaydus.common.AppLog
import nl.rhaydus.common.runCatchingCancellable
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.model.ButtonSize
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.designsystem.share.CapturableShareCard
import nl.rhaydus.designsystem.share.SaveOutcome
import nl.rhaydus.designsystem.share.ShareCardCapture
import nl.rhaydus.designsystem.share.rememberGalleryWritePermissionRequester
import nl.rhaydus.designsystem.share.rememberShareCardCapture
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.share.BookShareCardUiModel
import nl.rhaydus.softcover.core.component.share.QuoteShareCardUiModel
import nl.rhaydus.softcover.core.component.share.ShareCard
import nl.rhaydus.softcover.core.component.share.ShareCardUiModel
import nl.rhaydus.softcover.core.component.share.StatShareCardUiModel
import nl.rhaydus.softcover.core.component.share.YearRecapShareCardUiModel
import nl.rhaydus.softcover.core.component.share.softcoverShareCardCaptureConfig
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.component.topbar.TopBar
import nl.rhaydus.softcover.core.component.topbar.TopBarEvent
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

/** Fixed for this screen, so the model is a constant rather than rebuilt per frame. */
private val SHARE_DEBUG_TOP_BAR = TopBarUiModel(
    title = "Share previews",
)

object ShareCardDebugScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val screenModel = koinScreenModel<ShareCardDebugScreenModel>()

        val state by screenModel.state.collectAsStateWithLifecycle()

        Screen(
            state = state,
            onNavigateBack = navigator::pop,
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Screen(
        state: ShareCardDebugUiState,
        onNavigateBack: () -> Unit,
    ) {
        val snackbarHostState = remember { SnackbarHostState() }

        val scope = rememberCoroutineScope()

        val bookCapture = rememberShareCardCapture(config = softcoverShareCardCaptureConfig)
        val statCapture = rememberShareCardCapture(config = softcoverShareCardCaptureConfig)
        val quoteCapture = rememberShareCardCapture(config = softcoverShareCardCaptureConfig)
        val recapCapture = rememberShareCardCapture(config = softcoverShareCardCaptureConfig)

        var pendingSave by remember { mutableStateOf<(suspend () -> Unit)?>(null) }

        val permissionRequester = rememberGalleryWritePermissionRequester { granted ->
            val captured = pendingSave

            pendingSave = null

            if (granted && captured != null) {
                scope.launch { captured() }
            } else if (granted.not()) {
                scope.launch {
                    snackbarHostState.showSnackbar("Storage permission denied — can't write to gallery.")
                }
            }
        }

        val onSaveClick: (ShareCardCapture, String) -> Unit = { capture, filenameHint ->
            pendingSave = {
                runCatchingCancellable {
                    when (val outcome = capture.saveToGallery(filenameHint)) {
                        is SaveOutcome.Saved -> {
                            snackbarHostState.showSnackbar("Saved to gallery → ${outcome.displayPath}")
                        }

                        is SaveOutcome.Failure -> {
                            snackbarHostState.showSnackbar("Save failed: ${outcome.reason}")
                        }

                        is SaveOutcome.Cached -> Unit
                    }
                }.onFailure { throwable ->
                    AppLog.e(
                        throwable,
                        "Failed to save share card $filenameHint to gallery",
                    )

                    snackbarHostState.showSnackbar("Save failed: ${throwable.message}")
                }
            }

            permissionRequester.request()
        }

        Scaffold(
            topBar = {
                TopBar(
                    model = SHARE_DEBUG_TOP_BAR,
                    onEvent = { event ->
                        when (event) {
                            TopBarEvent.BackClicked -> onNavigateBack()
                        }
                    },
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(
                        horizontal = Spacing.gutter,
                        vertical = Spacing.md,
                    ),
            ) {
                SectionHeader(model = state.pageHeader)

                Spacer(modifier = Modifier.height(Spacing.xxxl))

                VariantBlock(
                    label = "Book",
                    capture = bookCapture,
                    content = SampleBook,
                    onSaveClick = {
                        onSaveClick(
                            bookCapture,
                            "book",
                        )
                    },
                )

                Spacer(modifier = Modifier.height(Spacing.section))

                VariantBlock(
                    label = "Stat",
                    capture = statCapture,
                    content = SampleStat,
                    onSaveClick = {
                        onSaveClick(
                            statCapture,
                            "stat",
                        )
                    },
                )

                Spacer(modifier = Modifier.height(Spacing.section))

                VariantBlock(
                    label = "Quote",
                    capture = quoteCapture,
                    content = SampleQuote,
                    onSaveClick = {
                        onSaveClick(
                            quoteCapture,
                            "quote",
                        )
                    },
                )

                Spacer(modifier = Modifier.height(Spacing.section))

                VariantBlock(
                    label = "Year recap",
                    capture = recapCapture,
                    content = SampleRecap,
                    onSaveClick = {
                        onSaveClick(
                            recapCapture,
                            "year-recap",
                        )
                    },
                )

                Spacer(modifier = Modifier.height(Spacing.x5l))
            }
        }
    }

    @Composable
    private fun VariantBlock(
        label: String,
        capture: ShareCardCapture,
        content: ShareCardUiModel,
        onSaveClick: () -> Unit,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SoftcoverText(
                text = label.uppercase(),
                style = MaterialTheme.editorialTypography.eyebrow,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.Start),
            )

            Spacer(modifier = Modifier.height(Spacing.sm))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                CapturableShareCard(capture = capture) {
                    ShareCard(model = content)
                }
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            RhaydusButton(
                label = "Save to gallery",
                style = ButtonStyle.TONAL,
                size = ButtonSize.S,
                onClick = onSaveClick,
            )
        }
    }

    private val SampleBook = BookShareCardUiModel(
        cover = CoverUiModel(
            source = null,
            coverlessTitle = "Lonesome Dove",
            variant = CoverVariant.Floating,
        ),
        title = "Lonesome Dove",
        author = "Larry McMurtry",
        statsLine = "★ 4.6 · Your 9/10 · 1985 · 945 pages",
        description = "A sprawling cattle drive from the dust of Texas to the high grass of Montana, " +
            "told as one of the great American friendships and one of the longest goodbyes in the genre.",
    )

    private val SampleStat = StatShareCardUiModel(
        eyebrow = "Pages read in 2026",
        value = 8_402L,
        caption = "across 14 books, mostly on Sunday afternoons.",
    )

    private val SampleQuote = QuoteShareCardUiModel(
        quote = "The reader is the protagonist; the page is the stage.",
        sourceTitle = "Piranesi",
        sourceAuthor = "Susanna Clarke",
        page = 142,
    )

    private val SampleRecap = YearRecapShareCardUiModel(
        year = 2026,
        eyebrow = "Your year in books",
        headline = "14 books, 8,402 pages.",
        highlights = persistentListOf(
            "Longest haul — The Power Broker, 1,296 pp.",
            "Most-read author — Larry McMurtry.",
            "Highest rating — Piranesi, 10/10.",
        ),
    )
}
