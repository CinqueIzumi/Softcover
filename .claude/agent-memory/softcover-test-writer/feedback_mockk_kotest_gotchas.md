---
name: feedback_mockk_kotest_gotchas
description: MockK/kotest/JUnit traps that cost a compile or red-suite cycle — default-param stubs, generic erasure, recording-mode reads, relaxed lambdas, stateful stores, capture import, === precedence, test-name chars.
metadata:
  type: feedback
---

- **An omitted trailing default param pins it to the default.** `coEvery { mock(any(), any(), any()) }`
  on a 4-param function with a default last param matches only calls where that param equals its default.
  A test passing a non-default value (e.g. `actionAt`) falls through to the relaxed mock's untyped return
  and fails with a confusing `ClassCastException`. Add a local `coEvery` with an explicit matcher for that
  param.
- **Generic `any<T>()` matchers collide under erasure.** Two `safeQuery(query = any<AQuery>())` /
  `any<BQuery>()` stubs are identical at runtime; the last wins. Use
  `match<Query<AQuery.Data>> { it is AQuery }` in both `coEvery` and `coVerify`.
- **Reading another mock's property inside `every {}` records a default** (`0`, `""`). Extract it to a
  local `val` before the block.
- **A relaxed mock never invokes a lambda argument.** E.g. `offlineSync.drainAndReconcile { initializeBooks(...) }`
  in `BooksRepositoryImplTest`: add
  `coEvery { offlineSync.drainAndReconcile(any()) } coAnswers { firstArg<suspend () -> List<Book>>().invoke() }`
  wherever the test relies on the lambda running.
- **Read-after-write against a keyed store** (`SecureStorage.read/write/delete`): back the stubs with a
  class-level `var stored: String?` (`read` returns it, `write` sets `secondArg()`, `delete` nulls it),
  reset it in `@BeforeEach`, and seed it directly in Arrange.
- **`import io.mockk.capture` does not resolve.** Import only `io.mockk.slot`; `capture(slot)` resolves
  inside `every {}` / `coEvery {}`.
- **Parenthesise `===` before `shouldBe`:** `(a === b) shouldBe true`. Infix functions bind tighter than
  equality, so `a === b shouldBe true` asserts `b shouldBe true`.
- **Backtick test names can't contain `:` `/` `<` `>` `.` `[` `]`** ("illegal characters"). Use an
  em-dash or plain words.
