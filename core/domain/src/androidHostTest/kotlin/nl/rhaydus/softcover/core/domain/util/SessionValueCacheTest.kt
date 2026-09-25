package nl.rhaydus.softcover.core.domain.util

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicInteger

class SessionValueCacheTest {
    @Nested
    inner class GetOrPutKeyed {
        @Test
        fun `second call for the same key does not invoke load again`() = runTest {
            // ----- Arrange -----
            val cache = SessionValueCache<String, String>()
            val loadCount = AtomicInteger(0)
            val load: suspend () -> String = {
                loadCount.incrementAndGet()
                "value"
            }

            // ----- Act -----
            cache.getOrPut(
                key = "key",
                load = load,
            )
            cache.getOrPut(
                key = "key",
                load = load,
            )

            // ----- Assert -----
            loadCount.get() shouldBe 1
        }

        @Test
        fun `load throwing propagates the exception and stores nothing`() = runTest {
            // ----- Arrange -----
            val cache = SessionValueCache<String, String>()
            val error = RuntimeException("load failed")
            val failingLoad: suspend () -> String = { throw error }

            // ----- Act -----
            val thrown = runCatching { cache.getOrPut(
                key = "key",
                load = failingLoad,
            ) }.exceptionOrNull()

            // ----- Assert -----
            thrown shouldBe error
        }

        @Test
        fun `a call after a failed load retries load and can succeed`() = runTest {
            // ----- Arrange -----
            val cache = SessionValueCache<String, String>()
            val loadCount = AtomicInteger(0)
            val load: suspend () -> String = {
                val attempt = loadCount.incrementAndGet()
                if (attempt == 1) throw RuntimeException("load failed")
                "recovered"
            }
            runCatching { cache.getOrPut(
                key = "key",
                load = load,
            ) }

            // ----- Act -----
            val result = cache.getOrPut(
                key = "key",
                load = load,
            )

            // ----- Assert -----
            result shouldBe "recovered"
            loadCount.get() shouldBe 2
        }

        @Test
        fun `a value of null is still treated as cached and load is not invoked again`() = runTest {
            // ----- Arrange -----
            val cache = SessionValueCache<String, String?>()
            val loadCount = AtomicInteger(0)
            val load: suspend () -> String? = {
                loadCount.incrementAndGet()
                null
            }

            // ----- Act -----
            val first = cache.getOrPut(
                key = "key",
                load = load,
            )
            val second = cache.getOrPut(
                key = "key",
                load = load,
            )

            // ----- Assert -----
            first shouldBe null
            second shouldBe null
            loadCount.get() shouldBe 1
        }

        @Test
        fun `different keys load independently and both remain retrievable`() = runTest {
            // ----- Arrange -----
            val cache = SessionValueCache<String, String>()
            val aLoadCount = AtomicInteger(0)
            val bLoadCount = AtomicInteger(0)
            val loadA: suspend () -> String = {
                aLoadCount.incrementAndGet()
                "value-a"
            }
            val loadB: suspend () -> String = {
                bLoadCount.incrementAndGet()
                "value-b"
            }

            // ----- Act -----
            val a = cache.getOrPut(
                key = "a",
                load = loadA,
            )
            val b = cache.getOrPut(
                key = "b",
                load = loadB,
            )
            val aAgain = cache.getOrPut(
                key = "a",
                load = loadA,
            )
            val bAgain = cache.getOrPut(
                key = "b",
                load = loadB,
            )

            // ----- Assert -----
            a shouldBe "value-a"
            b shouldBe "value-b"
            aAgain shouldBe "value-a"
            bAgain shouldBe "value-b"
            aLoadCount.get() shouldBe 1
            bLoadCount.get() shouldBe 1
        }

        @Test
        fun `concurrent callers for the same key collapse into one load invocation`() = runTest {
            // ----- Arrange -----
            val cache = SessionValueCache<String, String>()
            val loadCount = AtomicInteger(0)
            val readyToProceed = CompletableDeferred<Unit>()
            val load: suspend () -> String = {
                loadCount.incrementAndGet()
                readyToProceed.await()
                "shared-value"
            }

            // ----- Act -----
            val first = launch { cache.getOrPut(
                key = "key",
                load = load,
            ) }
            val second = launch { cache.getOrPut(
                key = "key",
                load = load,
            ) }
            readyToProceed.complete(Unit)
            first.join()
            second.join()
            val resultAfterBothFinished = cache.getOrPut(key = "key") { error("must not load again") }

            // ----- Assert -----
            loadCount.get() shouldBe 1
            resultAfterBothFinished shouldBe "shared-value"
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        @Test
        fun `concurrent loads for different keys do not block each other`() = runTest {
            // ----- Arrange -----
            val cache = SessionValueCache<String, String>()
            val loadA: suspend () -> String = {
                delay(10_000)
                "value-a"
            }
            val loadB: suspend () -> String = { "value-b" }

            // ----- Act -----
            val aJob = launch {
                cache.getOrPut(
                    key = "a",
                    load = loadA,
                )
            }
            runCurrent()
            val bResult = cache.getOrPut(
                key = "b",
                load = loadB,
            )

            // ----- Assert -----
            // b resolves at virtual time 0, while a is still suspended in its 10s delay - a
            // class-wide lock held across load() would have forced b to wait for that delay too.
            bResult shouldBe "value-b"
            currentTime shouldBe 0L
            aJob.isActive shouldBe true

            advanceTimeBy(10_000)
            aJob.join()
            currentTime shouldBe 10_000L
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        @Test
        fun `a failed load releases the key so a concurrent caller can retry and succeed`() = runTest {
            // ----- Arrange -----
            val cache = SessionValueCache<String, String>()
            val loadCount = AtomicInteger(0)
            val loadError = RuntimeException("load failed")
            val readyToFail = CompletableDeferred<Unit>()
            val load: suspend () -> String = {
                val attempt = loadCount.incrementAndGet()
                if (attempt == 1) {
                    readyToFail.await()
                    throw loadError
                }
                "recovered"
            }

            // ----- Act -----
            val first = launch {
                runCatching {
                    cache.getOrPut(
                        key = "key",
                        load = load,
                    )
                }
            }
            // Drive `first` up to the point where it holds the key's lock and is suspended inside
            // load(), so `second` deterministically queues behind it rather than racing for the lock.
            runCurrent()
            val second = async {
                cache.getOrPut(
                    key = "key",
                    load = load,
                )
            }
            readyToFail.complete(Unit)
            first.join()
            val secondResult = second.await()
            val laterResult = cache.getOrPut(key = "key") { error("must not load again") }

            // ----- Assert -----
            loadCount.get() shouldBe 2
            secondResult shouldBe "recovered"
            laterResult shouldBe "recovered"
        }
    }

    @Nested
    inner class RefreshKeyed {
        @Test
        fun `refresh loads even when a value is already cached and getOrPut then returns the new value`() = runTest {
            // ----- Arrange -----
            val cache = SessionValueCache<String, String>()
            cache.getOrPut(key = "key") { "old-value" }
            val refreshLoadCount = AtomicInteger(0)

            // ----- Act -----
            val refreshed = cache.refresh(key = "key") {
                refreshLoadCount.incrementAndGet()
                "new-value"
            }
            val afterRefresh = cache.getOrPut(key = "key") { error("must not load again") }

            // ----- Assert -----
            refreshLoadCount.get() shouldBe 1
            refreshed shouldBe "new-value"
            afterRefresh shouldBe "new-value"
        }

        @Test
        fun `a failed refresh leaves the previously cached value intact and does not trigger a reload`() = runTest {
            // ----- Arrange -----
            val cache = SessionValueCache<String, String>()
            cache.getOrPut(key = "key") { "old-value" }
            val refreshError = RuntimeException("refresh failed")

            // ----- Act -----
            val thrown = runCatching { cache.refresh(key = "key") { throw refreshError } }.exceptionOrNull()
            val afterFailedRefresh = cache.getOrPut(key = "key") { error("must not load again") }

            // ----- Assert -----
            thrown shouldBe refreshError
            afterFailedRefresh shouldBe "old-value"
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        @Test
        fun `refresh and a concurrent getOrPut for the same key share the lock and do not both load`() = runTest {
            // ----- Arrange -----
            val cache = SessionValueCache<String, String>()
            val refreshLoadCount = AtomicInteger(0)
            val getOrPutLoadCount = AtomicInteger(0)
            val readyToProceed = CompletableDeferred<Unit>()
            val refreshLoad: suspend () -> String = {
                refreshLoadCount.incrementAndGet()
                readyToProceed.await()
                "refreshed-value"
            }
            val getOrPutLoad: suspend () -> String = {
                getOrPutLoadCount.incrementAndGet()
                "should-not-be-used"
            }

            // ----- Act -----
            val refreshJob = launch {
                cache.refresh(
                    key = "key",
                    load = refreshLoad,
                )
            }
            // Drive `refreshJob` up to the point where it holds the key's lock and is suspended
            // inside load(), so the getOrPut below deterministically queues behind it.
            runCurrent()
            val getOrPutJob = async {
                cache.getOrPut(
                    key = "key",
                    load = getOrPutLoad,
                )
            }
            readyToProceed.complete(Unit)
            refreshJob.join()
            val getOrPutResult = getOrPutJob.await()

            // ----- Assert -----
            refreshLoadCount.get() shouldBe 1
            getOrPutLoadCount.get() shouldBe 0
            getOrPutResult shouldBe "refreshed-value"
        }

        @Test
        fun `refresh on one key leaves another key's cached value untouched`() = runTest {
            // ----- Arrange -----
            val cache = SessionValueCache<String, String>()
            cache.getOrPut(key = "other") { "other-value" }

            // ----- Act -----
            val refreshed = cache.refresh(key = "key") { "refreshed-value" }
            val otherAfterRefresh = cache.getOrPut(key = "other") { error("must not load again") }

            // ----- Assert -----
            refreshed shouldBe "refreshed-value"
            otherAfterRefresh shouldBe "other-value"
        }
    }

    @Nested
    inner class GetOrPutImplicitSlot {
        @Test
        fun `second call does not invoke load again`() = runTest {
            // ----- Arrange -----
            val cache = SessionValueCache<Unit, String>()
            val loadCount = AtomicInteger(0)
            val load: suspend () -> String = {
                loadCount.incrementAndGet()
                "value"
            }

            // ----- Act -----
            cache.getOrPut(load)
            cache.getOrPut(load)

            // ----- Assert -----
            loadCount.get() shouldBe 1
        }

        @Test
        fun `load throwing propagates and a later call retries and can succeed`() = runTest {
            // ----- Arrange -----
            val cache = SessionValueCache<Unit, String>()
            val loadCount = AtomicInteger(0)
            val error = RuntimeException("load failed")
            val load: suspend () -> String = {
                val attempt = loadCount.incrementAndGet()
                if (attempt == 1) throw error
                "recovered"
            }
            val thrown = runCatching { cache.getOrPut(load) }.exceptionOrNull()

            // ----- Act -----
            val result = cache.getOrPut(load)

            // ----- Assert -----
            thrown shouldBe error
            result shouldBe "recovered"
            loadCount.get() shouldBe 2
        }
    }
}
