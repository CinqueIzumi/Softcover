package nl.rhaydus.softcover.feature.deadlines.data.mapper

import nl.rhaydus.softcover.feature.deadlines.data.model.BookDeadlineEntity
import nl.rhaydus.softcover.feature.deadlines.domain.model.BookDeadline
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val ISO: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

fun BookDeadlineEntity.toDomain(): BookDeadline = BookDeadline(
    bookId = bookId,
    deadlineDate = LocalDate.parse(deadlineDate, ISO),
    setAt = LocalDate.parse(setAt, ISO),
    initialPagesPerDay = initialPagesPerDay,
)

fun BookDeadline.toEntity(): BookDeadlineEntity = BookDeadlineEntity(
    bookId = bookId,
    deadlineDate = deadlineDate.format(ISO),
    setAt = setAt.format(ISO),
    initialPagesPerDay = initialPagesPerDay,
)
