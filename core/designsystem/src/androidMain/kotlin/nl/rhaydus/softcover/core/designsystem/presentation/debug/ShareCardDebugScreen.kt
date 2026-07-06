package nl.rhaydus.softcover.core.designsystem.presentation.debug

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import nl.rhaydus.common.AppLog
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.editorial.component.EditorialSectionHeader
import nl.rhaydus.designsystem.model.ButtonSize
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.designsystem.share.CapturableShareCard
import nl.rhaydus.designsystem.share.SaveOutcome
import nl.rhaydus.designsystem.share.ShareCardCapture
import nl.rhaydus.designsystem.share.rememberGalleryWritePermissionRequester
import nl.rhaydus.designsystem.share.rememberShareCardCapture
import nl.rhaydus.softcover.core.designsystem.presentation.component.SoftcoverTopBar
import nl.rhaydus.softcover.core.designsystem.presentation.share.BookShareContent
import nl.rhaydus.softcover.core.designsystem.presentation.share.QuoteShareContent
import nl.rhaydus.softcover.core.designsystem.presentation.share.ShareCard
import nl.rhaydus.softcover.core.designsystem.presentation.share.ShareContent
import nl.rhaydus.softcover.core.designsystem.presentation.share.StatShareContent
import nl.rhaydus.softcover.core.designsystem.presentation.share.YearRecapShareContent
import nl.rhaydus.softcover.core.designsystem.presentation.share.softcoverShareCardCaptureConfig
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

object ShareCardDebugScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        Screen(onNavigateBack = navigator::pop)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Screen(onNavigateBack: () -> Unit) {
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
                try {
                    when (val outcome = capture.saveToGallery(filenameHint)) {
                        is SaveOutcome.Saved -> {
                            snackbarHostState.showSnackbar("Saved to gallery → ${outcome.displayPath}")
                        }

                        is SaveOutcome.Failure -> {
                            snackbarHostState.showSnackbar("Save failed: ${outcome.reason}")
                        }

                        is SaveOutcome.Cached -> Unit
                    }
                } catch (throwable: Throwable) {
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
                SoftcoverTopBar(
                    title = "Share previews",
                    onNavigateBack = onNavigateBack,
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(
                        horizontal = 24.dp,
                        vertical = 16.dp,
                    ),
            ) {
                EditorialSectionHeader(
                    eyebrow = "Debug",
                    headline = "Share card variants.",
                    description = "Tap a card's “Save to gallery” button to write the rendered PNG to Pictures/Softcover. On Android 9 and below the system will ask for storage permission first.",
                )

                Spacer(modifier = Modifier.height(32.dp))

                VariantBlock(
                    label = "Book",
                    capture = bookCapture,
                    content = SampleBook,
                    onSaveClick = { onSaveClick(
                        bookCapture,
                        "book",
                    ) },
                )

                Spacer(modifier = Modifier.height(40.dp))

                VariantBlock(
                    label = "Stat",
                    capture = statCapture,
                    content = SampleStat,
                    onSaveClick = { onSaveClick(
                        statCapture,
                        "stat",
                    ) },
                )

                Spacer(modifier = Modifier.height(40.dp))

                VariantBlock(
                    label = "Quote",
                    capture = quoteCapture,
                    content = SampleQuote,
                    onSaveClick = { onSaveClick(
                        quoteCapture,
                        "quote",
                    ) },
                )

                Spacer(modifier = Modifier.height(40.dp))

                VariantBlock(
                    label = "Year recap",
                    capture = recapCapture,
                    content = SampleRecap,
                    onSaveClick = { onSaveClick(
                        recapCapture,
                        "year-recap",
                    ) },
                )

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }

    @Composable
    private fun VariantBlock(
        label: String,
        capture: ShareCardCapture,
        content: ShareContent,
        onSaveClick: () -> Unit,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.editorialTypography.eyebrow,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.Start),
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                CapturableShareCard(capture = capture) {
                    ShareCard(content = content)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            RhaydusButton(
                label = "Save to gallery",
                style = ButtonStyle.TONAL,
                size = ButtonSize.S,
                onClick = onSaveClick,
            )
        }
    }

    private val SampleBook = BookShareContent(
        coverUrl = null,
        title = "Lonesome Dove",
        author = "Larry McMurtry",
        communityRating = 4.6,
        userRating = 9,
        releaseYear = 1985,
        pageCount = 945,
        description = "A sprawling cattle drive from the dust of Texas to the high grass of Montana, told as one of the great American friendships and one of the longest goodbyes in the genre.",
        quote = "It's a fine world, though rich in hardships at times.",
    )

    private val SampleStat = StatShareContent(
        eyebrow = "Pages read in 2026",
        value = 8_402L,
        caption = "across 14 books, mostly on Sunday afternoons.",
    )

    private val SampleQuote = QuoteShareContent(
        quote = "The reader is the protagonist; the page is the stage.",
        sourceTitle = "Piranesi",
        sourceAuthor = "Susanna Clarke",
        page = 142,
    )

    private val SampleRecap = YearRecapShareContent(
        year = 2026,
        eyebrow = "Your year in books",
        headline = "14 books, 8,402 pages.",
        highlights = listOf(
            "Longest haul — The Power Broker, 1,296 pp.",
            "Most-read author — Larry McMurtry.",
            "Highest rating — Piranesi, 10/10.",
        ),
    )
}
