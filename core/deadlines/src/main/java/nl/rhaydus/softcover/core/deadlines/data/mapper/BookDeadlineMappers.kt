package nl.rhaydus.softcover.core.deadlines.data.mapper

import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.database.model.BookDeadlineEntity
import nl.rhaydus.softcover.core.domain.model.BookDeadline
import nl.rhaydus.softcover.core.domain.model.DeadlineUnit

internal fun BookDeadlineEntity.toDomain(): BookDeadline = BookDeadline(
    bookId = bookId,
    deadlineDate = LocalDate.parse(deadlineDate),
    setAt = LocalDate.parse(setAt),
    initialPerDay = initialPerDay,
    unit = runCatching { DeadlineUnit.valueOf(unit) }.getOrDefault(DeadlineUnit.PAGES),
)

internal fun BookDeadline.toEntity(): BookDeadlineEntity = BookDeadlineEntity(
    bookId = bookId,
    deadlineDate = deadlineDate.toString(),
    setAt = setAt.toString(),
    initialPerDay = initialPerDay,
    unit = unit.name,
)
