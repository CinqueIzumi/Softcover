package nl.rhaydus.softcover.core.profile.domain.model

import nl.rhaydus.softcover.core.domain.model.AcquisitionKind

data class AcquisitionSplit(
    val byKind: Map<AcquisitionKind, Int> = emptyMap(),
    val somewhereElse: Int = 0,
    val notNoted: Int = 0,
)
