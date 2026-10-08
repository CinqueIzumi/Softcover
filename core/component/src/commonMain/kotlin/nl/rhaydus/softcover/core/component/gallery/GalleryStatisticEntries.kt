package nl.rhaydus.softcover.core.component.gallery

import androidx.compose.material3.MaterialTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.chart.ColumnChart
import nl.rhaydus.softcover.core.component.chart.ColumnChartUiModel
import nl.rhaydus.softcover.core.component.chart.ProportionChart
import nl.rhaydus.softcover.core.component.chart.ProportionChartUiModel
import nl.rhaydus.softcover.core.component.chart.RankedBars
import nl.rhaydus.softcover.core.component.chart.RankedBarsUiModel
import nl.rhaydus.softcover.core.component.progress.ProgressBar
import nl.rhaydus.softcover.core.component.statistic.StatNumber
import nl.rhaydus.softcover.core.component.statistic.StatNumberUiModel
import nl.rhaydus.softcover.core.component.statistic.StatTile
import nl.rhaydus.softcover.core.component.statistic.StatTileUiModel

internal val statisticEntries: ImmutableList<GalleryEntry> = persistentListOf(
    galleryEntry(
        name = "RankedBars",
        family = GalleryFamily.STATISTIC,
        blurb = "Horizontal bars, each measured against its own full-width track; while loading " +
            "the rows keep their layout and mask.",
        previews = RankedBarsUiModel,
        content = { model, modifier ->
            RankedBars(
                model = model,
                modifier = modifier,
            )
        },
    ),
    galleryEntry(
        name = "ColumnChart",
        family = GalleryFamily.STATISTIC,
        blurb = "Vertical columns scaled against the tallest, each with an optional label and value; " +
            "while loading the columns keep their layout and mask.",
        previews = ColumnChartUiModel,
        content = { model, modifier ->
            ColumnChart(
                model = model,
                modifier = modifier,
            )
        },
    ),
    galleryEntry(
        name = "ProportionChart",
        family = GalleryFamily.STATISTIC,
        blurb = "A stacked bar of parts of a whole with its legend beneath; an Unknown segment " +
            "renders muted and a zero segment draws nothing.",
        previews = ProportionChartUiModel,
        content = { model, modifier ->
            ProportionChart(
                model = model,
                modifier = modifier,
            )
        },
    ),
    GalleryEntry(
        name = "ProgressBar",
        family = GalleryFamily.PROGRESS,
        blurb = "A determinate wavy bar for reading progress; it animates to each new fraction " +
            "and masks while loading.",
        fixtures = persistentListOf(
            GalleryFixture(
                label = "Partial",
                render = { modifier ->
                    ProgressBar(
                        fraction = 0.42f,
                        modifier = modifier,
                    )
                },
            ),
            GalleryFixture(
                label = "Empty",
                render = { modifier ->
                    ProgressBar(
                        fraction = 0f,
                        modifier = modifier,
                    )
                },
            ),
            GalleryFixture(
                label = "Full",
                render = { modifier ->
                    ProgressBar(
                        fraction = 1f,
                        modifier = modifier,
                    )
                },
            ),
            GalleryFixture(
                label = "Loading",
                render = { modifier ->
                    ProgressBar(
                        fraction = 0f,
                        modifier = modifier,
                        isLoading = true,
                    )
                },
            ),
        ),
    ),
    galleryEntry(
        name = "StatNumber",
        family = GalleryFamily.STATISTIC,
        blurb = "A number bound to live state: it tweens between values, holds tabular figures " +
            "so digits don't jitter, and ticks a hairline on each integer crossing.",
        previews = StatNumberUiModel,
        content = { model, modifier ->
            StatNumber(
                model = model,
                style = MaterialTheme.typography.headlineLarge,
                modifier = modifier,
            )
        },
    ),
    galleryEntry(
        name = "StatTile",
        family = GalleryFamily.STATISTIC,
        blurb = "A labelled statistic on a tile, in a hero, standard or compact surface; while " +
            "loading only the number masks and the tile around it stays real.",
        previews = StatTileUiModel,
        content = { model, modifier ->
            StatTile(
                model = model,
                modifier = modifier,
            )
        },
    ),
)
