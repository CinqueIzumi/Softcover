package nl.rhaydus.softcover.core.profile.domain.mapper

import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.domain.model.UserBook

internal fun UserBook.firstStartOrNull(): LocalDate? = reads.mapNotNull { it.startedAt.toLocalDateOrNull() }.minOrNull()

/** The earliest start or finish date across every read, whichever parses. */
internal fun UserBook.earliestReadDateOrNull(): LocalDate? = reads
    .flatMap { listOf(it.startedAt, it.finishedAt) }
    .mapNotNull { it.toLocalDateOrNull() }
    .minOrNull()

internal fun UserBook.latestReadDateOrNull(): LocalDate? = reads
    .flatMap { listOf(it.startedAt, it.finishedAt) }
    .mapNotNull { it.toLocalDateOrNull() }
    .maxOrNull()
