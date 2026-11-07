package nl.rhaydus.softcover.feature.profile.presentation.screen.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.designsystem.theme.Spacing
import nl.rhaydus.softcover.core.component.chart.RankedBars
import nl.rhaydus.softcover.core.component.chart.RankedBarsEvent
import nl.rhaydus.softcover.core.component.quote.QuoteCard
import nl.rhaydus.softcover.core.component.quote.QuoteCardEvent
import nl.rhaydus.softcover.core.component.statistic.StatTile
import nl.rhaydus.softcover.feature.profile.presentation.state.AtlasChapter
import nl.rhaydus.softcover.feature.profile.presentation.state.InYourWordsUiModel

/**
 * Reviews and quotes in scope, then the most-quoted book and the reader's own tags. The chapter, with the gap above it,
 * folds away its height when [InYourWordsUiModel.hasContent] is false.
 */
@Composable
internal fun InYourWordsChapter(
    model: InYourWordsUiModel,
    jumper: AtlasChapterJumper,
    onMostQuotedTapped: () -> Unit,
    onOwnTagTapped: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.animateScopeContentSize()) {
        if (model.hasContent) {
            Spacer(modifier = Modifier.height(Spacing.section))

            Column(
                modifier = Modifier.atlasChapter(
                    jumper,
                    AtlasChapter.IN_YOUR_WORDS,
                ),
            ) {
                InYourWordsContent(
                    model = model,
                    onMostQuotedTapped = onMostQuotedTapped,
                    onOwnTagTapped = onOwnTagTapped,
                )
            }
        }
    }
}

@Composable
private fun InYourWordsContent(
    model: InYourWordsUiModel,
    onMostQuotedTapped: () -> Unit,
    onOwnTagTapped: (String) -> Unit,
) {
    ScopedSectionHeader(header = model.header)

    Spacer(modifier = Modifier.height(Spacing.md))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        model.tiles.forEach { tile ->
            StatTile(
                model = tile,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
        }
    }

    val mostQuoted = model.mostQuoted

    if (mostQuoted != null) {
        Spacer(modifier = Modifier.height(Spacing.xl))

        AtlasSubhead(
            title = mostQuoted.title,
            caption = mostQuoted.caption,
            isLoading = model.isLoading,
        )

        Spacer(modifier = Modifier.height(Spacing.md))

        QuoteCard(
            model = mostQuoted.card,
            onEvent = { event ->
                if (event is QuoteCardEvent.Clicked) onMostQuotedTapped()
            },
        )
    }

    val ownTags = model.ownTags

    if (ownTags != null) {
        Spacer(modifier = Modifier.height(Spacing.xl))

        AtlasSubhead(
            title = ownTags.title,
            caption = ownTags.caption,
            isLoading = model.isLoading,
        )

        Spacer(modifier = Modifier.height(Spacing.md))

        RankedBars(
            model = ownTags.ranking,
            onEvent = { event ->
                when (event) {
                    is RankedBarsEvent.RowClicked -> onOwnTagTapped(event.key)
                }
            },
        )
    }
}
