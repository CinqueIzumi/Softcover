package nl.rhaydus.softcover.core.domain.connectivity

data class PendingQuoteWrite(
    val kind: PendingQuoteWriteKind,
    val quoteLocalId: Long,
    val enqueuedAt: String,
    val attempts: Int = 0,
)
