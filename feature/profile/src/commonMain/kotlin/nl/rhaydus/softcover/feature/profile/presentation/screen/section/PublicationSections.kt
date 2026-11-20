package nl.rhaydus.softcover.feature.profile.presentation.screen.section

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.designsystem.theme.Spacing
import nl.rhaydus.softcover.core.component.chart.ColumnChart
import nl.rhaydus.softcover.core.component.chart.ProportionChart
import nl.rhaydus.softcover.core.component.text.FadeThroughText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.feature.profile.presentation.state.ColumnSectionUiModel
import nl.rhaydus.softcover.feature.profile.presentation.state.DiversityChapterUiModel
import nl.rhaydus.softcover.feature.profile.presentation.state.ProportionSectionUiModel

@Composable
internal fun PublicationSections(
    model: DiversityChapterUiModel,
    isLoading: Boolean,
) {
    ProportionSection(
        section = model.languages,
        isLoading = isLoading,
    )

    Spacer(modifier = Modifier.height(Spacing.xl))

    ColumnSection(
        section = model.decades,
        isLoading = isLoading,
    )

    Spacer(modifier = Modifier.height(Spacing.xl))

    ProportionSection(
        section = model.acquisition,
        isLoading = isLoading,
    )
}

@Composable
private fun ProportionSection(
    section: ProportionSectionUiModel,
    isLoading: Boolean,
) {
    val isMasked = isLoading || section.chart.isLoading

    PublicationSection(
        title = section.title,
        caption = section.caption,
        emptyCaption = section.emptyCaption,
        isLoading = isMasked,
        isEmpty = section.hasData.not() && isMasked.not(),
    ) {
        ProportionChart(model = section.chart)
    }
}

@Composable
private fun ColumnSection(
    section: ColumnSectionUiModel,
    isLoading: Boolean,
) {
    val isMasked = isLoading || section.chart.isLoading

    PublicationSection(
        title = section.title,
        caption = section.caption,
        emptyCaption = section.emptyCaption,
        isLoading = isMasked,
        isEmpty = section.hasData.not() && isMasked.not(),
    ) {
        ColumnChart(model = section.chart)
    }
}

@Composable
private fun PublicationSection(
    title: String,
    caption: String,
    emptyCaption: String,
    isLoading: Boolean,
    isEmpty: Boolean,
    chart: @Composable () -> Unit,
) {
    AtlasSubhead(
        title = title,
        caption = caption,
        isLoading = isLoading,
    )

    ScopeEmptySwitch(
        isEmpty = isEmpty,
        empty = {
            Column {
                Spacer(modifier = Modifier.height(Spacing.sm))

                FadeThroughText(
                    text = emptyCaption,
                    style = MaterialTheme.editorialTypography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        filled = {
            Column {
                Spacer(modifier = Modifier.height(Spacing.md))

                chart()
            }
        },
    )
}
