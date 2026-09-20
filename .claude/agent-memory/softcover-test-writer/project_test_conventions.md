---
name: project_test_conventions
description: Softcover test stack and the non-obvious conventions not spelled out in the code-style docs — mockk domain models, the TOAD action launch workaround, the @Nested name-clash FQN exception, UserBook.status.
metadata:
  type: project
---

Stack: JUnit 5, MockK, kotest assertions (`shouldBe`), kotlinx-coroutines-test (`runTest`), Turbine.
`@Nested` structure, AAA markers (incl. the `// ----- Act & Assert -----` collapse) and stub layout are in
`docs/rhaydus/0.3.1/code-style.md` §Test Class Organization / §Unit Test Structure — follow those.
Tests live in each module's `src/androidHostTest/kotlin/...`, mirroring the source package.

- **Mock domain models, don't construct them.** `Book`, `UserBook`, `BookList`, `ListBook`, `BookEdition`
  etc. have many required args — use `mockk { every { ... } returns ... }`.
- **TOAD action tests:** `ActionScope` is concrete — build it with real `MutableStateFlow`s and a `Channel`
  and assert on `stateFlow.value` after `execute()`. A `mockk(relaxed = true)` `*Dependencies` also mocks
  the concrete `ActionDependencies.launch`, so launched work never runs; add
  `every { mock.launch(any()) } answers { callOriginal() }`, with `UnconfinedTestDispatcher(testScope.testScheduler)`
  as `mainDispatcher` and the `runTest` scope as `coroutineScope`.
- **`@Nested` class name vs. imported type clash:** when a nested class is named after a member that
  collides with an imported type (inner class `DateStyle` vs. the `DateStyle` enum), fully-qualify the type
  inside that class. This is the one sanctioned exception to "no inline FQN"; no import alias is used.
- **`UserBook.status` is `BookStatus`, not `UserBookStatus`:** stub with
  `every { status } returns BookStatus.getFromCode(userBookStatus.code)` (codes match).

See [[project_canonical_test_templates]] for the per-layer file to clone and
[[feedback_mockk_kotest_gotchas]] for MockK traps.
