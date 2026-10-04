package nl.rhaydus.softcover.core.component.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.callout.Banner
import nl.rhaydus.softcover.core.component.callout.BannerUiModel
import nl.rhaydus.softcover.core.component.callout.Callout
import nl.rhaydus.softcover.core.component.callout.CalloutActionUiModel
import nl.rhaydus.softcover.core.component.callout.CalloutUiModel
import nl.rhaydus.softcover.core.component.celebration.MarkAsReadBurst
import nl.rhaydus.softcover.core.component.celebration.MarkAsReadBurstScale
import nl.rhaydus.softcover.core.component.chart.ChartTone
import nl.rhaydus.softcover.core.component.chart.ColumnChart
import nl.rhaydus.softcover.core.component.chart.ColumnChartUiModel
import nl.rhaydus.softcover.core.component.chart.ProportionChart
import nl.rhaydus.softcover.core.component.chart.ProportionChartUiModel
import nl.rhaydus.softcover.core.component.chart.RankedBars
import nl.rhaydus.softcover.core.component.chart.RankedBarsUiModel
import nl.rhaydus.softcover.core.component.control.ColorPalettePreviewTile
import nl.rhaydus.softcover.core.component.control.ColorPalettePreviewTileUiModel
import nl.rhaydus.softcover.core.component.control.ThemePreviewTile
import nl.rhaydus.softcover.core.component.control.ThemePreviewTileUiModel
import nl.rhaydus.softcover.core.component.divider.Divider
import nl.rhaydus.softcover.core.component.divider.DividerCenter
import nl.rhaydus.softcover.core.component.divider.DividerInk
import nl.rhaydus.softcover.core.component.divider.VerticalDivider
import nl.rhaydus.softcover.core.component.progress.ProgressBar
import nl.rhaydus.softcover.core.component.richtext.ClickableText
import nl.rhaydus.softcover.core.component.richtext.ClickableTextUiModel
import nl.rhaydus.softcover.core.component.state.EmptyState
import nl.rhaydus.softcover.core.component.state.EmptyStateUiModel
import nl.rhaydus.softcover.core.component.statistic.StatNumber
import nl.rhaydus.softcover.core.component.statistic.StatNumberFormat
import nl.rhaydus.softcover.core.component.statistic.StatNumberUiModel
import nl.rhaydus.softcover.core.component.statistic.StatTile
import nl.rhaydus.softcover.core.component.statistic.StatTileUiModel
import nl.rhaydus.softcover.core.component.statistic.StatTileVariant
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.component.topbar.EditorialTopBar
import nl.rhaydus.softcover.core.component.topbar.SearchTopBar
import nl.rhaydus.softcover.core.component.topbar.SearchTopBarUiModel
import nl.rhaydus.softcover.core.component.topbar.TopBar
import nl.rhaydus.softcover.core.component.topbar.TopBarSurface
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel

