---
name: mockk capture() needs no import
description: io.mockk.capture is not a top-level import — importing it fails compilation; capture() is used unqualified inside every{}/coEvery{} blocks
type: feedback
---

`import io.mockk.capture` does not resolve (compile error "Unresolved reference 'capture'") on mockk 1.13.17 in this project. Only `import io.mockk.slot` is needed; `capture(mySlot)` is called directly inside `every { ... }` / `coEvery { ... }` without any import — it resolves via the matcher-scope receiver. Confirmed working precedent: `core/lists/.../ListsRemoteDataSourceImplTest.kt` uses `capture(capturedMutation)` with only `slot` imported.

**Why:** Wasted a compile cycle guessing the import; the IDE-style "import the symbol you call" instinct is wrong here specifically for `capture`.

**How to apply:** When writing a slot-capture test (`val s = slot<T>()`, `coEvery { x(capture(s)) } returns y`), import only `io.mockk.slot`, never `io.mockk.capture`.
