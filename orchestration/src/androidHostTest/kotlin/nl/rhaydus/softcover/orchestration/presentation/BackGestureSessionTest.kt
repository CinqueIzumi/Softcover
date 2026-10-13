package nl.rhaydus.softcover.orchestration.presentation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BackGestureSessionTest {
    private class FakeScreen : Screen {
        @Composable
        override fun Content() = Unit
    }

    @Nested
    inner class SeekingFollowing {
        @Test
        fun `the same top keeps the gesture as it was`() {
            // ----- Arrange -----
            val from = FakeScreen()
            val seeking = BackGestureSession.Seeking(
                from = from,
                target = FakeScreen(),
            )

            // ----- Act -----
            val result = seeking.following(top = from)

            // ----- Assert -----
            result shouldBe seeking
        }

        @Test
        fun `a different top abandons an unreleased gesture without releasing it`() {
            // ----- Arrange -----
            val seeking = BackGestureSession.Seeking(
                from = FakeScreen(),
                target = FakeScreen(),
            )

            // ----- Act -----
            val result = seeking.following(top = FakeScreen())

            // ----- Assert -----
            result shouldBe BackGestureSession.Abandoned(isReleased = false)
        }

        @Test
        fun `a different top abandons a released gesture and keeps it released`() {
            // ----- Arrange -----
            val seeking = BackGestureSession.Seeking(
                from = FakeScreen(),
                target = FakeScreen(),
            ).released()

            // ----- Act -----
            val result = seeking.following(top = FakeScreen())

            // ----- Assert -----
            result shouldBe BackGestureSession.Abandoned(isReleased = true)
        }
    }
}
