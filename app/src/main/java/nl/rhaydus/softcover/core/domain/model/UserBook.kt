package nl.rhaydus.softcover.core.domain.model

import nl.rhaydus.softcover.core.domain.model.enum.BookStatus
import nl.rhaydus.softcover.core.domain.model.enum.JournalEventType
import nl.rhaydus.softcover.feature.settings.domain.model.DateStyle
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class UserBook(
    val id: Int,
    val status: BookStatus,
    val dateAdded: String,
    val createdAt: String?,
    val privacySettingId: Int,
    val reviewHasSpoilers: Boolean,
    val editionId: Int?,
    val lastReadDate: String?,
    val rating: Double?,
    val referrerUserId: Int?,
    val reviewedAt: String?,
    val updatedAt: String?,
    val journals: List<ReadingJournal>,
) {
    fun getDnfDateString(style: DateStyle): String? {
        return getUpdatedDateForEventType(
            type = JournalEventType.StatusDidNotFinish,
            style = style,
        )
    }

    fun getReadDateString(style: DateStyle): String? {
        return getUpdatedDateForEventType(
            type = JournalEventType.StatusFinished,
            style = style,
        )
    }

    fun getFallbackDateString(style: DateStyle): String {
        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val outputFormatter = style.formatter

        val date = LocalDate.parse(dateAdded, inputFormatter)
        val result = date.format(outputFormatter)

        return result
    }

    private fun getUpdatedDateForEventType(
        type: JournalEventType,
        style: DateStyle,
    ): String? {
        val mostRecentUpdatedDate = journals
            .sortedByDescending { it.updatedAt }
            .firstOrNull { it.event == type.eventName }
            ?.updatedAt ?: return null

        return LocalDateTime
            .parse(mostRecentUpdatedDate)
            .format(style.formatter)
    }
}