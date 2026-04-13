# Code Style Guide

This project follows the [official Kotlin code style](https://kotlinlang.org/docs/coding-conventions.html) as declared in `gradle.properties` (`kotlin.code.style=official`).

## Naming Conventions

### Files and Classes

Files are named in **PascalCase**, matching their primary class.

| Type | Convention | Example |
|------|-----------|---------|
| Domain models | Plain nouns | `Book`, `Author`, `UserBook` |
| Data entities | `*Entity` suffix | `BookEntity`, `UserBookEntity` |
| Data sources | `*DataSource` / `*DataSourceImpl` | `BookRemoteDataSource`, `BookLocalDataSourceImpl` |
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

- Leave a blank line immediately **after the opening `{`** of a class, object, or init block.
- Do **not** add a blank line at the top of a function body — the first statement should follow the opening `{` directly.
- Leave a blank line immediately **after the closing `}`** of a code block (unless it is the very last line of its enclosing block).
- mockk's `coEvery { ... }` and `every { ... }` stubs are never one-liners. Always open the block onto its own line so the body sits on a separate line, and leave a blank line after the closing `}` of each stub.

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

## Data Flow

- UI state is always exposed as `StateFlow` (immutable from the UI's perspective).
- One-time events (navigation, toasts) are sent via `Channel`.
- Repository data is exposed as `Flow` and collected in Initializers.
- Actions execute on the `Main` dispatcher; network/DB operations run on `IO`.

## Dependencies

- Dependency versions are managed centrally in `gradle/libs.versions.toml`.
- Reference dependencies via the version catalog (`libs.<alias>`) in `build.gradle.kts`.
