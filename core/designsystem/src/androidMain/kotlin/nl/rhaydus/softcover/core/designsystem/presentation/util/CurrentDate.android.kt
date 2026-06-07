package nl.rhaydus.softcover.core.designsystem.presentation.util

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

actual fun currentLocalDate(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
