# Code Style Guide

This project follows the [official Kotlin code style](https://kotlinlang.org/docs/coding-conventions.html) as declared in `gradle.properties` (`kotlin.code.style=official`).

## Naming Conventions

### Files and Classes

Files are named in **PascalCase**, matching their primary class.

| Type | Convention | Example |
|------|-----------|---------|
| Domain models | Plain nouns | `Book`, `Author`, `UserBook` |
| Data entities | `*Entity` suffix | `BookEntity`, `UserBookEntity` |
| Data sources | `*DataSource` / `*DataSourceImpl` (both in the same file, named after the interface) | `BookRemoteDataSource`, `BookLocalDataSourceImpl` |
| Repositories | `*Repository` / `*RepositoryImpl` | `BookRepository`, `BookRepositoryImpl` |
| Use cases | `*UseCase` | `SearchForNameUseCase`, `MarkBookAsReadUseCase` |
| Screens | `*Screen` | `SearchScreen`, `LibraryScreen` |
| Screen models | `*ScreenModel` | `SearchScreenModel`, `LibraryScreenModel` |
| ViewModels | `*ViewModel` | `MainViewModel` |
| Actions | `*Action` | `OnQueryChangeAction`, `OnAddBookToLibraryClickAction` |
| Events | `*Event` | `SearchScreenEvent`, `BookDetailEvent` |
| UI state | `*UiState` | `SearchScreenUiState`, `LibraryUiState` |
| Local variables | `*LocalVariables` | `SearchLocalVariables` |
| Dependencies | `*Dependencies` | `SearchDependencies`, `LibraryDependencies` |
| Mappers | `*Mapper` | `BookMapper`, `EditionMapper` |
| DI modules | `*Module` | `SearchModule`, `BooksModule` |

### Functions and Variables

- Functions and variables use **camelCase**.
- Functions are action-oriented: `searchForName`, `markBookAsWantToRead`, `getBookById`.
- Boolean variables/properties are prefixed with `is`/`has`: `isLoading`, `isAuthenticated`.

## Project Structure

### Feature Organization

Every feature follows the `data` / `domain` / `presentation` layer split. See [ARCHITECTURE.md](ARCHITECTURE.md) for the full directory layout.

### Data Source Interface and Implementation Colocation

A data source interface and its implementation live in the **same file**, named after the interface (e.g. `BookRemoteDataSource.kt` contains both `BookRemoteDataSource` and `BookRemoteDataSourceImpl`). Do not split them into separate files.

### Koin Module per Feature

Each feature has its own Koin DI module in a `di/` subdirectory, keeping dependency declarations close to the code they serve.

## Compose

- Screens are top-level `@Composable` functions, not classes.
- Reusable UI components live in `core/presentation/component/`.
- Custom modifiers live in `core/presentation/modifier/`.
- Material 3 theming is applied via the shared `core/presentation/theme/` setup.

## Error Handling

- Use Kotlin's `Result<T>` with `.onSuccess()` / `.onFailure()` for operations that can fail.
- Apollo network calls use `safeQuery()` and `safeMutation()` extension functions.
- Apollo errors are wrapped in `RuntimeException` with descriptive messages.
- Use `Timber` for logging errors — never `println` or `Log.*`.

## Code Organization Within Files

- Use **region comments** to group related code sections:
  ```kotlin
  // region Search Logic
  ...
  // endregion
  ```
- `// region` and `// endregion` lines sit flush against the code they wrap — do **not** add a blank line directly before or after them.
- Keep the code self-documenting. Prefer descriptive naming over inline comments.
- Separate logical sections with blank lines.

## Test Class Organization

Group all tests targeting a single function of the unit under test inside a JUnit 5 `@Nested inner class`. This replaces the use of `// region` / `// endregion` blocks inside test files — region comments are for production code only.

- Each nested class targets exactly **one** function (or property) on the unit under test.
- Name the nested class in **PascalCase**, matching the function name (e.g. `initializeBooks` → `inner class InitializeBooks`). For property wiring tests, use the property name (e.g. `books` → `inner class Books`).
- Annotate it with `@Nested` and declare it as `inner class` so it has access to the outer class's mocks, `@BeforeEach` setup, and helper functions.
- The outer test class holds shared mocks, the `@BeforeEach` setup, and any private helper functions (e.g. `stubBook`). Do **not** redeclare these inside the nested classes.
- Every test must live inside a `@Nested inner class` — do not place `@Test` functions directly on the outer class. If a function has only a single test case, that test still goes inside its own `@Nested` block for consistency.

Example:

```kotlin
class BooksRepositoryImplTest {

    private lateinit var booksRemoteDataSource: BooksRemoteDataSource
    private lateinit var booksLocalDataSource: BooksLocalDataSource
    private lateinit var repository: BooksRepositoryImpl

    @BeforeEach
    fun setUp() {
        booksRemoteDataSource = mockk()
        booksLocalDataSource = mockk()
        repository = BooksRepositoryImpl(
            booksRemoteDataSource = booksRemoteDataSource,
            booksLocalDataSource = booksLocalDataSource,
        )
    }

    @Nested
    inner class InitializeBooks {

        @Test
        fun `caches fetched books and lists on first call`() = runTest {
            // ----- Arrange -----
            ...
        }
    }

    @Nested
    inner class RefreshUserBooks {

        @Test
        fun `does not reset the session flag`() = runTest {
            // ----- Arrange -----
            ...
        }
    }
}
```

## Unit Test Structure

Unit tests follow the **Arrange-Act-Assert (AAA)** principle. Each test is visually divided into three sections using the following comment format:

```kotlin
@Test
fun `does the thing`() = runTest {
    // ----- Arrange -----
    val input = stubInput()

    coEvery {
        repository.fetch()
    } returns input

    // ----- Act -----
    val result = useCase()

    // ----- Assert -----
    result.isSuccess shouldBe true
    result.getOrNull() shouldBe input
}
```

- Use `// ----- Arrange -----`, `// ----- Act -----`, and `// ----- Assert -----` exactly as shown — five dashes on each side, capitalized section name in the middle.
- Every unit test must contain all three markers, in order, even if a section only holds a single line.
- Leave a blank line **before** each marker (except the very first marker, which directly follows the function's opening `{`). The body of each section starts on the line directly under its marker — no blank line between the marker and its first statement.
- When the act and the assertion are the same expression — for example, asserting on a `Flow` inside a Turbine `test { }` block, or wrapping a call in `shouldThrow { ... }` — collapse the two sections into a single `// ----- Act & Assert -----` marker instead of repeating the call. Use this only when there is no separable "act" step.
- These AAA markers are **not** Kotlin region blocks (`// region` / `// endregion`); the no-blank-line rule for actual region blocks does not apply to them.

## Code Block Whitespace

**Core principle: every multi-line construct acts as a *paragraph* — it gets a blank line before and after it.**

### Opening and closing braces

- **No blank line after an opening `{`.** Exception: sealed class / sealed interface bodies have a blank line after `{`.
- **No blank line before a closing `}`.**
- Leave a blank line immediately **after the closing `}`** of a code block (unless it is the very last line of its enclosing block).

### Paragraph rule for multi-line constructs

Every multi-line construct (code block, multi-line call, multi-line assignment) gets a blank line before and after it. This includes:

- `val`/`var` assignments where the right-hand side spans multiple lines (e.g. `if/else`, `when`, multi-line lambda, multi-line constructor/function call).
- A single-line `val` followed by a multi-line `val` needs a blank line between them.
- mockk's `coEvery { ... }` / `every { ... }` stubs are never one-liners — always open the block onto its own line and leave a blank line after each stub's closing `}`.

### Consecutive single-line statements

- Blank line **between logically unrelated groups**.
- **No blank line within a related group** (e.g. related property declarations, consecutive guard clauses bodies that belong to one extraction).

### Guard clauses

- Blank line **after** a `val`/`var` extraction before the first guard clause.
- Blank line **between** each guard clause.
- Blank line **after** the last guard clause before the main logic.
- Every guard clause is its own paragraph.
- This applies to **every** form of safeguard, including single-line ones that return a value pre-emptively (e.g. `if (ids.isEmpty()) return emptyList()`, `val x = foo ?: return null`, `val x = foo ?: return@map other`). Whether the guard exits with `return`, `return <value>`, `return@label`, `throw`, or `continue`, a blank line always follows it before the next statement.

### `if` statements that consume an extracted variable

- When an `if` statement references a `val`/`var` declared on the line directly above it, leave a blank line **between** the assignment and the `if`. The extraction is its own paragraph; the `if` that consumes it begins a new one. This applies to both single-line and multi-line `if` bodies, and whether the `if` is a guard clause or branching logic.

### Between declarations

- **Functions / methods**: always one blank line between them, in both classes and interfaces.
- **Interface members**: always one blank line between method signatures.
- **Sealed class variants**: always one blank line between variants.
- **Data class properties**: no blank lines between properties. DTO properties annotated with `@SerializedName` (or similar): blank line between each property — each is a 2-line construct.
- **Enum entries**: no blank lines between entries. One blank line before `companion object`.
- **Property groups in classes**: no blank lines within a logical group; one blank line between different groups.

### `when` expressions

- `when` inside `onEvent()`-style dispatchers: single-line branches grouped without blanks; blank line before the first block-body branch; blank lines between all block-body branches.
- `when` where **all** branches have block bodies: blank lines between each branch.

### Coroutine launches

- Blank lines between sequential `launch { }` blocks (e.g. in `init`).

### Super calls and error logs

- Blank line between a `super.*()` inheritance call (e.g. `super.onResume()`, `super.onCreate(...)`) and the following code.
- Blank line between a `Timber.e(...)` error log and the following code.

```kotlin
class Example {
    fun doWork() {
        coEvery {
            repository.fetch()
        } returns value

        every {
            mapper.map(any())
        } returns mapped

        val result = useCase()
    }
}
```

## Argument and Property Layout

Whenever a function declaration, function call, constructor invocation, or **object/data-class instantiation** passes **more than one argument or property**, every argument/property must sit on its own line. Single-argument forms stay inline.

- One-argument calls and declarations stay on a single line.
- As soon as a **second** argument or property is added, all of them break onto their own lines: the opening `(` stays on the same line as the name, the closing `)` sits on its own line, each argument is followed by a comma, and the **last argument also has a trailing comma**.
- This applies equally to:
  - function declarations,
  - function call sites,
  - constructor invocations,
  - data-class instantiations (including `Book(...)`, `BookEdition(...)`, etc.),
  - `apply { ... }` / `copy(...)` invocations on data classes,
  - any other parenthesised parameter list with two or more entries.
- A trailing lambda (Kotlin's `fn(arg) { ... }` form) is **not** counted as an argument for this rule. `runTest(dispatcher) { ... }` stays on one line because it has a single value argument plus a trailing lambda.

```kotlin
// Single argument — inline.
booksRepository.fetchBookById(id = bookId)

// Function declaration with two arguments — one per line, trailing comma, closing paren on its own line.
fun updateBookProgress(
    book: Book,
    newPage: Int,
): Book

// Function call with two arguments.
booksRemoteDataSource.updateBookProgress(
    book = book,
    newPage = newPage,
)

// Data-class instantiation with two or more properties — one property per line.
Book(
    id = id,
    title = title,
    rating = rating,
)

// Single-property data class — still inline.
Author(name = name)
```

## Import Ordering

Follow this order:

1. Android / AndroidX imports
2. Third-party libraries (Koin, Apollo, Compose, Timber, etc.)
3. Project-specific imports

Remove unused imports.

### No fully-qualified type references

Never reference a type by its fully-qualified name inline (e.g. `androidx.compose.foundation.layout.RowScope.() -> Unit`, `kotlinx.coroutines.test.TestScope`, `java.io.File`). Always add an `import` for the type and use its short name at the call/declaration site. This applies to production and test code, including parameter types, return types, generic arguments, receiver types on lambdas, and `mockk()` type witnesses.

```kotlin
// Bad
additionalActions: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit = {},

// Good
import androidx.compose.foundation.layout.RowScope

additionalActions: @Composable RowScope.() -> Unit = {},
```

## Data Flow

- UI state is always exposed as `StateFlow` (immutable from the UI's perspective).
- One-time events (navigation, toasts) are sent via `Channel`.
- Repository data is exposed as `Flow` and collected in Initializers.
- Actions execute on the `Main` dispatcher; network/DB operations run on `IO`.

## Dependencies

- Dependency versions are managed centrally in `gradle/libs.versions.toml`.
- Reference dependencies via the version catalog (`libs.<alias>`) in `build.gradle.kts`.
