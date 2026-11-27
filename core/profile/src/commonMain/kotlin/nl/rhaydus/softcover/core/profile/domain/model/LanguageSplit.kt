package nl.rhaydus.softcover.core.profile.domain.model

data class LanguageSplit(
    val languages: List<LanguageCount> = emptyList(),
    val unknown: Int = 0,
)
