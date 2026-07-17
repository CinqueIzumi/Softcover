package nl.rhaydus.softcover.core.profile.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ProfileRefreshGateTest {
    private lateinit var gate: ProfileRefreshGate

    @BeforeEach
    fun setUp() {
        gate = ProfileRefreshGate()
    }

    @Nested
    inner class RunOnce {
        @Test
        fun `executes the block on the first call`() = runTest {
            // ----- Arrange -----
            var executionCount = 0

            // ----- Act -----
            gate.runOnce { executionCount++ }

            // ----- Assert -----
            executionCount shouldBe 1
        }

        @Test
        fun `skips the block on a second call within the same session`() = runTest {
            // ----- Arrange -----
            var executionCount = 0

            // ----- Act -----
            gate.runOnce { executionCount++ }
            gate.runOnce { executionCount++ }

            // ----- Assert -----
            executionCount shouldBe 1
        }

        @Test
        fun `a throwing block propagates the exception and leaves the gate open for the next call`() = runTest {
            // ----- Arrange -----
            var executionCount = 0

            // ----- Act -----
            shouldThrow<RuntimeException> {
                gate.runOnce {
                    executionCount++
                    throw RuntimeException("fetch failed")
                }
            }
            gate.runOnce { executionCount++ }

            // ----- Assert -----
            executionCount shouldBe 2
        }

        @Test
        fun `two coroutines racing runOnce execute the block exactly once`() = runTest {
            // ----- Arrange -----
            var executionCount = 0

            // ----- Act -----
            val job1 = launch {
                gate.runOnce {
                    delay(10)
                    executionCount++
                }
            }
            val job2 = launch {
                gate.runOnce {
                    delay(10)
                    executionCount++
                }
            }
            job1.join()
            job2.join()

            // ----- Assert -----
            executionCount shouldBe 1
        }
    }

    @Nested
    inner class Reset {
        @Test
        fun `re-opens the gate so the next runOnce call executes the block again`() = runTest {
            // ----- Arrange -----
            var executionCount = 0
            gate.runOnce { executionCount++ }

            // ----- Act -----
            gate.reset()
            gate.runOnce { executionCount++ }

            // ----- Assert -----
            executionCount shouldBe 2
        }
    }
}
