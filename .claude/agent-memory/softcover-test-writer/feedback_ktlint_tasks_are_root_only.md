---
name: ktlint-tasks-are-root-only
description: ktlintFormat/ktlintCheck exist only at the root project, not per-module — module-scoped invocations fail
metadata:
  type: feedback
---

`:feature:<module>:ktlintFormat` and `:feature:<module>:ktlintCheck` (or any `:<module>:ktlintFormat` /
`:<module>:ktlintCheck`) do not exist as tasks — the custom ktlint ruleset (one-per-line multi-arg wrapping,
trailing commas) is wired as root-level `:ktlintFormat` / `:ktlintCheck` only. Running the module-scoped form
fails with "task not found".

**Why:** a coordinator asked for `:feature:book_detail:ktlintCheck`; `:feature:book_detail:tasks --all` showed
no ktlint tasks at all for that module, only `:ktlintCheck` at the root (confirmed via `:tasks --all` at root).

**How to apply:** always run `:ktlintFormat` / `:ktlintCheck` unscoped at the root. This reformats/checks the
whole repo, not just your target file — after running `:ktlintFormat`, `git diff --stat` (or check `git
status`) to confirm only the file(s) you were meant to touch actually changed content; a file that was already
dirty from concurrent, unrelated work may show in the diff too, but ktlint itself won't introduce semantic
changes (renames, restructuring) — only whitespace/wrapping/commas. See also
[[project_host_test_task_name]] for the analogous testAndroidHostTest gotcha.
