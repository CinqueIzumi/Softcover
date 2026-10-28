package nl.rhaydus.softcover.core.domain.connectivity

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UserBookWriteLockTest {
    private lateinit var lock: UserBookWriteLock

    @BeforeEach
    fun setUp() {
        lock = UserBookWriteLock()
    }

    @Nested
    inner class WithLock {
        @Test
        fun `second block on the same book starts after the first finishes`() = runTest {
            // ----- Arrange -----
            val events = mutableListOf<String>()

            // ----- Act -----
            val first = async {
                lock.withLock(bookId = 1) {
                    events += "first start"
                    delay(100)
                    events += "first end"
                }
            }
            val second = async {
                lock.withLock(bookId = 1) {
                    events += "second start"
                    events += "second end"
                }
            }
            advanceUntilIdle()
            first.await()
            second.await()

            // ----- Assert -----
            events shouldBe listOf("first start", "first end", "second start", "second end")
        }

        @Test
        fun `blocks on different books run concurrently`() = runTest {
            // ----- Arrange -----
            val events = mutableListOf<String>()

            // ----- Act -----
            val first = async {
                lock.withLock(bookId = 1) {
                    events += "first start"
                    delay(100)
                    events += "first end"
                }
            }
            val second = async {
                lock.withLock(bookId = 2) {
                    events += "second start"
                    events += "second end"
                }
            }
            advanceUntilIdle()
            first.await()
            second.await()

            // ----- Assert -----
            events shouldBe listOf("first start", "second start", "second end", "first end")
        }

        @Test
        fun `releases the lock when the block throws`() = runTest {
            // ----- Arrange -----
            shouldThrow<IllegalStateException> {
                lock.withLock(bookId = 1) { error("boom") }
            }

            // ----- Act -----
            val result = lock.withLock(bookId = 1) { "after" }

            // ----- Assert -----
            result shouldBe "after"
        }
    }
}
