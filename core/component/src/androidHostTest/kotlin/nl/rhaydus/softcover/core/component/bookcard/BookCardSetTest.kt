package nl.rhaydus.softcover.core.component.bookcard

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.collections.immutable.persistentListOf
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BookCardSetTest {
    private fun card(bookId: Int): BookCardUiModel =
        BookCardUiModel.previews.values.first().copy(key = BookCardKey.Book(bookId = bookId))

    @Nested
    inner class ToBookCardSet {
        @Test
        fun `cards keep the order of the source pairs`() {
            // ----- Arrange -----
            val first = card(bookId = 1)
            val second = card(bookId = 2)

            // ----- Act -----
            val result = listOf(first to "a", second to "b").toBookCardSet()

            // ----- Assert -----
            result.cards shouldBe listOf(first, second)
        }

        @Test
        fun `each card key resolves to its payload`() {
            // ----- Arrange -----
            val first = card(bookId = 1)
            val second = card(bookId = 2)

            // ----- Act -----
            val result = listOf(first to "a", second to "b").toBookCardSet()

            // ----- Assert -----
            result[first.key] shouldBe "a"
            result[second.key] shouldBe "b"
        }

        @Test
        fun `an empty source produces an empty set`() {
            // ----- Arrange -----
            val pairs = emptyList<Pair<BookCardUiModel, String>>()

            // ----- Act -----
            val result = pairs.toBookCardSet()

            // ----- Assert -----
            result.isEmpty shouldBe true
            result.cards.shouldBeEmpty()
        }
    }

    @Nested
    inner class Get {
        @Test
        fun `a key outside the set returns null`() {
            // ----- Arrange -----
            val set = listOf(card(bookId = 1) to "a").toBookCardSet()

            // ----- Act -----
            val result = set[BookCardKey.Book(bookId = 99)]

            // ----- Assert -----
            result shouldBe null
        }
    }

    @Nested
    inner class StandIns {
        @Test
        fun `a stand-in set keeps its cards and resolves no key`() {
            // ----- Arrange -----
            val standIn = card(bookId = 1)

            // ----- Act -----
            val result = BookCardSet.standIns(persistentListOf(standIn))

            // ----- Assert -----
            result.cards shouldBe listOf(standIn)
            result.isEmpty shouldBe false
            result[standIn.key].shouldBeNull()
        }

        @Test
        fun `an empty set has no cards`() {
            // ----- Act -----
            val result = BookCardSet.empty()

            // ----- Assert -----
            result.isEmpty shouldBe true
            result.cards.shouldBeEmpty()
        }
    }
}
