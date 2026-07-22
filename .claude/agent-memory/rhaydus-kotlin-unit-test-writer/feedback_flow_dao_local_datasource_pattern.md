---
name: feedback_flow_dao_local_datasource_pattern
description: How this project's DAO-backed LocalDataSourceImpl tests handle Flow-returning methods (turbine + flowOf, no custom TestDispatcher needed)
metadata:
  type: feedback
---

For a `*LocalDataSourceImpl` that just delegates a `Flow`-returning method straight to a Room DAO
(e.g. `dao.observe(userId) -> Flow<List<Entity>>`), the project convention (see
`DismissedContinueSeriesLocalDataSourceImplTest.kt`, and now
`UserTagVocabularyLocalDataSourceImplTest.kt` in `feature/book_detail`) is:

- Mock the DAO with `mockk(relaxed = true)`.
- Stub the flow with plain `every { dao.observe(...) } returns flowOf(entities)` — no
  `TestDispatcher`/`setMain` needed, because `flowOf` is a cold, synchronously-completing flow with
  no dispatcher of its own, so there's no scheduler mismatch to worry about (contrast with
  [[feedback_coroutine_safe_tests]], which is about mismatched *real* dispatchers, e.g. Room's
  `queryContext`).
- Assert with `app.cash.turbine.test { awaitItem() shouldBe expected; awaitComplete() }` inside
  `runTest`.
- For suspend delegate methods (`upsertAll`, `replaceAll`), use `coVerify` / `coVerifyOrder` — e.g.
  `replaceAll` must verify `dao.clearForUser(userId)` then `dao.upsertAll(entities)` in that order.

**Why:** keeps these tests dead simple; reaching for a shared `TestDispatcher` here would be
over-engineering for a flow that never touches a real dispatcher.

**How to apply:** any new `*LocalDataSourceImpl` test that just forwards a DAO `Flow` method.
