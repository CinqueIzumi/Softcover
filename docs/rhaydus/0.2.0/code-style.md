# Code Style Guide

This is the canonical, app-agnostic code-style guide for `nl.rhaydus` Kotlin/Compose projects.
All such projects follow the [official Kotlin code style](https://kotlinlang.org/docs/coding-conventions.html)
as declared in `gradle.properties` (`kotlin.code.style=official`). Conventions specific to the
TOAD presentation architecture live alongside this guide in the project's architecture docs; this
guide covers everything else.

## Tooling

Mechanizable style is enforced by the shared custom ktlint ruleset published as
`nl.rhaydus:ktlint-rules` (package `nl.rhaydus.ktlint`). Run it via `./gradlew ktlintFormat` to
auto-fix, and `./gradlew ktlintCheck` to verify. The ruleset gates the uniformly-internal
visibility categories and other mechanical rules described below. It is deliberately conservative:
the remaining cases (cross-module mappers, shared components, anything subtler) are caught in
review.

## Naming Conventions

### Files and Classes

Files are named in **PascalCase**, matching their primary class.

| Type | Convention | Example |
|------|-----------|---------|
| Domain models | Plain nouns | `AccountState`, `Author`, `DeviceCodeResponse` |
| Data entities | `*Entity` suffix | `AccountEntity` |
| Data sources | `*DataSource` / `*DataSourceImpl` (both in one file, named after the interface) | `RemoteDataSource`, `LocalDataSourceImpl` |
| Repositories | `*Repository` / `*RepositoryImpl` | `AccountRepository`, `AccountRepositoryImpl` |
| Use cases | `*UseCase` | `MarkContentAsReadUseCase`, `SearchForNameUseCase` |
| Screens | `*Screen` | `ProfileScreen`, `HomeScreen` |
| Screen models | `*ScreenModel` | `ProfileScreenModel` |
| ViewModels | `*ViewModel` | `MainViewModel` |
| Actions | `*Action` (one per file) | `StartLinkAction`, `OnQueryChangeAction` |
| Events | `*Event` | `ProfileEvent`, `SearchEvent` |
| UI state | `*UiState` | `ProfileUiState`, `SearchUiState` |
| Local variables | `*LocalVariables` | `ProfileLocalVariables` |
| Dependencies | `*Dependencies` | `ProfileDependencies` |
| Mappers | `*Mapper` | `AccountMapper` |
| DI modules | `*Module` | `ProfileModule`, `authModule` |

### Functions and Variables

- Functions and variables use **camelCase**; functions are action-oriented (`startDeviceFlow`,
  `getToken`, `searchForName`, `markAsRead`).
- Boolean variables/properties are prefixed with `is`/`has` (`isLoading`, `isActive`,
  `isAuthenticated`).
- **Injected use cases keep their full `*UseCase` name** as the property/parameter, e.g.
  `val startLinkUseCase: StartLinkUseCase`, never abbreviated to `startLink`.

## Project Structure

- Every feature follows the `data` / `domain` / `presentation` layer split. See the project's
  architecture docs for the full directory layout.
- A data source interface and its implementation live in the **same file**, named after the
  interface (e.g. `RemoteDataSource.kt` contains both `RemoteDataSource` and
  `RemoteDataSourceImpl`). Do not split them into separate files.
- Each feature has its own Koin DI module in a `di/` subdirectory, keeping dependency declarations
  close to the code they serve.

## One declaration per file

- **One data class per file**, named after the class. This applies to models, DTOs, snapshots,
  small result holders, wrappers, everything. Never declare a `data class` locally inside a
  function, method, or `init` block, and never colocate multiple unrelated `data class`
  declarations in a shared file. A single-caller data class still gets its own file next to that
  caller.
- **One enum per file**, named after the enum. Attach labels, icons, colors, and other display data
  as constructor properties on the enum (`status.label`) rather than re-deriving them in a `when` /
  `if` at each call site. This prevents drift when a new entry is added.
- **One action per file** - `<Name>Action.kt` holds only the `sealed interface`; each concrete
  `data object`/`data class` action lives in its own file implementing it and carries the `Action`
  suffix. Concrete events likewise carry the `Event` suffix; the same per-suffix rule applies
  across the naming table above (`*UiState`, `*UseCase`, `*Dependencies`, etc.).
- **Exception - sealed hierarchy variants.** The `data class` / `data object` variants of a
  `sealed interface` / `sealed class` MAY be co-located inside the sealed type's own file (named
  after the sealed type), for cohesion and the `SealedType.Variant` namespacing at call sites.
  Splitting the variants one-per-file (each variant a top-level subtype in its own file) is equally
  acceptable. Both keep exhaustive `when` support, since a sealed type's permitted subtypes can live
  in separate files of the same package. Pick whichever reads better for the hierarchy; do **not**
  treat a co-located sealed-variant `data class` as a violation of the one-data-class-per-file rule.

