package nl.rhaydus.softcover.feature.profile.presentation.screen.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.theme.Spacing
import nl.rhaydus.softcover.core.component.bookcard.BookCard
import nl.rhaydus.softcover.core.component.bookcard.BookCardEvent
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.chart.ColumnChart
import nl.rhaydus.softcover.core.component.chip.ChipFlow
import nl.rhaydus.softcover.core.component.statistic.CountRows
import nl.rhaydus.softcover.core.component.statistic.StatNumber
import nl.rhaydus.softcover.core.component.statistic.StatTile
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.feature.profile.presentation.state.AtlasChapter
import nl.rhaydus.softcover.feature.profile.presentation.state.DidNotFinishUiModel
import nl.rhaydus.softcover.feature.profile.presentation.state.VersusCellUiModel
import nl.rhaydus.softcover.feature.profile.presentation.state.WaitingLongestUiModel
import nl.rhaydus.softcover.feature.profile.presentation.state.YourReadingListUiModel

/**
 * What is still to come off the reader's list, the two books waiting longest, then the books they set
 * aside. The chapter always shows its headers; the set-aside section eases in and out as
 * [YourReadingListUiModel.didNotFinish] comes and goes.
 */
@Composable
internal fun YourReadingListChapter(
    model: YourReadingListUiModel,
    waiting: WaitingLongestUiModel,
    onWaitingEvent: (BookCardEvent) -> Unit,
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

            Spacer(modifier = Modifier.height(Spacing.md))

            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                VersusCell(
                    model = model.added,
                    modifier = Modifier.weight(1f),
                )

                VersusCell(
                    model = model.read,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            StatTile(model = model.timeOnList)

            Spacer(modifier = Modifier.height(Spacing.xl))

            WaitingLongestBlock(
                model = waiting,
                onEvent = onWaitingEvent,
            )

            Spacer(modifier = Modifier.height(Spacing.section))

            ScopedSectionHeader(header = model.setAside)

            ScopeCrossfade(
                target = model.didNotFinish,
                contentKey = { it == null },
            ) { didNotFinish ->
                if (didNotFinish != null) {
                    Column {
                        Spacer(modifier = Modifier.height(Spacing.xl))

                        DidNotFinishSection(
                            model = didNotFinish,
                            isLoading = model.setAside.isLoading,
                        )
                    }
                }
            }
        }
    }
}

/** One cell of the Added / Read pair under a 2dp rule, `primary` on the lead cell. */
@Composable
internal fun VersusCell(
    model: VersusCellUiModel,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        HorizontalDivider(
            thickness = 2.dp,
            color = if (model.isLead) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant
            },
        )

        Spacer(modifier = Modifier.height(Spacing.sm))

        SoftcoverText(
            text = model.eyebrow.uppercase(),
            style = MaterialTheme.editorialTypography.eyebrowSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        StatNumber(model = model.stat)

        SoftcoverText(
            text = model.caption,
            style = MaterialTheme.editorialTypography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** The waiting rows crossfade by book, and stay as stand-ins while loading. */
@Composable
private fun WaitingLongestBlock(
    model: WaitingLongestUiModel,
    onEvent: (BookCardEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        AtlasSubhead(
            title = model.title,
            caption = model.caption,
            isLoading = model.isLoading,
        )

        ScopeCrossfade(
            target = model.cards.cards,
            contentKey = { cards -> cards.map { (it.key as? BookCardKey.Book)?.bookId } },
        ) { cards ->
            Column {
                cards.forEach { card ->
                    BookCard(
                        model = card,
                        onEvent = onEvent,
                    ) {
                        TrailingChevron(tint = MaterialTheme.colorScheme.onSurfaceVariant)
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

    if (model.stopPoint != null) Spacer(modifier = Modifier.height(Spacing.xl))

    AtlasSubhead(
        title = model.whyTitle,
        caption = "",
        isLoading = isLoading,
    )

    Spacer(modifier = Modifier.height(Spacing.md))

    ScopeCrossfade(
        target = model,
        contentKey = { scoped -> scoped.why?.rows?.map { it.label } ?: scoped.whyEmptyLine },
    ) { scoped ->
        val why = scoped.why
        val emptyLine = scoped.whyEmptyLine

        when {
            why != null -> CountRows(model = why)

            emptyLine != null -> SoftcoverText(
                text = emptyLine,
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    model.genres?.let { genres ->
        Spacer(modifier = Modifier.height(Spacing.xl))

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
