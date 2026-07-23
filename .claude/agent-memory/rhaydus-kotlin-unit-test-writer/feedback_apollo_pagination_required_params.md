---
name: feedback_apollo_pagination_required_params
description: When a data source adds pagination to an Apollo query (limit/offset become required GraphQL Int! vars), existing tests constructing the query without those args stop compiling
metadata:
  type: feedback
---

When a `.graphql` query adds `$limit: Int!, $offset: Int!` (or any new required variable) for pagination,
the generated Apollo `data class XQuery(...)` gains non-default constructor params. Any EXISTING test that
built the query as `XQuery(userId = userId)` (relying on what used to be the only/optional param) breaks
at compile time with "No value passed for parameter 'limit'" etc.

**Why:** confirmed on `UserTagsRemoteDataSourceImplTest` (feature/book_detail) 2026-07-24: `fetchAllTaggings`
was changed to page through `FindTagsByUserQuery(userId, limit = PAGE_SIZE, offset)`, and the query's
`.graphql` file declares `$limit: Int!, $offset: Int!` with no GraphQL default. Apollo Kotlin generates
required (non-optional) constructor params regardless of a GraphQL default unless `Optional<T>` is used —
so any pre-existing bare `XQuery(userId = userId)` call site in tests fails to compile the moment pagination
lands, even though the task brief only asked for new pagination tests.

**How to apply:** when a task brief says "add tests for the newly-added pagination" and scopes you to ONE
nested test class only ("do not touch other parts of the file"), still run the test task FIRST before
writing anything new — if the existing tests in that class already fail to compile against the new required
params, fix ONLY those minimal call sites (add the new required args, nothing else) alongside the new tests.
This is not scope creep or a "re-audit" — it's a mandatory, direct consequence of the exact same source change
being tested, and the whole file must compile for any test (old or new) to run. See project CLAUDE.md:
"Project must always fully build" / "Never hide build/test failures".

Related: [[project_jvm_test_task_name]] for the `testAndroidHostTest` task-name gotcha in this same module.
