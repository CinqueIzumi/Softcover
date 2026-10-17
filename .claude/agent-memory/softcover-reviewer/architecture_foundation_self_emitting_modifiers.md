---
name: architecture-foundation-self-emitting-modifiers
description: Compose clickable/selectable already emit HoverInteraction; an extra hoverable on the same source doubles up
metadata:
  type: feedback
---

- `Modifier.clickable` / `combinedClickable` / `selectable` / `toggleable` emit `HoverInteraction` on the shared source themselves (Clickable.kt "Hover is already handled"); an added `Modifier.hoverable(source)` on the same source is a redundant second emitter. `hoverHighlight` and the tactile press scale only read the source.

**How to apply:** flag it on any change that wires hover.
