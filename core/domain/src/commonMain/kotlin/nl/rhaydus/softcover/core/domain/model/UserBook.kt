package nl.rhaydus.softcover.core.domain.model

import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.domain.util.utcJournalTimeToLocalDate

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
    val reads: List<UserBookRead>,
) {
    /** Newest first. Placeholders sort ahead of server reads because they are always created after them. */
    val readHistory: List<UserBookRead>
        get() = reads.sortedWith(
            compareByDescending<UserBookRead> { it.isPlaceholder }
                .thenByDescending { it.id },
        )

    val finishedReadCount: Int
        get() = reads.count { it.isFinished }

    val finishYears: List<Int>
        get() = reads
            .mapNotNull { read -> read.finishedAt?.toLocalDateOrNull()?.year }
            .distinct()
            .sorted()

    /** Mirrors the server: the read a status change or progress write acts on. */
    val currentRead: UserBookRead?
        get() = when (status) {
            BookStatus.Reading, BookStatus.Paused -> readHistory.firstOrNull { it.isFinished.not() }
                ?: readHistory.firstOrNull()

            BookStatus.Read -> newestFinishedRead() ?: readHistory.firstOrNull()

            BookStatus.DidNotFinish -> readHistory.firstOrNull()

            BookStatus.WantToRead, BookStatus.None -> null
        }

    val finishDate: LocalDate?
        get() {
            val journalDate = journals
                .filter { it.event == JournalEventType.StatusFinished.eventName }
                .maxByOrNull { it.updatedAt }
                ?.let { utcJournalTimeToLocalDate(it.updatedAt) }

            return listOfNotNull(
                newestFinishedRead()?.finishedAt?.toLocalDateOrNull(),
                lastReadDate?.toLocalDateOrNull(),
                journalDate,
            ).maxOrNull()
        }

    fun getDnfDateString(style: DateStyle): String? {
        return getUpdatedDateForEventType(
            type = JournalEventType.StatusDidNotFinish,
            style = style,
        )
    }

    fun getReadDateString(style: DateStyle): String? = finishDate?.let { style.formatter.format(it) }

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

        val date = utcJournalTimeToLocalDate(mostRecentUpdatedDate) ?: return null

        return style.formatter.format(date)
    }

    private fun newestFinishedRead(): UserBookRead? = reads
        .filter { it.isFinished }
        .maxWithOrNull(
            compareBy<UserBookRead> { it.finishedAt }
                .thenBy { it.isPlaceholder }
                .thenBy { it.id },
        )

    private fun String.toLocalDateOrNull(): LocalDate? = runCatching { LocalDate.parse(this) }.getOrNull()
}
