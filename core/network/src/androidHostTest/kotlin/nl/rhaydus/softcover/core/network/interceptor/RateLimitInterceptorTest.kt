package nl.rhaydus.softcover.core.network.interceptor

import com.apollographql.apollo.api.ApolloRequest
import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.CompiledField
import com.apollographql.apollo.api.CompiledSelection
import com.apollographql.apollo.api.CompiledType
import com.apollographql.apollo.api.CustomScalarType
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.http.HttpHeader
import com.apollographql.apollo.exception.ApolloHttpException
import com.apollographql.apollo.interceptor.ApolloInterceptorChain
import com.apollographql.apollo.network.http.HttpInfo
import com.benasher44.uuid.uuid4
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.time.Duration.Companion.seconds
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.GetUserBookListsQuery
import nl.rhaydus.softcover.core.network.helper.HTTP_TOO_MANY_REQUESTS

/**
 * All tests below drive [RateLimitInterceptor] with `refillTokensPerSecond = 1.0` unless noted
 * otherwise, so every wait duration in an assertion is directly in seconds. Cold-start credit is a
 * fixed [nl.rhaydus.softcover.core.network.interceptor] implementation detail
 * (`UNCONFIRMED_TOKEN_ALLOWANCE = 2.0`), not `fallbackBucketSize` — see [ServerBudgetAdoption].
 */
class RateLimitInterceptorTest {
    private lateinit var chain: ApolloInterceptorChain

    private val baseRequest: ApolloRequest<GetUserBookListsQuery.Data> =
        ApolloRequest.Builder(GetUserBookListsQuery()).build()

    @BeforeEach
    fun setUp() {
        chain = mockk()

        @Suppress("UNCHECKED_CAST")
        every {
            chain.proceed(any<ApolloRequest<GetUserBookListsQuery.Data>>())
        } returns emptyFlow()
    }

    private fun buildInterceptor(
        fallbackBucketSize: Int,
        refillTokensPerSecond: Double,
        scheduler: TestCoroutineScheduler,
    ): RateLimitInterceptor = RateLimitInterceptor(
        fallbackBucketSize = fallbackBucketSize,
        refillTokensPerSecond = refillTokensPerSecond,
        timeSource = SchedulerTimeSource(scheduler),
    )

    // Marker Data type for the fake operations built below — intercept() never reads response
    // data, only rootField(), so no real generated *.Data class is needed.
    private object FakeOperationData : Operation.Data

    private val fakeFieldType: CompiledType = CustomScalarType(
        name = "String",
        className = "kotlin.String",
    )

    /**
     * Builds a request whose operation has one root field per [fieldNames] entry — this is the
     * shape [UpdateListBookPositions] has with its aliased `clear` + `apply` root mutation fields,
     * built by hand here since only [Operation.rootField] needs to be real for `rateLimitCost` to
     * see it.
     */
    private fun requestWithRootFields(vararg fieldNames: String): ApolloRequest<FakeOperationData> {
        val rootSelections: List<CompiledSelection> = fieldNames.map { fieldName ->
            CompiledField.Builder(
                name = fieldName,
                type = fakeFieldType,
            ).build()
        }
        val rootField = CompiledField.Builder(
            name = "query",
            type = fakeFieldType,
        ).selections(rootSelections).build()
        val operation = mockk<Operation<FakeOperationData>>()

        every {
            operation.rootField()
        } returns rootField

        return ApolloRequest.Builder(operation).build()
    }

    private fun rateLimitHeaders(
        limit: Int? = null,
        remaining: Int? = null,
        retryAfterSeconds: Long? = null,
    ): List<HttpHeader> = listOfNotNull(
        limit?.let { HttpHeader(
            "X-Ratelimit-Limit",
            it.toString(),
        ) },
        remaining?.let { HttpHeader(
            "X-Ratelimit-Remaining",
            it.toString(),
        ) },
        retryAfterSeconds?.let { HttpHeader(
            "Retry-After",
            it.toString(),
        ) },
    )

    @Suppress("DEPRECATION")
    private fun successResponseWithHeaders(headers: List<HttpHeader>): ApolloResponse<GetUserBookListsQuery.Data> {
        val httpInfo = HttpInfo(
            startMillis = 0L,
            endMillis = 0L,
            statusCode = 200,
            headers = headers,
        )

        return ApolloResponse.Builder(
            baseRequest.operation,
            uuid4(),
        )
            .addExecutionContext(httpInfo)
            .build()
    }