```kotlin
// Good - label lives on the enum.
enum class DeadlineStatus(val label: String) {
    OnTrack(label = "On track"),
    Behind(label = "Behind"),
    Expired(label = "Expired"),
}

Text(text = status.label)

// Bad - label duplicated at every call site.
val label = when (status) {
    DeadlineStatus.OnTrack -> "On track"
    DeadlineStatus.Behind -> "Behind"
    DeadlineStatus.Expired -> "Expired"
}
```

```kotlin
// Good - HoursMinutesSeconds.kt
data class HoursMinutesSeconds(
    val hours: Int,
    val minutes: Int,
    val seconds: Int,
)

// Bad - declared locally inside a function.
fun onLaunch(...) {
    data class ProgressSnapshot(val itemId: Int?, val total: Int?)
    ...
}

// Bad - second data class tacked onto a file alongside helper functions.
fun secondsToHm(seconds: Int): String { ... }
data class HoursMinutesSeconds(...)
```

## Comments

**The default is no comment.** Code is kept self-documenting through descriptive names and small,
obvious functions; a comment is the exception that has to justify its own existence, not a habit. Before
writing one, try to make the code say it instead (a clearer name, an extracted function, a named
constant). Most comments that get written are noise and should simply be deleted.

A comment earns its place only when the code genuinely cannot carry the meaning on its own:

- a **non-obvious edge case** (e.g. why a value must be created on every recomposition, a first-frame
  guard, an off-by-one boundary),
- a **workaround or gotcha** (why the non-obvious API was chosen over the obvious one, a framework
  footgun, a deliberate `runCatching` swallow),
- a **rationale that is not derivable from the code** (why a magic constant has exactly this value, a
  spec or design requirement the code satisfies).

