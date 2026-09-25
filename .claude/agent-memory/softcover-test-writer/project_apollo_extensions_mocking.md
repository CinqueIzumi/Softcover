---
name: project_apollo_extensions_mocking
description: Mocking the ApolloClient.query().fetchPolicy().toFlow() chain for core/network ApolloExtensions tests, and testing the private retryTransientFailures helper through safeQuery/safeMutation.
metadata:
  type: project
---

Applies to tests of `core/network/.../helper/ApolloExtensions.kt` itself (`SafeQueryFlowTest.kt` is the
reference). Tests of data sources mock `safeQuery`/`safeMutation` instead — see [[feedback_no_mock_servers]].

## Mocking the call chain

1. `mockk<ApolloClient>()` and `mockk<ApolloCall<T>>()` both work despite private/internal constructors.
2. `every { apolloClient.query(any<MyQuery>()) } returns apolloCall`.
3. `mockkStatic("com.apollographql.cache.normalized.FetchPoliciesKt")` — `fetchPolicy` is a static
   extension in the `com.apollographql.cache:normalized-cache` library (not the old
   `com.apollographql.apollo.cache.normalized.NormalizedCache`), then
   `every { apolloCall.fetchPolicy(any()) } returns apolloCall`.
4. `every { apolloCall.toFlow() } returns flowOf(response1, ...)`.

Build responses with `ApolloResponse.Builder(operation = query, requestUuid = uuid4()).data(data).build()`
(or `.exception(ex)`), and HTTP errors with
`ApolloHttpException(statusCode = 401, headers = emptyList(), body = Buffer(), message = "HTTP 401")`.
`SessionExpiredNotifier`, `UserMessageNotifier` and `NetworkAvailability` are plain `object`s:
`mockkObject(...)` then stub.

## Testing the private retry helper

`retryTransientFailures` is `private`; drive it through its public callers:

- `safeQuery` (default `FetchPolicy.NetworkOnly` → retries on) with `every { apolloCall.toFlow() } returnsMany listOf(failFlow, successFlow)`.
  Count attempts with `verify(exactly = N) { apolloCall.toFlow() }` — N = 1 + `MAX_TRANSIENT_RETRIES` (3) = 4 at the bound.
- "OfflineException is not retried": pass `FetchPolicy.CacheFirst` (bypasses the early offline guard),
  stub `NetworkAvailability.isOnline() returns false`, return an `ApolloNetworkException` response.
  `OfflineException` is a sibling of `ServerUnavailableException` under `RetryableSyncException`, so the
  helper's catch misses it and it surfaces on attempt one.
- `Retry-After` precedence: build the exception with `headers = listOf(HttpHeader("Retry-After", "5"))` and
  assert `testScheduler.currentTime` advanced by 5_000 rather than the 1_000 backoff.
- `safeMutation`'s opt-out: mock `apolloMutationCall.execute()` to *return* a response with
  `.exception(httpException(429))` (Apollo's `execute()` returns, never throws, for a single exception
  response) and `coVerify(exactly = 1) { apolloMutationCall.execute() }`.

`io.kotest.assertions.throwables.shouldThrow` resolves in every `core:*` module transitively via
`kotest-assertions-shared-jvm`; no dependency needed.
