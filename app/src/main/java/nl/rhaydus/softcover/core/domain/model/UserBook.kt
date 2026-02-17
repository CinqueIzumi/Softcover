package nl.rhaydus.softcover.core.domain.model

import nl.rhaydus.softcover.core.domain.model.enum.BookStatus
import nl.rhaydus.softcover.core.domain.model.enum.JournalEventType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class UserBook(
    val id: Int,
    val status: BookStatus,
    val dateAdded: String,
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
    val dnfDate: String?
        get(): String? = getUpdatedDateForEventType(type = JournalEventType.StatusDidNotFinish)

    val wantToReadDate: String?
        get(): String? = getUpdatedDateForEventType(type = JournalEventType.StatusWantToRead)

    val readDate: String?
        get(): String? = getUpdatedDateForEventType(type = JournalEventType.StatusFinished)

    val fallbackDateAdded: String
        get() {
            val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

            val date = LocalDate.parse(dateAdded, inputFormatter)
            val result = date.format(outputFormatter)

            return result
        }

    private fun getUpdatedDateForEventType(type: JournalEventType): String? {
        val mostRecentStatusStoppedDate = journals
            .sortedByDescending { it.updatedAt }
            .firstOrNull { it.event == type.eventName }
            ?.updatedAt ?: return null

        return LocalDateTime.parse(mostRecentStatusStoppedDate)
            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    }
}