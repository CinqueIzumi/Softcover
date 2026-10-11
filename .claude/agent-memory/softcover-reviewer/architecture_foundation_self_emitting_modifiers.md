---
name: architecture-foundation-self-emitting-modifiers
description: Compose clickable/selectable already emit HoverInteraction, and foundation shakeOnError already fires haptics.reject() and ignores reduced motion; extra hoverable or reject calls double up
metadata:
  type: feedback
---

- `Modifier.clickable` / `combinedClickable` / `selectable` / `toggleable` emit `HoverInteraction` on the shared source themselves (Clickable.kt "Hover is already handled"); an added `Modifier.hoverable(source)` on the same source is a redundant second emitter. `hoverHighlight` and `pressScale` only read the source.
- Foundation `shakeOnError(trigger, onShakeEnd)` fires `haptics.reject()` itself and is not gated by `playDecorativeMotion()`; a caller that also calls `haptics.reject()` double-buzzes, and a reduced-motion user still sees the shake.

**How to apply:** flag both on any change that wires hover or an error shake. Gate by not raising the trigger when motion is off (and then fire the haptic by hand).
