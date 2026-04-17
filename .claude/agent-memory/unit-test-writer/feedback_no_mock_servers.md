---
name: MockK only — never set up mock servers
description: Never set up Apollo MockServer, WireMock, or similar test infrastructure. Use MockK and mock only the public surface actually consumed by the code under test.
type: feedback
---

When a test needs to fake a collaborator, use MockK and mock **only** the specific public methods or extension functions that the production code actually calls — nothing deeper, nothing broader. Never set up Apollo MockServer, WireMock, embedded HTTP servers, in-memory databases for unit tests, or any similar test infrastructure. Never add new test dependencies for this purpose.

**Why:** The user pushed back hard (twice) when an Apollo-backed data source was being tested. The first prompt offered MockServer as an option; the second mentioned "decompiling" Apollo internals to find a stubbing seam. The user's stance: mocking should target the public surface that's actually consumed (e.g. `apolloClient.safeQuery` / `safeMutation`) and stay consistent with the rest of the suite. Anything more is overengineering.

**How to apply:**
- Identify the exact public methods or extension functions the code under test calls on its collaborators. Mock those, and only those.
- For Kotlin extension functions (e.g. `apolloClient.safeQuery(...)`), use `mockkStatic("<fully.qualified.FileKt>")` to intercept them — this is fine.
- For mapper extensions (`toBook()`, `toModel()`, etc.) used inside the chain, `mockkStatic` the mapper file and stub the extension to return a pre-built `mockk<DomainModel>()` — same pattern as `BooksRepositoryImplTest.kt`.
- Do not stub deep Apollo internals like `apolloClient.query(...).execute()`, `ApolloCall`, or `ApolloResponse` construction.
- Do not propose MockServer/WireMock as a fallback "if MockK proves hard" — push through with MockK or stop and report the specific blocker.
- If a generated type genuinely cannot be mocked with `mockk<>()` (final value class, no zero-arg ctor, etc.), skip just that one test, leave a one-line comment, and report it. Do not change approach.
