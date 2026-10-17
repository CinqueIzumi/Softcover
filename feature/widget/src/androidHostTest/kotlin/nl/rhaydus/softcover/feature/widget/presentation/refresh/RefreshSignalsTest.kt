package nl.rhaydus.softcover.feature.widget.presentation.refresh

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class RefreshSignalsTest {
    private val debounce = 100.milliseconds

    private class Harness(
        val placed: MutableStateFlow<Set<String>>,
        val upstream: MutableSharedFlow<Unit>,
        val signals: MutableList<Unit>,
        val collecting: () -> Int,
        val starts: () -> Int,
    )

    private fun TestScope.harness(
        changes: (MutableSharedFlow<Unit>) -> Flow<Unit> = { it },
    ): Harness {
        val placed = MutableStateFlow<Set<String>>(emptySet())
        val upstream = MutableSharedFlow<Unit>()
        var active = 0
        var started = 0
        val signals = mutableListOf<Unit>()

        refreshSignals(
            placedWidgets = placed,
            changes = {
                changes(upstream)
                    .onStart {
                        active++
                        started++
                    }
                    .onCompletion { active-- }
            },
            debounce = debounce,
        )
            .onEach { signals.add(it) }
            .launchIn(backgroundScope)

        runCurrent()

        return Harness(
            placed = placed,
            upstream = upstream,
            signals = signals,
            collecting = { active },
            starts = { started },
        )
    }

    @Test
    fun `does not collect changes or emit while no widget is placed`() = runTest {
        // ----- Arrange -----
        val harness = harness()

        // ----- Act -----
        harness.upstream.emit(Unit)
        advanceTimeBy(debounce * 3)
        runCurrent()

        // ----- Assert -----
        harness.starts() shouldBe 0
        harness.signals.size shouldBe 0
    }

    @Test
    fun `emits one signal for a burst of changes within the debounce window`() = runTest {
        // ----- Arrange -----
        val harness = harness()
        harness.placed.value = setOf("1")
        runCurrent()

        // ----- Act -----
        repeat(3) {
            harness.upstream.emit(Unit)
            advanceTimeBy(debounce / 2)
        }
        advanceTimeBy(debounce * 2)
        runCurrent()

        // ----- Assert -----
        harness.signals.size shouldBe 1
    }

    @Test
    fun `emits one signal per change when changes are separated by more than the debounce window`() = runTest {
        // ----- Arrange -----
        val harness = harness()
        harness.placed.value = setOf("1")
        runCurrent()

        // ----- Act -----
        repeat(3) {
            harness.upstream.emit(Unit)
            advanceTimeBy(debounce * 2)
            runCurrent()
        }

        // ----- Assert -----
        harness.signals.size shouldBe 3
    }

    @Test
    fun `stops collecting changes and emits nothing once the placed set empties`() = runTest {
        // ----- Arrange -----
        val harness = harness()
        harness.placed.value = setOf("1")
        runCurrent()

        // ----- Act -----
        harness.placed.value = emptySet()
        runCurrent()
        harness.upstream.emit(Unit)
        advanceTimeBy(debounce * 3)
        runCurrent()

        // ----- Assert -----
        harness.starts() shouldBe 1
        harness.collecting() shouldBe 0
        harness.signals.size shouldBe 0
    }

    @Test
    fun `does not restart collection when the placed set changes but stays non-empty`() = runTest {
        // ----- Arrange -----
        val harness = harness()
        harness.placed.value = setOf("1")
        runCurrent()

        // ----- Act -----
        harness.placed.value = setOf("1", "2")
        runCurrent()

        // ----- Assert -----
        harness.starts() shouldBe 1
    }

    @Test
    fun `emits again after an upstream failure once the set empties and refills`() = runTest {
        // ----- Arrange -----
        var failNext = true
        val harness = harness { upstream ->
            if (failNext) {
                failNext = false
                flow { throw IllegalStateException("boom") }
            } else {
                upstream
            }
        }
        harness.placed.value = setOf("1")
        runCurrent()
        harness.placed.value = emptySet()
        runCurrent()

        // ----- Act -----
        harness.placed.value = setOf("1")
        runCurrent()
        harness.upstream.emit(Unit)
        advanceTimeBy(debounce * 2)
        runCurrent()

        // ----- Assert -----
        harness.signals.size shouldBe 1
    }
}
