package nl.rhaydus.softcover.core.designsystem.presentation.util

internal data class HoursMinutesSeconds(
    val hours: Int,
    val minutes: Int,
    val seconds: Int,
) {
    val totalSeconds: Int
        get() = hours * 3600 + minutes * 60 + seconds
}
