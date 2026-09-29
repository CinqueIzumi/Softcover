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
- Test sources are gated by ktlint and detekt like main sources; see `.claude/rules/kotlin-style.md`.
- Fakes are MockK stubs of only the public surface the code under test calls (e.g. `safeQuery`/
  `safeMutation` via `mockkStatic`) — never MockServer, WireMock, embedded servers, or new test
  dependencies.
