package nl.rhaydus.softcover.core.book.data.sort

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeLessThan
import io.kotest.matchers.string.shouldEndWith
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.SortDirection
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SortSqlTest {
    @Nested
    inner class DateFinished {
        @Test
        fun `falls back through finishedAt, lastReadDate, journal update then createdAt in that order`() {
            // ----- Arrange -----
            val mode = LibrarySortMode.DATE_FINISHED

            // ----- Act -----
            val fragment = mode.toOrderByFragment(SortDirection.ASCENDING)

            // ----- Assert -----
            val idxFinishedAt = fragment.indexOf("MAX(finishedAt)")
            val idxLastReadDate = fragment.indexOf("ub.lastReadDate")
            val idxReadingJournals = fragment.indexOf("reading_journals")
            val idxCreatedAt = fragment.indexOf("ub.createdAt")

            idxFinishedAt shouldBeGreaterThan -1
            idxFinishedAt shouldBeLessThan idxLastReadDate
            idxLastReadDate shouldBeLessThan idxReadingJournals
            idxReadingJournals shouldBeLessThan idxCreatedAt
        }

        @Test
        fun `ends with the keyword of the requested direction`() {
            // ----- Arrange -----
            val sortMode = LibrarySortMode.DATE_FINISHED

            // ----- Act -----
            val ascending = sortMode.toOrderByFragment(SortDirection.ASCENDING)
            val descending = sortMode.toOrderByFragment(SortDirection.DESCENDING)

            // ----- Assert -----
            ascending shouldEndWith "ASC"
            descending shouldEndWith "DESC"
        }
    }

    @Nested
    inner class ErrorModes {
        @Test
        fun `MANUAL throws an error`() {
            // ----- Arrange -----
            val sortMode = LibrarySortMode.MANUAL

            // ----- Act & Assert -----
            shouldThrow<IllegalStateException> {
                sortMode.toOrderByFragment(SortDirection.ASCENDING)
            }
        }

        @Test
        fun `ORDER throws an error`() {
            // ----- Arrange -----
            val sortMode = LibrarySortMode.ORDER

            // ----- Act & Assert -----
            shouldThrow<IllegalStateException> {
                sortMode.toOrderByFragment(SortDirection.ASCENDING)
            }
        }
    }
}