**Never narrate.** A comment that restates what the next line plainly does ("opens the sheet", "loops
over items", "the back button") adds nothing and must not be written. If a comment only describes
*what* the code does rather than *why*, delete it.

**KDoc is documentation, not narration.** A `/** ... */` doc comment on the public, cross-module surface
of a shared module (the published API: components, modifiers, contracts) is expected and welcome -- it is
the consumer's documentation, not an inline aside. It should describe the contract and the *why*, never
walk through the implementation. Private members lean on their names; give a private member KDoc only
when it clears the same bar as any other comment above.

## Code Organization Within Files

- Use **region comments** to group related code sections in production code:
  ```kotlin
  // region Search Logic
  ...
  // endregion
  ```
- `// region` and `// endregion` lines sit flush against the code they wrap. Do **not** add a blank
  line directly before or after them.
- Separate logical sections with blank lines.
- Region comments are for production code only; in test files, `@Nested inner class` grouping
  replaces them (see *Test Class Organization*).

## Boolean Negation

Never use the `!` prefix operator for boolean negation - always use `.not()`. This applies to all
booleans: local variables, properties, function call results, and complex expressions. The rule does
not apply to the non-null assertion operator `!!`, which stays as-is where needed (though it should
be rare).

```kotlin
// Good
if (isLoading.not()) { ... }
if (state.hasFocus.not()) { ... }
(items.isEmpty()).not()

// Bad
if (!isLoading) { ... }
if (!state.hasFocus) { ... }
!items.isEmpty()
```

## If / Else

- A single-line `if` / `else` expression (one where the whole statement fits on one line) may omit
  braces: `val x = if (a) b else c`.
- As soon as the `if` / `else` breaks across multiple lines, whether because the condition, a branch
  body, or the combined length exceeds one line, **every branch must use an explicit `{ ... }`
  block**. Never mix a braced branch with a brace-less branch, and never span a brace-less branch
  across multiple lines.

```kotlin
// Good - single line, no braces needed.
val dayLabel = if (daysRemaining == 1L) "day" else "days"

// Good - multi-line, both branches use blocks.
val message = if (progress.isOnTrack) {
    "You're on track."
} else {
    "You're ${progress.pagesBehindSchedule} pages behind schedule."
}

// Bad - brace-less branch spans multiple lines.
val message = if (progress.isOnTrack) "You're on track."
    else "You're ${progress.pagesBehindSchedule} pages behind schedule."
```

## Visibility

The build is a fully split multi-module graph, so every module is its own compilation unit. **A
top-level declaration that is referenced only within its own module must be `internal`** (or
`private`), never public-by-default. Public is reserved for the deliberate cross-module surface.
Making something `public` is a decision to export it; if no other module imports it, that decision
is wrong.

**Default to `internal` for these (module-internal by nature):**

- **TOAD plumbing** in a feature's `presentation/`: `*ScreenModel`, the sealed `*Action` and every
  subclass, `*UiState`, the `*Event` type and its subclasses, `*Dependencies`, `*LocalVariables`,
  and the `flows/` initializers/collectors. Mark a whole sealed hierarchy consistently (parent +
  all variants).
- **Data-layer implementations**: `*RepositoryImpl`, `*DataSourceImpl`, `*StorageImpl`, `*QueueImpl`,
  DataStore serializers/entities, and `*Mapper` functions/classes used only within their own module.
- **Module-private helpers**: top-level helper functions, extension functions, and `@Composable`
  helper components not referenced from another module.
- **The app shell** (the `Application`, `appModule`, version-provider impl, on which nothing depends)
  and **navigation-host internals** (the navigator impl, root/bottom-bar screens, cross-feature
  use-case impls bound to a domain contract).

**Stays `public` (the cross-module surface):**

- Domain contracts: `*Repository` / `*DataSource` **interfaces**, all `*UseCase` classes.
- Domain models, enums, value types, and sealed result types in the shared domain module (and
  feature-local models exposed through a public use case's signature).
- `*Screen` / `*Tab` navigation classes and navigation contracts.
- The aggregated Koin `val *Module`.
- Shared design-system components/theme/models/TOAD contracts, and any impl that realises a shared
  contract consumed elsewhere.
- A mapper or other helper genuinely called from another module (confirm with a cross-module
  reference before keeping it public).

**Cascade rule.** A `public` member that exposes a now-`internal` type must itself become `internal`
(e.g. a `Screen(state: FooUiState, …)` member composable whose `FooUiState` is internal). The
compiler enforces this; fix it at the member, do not re-widen the type.

This is partly **tool-enforced**: the `nl.rhaydus.ktlint` visibility-modifier rule (run by
`ktlintCheck` / `check`) gates the uniformly-internal categories above (the `*Action` / `*Event` /
`*UiState` / `*LocalVariables` / `*ScreenModel` / `*Dependencies` / `flows` / `*Impl` suffixes in
their packages). The remaining cases (cross-module mappers, shared components, anything subtler) are
caught in review.

> Single-module projects: if a project has not yet split into modules, use `private` for genuinely
> local helpers and keep the public surface intentional. Adopt internal-by-default for the
> categories above the moment the project splits into modules.

## Compose

- Screens follow the project's presentation architecture (Voyager `Screen` objects, or top-level
  `@Composable` functions, per the project). For Voyager screens the core `XScreen(state, runAction)`
  render function is `public` (so `@Preview` can drive it) and every sub-component composable is
  `private`. See the project's TOAD/architecture docs.
- Reusable UI components live in the shared design-system / presentation component package; custom
  modifiers in the modifier package; theming in the theme package.
- **Blank line between sibling composables.** Inside any layout scope (`Column`, `Row`, `Box`, etc.),
  leave a blank line between each child composable call, including `Spacer`. Never stack two
  composable calls back-to-back without a blank line between them.
- **Multi-argument composables break across lines.** Any composable invocation (or any function
  call, see *Argument and Property Layout*) that takes two or more arguments must put each argument
  on its own line with a trailing comma, even for short calls like
  `Text(text = ":", modifier = Modifier.padding(horizontal = 4.dp))`. Single-argument composables
  stay inline.

```kotlin
// Good
Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.Center,
) {
    TimeField(value = hours, ...)

    Text(
        text = ":",
        modifier = Modifier.padding(horizontal = 4.dp),
    )

    TimeField(value = minutes, ...)
}

// Bad - siblings touching, inline multi-arg Text.
Row(...) {
    TimeField(value = hours, ...)
    Text(text = ":", modifier = Modifier.padding(horizontal = 4.dp))
    TimeField(value = minutes, ...)
}
```

## Argument and Property Layout

Whenever a function declaration, function call, constructor invocation, or **object/data-class
instantiation** passes **more than one** argument or property, every argument/property must sit on
its own line. Single-argument forms stay inline.

- One-argument calls and declarations stay on a single line.
- As soon as a **second** argument or property is added, all of them break onto their own lines: the
  opening `(` stays on the same line as the name, the closing `)` sits on its own line, each argument
  is followed by a comma, and the **last argument also has a trailing comma**.
- This applies equally to:
  - function declarations (including abstract/member signatures inside `interface` and
    `abstract class` bodies - each parameter on its own line with a trailing comma, even when there
    is no body),
  - function call sites,
  - constructor invocations,
  - class, `sealed class`, and `sealed interface` declarations: both the primary constructor
    parameter list and the superclass/supertype invocation (`: Parent(arg1, arg2)`) must break onto
    one argument per line as soon as there are two or more,
  - data-class instantiations,
  - `apply { ... }` / `copy(...)` invocations on data classes,
  - any other parenthesised parameter list with two or more entries.
- A trailing lambda (Kotlin's `fn(arg) { ... }` form) is **not** counted as an argument for this
  rule. `runTest(dispatcher) { ... }` stays on one line because it has a single value argument plus a
  trailing lambda.

**Property/parameter annotations go on their own line**, above the declaration, never inline. When a
class's properties carry such annotations, separate **every** property in that list with a blank line
(each is now a multi-line construct), including any unannotated ones mixed in.

```kotlin
// Single argument - inline.
repository.fetchById(id = itemId)

// Function declaration with two arguments - one per line, trailing comma, closing paren on its own line.
fun updateProgress(
    item: Item,
    newPage: Int,
): Item

// Function call with two arguments.
remoteDataSource.updateProgress(
    item = item,
    newPage = newPage,
)

// Data-class instantiation with two or more properties - one property per line.
Item(
    id = id,
    title = title,
    rating = rating,
)

// Single-property data class - still inline.
Author(name = name)

// Interface member with two or more parameters - one per line, trailing comma.
interface ItemDao {
    suspend fun setStatusEnabled(
        code: Int,
        enabled: Boolean,
    )
}

// Sealed class with a multi-argument primary constructor, and subclasses whose
// supertype invocation also takes multiple arguments - both lists break apart.
sealed class LibraryTab(
    val id: String,
    val label: String,
) {
    data object All : LibraryTab(
        id = "all",
        label = "All",
    )

    data class CustomList(
        val listId: Int,
        val listName: String,
    ) : LibraryTab(
        id = "list-$listId",
        label = listName,
    )
}

// Annotated DTO property - annotation on its own line, blank line between properties.
@Serializable
data class DeviceCodeResponse(
    @SerialName("device_code")
    val deviceCode: String,

    @SerialName("user_code")
    val userCode: String,

    val interval: Int,
)
```

## Code Block Whitespace

**Core principle: every multi-line construct acts as a *paragraph* - it gets a blank line before and
after it.**

### Opening and closing braces

- **No blank line after an opening `{`.** Exception: sealed class / sealed interface bodies have a
  blank line after `{`.
- **No blank line before a closing `}`.**
- Leave a blank line immediately **after the closing `}`** of a code block (unless it is the very
  last line of its enclosing block).

### Paragraph rule for multi-line constructs

Every multi-line construct (code block, multi-line call, multi-line assignment, `when` / `if`
expression) gets a blank line before and after it. This includes:

- `val`/`var` assignments where the right-hand side spans multiple lines (e.g. `if/else`, `when`,
  multi-line lambda, multi-line constructor/function call).
- A single-line `val` followed by a multi-line `val` needs a blank line between them.
- mockk's `coEvery { ... }` / `every { ... }` stubs are never one-liners. Always open the block onto
  its own line and leave a blank line after each stub's closing `}`.

### Consecutive single-line statements

- Blank line **between logically unrelated groups**.
- **No blank line within a related group** (e.g. related property declarations, consecutive guard
  clause bodies that belong to one extraction).

### Guard clauses

- Blank line **after** a `val`/`var` extraction before the first guard clause.
- Blank line **between** each guard clause.
- Blank line **after** the last guard clause before the main logic.
- Every guard clause is its own paragraph.
- This applies to **every** form of safeguard, including single-line ones that return a value
  pre-emptively (e.g. `if (ids.isEmpty()) return emptyList()`, `val x = foo ?: return null`,
  `val x = foo ?: return@map other`). Whether the guard exits with `return`, `return <value>`,
  `return@label`, `throw`, or `continue`, a blank line always follows it before the next statement.

### `if` statements that consume an extracted variable

- When an `if` statement references a `val`/`var` declared on the line directly above it, leave a
  blank line **between** the assignment and the `if`. The extraction is its own paragraph; the `if`
  that consumes it begins a new one. This applies to both single-line and multi-line `if` bodies, and
  whether the `if` is a guard clause or branching logic.

### Between declarations

- **Functions / methods**: always one blank line between them, in both classes and interfaces.
- **Interface members**: always one blank line between method signatures.
- **Sealed class variants**: always one blank line between variants.
- **Data class properties**: no blank lines between properties. DTO properties annotated with
  `@SerialName` / `@SerializedName` (or similar): blank line between each property - each is a 2-line
  construct.
- **Enum entries**: no blank lines between entries. One blank line before `companion object`.
- **Property groups in classes**: no blank lines within a logical group; one blank line between
  different groups.

### `when` expressions

- `when` inside `onEvent()`-style dispatchers: single-line branches grouped without blanks; blank
  line before the first block-body branch; blank lines between all block-body branches.
- `when` where **all** branches have block bodies: blank lines between each branch.

### Coroutine launches

- Blank lines between sequential `launch { }` blocks (e.g. in `init`).

### Super calls and error logs

- Blank line between a `super.*()` inheritance call (e.g. `super.onResume()`, `super.onCreate(...)`)
  and the following code.
- Blank line between an error log call and the following code.

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

## Import Ordering

Follow this order:

1. Android / AndroidX imports
2. Third-party libraries (Kotlin stdlib, Coroutines, Ktor, Koin, Compose, etc.)
3. Project-specific imports (`nl.rhaydus.*`)

Remove unused imports.

### No fully-qualified references

Never reference anything by its fully-qualified name inline - not types, not top-level functions, not
enum entries, not object members. Always add an `import` and use the short name at the call/declaration
site. This applies to production and test code, including parameter types, return types, generic
arguments, receiver types on lambdas, `mockk()` type witnesses, and top-level function calls.

**This is especially strict for project-own code** (anything under `nl.rhaydus.*`). Never inline a
project-qualified reference just because it saves an import line - always add the import.

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
- Repository data is exposed as `Flow` and collected in the screen model's initializers/collectors.
- Actions execute on the `Main` dispatcher; network/DB/IO operations run on `IO`.

## Error Handling & Logging

The error model is layered. In short:

- **Data sources and repositories throw freely.** They never assume they can't crash and never
  swallow a genuine failure into a sentinel return; if something goes wrong (network, IO, parsing),
  the exception propagates. Repository-level `try`/`catch`/`runCatching` is reserved for *deliberate
  business decisions* (e.g. falling back to a placeholder), never for hiding errors the caller should
  see or for running failure-policy side effects.
- **Every use case returns `Result<T>` and owns failure policy,** wrapping its repository call in a
  cancellation-aware `runCatchingCancellable` - a `runCatching` that rethrows `CancellationException`
  so a cancelled coroutine never becomes a `Result.failure`. Never use bare `runCatching` around a
  suspend call. Reactions to a failure that span operations belong here, not in the repository.
- **Screen-model actions unpack the `Result` with `.onSuccess { }` / `.onFailure { }`** and fold the
  outcome into UI state. **Never use `.fold()`** - `.onSuccess`/`.onFailure` only. Actions therefore
  carry no `try`/`catch` of their own; the use case already converted the throw into a `Result`.

```kotlin
// Use case - domain layer.
class StartAuthorizationUseCase(private val accountRepository: AccountRepository) {
    suspend operator fun invoke(): Result<DeviceAuthorization> =
        runCatchingCancellable { accountRepository.startDeviceAuthorization() }
}

// Action - presentation layer.
dependencies.startAuthorizationUseCase()
    .onSuccess { authorization -> scope.setState { it.copy(link = Connecting(...)) } }
    .onFailure { scope.setState { it.copy(link = Disconnected(error = "Couldn't reach the server.")) } }
```

Plain `try`/`catch` remains acceptable inside data/repository code where control flow needs it (retry
loops, or rethrowing `CancellationException` before catching `Exception` so a deliberate recovery
doesn't fire on cancellation).

- Network calls use the project's shared HTTP client.
- Use the project's logging facade for errors - never `println`. Prefer `Timber` where it is a
  dependency; otherwise use `android.util.Log` with a per-class `TAG`. If `Timber` is added later,
  prefer it.

## Dependencies

- Dependency versions are managed centrally in `gradle/libs.versions.toml`. Reference them via the
  version catalog (`libs.<alias>`) in `build.gradle.kts`. Never hard-code a version in a module build
  file.

## Test Class Organization

Group all tests targeting a single function of the unit under test inside a JUnit 5
`@Nested inner class`. This replaces the use of `// region` / `// endregion` blocks inside test files;
region comments are for production code only.

- Each nested class targets exactly **one** function (or property) on the unit under test.
- Name the nested class in **PascalCase**, matching the function name (e.g. `initializeItems` →
  `inner class InitializeItems`). For property wiring tests, use the property name (e.g. `items` →
  `inner class Items`).
- Annotate it with `@Nested` and declare it as `inner class` so it has access to the outer class's
  mocks, `@BeforeEach` setup, and helper functions.
- The outer test class holds shared mocks, the `@BeforeEach` setup, and any private helper functions.
  Do **not** redeclare these inside the nested classes.
- Every test must live inside a `@Nested inner class`; do not place `@Test` functions directly on the
  outer class. If a function has only a single test case, that test still goes inside its own
  `@Nested` block for consistency.

```kotlin
class RepositoryImplTest {

    private lateinit var remoteDataSource: RemoteDataSource
    private lateinit var localDataSource: LocalDataSource
    private lateinit var repository: RepositoryImpl

    @BeforeEach
    fun setUp() {
        remoteDataSource = mockk()
        localDataSource = mockk()
        repository = RepositoryImpl(
            remoteDataSource = remoteDataSource,
            localDataSource = localDataSource,
        )
    }

    @Nested
    inner class InitializeItems {

        @Test
        fun `caches fetched items on first call`() = runTest {
            // ----- Arrange -----
            ...
        }
    }

    @Nested
    inner class RefreshItems {

        @Test
        fun `does not reset the session flag`() = runTest {
            // ----- Arrange -----
            ...
        }
    }
}
```

## Unit Test Structure

Unit tests follow the **Arrange-Act-Assert (AAA)** principle. Each test is visually divided into three
sections using the following comment format:

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

- Use `// ----- Arrange -----`, `// ----- Act -----`, and `// ----- Assert -----` exactly as shown -   five dashes on each side, capitalized section name in the middle.
- Every unit test must contain all three markers, in order, even if a section only holds a single line.
- Leave a blank line **before** each marker (except the very first marker, which directly follows the
  function's opening `{`). The body of each section starts on the line directly under its marker - no
  blank line between the marker and its first statement.
- When the act and the assertion are the same expression - for example, asserting on a `Flow` inside a
  Turbine `test { }` block, or wrapping a call in `shouldThrow { ... }` - collapse the two sections
  into a single `// ----- Act & Assert -----` marker instead of repeating the call. Use this only when
  there is no separable "act" step.
- These AAA markers are **not** Kotlin region blocks (`// region` / `// endregion`); the no-blank-line
  rule for actual region blocks does not apply to them.
