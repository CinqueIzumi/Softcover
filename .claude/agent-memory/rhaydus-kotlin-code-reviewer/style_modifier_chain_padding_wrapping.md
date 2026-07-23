---
name: style_modifier_chain_padding_wrapping
description: CLAUDE.md says ktlint exempts Modifier-chain calls from multi-arg wrapping, but the codebase convention still wraps 2+-arg Modifier calls (e.g. .padding(horizontal=, vertical=)) - keep flagging inline ones in review
metadata:
  type: feedback
---

CLAUDE.md's Code Style section describes the foundation ktlint ruleset's multi-arg one-per-line
wrapping rule as "exempting collection factories, `Modifier.…` chains, trailing-lambda calls" from
auto-fix/gating. That describes a **tooling gap**, not an authored style exception: the foundation's
own `docs/rhaydus/0.3.1/code-style.md` §Argument and Property Layout has no such carve-out — any
function call with 2+ args must wrap one-per-line with a trailing comma, full stop.

**Why this matters:** grepped evidence across `feature/` + `core/` (2026-07-17): 18 files wrap a
2-arg `.padding(horizontal = X, vertical = Y)` chain call across multiple lines; only 1 file
(`feature/library/.../LibraryScreenLayout.jvm.kt:447`) leaves one inline. The dominant, established
codebase convention is to wrap Modifier-chain calls too, despite ktlint not gating it.

**How to apply:** keep flagging an inline 2+-arg `Modifier.foo(a = ..., b = ...)` chain call as a
style deviation in review (mechanical/cosmetic severity, not correctness) — don't wave it through
just because CLAUDE.md notes the ktlint rule doesn't currently catch it. This came up reviewing
Settings 1a's `SettingsMenuRow` (`.padding(horizontal = 4.dp, vertical = 17.dp)` left inline, while
the same PR's `AppUpdateSection` correctly wrapped its own 4-arg `.padding(start=, end=, top=,
bottom=)`).