internal val chromeEntries: ImmutableList<GalleryEntry> = persistentListOf(
    galleryEntry(
        name = "TopBar",
        family = GalleryFamily.TOPBAR,
        blurb = "The page bar: an autosizing centred title, an optional subtitle, an optional " +
            "back affordance, and a slot for the screen's own actions.",
        previews = TopBarUiModel,
        label = ::topBarFixtureLabel,
        content = { model, modifier ->
            TopBar(
                model = model,
                onEvent = {},
                modifier = modifier,
            )
        },
    ),
    galleryEntry(
        name = "EditorialTopBar",
        family = GalleryFamily.TOPBAR,
        blurb = "The desktop page bar: a back button, a start-aligned editorial headline and a " +
            "slot for the screen's own actions.",
        previews = object : UiModelPreviews<TopBarUiModel> {
            override val previews: ImmutableList<TopBarUiModel> = persistentListOf(
                TopBarUiModel(title = "Appearance"),
                TopBarUiModel(),
            )
        },
        label = { model -> if (model.title == null) "Back only" else "Titled" },
        content = { model, modifier ->
            EditorialTopBar(
                model = model,
                onEvent = {},
                modifier = modifier,
            )
        },
    ),
    galleryEntry(
        name = "SearchTopBar",
        family = GalleryFamily.TOPBAR,
        blurb = "The search chrome: a rounded pill holding the query beside a barcode-scan " +
            "button. Kept separate from TopBar (§ 7.6) — its focus contract has no counterpart there.",
        previews = SearchTopBarUiModel,
        label = ::searchTopBarFixtureLabel,
        content = { model, modifier ->
            SearchTopBar(
                model = model,
                onEvent = {},
                modifier = modifier,
            )
        },
    ),
    galleryEntry(
        name = "Banner",
        family = GalleryFamily.CALLOUT,
        blurb = "A full-width notice that slides in above a screen's content — the offline " +
            "report at the app root, and a dismissible pace nudge on a book card.",
        previews = BannerUiModel,
        label = ::bannerFixtureLabel,
        content = { model, modifier ->
            Banner(
                model = model,
                modifier = modifier,
            )
        },
    ),
    galleryEntry(
        name = "Callout",
        family = GalleryFamily.CALLOUT,
        blurb = "A rounded card that calls out one thing worth acting on or knowing — a status, " +
            "a scanned-edition prompt, an app update — with an optional action and dismiss.",
        previews = CalloutUiModel,
        label = ::calloutFixtureLabel,
        content = { model, modifier ->
            Callout(
                model = model,
                modifier = modifier,
            )
        },
    ),
    GalleryEntry(
        name = "Divider",
        family = GalleryFamily.DIVIDER,
        blurb = "A hairline rule; anatomy and ink resolve per call site, from a plain row " +
            "break to a labelled or glyph-broken one, or a vertical pane separator.",
        fixtures = persistentListOf(
            GalleryFixture(
                label = "Plain rule",
                render = { modifier -> Divider(modifier = modifier) },
            ),
            GalleryFixture(
                label = "Content-tint rule",
                render = { modifier ->
                    Divider(
                        modifier = modifier,
                        ink = DividerInk.ContentTint,
                    )
                },
            ),
            GalleryFixture(
                label = "On-image rule",
                render = { modifier ->
                    Box(modifier = Modifier.background(Color.Black)) {
                        Divider(
                            modifier = modifier,
                            ink = DividerInk.OnImage,
                        )
                    }
                },
            ),
            GalleryFixture(
                label = "Quote mark centre",
                render = { modifier ->
                    Divider(
                        modifier = modifier,
                        center = DividerCenter.QuoteMark,
                    )
                },
            ),
            GalleryFixture(
                label = "Labelled centre",
                render = { modifier ->
                    Divider(
                        modifier = modifier,
                        center = DividerCenter.Label(text = "Or type it"),
                    )
                },
            ),
            GalleryFixture(
                label = "Vertical rule",
                render = { modifier -> VerticalDivider(modifier = modifier.height(48.dp)) },
            ),
        ),
    ),
    galleryEntry(
        name = "EmptyState",
        family = GalleryFamily.STATE,
        blurb = "The message a screen shows in place of content it has nothing to draw: a " +
            "centred flourish with an optional body and button, or an inline title and body.",
        previews = EmptyStateUiModel,
        label = { model -> model.title },
        content = { model, modifier ->
            EmptyState(
                model = model,
                modifier = modifier,
            )
        },
    ),
    GalleryEntry(
        name = "SoftcoverText",
        family = GalleryFamily.RICHTEXT,
        blurb = "The app's text leaf: one style channel, and it masks a stand-in in the real " +
            "style while loading or while the text is null, so the slot never jumps.",
        fixtures = persistentListOf(
            GalleryFixture(
                label = "Plain",
                render = { modifier ->
                    SoftcoverText(
                        text = "The Left Hand of Darkness",
                        standIn = "A book title stand-in",
                        modifier = modifier,
                    )
                },
            ),
            GalleryFixture(
                label = "Loading",
                render = { modifier ->
                    SoftcoverText(
                        text = "The Left Hand of Darkness",
                        isLoading = true,
                        standIn = "A book title stand-in",
                        modifier = modifier,
                    )
                },
            ),
            GalleryFixture(
                label = "Text null, not loading",
                render = { modifier ->
                    SoftcoverText(
                        text = null as String?,
                        standIn = "A book title stand-in",
                        modifier = modifier,
                    )
                },
            ),
            GalleryFixture(
                label = "Two lines, loading",
                render = { modifier ->
                    SoftcoverText(
                        text = null as String?,
                        isLoading = true,
                        standIn = "A two line stand-in\nfor a longer description",
                        maxLines = 2,
                        modifier = modifier.width(140.dp),
                    )
                },
            ),
            GalleryFixture(
                label = "Mixed spans",
                render = { modifier ->
                    SoftcoverText(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Bold ") }
                            withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append("italic ") }
                            withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                                append("underline")
                            }
                        },
                        modifier = modifier,
                    )
                },
            ),
        ),
    ),
    galleryEntry(
        name = "RankedBars",
        family = GalleryFamily.STATISTIC,
        blurb = "Horizontal bars, each measured against its own full-width track; while loading " +
            "the rows keep their layout and mask.",
        previews = RankedBarsUiModel,
        label = { model -> if (model.isLoading) "Loading" else "${model.rows.size} rows" },
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
        label = { model -> if (model.isLoading) "Loading" else "${model.columns.size} columns" },
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
        label = ::proportionChartFixtureLabel,
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
        label = ::statNumberFixtureLabel,
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
        label = ::statTileFixtureLabel,
        content = { model, modifier ->
            StatTile(
                model = model,
                modifier = modifier,
            )
        },
    ),
    galleryEntry(
        name = "ThemePreviewTile",
        family = GalleryFamily.CONTROL,
        blurb = "One choice in the theme picker: the app's own page in miniature, painted in " +
            "the scheme that choice would actually give.",
        previews = ThemePreviewTileUiModel,
        label = { model -> model.label },
        content = { model, modifier ->
            ThemePreviewTile(
                model = model,
                onEvent = {},
                modifier = modifier.width(120.dp),
            )
        },
    ),
    galleryEntry(
        name = "ColorPalettePreviewTile",
        family = GalleryFamily.CONTROL,
        blurb = "One choice in the spine-colour picker: the same miniature, painted in that " +
            "palette entire — its paper as well as its ink.",
        previews = ColorPalettePreviewTileUiModel,
        label = { model -> model.palette.label },
        content = { model, modifier ->
            ColorPalettePreviewTile(
                model = model,
                onEvent = {},
                modifier = modifier.width(120.dp),
            )
        },
    ),
    galleryEntry(
        name = "ClickableText",
        family = GalleryFamily.RICHTEXT,
        blurb = "Prose with tappable runs in it — the component resolves the link annotations " +
            "so a screen never wires its own tap detection.",
        previews = ClickableTextUiModel,
        label = { "With an inline link" },
        content = { model, modifier ->
            ClickableText(
                model = model,
                onEvent = {},
                style = MaterialTheme.typography.bodyMedium,
                modifier = modifier,
            )
        },
    ),
    GalleryEntry(
        name = "MarkAsReadBurst",
        family = GalleryFamily.CELEBRATION,
        blurb = "The radial particle burst played on a mark-as-read commit. A still fixture " +
            "cannot show it — the motion debug screen is where you watch one.",
        fixtures = persistentListOf(
            GalleryFixture(
                label = "Card",
                render = { modifier ->
                    MarkAsReadBurst(
                        triggerKey = 1,
                        modifier = modifier.size(160.dp),
                    )
                },
            ),
            GalleryFixture(
                label = "Screen",
                render = { modifier ->
                    MarkAsReadBurst(
                        triggerKey = 1,
                        modifier = modifier.size(160.dp),
                        scale = MarkAsReadBurstScale.Screen,
                    )
                },
            ),
        ),
    ),
)

