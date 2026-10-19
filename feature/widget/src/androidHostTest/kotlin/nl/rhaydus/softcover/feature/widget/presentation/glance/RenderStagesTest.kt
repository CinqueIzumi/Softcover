package nl.rhaydus.softcover.feature.widget.presentation.glance

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RenderStagesTest {
    private val source = FakeWidgetSource { flowOf() }

    @Nested
    inner class RenderStages {
        @Test
        fun `drops loading models`() = runTest {
            // ----- Arrange -----
            val upstream = flowOf(
                "loading",
                "a",
                "loading",
                "b!",
            )

            // ----- Act -----
            val result = upstream.renderStages(source).toList()

            // ----- Assert -----
            result shouldBe listOf("a", "b!")
        }

        @Test
        fun `drops consecutive duplicates but keeps a repeat after a change`() = runTest {
            // ----- Arrange -----
            val upstream = flowOf(
                "a",
                "a",
                "b",
                "a",
                "c!",
            )

            // ----- Act -----
            val result = upstream.renderStages(source).toList()

            // ----- Assert -----
            result shouldBe listOf("a", "b", "a", "c!")
        }

        @Test
        fun `stops after the first settled model without collecting later emissions`() = runTest {
            // ----- Arrange -----
            val emitted = mutableListOf<String>()
            val upstream = flow {
                listOf("a", "b!", "c", "d!").forEach {
                    emitted.add(it)
                    emit(it)
                }
            }

            // ----- Act -----
            val result = upstream.renderStages(source).toList()

            // ----- Assert -----
            result shouldBe listOf("a", "b!")
            emitted shouldBe listOf("a", "b!")
        }

        @Test
        fun `emits nothing when every model is loading`() = runTest {
            // ----- Arrange -----
            val upstream = flowOf(
                "loading",
                "loading",
            )

            // ----- Act -----
            val result = upstream.renderStages(source).toList()

            // ----- Assert -----
            result shouldBe emptyList()
        }
    }
}
