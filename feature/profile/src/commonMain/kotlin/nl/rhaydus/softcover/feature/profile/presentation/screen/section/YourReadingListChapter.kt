package nl.rhaydus.softcover.feature.profile.presentation.screen.section

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.designsystem.theme.Spacing
import nl.rhaydus.softcover.core.component.chart.ColumnChart
import nl.rhaydus.softcover.core.component.chip.ChipFlow
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.feature.profile.presentation.state.AtlasChapter
import nl.rhaydus.softcover.feature.profile.presentation.state.DidNotFinishUiModel
import nl.rhaydus.softcover.feature.profile.presentation.state.YourReadingListUiModel

/**
 * The books the reader put down. The chapter always shows its header; the section under it eases in and
 * out as [YourReadingListUiModel.didNotFinish] comes and goes.
 */
@Composable
internal fun YourReadingListChapter(
    model: YourReadingListUiModel,
    jumper: AtlasChapterJumper,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.animateScopeContentSize()) {
        Spacer(modifier = Modifier.height(Spacing.section))

        Column(
            modifier = Modifier.atlasChapter(
                jumper,
                AtlasChapter.YOUR_READING_LIST,
            ),
        ) {
            ScopedSectionHeader(header = model.header)

            ScopeCrossfade(
                target = model.didNotFinish,
                contentKey = { it == null },
            ) { didNotFinish ->
                if (didNotFinish != null) {
                    Column {
                        Spacer(modifier = Modifier.height(Spacing.xl))

                        DidNotFinishSection(
                            model = didNotFinish,
                            isLoading = model.isLoading,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DidNotFinishSection(
    model: DidNotFinishUiModel,
    isLoading: Boolean,
) {
    model.stopPoint?.let { stopPoint ->
        AtlasSubhead(
            title = model.stopPointTitle,
            caption = "",
            isLoading = isLoading,
        )

        Spacer(modifier = Modifier.height(Spacing.md))

        ColumnChart(model = stopPoint)

        model.unknownStopCaption?.let { caption ->
            Spacer(modifier = Modifier.height(Spacing.sm))

            SoftcoverText(
                text = caption,
                style = MaterialTheme.editorialTypography.body,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    model.genres?.let { genres ->
        if (model.stopPoint != null) Spacer(modifier = Modifier.height(Spacing.xl))

        AtlasSubhead(
            title = model.genresTitle,
            caption = "",
            isLoading = isLoading,
        )

        Spacer(modifier = Modifier.height(Spacing.md))

        ChipFlow(
            set = genres,
            onEvent = { _, _ -> },
        )

        Spacer(modifier = Modifier.height(Spacing.sm))

        SoftcoverText(
            text = model.genresNote,
            style = MaterialTheme.editorialTypography.body,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
