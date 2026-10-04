package nl.rhaydus.softcover.feature.settings.presentation.mapper

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.feature.settings.domain.model.RoadmapBlock
import nl.rhaydus.softcover.feature.settings.domain.model.RoadmapSpan
import nl.rhaydus.softcover.feature.settings.presentation.model.RoadmapRenderUnit

private const val ROADMAP_STAND_IN_ENTRY_COUNT = 3

/** Three entries, each a heading and a list sized like real ones, drawn masked until the roadmap loads. */
internal val ROADMAP_STAND_IN_UNITS: ImmutableList<RoadmapRenderUnit> = List(ROADMAP_STAND_IN_ENTRY_COUNT) {
    listOf(
        RoadmapRenderUnit.Heading(
            header = SectionHeaderUiModel.Section(
                eyebrow = "0.0.0",
                headline = "Roadmap entry title",
                isLoading = true,
            ),
        ),
        RoadmapRenderUnit.ListGroup(
            items = List(3) {
                RoadmapBlock.BulletListItem(
                    spans = listOf(RoadmapSpan(text = "A roadmap item describing what is coming next")),
                )
            },
            isLoading = true,
        ),
    )
}.flatten().toImmutableList()
