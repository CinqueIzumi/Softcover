package nl.rhaydus.softcover.core.component.chart

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.component.chart.SegmentRetention.Phase
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SegmentRetentionTest {
    private fun slice(label: String): ChartSliceUiModel {
        return ChartSliceUiModel(
            label = label,
            share = "10%",
            fraction = 0.1f,
            tone = ChartTone.Unknown,
        )
    }

    private fun slices(labels: String): List<ChartSliceUiModel> {
        return labels.split(" ").map { slice(it) }
    }

    private fun SegmentRetention.labelsWithPhase(phase: Phase): List<String> {
        return entries.filter { it.phase == phase }.map { it.slice.label }
    }

    private fun SegmentRetention.labels(): List<String> {
        return entries.map { it.slice.label }
    }

    @Nested
    inner class Sync {
        @Test
        fun `first sync after construction has no entering or departing entries`() {
            // ----- Arrange -----
            val retention = SegmentRetention(
                live = emptyList(),
                initialLoading = false,
            )
            val live = slices("A B")

            // ----- Act -----
            retention.sync(
                live = live,
                isLoading = false,
            )

            // ----- Assert -----
            retention.labelsWithPhase(Phase.Entering).shouldBeEmpty()
            retention.labelsWithPhase(Phase.Departing).shouldBeEmpty()
            retention.labelsWithPhase(Phase.Live) shouldContainExactly listOf("A", "B")
        }

        @Test
        fun `label dropped from live stays as departing at its old index`() {
            // ----- Arrange -----
            val initial = slices("A B C")
            val retention = SegmentRetention(
                live = initial,
                initialLoading = false,
            )
            val next = slices("A C")

            // ----- Act -----
            retention.sync(
                live = next,
                isLoading = false,
            )

            // ----- Assert -----
            retention.labels() shouldContainExactly listOf("A", "B", "C")
            retention.entries[1].phase shouldBe Phase.Departing
            retention.labelsWithPhase(Phase.Live) shouldContainExactly listOf("A", "C")
        }

        @Test
        fun `new label arriving next to existing ones is entering`() {
            // ----- Arrange -----
            val initial = slices("A B")
            val retention = SegmentRetention(
                live = initial,
                initialLoading = false,
            )
            val next = slices("A B C")

            // ----- Act -----
            retention.sync(
                live = next,
                isLoading = false,
            )

            // ----- Assert -----
            retention.labelsWithPhase(Phase.Entering) shouldContainExactly listOf("C")
            retention.labelsWithPhase(Phase.Live) shouldContainExactly listOf("A", "B")
        }

        @Test
        fun `departing label that comes back returns to live`() {
            // ----- Arrange -----
            val initial = slices("A B")
            val retention = SegmentRetention(
                live = initial,
                initialLoading = false,
            )
            retention.sync(
                live = slices("A"),
                isLoading = false,
            )
            val back = slices("A B")

            // ----- Act -----
            retention.sync(
                live = back,
                isLoading = false,
            )

            // ----- Assert -----
            retention.labels() shouldContainExactly listOf("A", "B")
            retention.labelsWithPhase(Phase.Live) shouldContainExactly listOf("A", "B")
            retention.labelsWithPhase(Phase.Departing).shouldBeEmpty()
        }

        @Test
        fun `loading to loaded edge marks nothing entering or departing`() {
            // ----- Arrange -----
            val retention = SegmentRetention(
                live = emptyList(),
                initialLoading = true,
            )
            val loaded = slices("A B")

            // ----- Act -----
            retention.sync(
                live = loaded,
                isLoading = false,
            )

            // ----- Assert -----
            retention.labelsWithPhase(Phase.Live) shouldContainExactly listOf("A", "B")
            retention.labelsWithPhase(Phase.Entering).shouldBeEmpty()
            retention.labelsWithPhase(Phase.Departing).shouldBeEmpty()
        }

        @Test
        fun `loaded to loading edge marks nothing entering or departing`() {
            // ----- Arrange -----
            val initial = slices("A B")
            val retention = SegmentRetention(
                live = initial,
                initialLoading = false,
            )
            val placeholders = slices("X Y Z")

            // ----- Act -----
            retention.sync(
                live = placeholders,
                isLoading = true,
            )

            // ----- Assert -----
            retention.labels() shouldContainExactly listOf("X", "Y", "Z")
            retention.labelsWithPhase(Phase.Live) shouldContainExactly listOf("X", "Y", "Z")
            retention.isLoading shouldBe true
        }
    }

    @Nested
    inner class Release {
        @Test
        fun `removes only the departing entry with that label`() {
            // ----- Arrange -----
            val initial = slices("A B C")
            val retention = SegmentRetention(
                live = initial,
                initialLoading = false,
            )
            retention.sync(
                live = slices("A"),
                isLoading = false,
            )

            // ----- Act -----
            retention.release("B")

            // ----- Assert -----
            retention.labels() shouldContainExactly listOf("A", "C")
            retention.entries.firstOrNull { it.slice.label == "C" }.shouldNotBeNull().phase shouldBe Phase.Departing
        }

        @Test
        fun `is a no-op for a live label`() {
            // ----- Arrange -----
            val initial = slices("A B")
            val retention = SegmentRetention(
                live = initial,
                initialLoading = false,
            )

            // ----- Act -----
            retention.release("A")

            // ----- Assert -----
            retention.labelsWithPhase(Phase.Live) shouldContainExactly listOf("A", "B")
        }

        @Test
        fun `is a no-op when repeated`() {
            // ----- Arrange -----
            val initial = slices("A B")
            val retention = SegmentRetention(
                live = initial,
                initialLoading = false,
            )
            retention.sync(
                live = slices("A"),
                isLoading = false,
            )
            retention.release("B")

            // ----- Act -----
            retention.release("B")

            // ----- Assert -----
            retention.labels() shouldContainExactly listOf("A")
        }
    }
}
