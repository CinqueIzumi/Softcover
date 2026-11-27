package nl.rhaydus.softcover.feature.profile.presentation.state

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel

@Immutable
internal data class DiversityChapterUiModel(
    val header: SectionHeaderUiModel.Section,
    val genderBreakdown: DemographicBreakdownUiModel,
    val authorBipoc: DemographicBreakdownUiModel,
    val authorLgbtq: DemographicBreakdownUiModel,
    val characterBipoc: DemographicBreakdownUiModel,
    val characterLgbtq: DemographicBreakdownUiModel,
    val scopeCaption: String,
    val characterSourceCaption: String,
    val emptyCaption: String,
    val canHideBooksWithoutDetail: Boolean,
    val languages: ProportionSectionUiModel,
    val decades: ColumnSectionUiModel,
    val acquisition: ProportionSectionUiModel,
    val isLoading: Boolean = false,
) {
    val hasContent: Boolean
        get() = genderBreakdown.hasData ||
            authorBipoc.hasData ||
            authorLgbtq.hasData ||
            characterBipoc.hasData ||
            characterLgbtq.hasData ||
            languages.hasData ||
            decades.hasData ||
            acquisition.hasData
}
