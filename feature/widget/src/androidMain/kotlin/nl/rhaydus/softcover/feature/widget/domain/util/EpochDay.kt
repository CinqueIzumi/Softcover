package nl.rhaydus.softcover.feature.widget.domain.util

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

internal fun currentEpochDay(): Long = Clock.System.now()
    .toLocalDateTime(TimeZone.currentSystemDefault())
    .date
    .toEpochDays()
    .toLong()

/**
 * Emits the current epoch day, then again each time the local date changes. A running Glance session is
 * not re-provided at midnight, so anything ranked per day must collect this to roll over.
 */
internal fun epochDayChanges(
    clock: Clock = Clock.System,
    timeZone: () -> TimeZone = TimeZone::currentSystemDefault,
): Flow<Long> = flow {
    while (true) {
        val zone = timeZone()
        val today = clock.now().toLocalDateTime(zone).date

        emit(today.toEpochDays().toLong())

        val untilNextDay = today
            .plus(
                value = 1,
                unit = DateTimeUnit.DAY,
            )
            .atStartOfDayIn(zone) - clock.now()

        delay(untilNextDay)
    }
}.distinctUntilChanged()