    private fun tooManyRequestsResponse(headers: List<HttpHeader>): ApolloResponse<GetUserBookListsQuery.Data> =
        ApolloResponse.Builder(
            baseRequest.operation,
            uuid4(),
        )
            .exception(
                ApolloHttpException(
                    statusCode = HTTP_TOO_MANY_REQUESTS,
                    headers = headers,
                    body = null,
                    message = "Too Many Requests",
                ),
            ).build()

    /**
     * Raises the ceiling to [ceiling] via a response carrying only `X-Ratelimit-Limit`, then advances
     * virtual time far enough for natural refill to saturate `availableTokens` at that new ceiling.
     *
     * A response's `remaining` is deliberately not used to seed a high credit directly here: whether it
     * raises the local estimate depends on `inFlight` (see [ServerBudgetAdoption]), which this helper's
     * single sequential seed request would trivially satisfy anyway — saturating via refill instead
     * keeps this helper's result independent of that mechanism, so tests that exercise it aren't
     * accidentally relying on the seed step too. Assumes `refillTokensPerSecond = 1.0`.
     */
    private suspend fun TestScope.seedCeilingAndSaturate(
        interceptor: RateLimitInterceptor,
        ceiling: Int,
    ) {
        every {
            chain.proceed(any<ApolloRequest<GetUserBookListsQuery.Data>>())
        } returns flowOf(successResponseWithHeaders(rateLimitHeaders(limit = ceiling)))

        interceptor.intercept(
            requestWithRootFields("seed"),
            chain,
        ).collect {}

        every {
            chain.proceed(any<ApolloRequest<GetUserBookListsQuery.Data>>())
        } returns emptyFlow()

        advanceTimeBy(ceiling.seconds + 5.seconds)
    }

    @Nested
    inner class Intercept {
        @Test
        fun `tokens refill up to but never beyond the fallback ceiling before any server response arrives`() =
            runTest {
                // ----- Arrange -----
                val interceptor = buildInterceptor(
                    fallbackBucketSize = 3,
                    refillTokensPerSecond = 1.0,
                    scheduler = testScheduler,
                )
                interceptor.intercept(
                    baseRequest,
                    chain,
                ).collect {} // cold-start credit: 2 -> 1
                advanceTimeBy(10.seconds)

                // ----- Act -----
                interceptor.intercept(
                    baseRequest,
                    chain,
                ).collect {} // refill caps at fallbackBucketSize=3
                interceptor.intercept(
                    baseRequest,
                    chain,
                ).collect {}
                val timeAfterTheTwoCappedTokens = testScheduler.currentTime
                interceptor.intercept(
                    baseRequest,
                    chain,
                ).collect {}

                // ----- Assert -----
                timeAfterTheTwoCappedTokens shouldBe 10_000L
                testScheduler.currentTime shouldBe 11_000L
            }

        @Test
        fun `concurrent acquirers are serialised without losing or double-granting tokens`() = runTest {
            // ----- Arrange -----
            val interceptor = buildInterceptor(
                fallbackBucketSize = 2,
                refillTokensPerSecond = 1.0,
                scheduler = testScheduler,
            )
            val completionTimes = mutableListOf<Long>()

            // ----- Act -----
            val jobs = List(4) {
                launch {
                    interceptor.intercept(
                        baseRequest,
                        chain,
                    ).collect {}
                    completionTimes += testScheduler.currentTime
                }
            }
            advanceUntilIdle()
            jobs.forEach { it.join() }

            // ----- Assert -----
            completionTimes.sorted() shouldBe listOf(0L, 1_000L, 2_000L, 3_000L)
        }
    }

