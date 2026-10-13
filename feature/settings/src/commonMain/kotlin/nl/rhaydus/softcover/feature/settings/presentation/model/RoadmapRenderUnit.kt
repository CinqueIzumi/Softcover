package nl.rhaydus.softcover.feature.settings.presentation.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.feature.settings.domain.model.RoadmapBlock
import nl.rhaydus.softcover.feature.settings.domain.model.RoadmapSpan

/**
 * One render pass over a
 * [RoadmapDocument][nl.rhaydus.softcover.feature.settings.domain.model.RoadmapDocument]'s flat block
 * list. [ListGroup] merges any consecutive run of [RoadmapBlock.BulletListItem] /
 * [RoadmapBlock.OrderedListItem], which the domain model emits as flat top-level blocks.
 */
@Immutable
internal sealed interface RoadmapRenderUnit {
    data class Heading(val header: SectionHeaderUiModel.Section) : RoadmapRenderUnit

    data class PlainHeading(val text: String) : RoadmapRenderUnit

    data class ListGroup(
        val items: ImmutableList<RoadmapBlock>,
        val isLoading: Boolean = false,
    ) : RoadmapRenderUnit

    data class Paragraph(val spans: ImmutableList<RoadmapSpan>) : RoadmapRenderUnit

    data class BlockQuote(val spans: ImmutableList<RoadmapSpan>) : RoadmapRenderUnit

    data object ThematicBreak : RoadmapRenderUnit
}
