package nl.rhaydus.softcover.core.book.data.sort

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldEndWith
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.SortDirection
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SortSqlTest {
    @Nested
    inner class DateFinished {
        @Test
        fun `contains all four COALESCE terms`() {
            // ----- Arrange -----
            val mode = LibrarySortMode.DATE_FINISHED

            // ----- Act -----
            val fragment = mode.toOrderByFragment(SortDirection.ASCENDING)

            // ----- Assert -----
            fragment shouldContain "SELECT MAX(finishedAt) FROM user_book_reads"
            fragment shouldContain "ub.lastReadDate"
            fragment shouldContain "SELECT MAX(updatedAt) FROM reading_journals"
            fragment shouldContain "ub.createdAt"
        }

        @Test
        fun `COALESCE terms appear in the correct relative order`() {
            // ----- Arrange -----
            val mode = LibrarySortMode.DATE_FINISHED

            // ----- Act -----
            val fragment = mode.toOrderByFragment(SortDirection.ASCENDING)

            // ----- Assert -----
            val idxFinishedAt = fragment.indexOf("MAX(finishedAt)")
            val idxLastReadDate = fragment.indexOf("ub.lastReadDate")
            val idxReadingJournals = fragment.indexOf("reading_journals")
            val idxCreatedAt = fragment.indexOf("ub.createdAt")

            assert(idxFinishedAt < idxLastReadDate) {
                "Expected MAX(finishedAt) before ub.lastReadDate"
            }
            assert(idxLastReadDate < idxReadingJournals) {
                "Expected ub.lastReadDate before reading_journals subquery"
            }
            assert(idxReadingJournals < idxCreatedAt) {
                "Expected reading_journals subquery before ub.createdAt"
            }
        }

        @Test
        fun `ASCENDING ends with ASC`() {
            // ----- Arrange & Act -----
            val fragment = LibrarySortMode.DATE_FINISHED.toOrderByFragment(SortDirection.ASCENDING)

            // ----- Assert -----
            fragment shouldEndWith "ASC"
        }

        @Test
        fun `DESCENDING ends with DESC`() {
            // ----- Arrange & Act -----
            val fragment = LibrarySortMode.DATE_FINISHED.toOrderByFragment(SortDirection.DESCENDING)

            // ----- Assert -----
            fragment shouldEndWith "DESC"
        }
    }

    @Nested
    inner class DateAdded {
        @Test
        fun `ASCENDING produces correct fragment`() {
            // ----- Arrange & Act -----
            val fragment = LibrarySortMode.DATE_ADDED.toOrderByFragment(SortDirection.ASCENDING)

            // ----- Assert -----
            fragment shouldEndWith "ASC"
            fragment shouldContain "ub.dateAdded"
        }

        @Test
        fun `DESCENDING produces correct fragment`() {
            // ----- Arrange & Act -----
            val fragment = LibrarySortMode.DATE_ADDED.toOrderByFragment(SortDirection.DESCENDING)

            // ----- Assert -----
            fragment shouldEndWith "DESC"
            fragment shouldContain "ub.dateAdded"
        }
    }

    @Nested
    inner class ErrorModes {
        @Test
        fun `MANUAL throws an error`() {
            // ----- Assert -----
            shouldThrow<IllegalStateException> {
                LibrarySortMode.MANUAL.toOrderByFragment(SortDirection.ASCENDING)
            }
        }

        @Test
        fun `ORDER throws an error`() {
            // ----- Assert -----
            shouldThrow<IllegalStateException> {
                LibrarySortMode.ORDER.toOrderByFragment(SortDirection.ASCENDING)
            }
        }
    }
}
