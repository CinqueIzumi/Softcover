package nl.rhaydus.softcover.feature.widget.presentation.glance

import android.os.Bundle
import androidx.glance.appwidget.GlanceAppWidget
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class BackgroundWidgetRendererTest {
    private class FakeHost(
        private val placedIds: () -> List<Int> = { emptyList() },
        private val onPush: (appWidgetId: Int, model: String) -> Unit = { _, _ -> },
    ) : WidgetHost {
        val pushes = mutableListOf<Pair<Int, String>>()

        override suspend fun placedAppWidgetIds(widget: GlanceAppWidget): List<Int> = placedIds()

        override fun options(appWidgetId: Int): Bundle = mockk()

        override suspend fun <T> push(
            widget: GlanceAppWidget,
            source: WidgetModelSource<T>,
            appWidgetId: Int,
            model: T,
            options: Bundle,
        ) {
            pushes.add(appWidgetId to model.toString())
            onPush(
                appWidgetId,
                model.toString(),
            )
        }
    }

    private val widget: GlanceAppWidget = mockk()

    private val liveSessions = LiveWidgetSessions()

    private fun renderer(
        host: WidgetHost,
        modelsFor: (call: Int) -> Flow<String>,
    ) = BackgroundWidgetRenderer(
        host = host,
        liveSessions = liveSessions,
        sources = mapOf(widget.javaClass to FakeWidgetSource(modelsFor)),
    )

    private fun unregisteredRenderer(host: WidgetHost) = BackgroundWidgetRenderer(
        host = host,
        liveSessions = liveSessions,
        sources = emptyMap(),
    )

    @Nested
    inner class Render {
        @Test
        fun `returns normally and pushes nothing for a widget with no registered source`() = runTest {
            // ----- Arrange -----
            val host = FakeHost()
            val renderer = unregisteredRenderer(host)

            // ----- Act -----
            renderer.render(
                widget,
                1,
            )

            // ----- Assert -----
            host.pushes shouldBe emptyList()
        }

        @Test
        fun `pushes every stage in order until settled`() = runTest {
            // ----- Arrange -----
            val host = FakeHost()
            val renderer = renderer(host) {
                flowOf(
                    "a",
                    "b",
                    "c!",
                    "d",
                )
            }

            // ----- Act -----
            renderer.render(
                widget,
                1,
            )

            // ----- Assert -----
            host.pushes shouldBe listOf(1 to "a", 1 to "b", 1 to "c!")
        }

        @Test
        fun `pushes nothing to an instance marked live`() = runTest {
            // ----- Arrange -----
            val host = FakeHost()
            val renderer = renderer(host) { flowOf("a!") }
            liveSessions.markLive(1)

            // ----- Act -----
            renderer.render(
                widget,
                1,
            )

            // ----- Assert -----
            host.pushes shouldBe emptyList()
        }

        @Test
        fun `a newer render for the same id cancels the in-flight one`() = runTest {
            // ----- Arrange -----
            val host = FakeHost()
            val renderer = renderer(host) { call ->
                if (call == 0) {
                    flow {
                        emit("old1")
                        delay(5.seconds)
                        emit("old2!")
                    }
                } else {
                    flowOf("new!")
                }
            }
            launch {
                renderer.render(
                    widget,
                    1,
                )
            }
            runCurrent()

            // ----- Act -----
            renderer.render(
                widget,
                1,
            )
            advanceUntilIdle()

            // ----- Assert -----
            host.pushes shouldBe listOf(1 to "old1", 1 to "new!")
        }

        @Test
        fun `renders for different ids do not cancel each other`() = runTest {
            // ----- Arrange -----
            val host = FakeHost()
            val renderer = renderer(host) { call ->
                flow {
                    emit("first$call")
                    delay(5.seconds)
                    emit("last$call!")
                }
            }
            launch {
                renderer.render(
                    widget,
                    1,
                )
            }
            runCurrent()

            // ----- Act -----
            renderer.render(
                widget,
                2,
            )
            advanceUntilIdle()

            // ----- Assert -----
            host.pushes.toSet() shouldBe setOf(
                1 to "first0",
                1 to "last0!",
                2 to "first1",
                2 to "last1!",
            )
        }

        @Test
        fun `keeps the stages pushed so far and returns when nothing settles within 10 seconds`() = runTest {
            // ----- Arrange -----
            val host = FakeHost()
            val renderer = renderer(host) {
                flow {
                    emit("a")
                    delay(9.seconds)
                    emit("b")
                    delay(2.seconds)
                    emit("late!")
                }
            }

            // ----- Act -----
            renderer.render(
                widget,
                1,
            )

            // ----- Assert -----
            host.pushes shouldBe listOf(1 to "a", 1 to "b")
            testScheduler.currentTime shouldBe 10_000L
        }

        @Test
        fun `swallows a push that throws`() = runTest {
            // ----- Arrange -----
            val host = FakeHost(onPush = { _, _ -> error("push failed") })
            val renderer = renderer(host) {
                flowOf(
                    "a",
                    "b!",
                )
            }

            // ----- Act -----
            renderer.render(
                widget,
                1,
            )

            // ----- Assert -----
            host.pushes shouldBe listOf(1 to "a")
        }
    }

    @Nested
    inner class RenderAll {
        @Test
        fun `renders every placed id`() = runTest {
            // ----- Arrange -----
            val host = FakeHost(placedIds = { listOf(1, 2, 3) })
            val renderer = renderer(host) { flowOf("a!") }

            // ----- Act -----
            renderer.renderAll(widget)

            // ----- Assert -----
            host.pushes.toSet() shouldBe setOf(1 to "a!", 2 to "a!", 3 to "a!")
        }

        @Test
        fun `returns normally and pushes nothing for a widget with no registered source`() = runTest {
            // ----- Arrange -----
            val host = FakeHost(placedIds = { listOf(1, 2) })
            val renderer = unregisteredRenderer(host)

            // ----- Act -----
            renderer.renderAll(widget)

            // ----- Assert -----
            host.pushes shouldBe emptyList()
        }

        @Test
        fun `returns normally when listing placed ids throws`() = runTest {
            // ----- Arrange -----
            val host = FakeHost(placedIds = { error("no ids") })
            val renderer = renderer(host) { flowOf("a!") }

            // ----- Act -----
            renderer.renderAll(widget)

            // ----- Assert -----
            host.pushes shouldBe emptyList()
        }
    }
}
