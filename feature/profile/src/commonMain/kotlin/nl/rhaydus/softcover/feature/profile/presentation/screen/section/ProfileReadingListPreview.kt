package nl.rhaydus.softcover.feature.profile.presentation.screen.section

import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.profile.domain.model.LongestWaiting
import nl.rhaydus.softcover.core.profile.domain.model.ReadingListStats
import nl.rhaydus.softcover.core.profile.domain.model.WaitingBook

internal fun profileReadingListStatsPreview(): ReadingListStats = ReadingListStats(
    addedCount = 46,
    waitDays = 128,
    waitedCount = 27,
)

internal fun profileLongestWaitingPreview(): LongestWaiting = LongestWaiting(
    onList = WaitingBook(
        book = previewRecordBook(
            id = 201,
            title = "Middlemarch",
        ),
        since = LocalDate(
            year = 2023,
            monthNumber = 3,
            dayOfMonth = 14,
        ),
    ),
    onShelf = WaitingBook(
        book = previewRecordBook(
            id = 202,
            title = "The Mountain in the Sea",
        ),
        since = LocalDate(
            year = 2021,
            monthNumber = 11,
            dayOfMonth = 2,
        ),
    ),
)
