package nl.rhaydus.softcover.core.deadlines.data.mapper

import nl.rhaydus.softcover.core.database.model.BookDeadlineEntity
import nl.rhaydus.softcover.core.domain.model.BookDeadline
import nl.rhaydus.softcover.core.domain.model.DeadlineUnit
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val ISO: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

fun BookDeadlineEntity.toDomain(): BookDeadline = BookDeadline(
    bookId = bookId,
    deadlineDate = LocalDate.parse(
        deadlineDate,
        ISO,
    ),
    setAt = LocalDate.parse(
        setAt,
        ISO,
    ),
    initialPerDay = initialPerDay,
    unit = runCatching { DeadlineUnit.valueOf(unit) }.getOrDefault(DeadlineUnit.PAGES),
)

fun BookDeadline.toEntity(): BookDeadlineEntity = BookDeadlineEntity(
    bookId = bookId,
    deadlineDate = deadlineDate.format(ISO),
    setAt = setAt.format(ISO),
    initialPerDay = initialPerDay,
    unit = unit.name,
)
