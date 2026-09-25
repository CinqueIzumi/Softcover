---
name: project_canonical_test_templates
description: One canonical test file per layer (action, use case, repository, local/remote data source, mapper) — clone these instead of exploring broadly
type: project
---

When writing a new test, first identify which layer the target belongs to, then read **only** the matching canonical test file below and clone its structure. Do not search across unrelated layers — the setup, mocking, and assertion style differs enough between layers that cross-referencing wastes tokens and often leads astray.

| Target type | Canonical test file |
|---|---|
| `*Action` (TOAD presentation action) | `feature/book_detail/src/androidHostTest/kotlin/nl/rhaydus/softcover/feature/book_detail/presentation/action/InitializeBookWithIdActionTest.kt` |
| `*Collector` (TOAD flow collector) | `feature/settings/src/androidHostTest/kotlin/nl/rhaydus/softcover/feature/settings/presentation/collector/DateStyleCollectorTest.kt` |
| `*UseCase` | `core/book/src/androidHostTest/kotlin/nl/rhaydus/softcover/core/book/domain/usecase/FetchBookByIdUseCaseTest.kt` |
| `*RepositoryImpl` | `core/book/src/androidHostTest/kotlin/nl/rhaydus/softcover/core/book/data/repository/BooksRepositoryImplTest.kt` |
| `*LocalDataSourceImpl` (Room-backed) | `core/book/src/androidHostTest/kotlin/nl/rhaydus/softcover/core/book/data/datasource/BooksLocalDataSourceImplTest.kt` |
| `*RemoteDataSourceImpl` (Apollo-backed) | `core/book/src/androidHostTest/kotlin/nl/rhaydus/softcover/core/book/data/datasource/BooksRemoteDataSourceImplTest.kt` |
| `*Mapper` (data → domain) | `core/book/src/androidHostTest/kotlin/nl/rhaydus/softcover/core/book/data/mapper/BookMapperTest.kt` |

**Workflow for a new test:**
1. Read the target source file.
2. Read the single canonical template that matches its layer.
3. Read [[project_test_conventions]] if not already in context.
4. Write the new test. Do not explore other test files unless the target has an unusual shape not covered by the template.

**Why:** Softcover tests follow layer-specific patterns (e.g. actions need the `ActionScope` + `launch` workaround; remote data sources stub Apollo `safeQuery`; local data sources mock the DAO). Without a named template per layer, the agent spends a lot of context rediscovering the pattern from scratch — the user flagged this as expensive after the first Action test run.

**How to apply:** Every time the user asks for tests on a new class, match it against the table above first. If none of the rows fit (e.g. a new layer type appears), *then* fall back to broader exploration — and add a new row to this memory once a canonical template emerges.
