package nl.rhaydus.softcover.core.uibinding.session

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.presentation.session.ActiveSessionController

class StopSessionThenTest {
    @Nested
    inner class StopSessionThen {
        @Test
        fun `stops the session before running the pause`() = runTest {
            // ----- Arrange -----
            val calls = mutableListOf<String>()
            val controller = mockk<ActiveSessionController>()
            every {
                controller.stop()
            } answers { calls.add("stop") }

            // ----- Act -----
            controller.stopSessionThen {
                calls.add("pause")
            }

            // ----- Assert -----
            calls shouldBe listOf("stop", "pause")
        }

        @Test
        fun `a failing pause still comes after the session was stopped`() = runTest {
            // ----- Arrange -----
            val calls = mutableListOf<String>()
            val controller = mockk<ActiveSessionController>()
            every {
                controller.stop()
            } answers { calls.add("stop") }

            // ----- Act -----
            shouldThrow<IllegalStateException> {
                controller.stopSessionThen {
                    error("pause failed")
                }
            }

            // ----- Assert -----
            calls shouldBe listOf("stop")
        }
    }
}