/** Names what a [TopBarUiModel] fixture demonstrates, derived from its anatomy branch. */
private fun topBarFixtureLabel(model: TopBarUiModel): String = when {
    model.surface == TopBarSurface.OVER_MEDIA -> "Over cover art"
    (model.title?.length ?: 0) > TOP_BAR_LONG_TITLE_FLOOR -> "Long title, autosized"
    else -> "With back"
}

private const val TOP_BAR_LONG_TITLE_FLOOR = 30

/** Names what a [SearchTopBarUiModel] fixture demonstrates. */
private fun searchTopBarFixtureLabel(model: SearchTopBarUiModel): String = when {
    model.isLoading -> "Searching"
    model.active && model.focused.not() -> "Results, unfocused"
    model.active -> "Focused, empty"
    else -> "Resting"
}

/** Names what a [BannerUiModel] fixture demonstrates — its variant is its anatomy. */
private fun bannerFixtureLabel(model: BannerUiModel): String =
    model.variant.name

private fun calloutFixtureLabel(model: CalloutUiModel): String = when (model.callToAction) {
    null -> "Status"
    is CalloutActionUiModel.Button -> if (model.dismissible) "Edition prompt" else "Update available"
    is CalloutActionUiModel.Busy -> "Edition updating"
    CalloutActionUiModel.Progress -> "Update downloading"
}

private fun statNumberFixtureLabel(model: StatNumberUiModel): String {
    if (model.isLoading) return "Loading"

    return when (model.format) {
        StatNumberFormat.Grouped -> "Grouped thousands"
        StatNumberFormat.Plain -> "Plain integer"
        is StatNumberFormat.Decimal -> "One decimal"
    }
}

private fun statTileFixtureLabel(model: StatTileUiModel): String {
    val variant = when (model.variant) {
        is StatTileVariant.Hero -> "Hero"
        is StatTileVariant.Standard -> "Standard"
        is StatTileVariant.Compact -> "Compact"
    }

    return if (model.stat.isLoading) "$variant, loading" else variant
}

private fun proportionChartFixtureLabel(model: ProportionChartUiModel): String = when {
    model.isLoading -> "Loading"
    model.segments.any { it.fraction == 0f } -> "Zero segment"
    model.segments.any { it.tone == ChartTone.Unknown } -> "With Unknown"
    else -> "Known segments"
}
