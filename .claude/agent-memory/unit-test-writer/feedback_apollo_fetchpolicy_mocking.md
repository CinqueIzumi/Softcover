---
name: feedback_apollo_fetchpolicy_mocking
description: How to mock the ApolloClient.query().fetchPolicy().toFlow() chain in safeQueryFlow tests
metadata:
  type: feedback
---

To test `safeQueryFlow` (which calls `apolloClient.query(q).fetchPolicy(fp).toFlow()`):

1. `mockk<ApolloClient>()` — MockK can mock this despite its private constructor
2. `mockk<ApolloCall<T>>()` — same; `ApolloCall` has `internal constructor` but MockK handles it
3. Stub `apolloClient.query(any<MyQuery>()) returns apolloCall`
4. `mockkStatic("com.apollographql.apollo.cache.normalized.NormalizedCache")` — `fetchPolicy` is a top-level extension in `ClientCacheExtensions.kt` with `@file:JvmName("NormalizedCache")`, so the JVM class is `com.apollographql.apollo.cache.normalized.NormalizedCache`
5. Stub `every { apolloCall.fetchPolicy(any()) } returns apolloCall`
6. Stub `every { apolloCall.toFlow() } returns flowOf(response1, response2, ...)`

Build `ApolloResponse<T>` via the public 2-param constructor:
```kotlin
ApolloResponse.Builder(operation = query, requestUuid = uuid4()).data(data).build()
ApolloResponse.Builder(operation = query, requestUuid = uuid4()).exception(ex).build()
```

Build `ApolloHttpException` as:
```kotlin
ApolloHttpException(statusCode = 401, headers = emptyList(), body = Buffer(), message = "HTTP 401")
```

`SessionExpiredNotifier` and `UserMessageNotifier` are plain `object`s — use `mockkObject(...)` then stub `every { SessionExpiredNotifier.notifySessionExpired() } returns Unit`.

`NetworkAvailability.isOnline()` is also a plain object method — `mockkObject(NetworkAvailability)` then `every { NetworkAvailability.isOnline() } returns false/true`.

**Why:** The extension `fetchPolicy` in normalized-cache cannot be mocked with `every { apolloCall.fetchPolicy(...) }` without `mockkStatic` because it compiles as a static JVM method, not a virtual dispatch.
