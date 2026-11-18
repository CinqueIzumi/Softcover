package nl.rhaydus.softcover.core.domain.model

sealed interface AcquisitionSource {
    data class Preset(val kind: AcquisitionKind) : AcquisitionSource

    data class Other(val text: String) : AcquisitionSource
}
