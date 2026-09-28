---
name: architecture-scaffold-merge-widens-fillmaxwidth
description: Collapsing N sibling composables into one scaffold + outer Row/Column can silently apply a modifier (fillMaxWidth, padding) that used to belong to only one variant to all of them
metadata:
  type: architecture
---

When a "one private scaffold" refactor wraps several previously-separate render branches in a new
common outer container to add a shared slot (a trailing/actions row, a shared background), check
whether that outer container's own modifier (`fillMaxWidth()`, `padding()`, alignment) now applies
unconditionally to every branch — even the ones that previously did not have it.

**Why:** a real instance merged three header registers behind one `SectionHeader` entry point to add a
`trailing` slot. Only one of the three registers used to `fillMaxWidth()`; the new outer `Row` applies it
to all three, so the other two silently went from self-sizing (shrink-wrap to bar+eyebrow content) to
always stretching to the available width, even with the default empty trailing slot. Invisible today
because every call site happens to sit inside an already-`fillMaxWidth()` container, but an unrequested
contract widening the approved shape never asked for (it changes a `Row` sibling's or a centred parent's
layout the moment a caller relies on shrink-wrap). `fillMaxWidth()` under unbounded width is a no-op, not
a crash; do not claim otherwise.

**How to apply:** when reviewing a "collapse N composables into one + shared outer container" diff, diff
each branch's *old* modifier chain against what the *new* shared container now imposes on it. A width/
padding/alignment modifier moving from "only variant X had it" to "the outer container gives it to
everyone" is a footprint change worth calling out even when it renders identically in every sampled
call site.
