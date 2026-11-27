package nl.rhaydus.softcover.core.profile.domain.model

data class PublicationStats(
    val languages: LanguageSplit = LanguageSplit(),
    val decades: PublicationDecades = PublicationDecades(),
    val acquisition: AcquisitionSplit = AcquisitionSplit(),
)
