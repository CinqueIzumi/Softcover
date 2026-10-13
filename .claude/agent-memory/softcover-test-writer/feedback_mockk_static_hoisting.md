---
name: mockk-static-hoisting
description: Per-test mockkStatic/mockkObject + unmockkAll costs 0.1-0.5 s per nested class; hoist to companion @BeforeAll and clearAllMocks per test
metadata:
  type: feedback
---

Re-registering `mockkStatic` / `mockkObject(Companion)` in every `@BeforeEach`/test and calling `unmockkAll()` in `@AfterEach` re-instruments classes each time; ListsRemoteDataSourceImplTest dropped 2.4 s to 0.7 s by hoisting.

**How to apply:** register statics and generated-Apollo companions once in a `companion object` `@JvmStatic @BeforeAll`, `unmockkAll()` in `@JvmStatic @AfterAll`, and `clearAllMocks()` in `@AfterEach` (defaults also clear static/object stubs, partial-mock originals stay). The first test class in a module still pays about 1 s of JVM/mockk warm-up; that is not fixable test-side.

Related: [[project_test_conventions]].