    @Nested
    inner class RootFieldCost {
        @Test
        fun `a two-root-field operation debits two tokens per call`() = runTest {
            // ----- Arrange -----
            val interceptor = buildInterceptor(
                fallbackBucketSize = 10,
                refillTokensPerSecond = 1.0,
                scheduler = testScheduler,
            )
            seedCeilingAndSaturate(
                interceptor,
                ceiling = 5,
            )
            val baselineTime = testScheduler.currentTime
            val clearAndApply = requestWithRootFields(
                "clear",
                "apply",
            )

            // ----- Act — under the old per-request accounting all three calls would pass free -----
            interceptor.intercept(
                clearAndApply,
                chain,
            ).collect {}
            interceptor.intercept(
                clearAndApply,
                chain,
            ).collect {}
            val timeAfterTwoTwoCostCalls = testScheduler.currentTime
            interceptor.intercept(
                clearAndApply,
                chain,
            ).collect {}

            // ----- Assert -----
            timeAfterTwoTwoCostCalls shouldBe baselineTime
            testScheduler.currentTime shouldBe baselineTime + 2_000L
        }

        @Test
        fun `mixed costs draw from one shared budget until it is exactly exhausted`() = runTest {
            // ----- Arrange -----
            val interceptor = buildInterceptor(
                fallbackBucketSize = 10,
                refillTokensPerSecond = 1.0,
                scheduler = testScheduler,
            )
            seedCeilingAndSaturate(
                interceptor,
                ceiling = 4,
            )
            val baselineTime = testScheduler.currentTime
            val twoRootFields = requestWithRootFields(
                "clear",
                "apply",
            )
            val oneRootField = requestWithRootFields("books")

            // ----- Act -----
            interceptor.intercept(
                twoRootFields,
                chain,
            ).collect {}
            interceptor.intercept(
                oneRootField,
                chain,
            ).collect {}
            val timeAfterBudgetExhausted = testScheduler.currentTime
            interceptor.intercept(
                oneRootField,
                chain,
            ).collect {}

            // ----- Assert -----
            timeAfterBudgetExhausted shouldBe baselineTime
            testScheduler.currentTime shouldBe baselineTime + 1_000L
        }

        @Test
        fun `a short bucket waits for the full cost, not just one token`() = runTest {
            // ----- Arrange -----
            val interceptor = buildInterceptor(
                fallbackBucketSize = 10,
                refillTokensPerSecond = 1.0,
                scheduler = testScheduler,
            )
            every {
                chain.proceed(any<ApolloRequest<GetUserBookListsQuery.Data>>())
            } returns flowOf(tooManyRequestsResponse(rateLimitHeaders(limit = 5)))
            interceptor.intercept(
                baseRequest,
                chain,
            ).collect {} // zeroes credit, adopts a ceiling of 5
            every {
                chain.proceed(any<ApolloRequest<GetUserBookListsQuery.Data>>())
            } returns emptyFlow()
            val twoRootFields = requestWithRootFields(
                "clear",
                "apply",
            )

            // ----- Act — zero tokens available, a cost-2 call must wait ~3s (cost 2 + the reserve), not ~2s -----
            interceptor.intercept(
                twoRootFields,
                chain,
            ).collect {}

            // ----- Assert -----
            testScheduler.currentTime shouldBe 3_000L
        }

        @Test
        fun `__typename at the root is not billed`() = runTest {
            // ----- Arrange -----
            val interceptor = buildInterceptor(
                fallbackBucketSize = 10,
                refillTokensPerSecond = 1.0,
                scheduler = testScheduler,
            )
            seedCeilingAndSaturate(
                interceptor,
                ceiling = 4,
            )
            val baselineTime = testScheduler.currentTime
            val typenameAndOneRealField = requestWithRootFields(
                "__typename",
                "books",
            )

            // ----- Act — three cost-1 calls fit in four tokens only if __typename is free -----
            interceptor.intercept(
                typenameAndOneRealField,
                chain,
            ).collect {}
            interceptor.intercept(
                typenameAndOneRealField,
                chain,
            ).collect {}
            interceptor.intercept(
                typenameAndOneRealField,
                chain,
            ).collect {}
            val timeAfterThreeSingleCostCalls = testScheduler.currentTime
            interceptor.intercept(
                typenameAndOneRealField,
                chain,
            ).collect {}

            // ----- Assert -----
            timeAfterThreeSingleCostCalls shouldBe baselineTime
            testScheduler.currentTime shouldBe baselineTime + 1_000L
        }

        @Test
        fun `a cost above the ceiling is clamped to the ceiling, dropping the reserve rather than hanging`() =
            runTest {
                // ----- Arrange -----
                val interceptor = buildInterceptor(
                    fallbackBucketSize = 10,
                    refillTokensPerSecond = 1.0,
                    scheduler = testScheduler,
                )
                seedCeilingAndSaturate(
                    interceptor,
                    ceiling = 2,
                )
                val fiveRootFields = requestWithRootFields(
                    "a",
                    "b",
                    "c",
                    "d",
                    "e",
                )
                val timeAfterSeeding = testScheduler.currentTime

                // ----- Act — clamped cost is 2, exactly the ceiling, so this proceeds immediately -----
                interceptor.intercept(
                    fiveRootFields,
                    chain,
                ).collect {}

                // ----- Assert -----
                testScheduler.currentTime shouldBe timeAfterSeeding
            }

        @Test
        fun `an operation with no billable root fields still costs at least one token`() = runTest {
            // ----- Arrange -----
            val interceptor = buildInterceptor(
                fallbackBucketSize = 10,
                refillTokensPerSecond = 1.0,
                scheduler = testScheduler,
            )
            seedCeilingAndSaturate(
                interceptor,
                ceiling = 2,
            )
            val baselineTime = testScheduler.currentTime
            val typenameOnly = requestWithRootFields("__typename")
            interceptor.intercept(
                typenameOnly,
                chain,
            ).collect {}

            // ----- Act — the first call must have spent one of the two available tokens, not zero -----
            interceptor.intercept(
                typenameOnly,
                chain,
            ).collect {}

            // ----- Assert -----
            testScheduler.currentTime shouldBe baselineTime + 1_000L
        }
    }

