package nl.rhaydus.softcover.core.profile.domain.model

import nl.rhaydus.softcover.core.domain.model.AcquisitionKind

sealed interface SpendSource {
    data class Preset(val kind: AcquisitionKind) : SpendSource

    data object SomewhereElse : SpendSource

    data object NotNoted : SpendSource
}
