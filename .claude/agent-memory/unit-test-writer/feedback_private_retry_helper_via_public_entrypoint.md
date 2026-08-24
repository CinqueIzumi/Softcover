---
name: private-retry-helper-via-public-entrypoint
description: How to test a private suspend retry/backoff helper in core/network's ApolloExtensions.kt indirectly via safeQuery/safeMutation, plus kotest shouldThrow availability
metadata:
  type: project
---

`retryTransientFailures` in `core/network/src/commonMain/kotlin/nl/rhaydus/softcover/core/network/helper/ApolloExtensions.kt`
is `private` — test it through its public callers, not directly:

- `safeQuery` (default `fetchPolicy = FetchPolicy.NetworkOnly` → `retryOnTransientFailure = true`) is the
  cleanest vehicle: it's a plain suspend fn (not a multi-emission flow like `safeQueryFlow`), so mocking
  `apolloCall.toFlow()` with `every { ... } returnsMany listOf(failFlow, successFlow)` lets one test cover
  "retried then succeeds" in one assertion on the returned value.
- Count retries by `verify(exactly = N) { apolloCall.toFlow() }` — N = 1 + `MAX_TRANSIENT_RETRIES` (3) = 4
  for "retries stop at the bound".
- Test "OfflineException is not retried" by passing `fetchPolicy = FetchPolicy.CacheFirst` (so
  `requireNetwork = false` and the early explicit offline guard in `executeCall` is bypassed), stubbing
  `NetworkAvailability.isOnline() returns false`, and returning an `ApolloNetworkException` response —
  `retryableTransportFailureOrNull` then throws `OfflineException`, which is a sibling of
  `ServerUnavailableException` (both extend sealed `RetryableSyncException`), so `retryTransientFailures`'s
  `catch (exception: ServerUnavailableException)` does not catch it — it surfaces on the first attempt.
- Test the `Retry-After` header precedence by asserting `testScheduler.currentTime` inside `runTest`
  advanced by the header's value (e.g. `5_000L`) rather than the computed exponential backoff (`1_000L`
  for the first retry) — build the `ApolloHttpException` with
  `headers = listOf(HttpHeader("Retry-After", "5"))` (`com.apollographql.apollo.api.http.HttpHeader(name,
  value)`).
- Test `safeMutation`'s retry opt-out by mocking `apolloMutationCall.execute()` directly (not `.toFlow()`)
  to `return` (not throw) an `ApolloResponse` with `.exception(httpException(429))` set — Apollo's
  `ApolloCall.execute()` never throws for a single exception-response, it returns it with `.exception`
  populated (see `singleSuccessOrException` in apollo-runtime sources) — then assert
  `coVerify(exactly = 1) { apolloMutationCall.execute() }` to prove no retry happened.

`io.kotest.assertions.throwables.shouldThrow` (used to collapse Act & Assert per the AAA marker
convention) is NOT in `kotest-assertions-core` itself but is pulled in transitively via
`kotest-assertions-shared-jvm` — it resolves fine in every `core:*` module without adding a dependency;
confirmed already in use in `core/personal`'s `ReadingJournalHistoryRemoteDataSourceImplTest.kt`.

**Why:** Written for the hotfix adding `retryTransientFailures`/`RateLimitInterceptor` rate-limit
infrastructure (2026-08-23). The function has no test seam of its own since it's private, so the public
surface (`safeQuery`/`safeMutation`) is the only way in — mirroring the existing `SafeQueryFlowTest.kt`
mocking conventions (`mockkObject(NetworkAvailability)`, `mockkStatic(".../FetchPoliciesKt")`,
`ApolloResponse.Builder(operation, requestUuid = uuid4())`).

**How to apply:** Any future retry/backoff logic added to `ApolloExtensions.kt` as a private helper should
be tested the same way — via whichever public `safe*` function wires `retryOnTransientFailure` to it,
never by trying to make the helper itself visible to tests.
