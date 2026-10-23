package nl.rhaydus.softcover.showcase.family.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
internal data class EntrySectionUiModel(
    val name: String,
    val blurb: String,
    val fixtures: ImmutableList<FixtureFrameUiModel>,
)
