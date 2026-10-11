package nl.rhaydus.softcover.core.component.snackbar

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SnackbarHostRegistryTest {
    @Nested
    inner class IsTopmost {
        @Test
        fun `nothing is topmost in an empty registry`() {
            // ----- Arrange -----
            val registry = SnackbarHostRegistry()

            // ----- Act -----
            val result = registry.isTopmost(Any())

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `a lone registered token is topmost`() {
            // ----- Arrange -----
            val registry = SnackbarHostRegistry()
            val token = Any()
            registry.register(
                token = token,
                level = 0,
            )

            // ----- Act -----
            val result = registry.isTopmost(token)

            // ----- Assert -----
            result shouldBe true
        }

        @Test
        fun `only the last registered token is topmost`() {
            // ----- Arrange -----
            val registry = SnackbarHostRegistry()
            val first = Any()
            val second = Any()
            registry.register(
                token = first,
                level = 0,
            )
            registry.register(
                token = second,
                level = 0,
            )

            // ----- Act -----
            val firstResult = registry.isTopmost(first)
            val secondResult = registry.isTopmost(second)

            // ----- Assert -----
            firstResult shouldBe false
            secondResult shouldBe true
        }

        @Test
        fun `an unregistered token is never topmost`() {
            // ----- Arrange -----
            val registry = SnackbarHostRegistry()
            registry.register(
            token = Any(),
            level = 0,
        )

            // ----- Act -----
            val result = registry.isTopmost(Any())

            // ----- Assert -----
            result shouldBe false
        }
    }

    @Nested
    inner class Unregister {
        @Test
        fun `unregistering the top restores the previous token as topmost`() {
            // ----- Arrange -----
            val registry = SnackbarHostRegistry()
            val first = Any()
            val second = Any()
            registry.register(
                token = first,
                level = 0,
            )
            registry.register(
                token = second,
                level = 0,
            )

            // ----- Act -----
            registry.unregister(second)

            // ----- Assert -----
            registry.isTopmost(first) shouldBe true
            registry.isTopmost(second) shouldBe false
        }

        @Test
        fun `unregistering a middle token leaves the top unchanged`() {
            // ----- Arrange -----
            val registry = SnackbarHostRegistry()
            val first = Any()
            val middle = Any()
            val top = Any()
            registry.register(
                token = first,
                level = 0,
            )
            registry.register(
                token = middle,
                level = 0,
            )
            registry.register(
                token = top,
                level = 0,
            )

            // ----- Act -----
            registry.unregister(middle)

            // ----- Assert -----
            registry.isTopmost(top) shouldBe true
            registry.isTopmost(first) shouldBe false
        }

        @Test
        fun `unregistering the only token empties the registry`() {
            // ----- Arrange -----
            val registry = SnackbarHostRegistry()
            val token = Any()
            registry.register(
                token = token,
                level = 0,
            )

            // ----- Act -----
            registry.unregister(token)

            // ----- Assert -----
            registry.isTopmost(token) shouldBe false
        }
    }

    @Nested
    inner class Levels {
        @Test
        fun `a higher level outranks a later lower level registration`() {
            // ----- Arrange -----
            val registry = SnackbarHostRegistry()
            val high = Any()
            val low = Any()
            registry.register(
                token = high,
                level = 2,
            )
            registry.register(
                token = low,
                level = 1,
            )

            // ----- Act -----
            val highResult = registry.isTopmost(high)
            val lowResult = registry.isTopmost(low)

            // ----- Assert -----
            highResult shouldBe true
            lowResult shouldBe false
        }

        @Test
        fun `a tie at the same level resolves to the latest registration`() {
            // ----- Arrange -----
            val registry = SnackbarHostRegistry()
            val first = Any()
            val second = Any()
            registry.register(
                token = first,
                level = 1,
            )
            registry.register(
                token = second,
                level = 1,
            )

            // ----- Act -----
            val firstResult = registry.isTopmost(first)
            val secondResult = registry.isTopmost(second)

            // ----- Assert -----
            firstResult shouldBe false
            secondResult shouldBe true
        }

        @Test
        fun `unregistering the top level hands over to the next level`() {
            // ----- Arrange -----
            val registry = SnackbarHostRegistry()
            val high = Any()
            val low = Any()
            registry.register(
                token = low,
                level = 1,
            )
            registry.register(
                token = high,
                level = 2,
            )

            // ----- Act -----
            registry.unregister(high)

            // ----- Assert -----
            registry.isTopmost(low) shouldBe true
            registry.isTopmost(high) shouldBe false
        }
    }

    @Nested
    inner class RemainingMillis {
        @Test
        fun `first call for an instance returns the full duration`() {
            // ----- Arrange -----
            val registry = SnackbarHostRegistry()

            // ----- Act -----
            val result = registry.remainingMillis(
                data = Any(),
                durationMillis = 4_000L,
                nowMillis = 10_000L,
            )

            // ----- Assert -----
            result shouldBe 4_000L
        }

        @Test
        fun `a later call with the same instance returns the reduced time`() {
            // ----- Arrange -----
            val registry = SnackbarHostRegistry()
            val data = Any()
            registry.remainingMillis(
                data = data,
                durationMillis = 4_000L,
                nowMillis = 10_000L,
            )

            // ----- Act -----
            val result = registry.remainingMillis(
                data = data,
                durationMillis = 4_000L,
                nowMillis = 11_500L,
            )

            // ----- Assert -----
            result shouldBe 2_500L
        }

        @Test
        fun `a new instance resets the start`() {
            // ----- Arrange -----
            val registry = SnackbarHostRegistry()
            registry.remainingMillis(
                data = Any(),
                durationMillis = 4_000L,
                nowMillis = 10_000L,
            )

            // ----- Act -----
            val result = registry.remainingMillis(
                data = Any(),
                durationMillis = 4_000L,
                nowMillis = 13_000L,
            )

            // ----- Assert -----
            result shouldBe 4_000L
        }

        @Test
        fun `time past the duration is clamped at zero`() {
            // ----- Arrange -----
            val registry = SnackbarHostRegistry()
            val data = Any()
            registry.remainingMillis(
                data = data,
                durationMillis = 4_000L,
                nowMillis = 10_000L,
            )

            // ----- Act -----
            val result = registry.remainingMillis(
                data = data,
                durationMillis = 4_000L,
                nowMillis = 20_000L,
            )

            // ----- Assert -----
            result shouldBe 0L
        }
    }
}
