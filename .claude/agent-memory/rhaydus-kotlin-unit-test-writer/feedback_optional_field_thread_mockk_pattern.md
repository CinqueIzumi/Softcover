---
name: optional-field-thread-mockk-pattern
description: For an optional String? param threaded through Apollo Optional.presentIfNotNull + a paired sentinel field (e.g. actionAt -> action_at/action), mirror the exact match{} coVerify pattern of adjacent existing tests rather than inventing a new assertion style.
metadata:
  type: feedback
---

When a new optional field (e.g. `actionAt: String?`) is threaded through a full write path
(data source mutation input -> repository -> use case -> offline sync queue), each layer's test
file already has an established assertion idiom for that layer — reuse it verbatim instead of
introducing a new one:

- **Remote data source** (Apollo mutation input): `coVerify { apolloClient.safeMutation(mutation = match<X> { it.datesReadInput.field.getOrNull() == value }) }`. For "absent" cases assert `is Optional.Absent`, not `== null`.
- **Repository**: extend the existing `coEvery { remote.fn(book=..., newPage=..., actionAt=...) } returns ...` call-and-verify pair — mockk matches named args exactly, so a fresh test with the new arg added is more scannable than trying to generalize the mock.
- **Use case**: same coEvery/coVerify call-and-verify pair one layer up.
- **Offline sync**: assert on the captured `PendingUserBookWrite` field directly via `match { write -> write.field == value }`.

**Why:** keeps diffs minimal and consistent with `[[feedback_import_order_convention]]`-style
on-touch conventions; a reviewer scanning the diff sees the same pattern repeated, not five
different assertion strategies for one feature.

**How to apply:** when a task says "thread field X through the write path," read one existing
test per layer first (via offset/limit, not whole-file reads per the on-touch scoping in
CLAUDE.md), then clone its structure for the new assertion.

Also: when the task explicitly asks for a "replay" counterpart (e.g.
`replayUpdateBookProgress`) and no test class exists for that method yet, check first — do not
assume coverage exists. Model the new nested test class after a sibling replay method's tests in
the same file (e.g. `ReplayUpdateBookRating` used the `coEvery { safeMutation(...) } returns
mockk(relaxed = true)` + `coVerify(exactly = 1) { safeMutation(mutation = match<X> { ... }) }`
pattern, which is simpler than the full data-mapping mocks needed for the non-replay method).
