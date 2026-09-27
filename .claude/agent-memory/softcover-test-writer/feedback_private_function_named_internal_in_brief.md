---
name: feedback_private_function_named_internal_in_brief
description: A brief's "pure internal top-level function" claim can be stale — verify visibility with grep before writing the test; report a mismatch as a blocker instead of working around it
metadata:
  type: feedback
---

A brief can describe a target as "a pure internal top-level function" when the source currently has it
`private` (Kotlin top-level `private` is file-scoped — invisible even to another file in the same package).
Seen with `whenReadChipModel` in `WhenReadRow.kt` (core/component) while the migration branch was mid-edit;
the sibling helper `dayLabelFor` in the same feature was already `internal`, so the brief likely assumed the
same pattern without re-checking after a recent edit.

**Why:** the project rule is "never change production code," so bumping `private` → `internal` to make the
function reachable from `androidHostTest` is off-limits from this agent, even though it's a one-line,
behavior-neutral fix. Confirmed this is the right call: reporting the mismatch (instead of a reflection
workaround) let the coordinator make the visibility change on its own, then hand the exact same brief back
— at which point the test was straightforward to add.

**How to apply:** before writing a test against a brief-named function, `grep -n "fun <name>"` the file to
confirm its actual modifier. If it's `private` and the brief's premise requires external visibility, treat
it as a blocker for that specific case only: skip that test, deliver whatever else the brief asks for, and
report the mismatch (file, line, actual modifier) with a suggested one-line fix, unapplied — same as any
other production bug found while testing. Don't let one blocked case stall the rest of the brief. When the
coordinator later reports the visibility fixed, re-verify with the same grep before writing the test (don't
just trust the message) — here it took one round-trip and the fix landed exactly as suggested.
