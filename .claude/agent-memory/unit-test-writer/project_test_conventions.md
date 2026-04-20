---
name: Softcover Test Conventions
description: Testing framework, style rules, AAA markers, mockk patterns, and directory layout for Softcover unit tests
type: project
---

Test stack: JUnit 5 (`org.junit.jupiter.api.Test`, `@BeforeEach`), mockk, kotest assertions (`io.kotest.matchers.shouldBe`), kotlinx-coroutines-test (`runTest`), Turbine (`app.cash.turbine.test`) for Flow assertions.

**Test class organization:** Every `@Test` must live inside a `@Nested inner class` whose name is the PascalCase version of the function/property under test (e.g. `initializeBooks` → `InitializeBooks`, `books` property → `Books`). The outer class holds shared mocks, `@BeforeEach`, and private helper functions — never duplicated inside nested classes. Requires `import org.junit.jupiter.api.Nested`. Region comments (`// region` / `// endregion`) are NOT used in test files — `@Nested` replaces them.

AAA marker format (exact, non-negotiable):
```
// ----- Arrange -----
// ----- Act -----
// ----- Assert -----
```
Five dashes on each side, capitalized name. All three must appear in every `@Test`, in order, unless act and assert are the same expression (Turbine `test { }`, `shouldThrow { }`) — then collapse to `// ----- Act & Assert -----`. Blank line before each marker except the very first (which follows `{` directly — no blank line at top of function body). No blank line after a marker — first body statement sits directly underneath.

`coEvery`/`every` stubs are always 3-line blocks (never one-liners). Blank line after each stub's closing `}`.

Domain models (`Book`, `UserBook`, `BookList`, `ListBook`, `BookEdition`, etc.) have many required constructor args — always use `mockk { every { ... } returns ... }` stubs rather than instantiating real data classes.

Test file location mirrors source: `app/src/test/java/nl/rhaydus/softcover/feature/<feature>/...`

Canonical example tests (paths relative to the project root):
- `app/src/test/java/nl/rhaydus/softcover/feature/books/domain/usecase/FetchBookByIdUseCaseTest.kt`
- `app/src/test/java/nl/rhaydus/softcover/feature/books/data/repository/BooksRepositoryImplTest.kt`

**TOAD action testing pattern:** `ActionScope` is a concrete class — construct it with real `MutableStateFlow`s and a `Channel`, hold the `stateFlow` reference, assert on `stateFlow.value` after `execute()`. `BookDetailDependencies` (and other `*Dependencies` classes) extend the abstract `ActionDependencies` which has a concrete `launch` method. When mocking dependencies with `mockk(relaxed = true)`, the `launch` method is also mocked (does nothing). Fix: add `every { mock.launch(any()) } answers { callOriginal() }` to make the real `launch` execute using the mocked `coroutineScope` + `mainDispatcher`. Use `UnconfinedTestDispatcher(testScope.testScheduler)` as `mainDispatcher` and the `runTest` scope as `coroutineScope` so launched coroutines run eagerly. Note: `UnconfinedTestDispatcher` triggers an `ExperimentalCoroutinesApi` compiler warning — this is acceptable and matches the existing codebase pattern (no `@OptIn` annotation used anywhere in test files).

**mockkStatic generic function stub disambiguation:** When the same mocked static function is stubbed twice with different generic type parameters (e.g. `apolloClient.safeQuery(query = any<GetUserBooksQuery>())` AND `safeQuery(query = any<GetBooksByIdsQuery>())`), mockk's `any<T>()` matchers are identical at runtime due to type erasure — the last-registered stub wins for ALL calls. Fix: use typed lambda matchers with explicit type argument: `match<Query<GetUserBooksQuery.Data>> { it is GetUserBooksQuery }` and `match<Query<GetBooksByIdsQuery.Data>> { it is GetBooksByIdsQuery }`. These force an `instanceof` check at runtime. Also apply this pattern to `coVerify` blocks.

**mockk property access inside `every {}` blocks:** Accessing properties of another mock inside an `every {}` recording block captures the default mock value (e.g. `0` for Int, `""` for String) instead of the stubbed value, because mockk is in recording mode. Always extract mock property values into local `val`s BEFORE the `every {}` block and use those vals inside the matcher arguments.

**@Nested class name vs. imported type name clash:** When a `@Nested inner class` is named after a function/property whose name collides with an imported type (e.g. inner class `DateStyle` shadowing the imported `DateStyle` enum), usages of that type inside the nested class must be fully-qualified (`nl.rhaydus.softcover.feature.settings.domain.model.DateStyle.MONTH_DAY_YEAR`). This is the one permitted exception to the "no FQN inline" style rule — the nested class name takes precedence because the naming convention is non-negotiable. No import alias is used anywhere in the project for this purpose.

**Why:** CODE_STYLE_GUIDE.md and CLAUDE.md mandate these rules; violations cause style inconsistency across the codebase.

**How to apply:** Every new test file in this project must follow these conventions exactly, regardless of test complexity.
