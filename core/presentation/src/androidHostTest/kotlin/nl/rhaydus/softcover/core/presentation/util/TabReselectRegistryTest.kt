package nl.rhaydus.softcover.core.presentation.util

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TabReselectRegistryTest {
    private val registry = TabReselectRegistry()
    private val key = Any()

    @Nested
    inner class Reselect {
        @Test
        fun `returns false when no handler is registered`() = runTest {
            // ----- Arrange -----
            // registry starts empty

            // ----- Act -----
            val result = registry.reselect(key)

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `returns the registered handler result`() = runTest {
            // ----- Arrange -----
            registry.register(key) { true }

            // ----- Act -----
            val result = registry.reselect(key)

            // ----- Assert -----
            result shouldBe true
        }

        @Test
        fun `does not invoke handlers registered for other keys`() = runTest {
            // ----- Arrange -----
            registry.register(Any()) { true }

            // ----- Act -----
            val result = registry.reselect(key)

            // ----- Assert -----
            result shouldBe false
        }
    }

    @Nested
    inner class Register {
        @Test
        fun `registering the same key replaces the previous handler`() = runTest {
            // ----- Arrange -----
            registry.register(key) { true }
            registry.register(key) { false }

            // ----- Act -----
            val result = registry.reselect(key)

            // ----- Assert -----
            result shouldBe false
        }
    }

    @Nested
    inner class Unregister {
        @Test
        fun `a different handler instance leaves the current one registered`() = runTest {
            // ----- Arrange -----
            val current: suspend () -> Boolean = { true }
            val stale: suspend () -> Boolean = { false }
            registry.register(
                key,
                current,
            )

            // ----- Act -----
            registry.unregister(
                key,
                stale,
            )

            // ----- Assert -----
            registry.reselect(key) shouldBe true
        }

        @Test
        fun `the current handler is removed`() = runTest {
            // ----- Arrange -----
            val current: suspend () -> Boolean = { true }
            registry.register(
                key,
                current,
            )

            // ----- Act -----
            registry.unregister(
                key,
                current,
            )

            // ----- Assert -----
            registry.reselect(key) shouldBe false
        }
    }
}
