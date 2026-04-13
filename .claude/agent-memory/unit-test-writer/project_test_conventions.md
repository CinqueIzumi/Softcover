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

**Why:** CODE_STYLE_GUIDE.md and CLAUDE.md mandate these rules; violations cause style inconsistency across the codebase.

**How to apply:** Every new test file in this project must follow these conventions exactly, regardless of test complexity.
