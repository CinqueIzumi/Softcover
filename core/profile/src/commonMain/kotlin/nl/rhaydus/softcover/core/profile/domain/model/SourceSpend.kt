package nl.rhaydus.softcover.core.profile.domain.model

data class SourceSpend(
    val source: SpendSource,
    val totalThousandths: Long,
    val books: Int,
)
