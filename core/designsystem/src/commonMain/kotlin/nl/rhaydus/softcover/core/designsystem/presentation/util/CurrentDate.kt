package nl.rhaydus.softcover.core.designsystem.presentation.util

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

/**
 * Today's date in the system time zone. Behind a seam because the underlying `Clock` type differs by
 * kotlinx-datetime version across this module's targets: Android resolves 0.6.2 (`kotlinx.datetime.Clock`),
 * while the iOS target resolves 0.7.1 (forced by Compose Multiplatform, where `Clock` moved to
 * `kotlin.time`). `LocalDate` itself is stable across both versions, so it is safe in `commonMain`.
 */
expect fun currentLocalDate(): LocalDate

/**
 * The current date-and-time in the system time zone. Behind the same seam as [currentLocalDate] for
 * the same reason — the `Clock`/`Instant` types moved to `kotlin.time` in kotlinx-datetime 0.7.1 (the
 * CMP/iOS version) while Android resolves 0.6.2. `LocalDateTime` is stable across both, so it is safe
 * in `commonMain`.
 */
expect fun currentLocalDateTime(): LocalDateTime
