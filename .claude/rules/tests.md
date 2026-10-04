---
paths:
  - "**/androidHostTest/**"
  - "**/commonTest/**"
---

# Tests

- Test code is written only by `softcover-test-writer`, from a brief following `docs/reference/agent-briefs.md`.
- Structure: foundation `docs/rhaydus/0.3.1/code-style.md` § Test Class Organization and § Unit Test Structure;
  the review-only test rules are in `docs/reference/code-style.md` § Review-only rules.
- A test earns its place only when input decides output: a mapper, formatter, collector, a `resolve` with a
  real branch, or a fixture list that must cover every variant. Never assert a value you can only get by
  copying a literal out of production code (a style table's dp / alpha / typography, a constructor default).
  Such a test breaks on every deliberate redesign and catches no regression. The gallery is the visual check.
- Every test passes all of these checks, whether it is being written or audited:
  - **Behaviour, not implementation.** If a refactor that keeps behaviour unchanged would break the test, it
    is in the way. Assert observable output, not call order, `verify` on an internal seam, or mock-shaped
    intermediate state.
  - **It would be missed.** Delete a test if removing it leaves release confidence unchanged, such as one on
    a trivial pass-through, a getter or a data-class copy.
  - **Not a copy.** If two tests drive the same path under different names or near-identical fixtures, keep
    one. Parameterise only where the code branches on the inputs.
  - **Keep what guards production harm,** even if the test is plain: auth and tokens, database migrations,
    offline-sync merges, and mutations written to Hardcover.
  - **Fast.** Agents rerun the suite on every retry. Use virtual time (`runTest`, test dispatchers), never a
    real `delay`, real dispatcher or wall-clock wait. Keep a class under ~1 s with the smallest fixture that
    covers the branch.
- Test sources are gated by ktlint and detekt like main sources; see `.claude/rules/kotlin-style.md`.
- Fakes are MockK stubs of only the public surface the code under test calls (e.g. `safeQuery`/
  `safeMutation` via `mockkStatic`) — never MockServer, WireMock, embedded servers, or new test
  dependencies.