    @Nested
    inner class ServerBudgetAdoption {
        @Test
        fun `adoption lowers an optimistic local estimate down to the server's reported remaining`() = runTest {
            // ----- Arrange -----
            val interceptor = buildInterceptor(
                fallbackBucketSize = 20,
                refillTokensPerSecond = 1.0,
                scheduler = testScheduler,
            )
            seedCeilingAndSaturate(
                interceptor,
                ceiling = 20,
            ) // a very optimistic local estimate

            every {
                chain.proceed(any<ApolloRequest<GetUserBookListsQuery.Data>>())
            } returns flowOf(successResponseWithHeaders(rateLimitHeaders(
                limit = 5,
                remaining = 4,
            ),),)
            interceptor.intercept(
                baseRequest,
                chain,
            ).collect {} // adopts remaining=4, overwriting the 20
            every {
                chain.proceed(any<ApolloRequest<GetUserBookListsQuery.Data>>())
            } returns emptyFlow()
            val timeAfterAdoption = testScheduler.currentTime

            // ----- Act — exactly 3 more single-cost calls fit in 4 adopted tokens, a 4th must wait -----
            repeat(3) {
                interceptor.intercept(
                    baseRequest,
                    chain,
                ).collect {}
            }
            val timeAfterThreeMoreSingleCostCalls = testScheduler.currentTime
            interceptor.intercept(
                baseRequest,
                chain,
            ).collect {}

            // ----- Assert -----
            timeAfterThreeMoreSingleCostCalls shouldBe timeAfterAdoption
            testScheduler.currentTime shouldBe timeAfterAdoption + 1_000L
        }

        @Test
        fun `a higher remaining does not raise the estimate while another request is still outstanding`() =
            runTest {
                // ----- Arrange -----
                val interceptor = buildInterceptor(
                    fallbackBucketSize = 10,
                    refillTokensPerSecond = 1.0,
                    scheduler = testScheduler,
                )
                seedCeilingAndSaturate(
                    interceptor,
                    ceiling = 4,
                )
                val baselineTime = testScheduler.currentTime

                // Both responses report remaining=3 — higher than the local estimate will be once both
                // requests below have each spent a token (4 - 1 - 1 = 2) — but neither must raise it,
                // since a second request is still outstanding when the first response is adopted.
                val responseA = flow {
                    delay(1.seconds)
                    emit(successResponseWithHeaders(rateLimitHeaders(
                        limit = 4,
                        remaining = 3,
                    ),),)
                }
                val responseB = flow {
                    delay(10.seconds)
                    emit(successResponseWithHeaders(rateLimitHeaders(
                        limit = 4,
                        remaining = 3,
                    ),),)
                }
                every {
                    chain.proceed(any<ApolloRequest<GetUserBookListsQuery.Data>>())
                } returnsMany listOf(responseA, responseB)

                // ----- Act — both acquire instantly (avail 4 -> 2) and go in flight together -----
                val jobA = launch { interceptor.intercept(
                    baseRequest,
                    chain,
                ).collect {} }
                val jobB = launch { interceptor.intercept(
                    baseRequest,
                    chain,
                ).collect {} }
                runCurrent()
                advanceTimeBy(1.seconds)
                runCurrent() // responseA lands and is adopted while B is still outstanding (inFlight == 2)

                every {
                    chain.proceed(any<ApolloRequest<GetUserBookListsQuery.Data>>())
                } returns emptyFlow()

                // ----- Act — 2 cost-1 probes: only 1 passes free if availableTokens stayed at 2, not 3 -----
                interceptor.intercept(
                    baseRequest,
                    chain,
                ).collect {}
                val timeAfterOneProbe = testScheduler.currentTime
                interceptor.intercept(
                    baseRequest,
                    chain,
                ).collect {}

                // ----- Assert -----
                timeAfterOneProbe shouldBe baselineTime + 1_000L
                testScheduler.currentTime shouldBe baselineTime + 2_000L

                jobB.cancel()
                jobA.join()
            }

        @Test
        fun `a higher remaining raises the estimate when no other request is outstanding, unblocking cold start`() =
            runTest {
                // ----- Arrange -----
                val interceptor = buildInterceptor(
                    fallbackBucketSize = 10,
                    refillTokensPerSecond = 1.0,
                    scheduler = testScheduler,
                )
                every {
                    chain.proceed(any<ApolloRequest<GetUserBookListsQuery.Data>>())
                } returns flowOf(successResponseWithHeaders(rateLimitHeaders(
                    limit = 10,
                    remaining = 8,
                ),),)
                // Cold start: exactly one request can go before any response arrives. It is also the
                // only one outstanding, so its response's higher remaining=8 must be trusted upward.
                interceptor.intercept(
                    baseRequest,
                    chain,
                ).collect {}
                every {
                    chain.proceed(any<ApolloRequest<GetUserBookListsQuery.Data>>())
                } returns emptyFlow()
                val baselineTime = testScheduler.currentTime

                // ----- Act — a follow-up request must proceed without waiting for refill, proving
                // ----- availableTokens rose to 8, not the pre-adoption 1 -----
                interceptor.intercept(
                    baseRequest,
                    chain,
                ).collect {}

                // ----- Assert -----
                testScheduler.currentTime shouldBe baselineTime
            }

        @Test
        fun `a lower remaining is adopted even while another request is still outstanding`() = runTest {
            // ----- Arrange -----
            val interceptor = buildInterceptor(
                fallbackBucketSize = 10,
                refillTokensPerSecond = 1.0,
                scheduler = testScheduler,
            )
            seedCeilingAndSaturate(
                interceptor,
                ceiling = 8,
            )
            val baselineTime = testScheduler.currentTime

            val responseA = flow {
                delay(1.seconds)
                emit(successResponseWithHeaders(rateLimitHeaders(
                    limit = 8,
                    remaining = 1,
                ),),)
            }
            val responseB = flow {
                delay(10.seconds)
                emit(successResponseWithHeaders(rateLimitHeaders(
                    limit = 8,
                    remaining = 1,
                ),),)
            }
            every {
                chain.proceed(any<ApolloRequest<GetUserBookListsQuery.Data>>())
            } returnsMany listOf(responseA, responseB)

            // ----- Act — both acquire instantly (avail 8 -> 6) and go in flight together -----
            val jobA = launch { interceptor.intercept(
                baseRequest,
                chain,
            ).collect {} }
            val jobB = launch { interceptor.intercept(
                baseRequest,
                chain,
            ).collect {} }
            runCurrent()
            advanceTimeBy(1.seconds)
            runCurrent() // responseA lands and lowers availableTokens to 1 while B is still outstanding

            every {
                chain.proceed(any<ApolloRequest<GetUserBookListsQuery.Data>>())
            } returns emptyFlow()

            // ----- Act — a cost-1 probe must wait, proving availableTokens dropped to 1, not stayed at 6 -----
            interceptor.intercept(
                baseRequest,
                chain,
            ).collect {}

            // ----- Assert -----
            testScheduler.currentTime shouldBe baselineTime + 2_000L

            jobB.cancel()
            jobA.join()
        }

        @Test
        fun `inFlight is decremented even when the response flow fails, so upward adoption still works afterward`() =
            runTest {
                // ----- Arrange -----
                val interceptor = buildInterceptor(
                    fallbackBucketSize = 10,
                    refillTokensPerSecond = 1.0,
                    scheduler = testScheduler,
                )
                seedCeilingAndSaturate(
                    interceptor,
                    ceiling = 4,
                )
                val baselineTime = testScheduler.currentTime

                val failingResponse = flow<ApolloResponse<GetUserBookListsQuery.Data>> {
                    throw RuntimeException("network error")
                }
                every {
                    chain.proceed(any<ApolloRequest<GetUserBookListsQuery.Data>>())
                } returns failingResponse

                // ----- Act — the response flow fails; inFlight must still be decremented back to 0 -----
                runCatching { interceptor.intercept(
                    baseRequest,
                    chain,
                ).collect {} }

                // ----- Act — a solo request with a higher reported remaining must still be trusted
                // ----- upward; if the earlier failure had leaked inFlight, this would wrongly stay
                // ----- untrusted -----
                every {
                    chain.proceed(any<ApolloRequest<GetUserBookListsQuery.Data>>())
                } returns flowOf(successResponseWithHeaders(rateLimitHeaders(
                    limit = 4,
                    remaining = 4,
                ),),)
                interceptor.intercept(
                    baseRequest,
                    chain,
                ).collect {}
                every {
                    chain.proceed(any<ApolloRequest<GetUserBookListsQuery.Data>>())
                } returns emptyFlow()

                // ----- Act — two more cost-1 probes: only possible free if availableTokens rose to 4 -----
                interceptor.intercept(
                    baseRequest,
                    chain,
                ).collect {}
                interceptor.intercept(
                    baseRequest,
                    chain,
                ).collect {}

                // ----- Assert — nothing above ever needed to wait -----
                testScheduler.currentTime shouldBe baselineTime
            }

        @Test
        fun `inFlight is decremented even when the response flow is cancelled, so upward adoption still works afterward`() =
            runTest {
                // ----- Arrange -----
                val interceptor = buildInterceptor(
                    fallbackBucketSize = 10,
                    refillTokensPerSecond = 1.0,
                    scheduler = testScheduler,
                )
                seedCeilingAndSaturate(
                    interceptor,
                    ceiling = 4,
                )
                val baselineTime = testScheduler.currentTime

                val neverCompletingResponse = flow<ApolloResponse<GetUserBookListsQuery.Data>> {
                    delay(100_000.seconds) // never arrives before the job below is cancelled
                }
                every {
                    chain.proceed(any<ApolloRequest<GetUserBookListsQuery.Data>>())
                } returns neverCompletingResponse

                // ----- Act — acquire a token and go in flight, then cancel before any response arrives -----
                val job = launch { interceptor.intercept(
                    baseRequest,
                    chain,
                ).collect {} }
                runCurrent()
                job.cancelAndJoin()

                // ----- Act — a solo request with a higher reported remaining must still be trusted
                // ----- upward; if cancellation had leaked inFlight, this would wrongly stay untrusted -----
                every {
                    chain.proceed(any<ApolloRequest<GetUserBookListsQuery.Data>>())
                } returns flowOf(successResponseWithHeaders(rateLimitHeaders(
                    limit = 4,
                    remaining = 4,
                ),),)
                interceptor.intercept(
                    baseRequest,
                    chain,
                ).collect {}
                every {
                    chain.proceed(any<ApolloRequest<GetUserBookListsQuery.Data>>())
                } returns emptyFlow()

                // ----- Act — two more cost-1 probes: only possible free if availableTokens rose to 4 -----
                interceptor.intercept(
                    baseRequest,
                    chain,
                ).collect {}
                interceptor.intercept(
                    baseRequest,
                    chain,
                ).collect {}

                // ----- Assert — nothing above ever needed to wait -----
                testScheduler.currentTime shouldBe baselineTime
            }

        @Test
        fun `adoption raises the ceiling so refill can accumulate beyond the fallback size`() = runTest {
            // ----- Arrange -----
            val interceptor = buildInterceptor(
                fallbackBucketSize = 5,
                refillTokensPerSecond = 1.0,
                scheduler = testScheduler,
            )
            every {
                chain.proceed(any<ApolloRequest<GetUserBookListsQuery.Data>>())
            } returns flowOf(successResponseWithHeaders(rateLimitHeaders(limit = 10)))
            interceptor.intercept(
                baseRequest,
                chain,
            ).collect {} // raises the ceiling to 10
            every {
                chain.proceed(any<ApolloRequest<GetUserBookListsQuery.Data>>())
            } returns emptyFlow()
            advanceTimeBy(20.seconds)

            // ----- Act — refill saturates at the new ceiling of 10, well past the fallback of 5 -----
            repeat(9) {
                interceptor.intercept(
                    baseRequest,
                    chain,
                ).collect {}
            }
            val timeAfterNineFreeCalls = testScheduler.currentTime
            interceptor.intercept(
                baseRequest,
                chain,
            ).collect {}

            // ----- Assert -----
            timeAfterNineFreeCalls shouldBe 20_000L
            testScheduler.currentTime shouldBe 21_000L
        }

        @Test
        fun `a successful response carrying Retry-After does not block subsequent requests`() = runTest {
            // ----- Arrange -----
            val interceptor = buildInterceptor(
                fallbackBucketSize = 10,
                refillTokensPerSecond = 1.0,
                scheduler = testScheduler,
            )
            seedCeilingAndSaturate(
                interceptor,
                ceiling = 9,
            ) // headroom so only a hold could delay the next call
            val baselineTime = testScheduler.currentTime

            every {
                chain.proceed(any<ApolloRequest<GetUserBookListsQuery.Data>>())
            } returns flowOf(successResponseWithHeaders(rateLimitHeaders(
                limit = 9,
                remaining = 9,
                retryAfterSeconds = 5,
            ),),)
            interceptor.intercept(
                baseRequest,
                chain,
            ).collect {} // 200 with Retry-After: 5 — must not hold
            every {
                chain.proceed(any<ApolloRequest<GetUserBookListsQuery.Data>>())
            } returns emptyFlow()

            // ----- Act — a hold would block this for 5s; it must instead proceed immediately -----
            interceptor.intercept(
                baseRequest,
                chain,
            ).collect {}

            // ----- Assert -----
            testScheduler.currentTime shouldBe baselineTime
        }

        @Test
        fun `a 429 zeroes available credit even without a Retry-After header`() = runTest {
            // ----- Arrange -----
            val interceptor = buildInterceptor(
                fallbackBucketSize = 10,
                refillTokensPerSecond = 1.0,
                scheduler = testScheduler,
            )
            seedCeilingAndSaturate(
                interceptor,
                ceiling = 10,
            )
            val baselineTime = testScheduler.currentTime

            every {
                chain.proceed(any<ApolloRequest<GetUserBookListsQuery.Data>>())
            } returns flowOf(tooManyRequestsResponse(rateLimitHeaders(
                limit = 10,
                remaining = 8,
            ),),)
            interceptor.intercept(
                baseRequest,
                chain,
            ).collect {} // 429, no Retry-After: zeroes credit, sets no hold
            every {
                chain.proceed(any<ApolloRequest<GetUserBookListsQuery.Data>>())
            } returns emptyFlow()

            // ----- Act — with 8 tokens left over unzeroed this would pass free; it must instead wait for refill -----
            interceptor.intercept(
                baseRequest,
                chain,
            ).collect {}

            // ----- Assert -----
            testScheduler.currentTime shouldBe baselineTime + 2_000L
        }

        @Test
        fun `a 429 with Retry-After blocks all acquisition until it elapses`() = runTest {
            // ----- Arrange -----
            val interceptor = buildInterceptor(
                fallbackBucketSize = 5,
                refillTokensPerSecond = 5.0,
                scheduler = testScheduler,
            )
            every {
                chain.proceed(any<ApolloRequest<GetUserBookListsQuery.Data>>())
            } returns flowOf(
                tooManyRequestsResponse(rateLimitHeaders(
                    limit = 5,
                    remaining = 3,
                    retryAfterSeconds = 1,
                ),),
            )

            // ----- Act — the first call is the one that receives the 429 and its hold -----
            interceptor.intercept(
                baseRequest,
                chain,
            ).collect {}
            every {
                chain.proceed(any<ApolloRequest<GetUserBookListsQuery.Data>>())
            } returns emptyFlow()

            // ----- Act — the very next acquisition must sit out the full Retry-After before proceeding -----
            interceptor.intercept(
                baseRequest,
                chain,
            ).collect {}

            // ----- Assert -----
            testScheduler.currentTime shouldBe 1_000L
        }

        @Test
        fun `the server's reserve floor makes a cost-1 request wait when remaining is exactly one`() = runTest {
            // ----- Arrange -----
            val interceptor = buildInterceptor(
                fallbackBucketSize = 10,
                refillTokensPerSecond = 1.0,
                scheduler = testScheduler,
            )
            seedCeilingAndSaturate(
                interceptor,
                ceiling = 10,
            )
            val baselineTime = testScheduler.currentTime

            every {
                chain.proceed(any<ApolloRequest<GetUserBookListsQuery.Data>>())
            } returns flowOf(successResponseWithHeaders(rateLimitHeaders(
                limit = 10,
                remaining = 1,
            ),),)
            // This is the exact scenario that produced the real 429: the server reports one token left.
            interceptor.intercept(
                baseRequest,
                chain,
            ).collect {}
            every {
                chain.proceed(any<ApolloRequest<GetUserBookListsQuery.Data>>())
            } returns emptyFlow()

            // ----- Act -----
            interceptor.intercept(
                baseRequest,
                chain,
            ).collect {}

            // ----- Assert -----
            testScheduler.currentTime shouldBe baselineTime + 1_000L
        }

        @Test
        fun `a ceiling of one still lets a cost-1 request complete instead of hanging on an unsatisfiable reserve`() =
            runTest {
                // ----- Arrange -----
                val interceptor = buildInterceptor(
                    fallbackBucketSize = 10,
                    refillTokensPerSecond = 1.0,
                    scheduler = testScheduler,
                )
                every {
                    chain.proceed(any<ApolloRequest<GetUserBookListsQuery.Data>>())
                } returns flowOf(tooManyRequestsResponse(rateLimitHeaders(limit = 1)))
                // Cold-start credit covers this one; the response then drops the ceiling to 1 and zeroes credit.
                interceptor.intercept(
                    baseRequest,
                    chain,
                ).collect {}
                every {
                    chain.proceed(any<ApolloRequest<GetUserBookListsQuery.Data>>())
                } returns emptyFlow()

                // ----- Act — cost(1) + RESERVED_TOKENS(1) can never fit under a ceiling of 1, so the reserve
                // ----- must be dropped, or this would wait forever -----
                interceptor.intercept(
                    baseRequest,
                    chain,
                ).collect {}

                // ----- Assert — it completes, not just "is delayed" -----
                testScheduler.currentTime shouldBe 1_000L
            }

        @Test
        fun `a server-reported limit of 0 still lets a cost-1 request complete instead of hanging`() = runTest {
            // ----- Arrange -----
            val interceptor = buildInterceptor(
                fallbackBucketSize = 10,
                refillTokensPerSecond = 1.0,
                scheduler = testScheduler,
            )
            every {
                chain.proceed(any<ApolloRequest<GetUserBookListsQuery.Data>>())
            } returns flowOf(tooManyRequestsResponse(rateLimitHeaders(limit = 0)))
            // Cold-start credit covers this one; the response then floors the ceiling at 1 (not 0) and
            // zeroes credit — a ceiling of 0 would leave refill unable to reach even a 1-token cost.
            interceptor.intercept(
                baseRequest,
                chain,
            ).collect {}
            every {
                chain.proceed(any<ApolloRequest<GetUserBookListsQuery.Data>>())
            } returns emptyFlow()

            // ----- Act — must complete, not hang, exactly as with a reported ceiling of 1 -----
            interceptor.intercept(
                baseRequest,
                chain,
            ).collect {}

            // ----- Assert — it completes, not just "is delayed" -----
            testScheduler.currentTime shouldBe 1_000L
        }

        @Test
        fun `cold start allows exactly one free request — the unconfirmed allowance net of the reserve, not fallbackBucketSize`() =
            runTest {
                // ----- Arrange -----
                val interceptor = buildInterceptor(
                    fallbackBucketSize = 10,
                    refillTokensPerSecond = 1.0,
                    scheduler = testScheduler,
                )

                // ----- Act — no response has arrived yet, so this is pure cold-start local accounting -----
                interceptor.intercept(
                    baseRequest,
                    chain,
                ).collect {}
                val timeAfterOneFreeCall = testScheduler.currentTime

                // ----- Act — a second call must wait: fallbackBucketSize (10) was never the free
                // ----- allowance, and UNCONFIRMED_TOKEN_ALLOWANCE (2) net of RESERVED_TOKENS (1) only
                // ----- ever covers exactly one request before the server has said anything -----
                interceptor.intercept(
                    baseRequest,
                    chain,
                ).collect {}

                // ----- Assert -----
                timeAfterOneFreeCall shouldBe 0L
                testScheduler.currentTime shouldBe 1_000L
            }
    }

    @Nested
    inner class Construction {
        @Test
        fun `a zero refillTokensPerSecond is rejected at construction`() {
            shouldThrow<IllegalArgumentException> {
                buildInterceptor(
                    fallbackBucketSize = 10,
                    refillTokensPerSecond = 0.0,
                    scheduler = TestCoroutineScheduler(),
                )
            }
        }

        @Test
        fun `a negative refillTokensPerSecond is rejected at construction`() {
            shouldThrow<IllegalArgumentException> {
                buildInterceptor(
                    fallbackBucketSize = 10,
                    refillTokensPerSecond = -1.0,
                    scheduler = TestCoroutineScheduler(),
                )
            }
        }
    }
}
