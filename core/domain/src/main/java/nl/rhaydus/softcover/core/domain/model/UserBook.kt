package nl.rhaydus.softcover.core.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

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
    val reviewDocument: ReviewDocument? = null,
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

    fun getReadDateString(
        style: DateStyle,
        finishedAt: String? = null,
    ): String? {
        finishedAt?.let {
            runCatching { style.formatter.format(LocalDate.parse(it)) }
                .getOrNull()
                ?.let { formatted -> return formatted }
        }

        return getUpdatedDateForEventType(
            type = JournalEventType.StatusFinished,
            style = style,
        )
    }

    fun getFallbackDateString(style: DateStyle): String {
        val date = LocalDate.parse(dateAdded)

        return style.formatter.format(date)
    }

    private fun getUpdatedDateForEventType(
        type: JournalEventType,
        style: DateStyle,
    ): String? {
        val mostRecentUpdatedDate = journals
            .sortedByDescending { it.updatedAt }
            .firstOrNull { it.event == type.eventName }
            ?.updatedAt ?: return null

        val date = LocalDateTime.parse(mostRecentUpdatedDate).date

        return style.formatter.format(date)
    }
}
