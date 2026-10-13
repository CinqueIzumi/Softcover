package nl.rhaydus.softcover.core.component.transition

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ExitRetentionTest {
    private fun retained(
        item: String,
        exiting: Boolean = false,
    ): RetainedItem<String> {
        return RetainedItem(
            item = item,
            key = item,
            exiting = exiting,
        )
    }

    private fun fold(
        previous: List<RetainedItem<String>>,
        current: List<String>,
    ): List<RetainedItem<String>> {
        return retainExiting(
            previous = previous,
            current = current,
            keyOf = { it },
        )
    }

    private fun List<RetainedItem<String>>.exitingItems(): List<String> {
        return filter { it.exiting }.map { it.item }
    }

    private fun List<RetainedItem<String>>.items(): List<String> {
        return map { it.item }
    }

    @Nested
    inner class FirstFold {
        @Test
        fun `empty previous gives plain items in current order`() {
            // ----- Arrange -----
            val current = listOf("A", "B", "C")

            // ----- Act -----
            val result = fold(
                previous = emptyList(),
                current = current,
            )

            // ----- Assert -----
            result.items() shouldContainExactly current
            result.exitingItems().shouldBeEmpty()
        }

        @Test
        fun `empty previous and empty current gives empty result`() {
            // ----- Arrange -----
            val current = emptyList<String>()

            // ----- Act -----
            val result = fold(
                previous = emptyList(),
                current = current,
            )

            // ----- Assert -----
            result.shouldBeEmpty()
        }
    }

    @Nested
    inner class Removal {
        @Test
        fun `removed item stays at its old position marked exiting`() {
            // ----- Arrange -----
            val previous = listOf(retained("A"), retained("B"), retained("C"))

            // ----- Act -----
            val result = fold(
                previous = previous,
                current = listOf("A", "C"),
            )

            // ----- Assert -----
            result.items() shouldContainExactly listOf("A", "B", "C")
            result.exitingItems() shouldContainExactly listOf("B")
        }

        @Test
        fun `removed first item stays at the front`() {
            // ----- Arrange -----
            val previous = listOf(retained("A"), retained("B"))

            // ----- Act -----
            val result = fold(
                previous = previous,
                current = listOf("B"),
            )

            // ----- Assert -----
            result.items() shouldContainExactly listOf("A", "B")
            result.exitingItems() shouldContainExactly listOf("A")
        }

        @Test
        fun `removed last item stays at the end`() {
            // ----- Arrange -----
            val previous = listOf(retained("A"), retained("B"))

            // ----- Act -----
            val result = fold(
                previous = previous,
                current = listOf("A"),
            )

            // ----- Assert -----
            result.items() shouldContainExactly listOf("A", "B")
            result.exitingItems() shouldContainExactly listOf("B")
        }

        @Test
        fun `consecutive removed items keep their relative order`() {
            // ----- Arrange -----
            val previous = listOf(retained("A"), retained("B"), retained("C"), retained("D"))

            // ----- Act -----
            val result = fold(
                previous = previous,
                current = listOf("A", "D"),
            )

            // ----- Assert -----
            result.items() shouldContainExactly listOf("A", "B", "C", "D")
            result.exitingItems() shouldContainExactly listOf("B", "C")
        }

        @Test
        fun `removing everything keeps every item exiting`() {
            // ----- Arrange -----
            val previous = listOf(retained("A"), retained("B"))

            // ----- Act -----
            val result = fold(
                previous = previous,
                current = emptyList(),
            )

            // ----- Assert -----
            result.items() shouldContainExactly listOf("A", "B")
            result.exitingItems() shouldContainExactly listOf("A", "B")
        }
    }

    @Nested
    inner class AlreadyExiting {
        @Test
        fun `item that is already exiting persists`() {
            // ----- Arrange -----
            val previous = listOf(retained("A"), retained(
                "B",
                exiting = true,
            ), retained("C"),)

            // ----- Act -----
            val result = fold(
                previous = previous,
                current = listOf("A", "C"),
            )

            // ----- Assert -----
            result.items() shouldContainExactly listOf("A", "B", "C")
            result.exitingItems() shouldContainExactly listOf("B")
        }

        @Test
        fun `key returning mid-exit comes back non-exiting without a duplicate`() {
            // ----- Arrange -----
            val previous = listOf(retained("A"), retained(
                "B",
                exiting = true,
            ), retained("C"),)

            // ----- Act -----
            val result = fold(
                previous = previous,
                current = listOf("A", "B", "C"),
            )

            // ----- Assert -----
            result.items() shouldContainExactly listOf("A", "B", "C")
            result.exitingItems().shouldBeEmpty()
        }
    }

    @Nested
    inner class NonAnimatingExit {
        @Test
        fun `removed item that does not animate its exit is dropped`() {
            // ----- Arrange -----
            val previous = listOf(retained("A"), retained("B"), retained("C"))

            // ----- Act -----
            val result = retainExiting(
                previous = previous,
                current = listOf("A", "C"),
                keyOf = { it },
                animatesExit = { it != "B" },
            )

            // ----- Assert -----
            result.items() shouldContainExactly listOf("A", "C")
            result.exitingItems().shouldBeEmpty()
        }

        @Test
        fun `dropped item is not the position anchor for a later exiting item`() {
            // ----- Arrange -----
            val previous = listOf(retained("A"), retained("S"), retained("B"))

            // ----- Act -----
            val result = retainExiting(
                previous = previous,
                current = listOf("A"),
                keyOf = { it },
                animatesExit = { it != "S" },
            )

            // ----- Assert -----
            result.items() shouldContainExactly listOf("A", "B")
            result.exitingItems() shouldContainExactly listOf("B")
        }

        @Test
        fun `previous items that all skip the exit animation leave exactly current`() {
            // ----- Arrange -----
            val previous = listOf(retained("S1"), retained("S2"))
            val current = listOf("X", "Y")

            // ----- Act -----
            val result = retainExiting(
                previous = previous,
                current = current,
                keyOf = { it },
                animatesExit = { false },
            )

            // ----- Assert -----
            result.items() shouldContainExactly current
            result.exitingItems().shouldBeEmpty()
        }
    }

    @Nested
    inner class CurrentOrder {
        @Test
        fun `inserted item appears at its current position`() {
            // ----- Arrange -----
            val previous = listOf(retained("A"), retained("C"))

            // ----- Act -----
            val result = fold(
                previous = previous,
                current = listOf("A", "B", "C"),
            )

            // ----- Assert -----
            result.items() shouldContainExactly listOf("A", "B", "C")
            result.exitingItems().shouldBeEmpty()
        }

        @Test
        fun `reordered items follow the current order`() {
            // ----- Arrange -----
            val previous = listOf(retained("A"), retained("B"), retained("C"))

            // ----- Act -----
            val result = fold(
                previous = previous,
                current = listOf("C", "A", "B"),
            )

            // ----- Assert -----
            result.items() shouldContainExactly listOf("C", "A", "B")
            result.exitingItems().shouldBeEmpty()
        }

        @Test
        fun `current item replaces the previous instance with the same key`() {
            // ----- Arrange -----
            val previous = listOf(retained("A"))

            // ----- Act -----
            val result = retainExiting(
                previous = previous,
                current = listOf("A2"),
                keyOf = { it.take(1) },
            )

            // ----- Assert -----
            result.map { it.item } shouldContainExactly listOf("A2")
        }
    }
}
