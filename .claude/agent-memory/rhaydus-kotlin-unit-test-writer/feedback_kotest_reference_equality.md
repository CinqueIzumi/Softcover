---
name: kotest-reference-equality-parentheses
description: Always parenthesise === comparisons when the result feeds into shouldBe — Kotlin's === has lower precedence than named infix functions
metadata:
  type: feedback
---

`===` (referential equality) has LOWER precedence than named infix functions like kotest's `shouldBe`. Without parentheses:

```kotlin
a === b shouldBe true   // BAD — parsed as: a === (b shouldBe true)
```

`b shouldBe true` runs first (asserting b == true), which throws if b is not a Boolean. The outer `===` never fires.

**Fix:** always parenthesise the reference-equality sub-expression:

```kotlin
(a === b) shouldBe true   // GOOD
```

**Why:** Kotlin grammar: multiplicative > additive > range > infix functions > equality (`==`, `!=`, `===`, `!==`). Infix functions bind tighter than equality operators.

**How to apply:** Whenever writing a reference-stability assertion (`===`) that feeds into `shouldBe`, wrap the `===` clause in parens.
