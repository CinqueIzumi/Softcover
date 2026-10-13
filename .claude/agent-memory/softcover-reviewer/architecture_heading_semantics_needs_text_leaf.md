---
name: architecture-heading-semantics-needs-text-leaf
description: Modifier.semantics { heading() } on a wrapper Row (bar + Text) is inert for screen readers unless it sits on the Text leaf or the Row sets mergeDescendants = true
metadata:
  type: architecture
---

`Modifier.semantics { heading() }` only marks the semantics node it's attached to as a heading; it
does not pull in a sibling's text. A `Row { Box(decorative bar); Text(eyebrow) }.semantics { heading() }`
produces a heading-flagged node with no accessible label of its own (the Box has no semantics, the
Text's own node is separate and unmarked) — TalkBack/VoiceOver headings-navigation lands on an
unlabeled node.

**Why:** a real instance had `heading()` on an eyebrow `Row` (bar + Text) while a sibling component in
the same family correctly put it directly on its title `Text`. The inconsistency between two components
of the same kind, in the same change, is the tell — when a heading requirement lands on a composite
(bar + label) rather than a single `Text`, check whether it sits on the leaf that actually carries the
string.

**How to apply:** whenever a diff adds `semantics { heading() }` (or any semantics property that implies
readable content) to a `Row`/`Column` wrapping more than one child, verify either (a) it is moved to the
`Text` leaf, or (b) the container also sets `mergeDescendants = true`. Compare against any sibling
component in the same diff that does the same thing correctly — the fix is usually already present
elsewhere in the PR.
