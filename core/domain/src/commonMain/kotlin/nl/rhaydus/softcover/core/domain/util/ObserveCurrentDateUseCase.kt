package nl.rhaydus.softcover.core.domain.util

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import nl.rhaydus.common.currentLocalDate
import kotlin.time.Clock

/** Today's date, re-emitted whenever local midnight passes while a collector is running. */
interface ObserveCurrentDateUseCase {
    operator fun invoke(): Flow<LocalDate>
}

internal class ObserveCurrentDateUseCaseImpl : ObserveCurrentDateUseCase {
    override fun invoke(): Flow<LocalDate> = flow {
        while (true) {
            val timeZone = TimeZone.currentSystemDefault()
            val today = currentLocalDate()
            emit(today)

            val startOfTomorrow = today.plus(
                1,
                DateTimeUnit.DAY,
            ).atStartOfDayIn(timeZone)
            val untilTomorrow = startOfTomorrow - Clock.System.now()
            delay(untilTomorrow.inWholeMilliseconds.coerceAtLeast(0))
        }
    }
}
