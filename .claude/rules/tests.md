---
paths:
  - "**/androidHostTest/**"
  - "**/commonTest/**"
---

# Tests

- Test code is written only by `softcover-test-writer`, from a brief following `docs/reference/agent-briefs.md`.
- Structure: foundation `docs/rhaydus/0.3.1/code-style.md` § Test Class Organization and § Unit Test Structure;
  the review-only test rules are in `docs/reference/code-style.md` § Review-only rules.
- Test sources are gated by ktlint and detekt like main sources; see `.claude/rules/kotlin-style.md`.
