---
name: feedback_styleCheck_pulls_unrelated_module_failures
description: styleCheck/ktlintCheck are whole-project tasks; a pre-existing break in another module can fail the exact Verify command for an unrelated single-module brief
metadata:
  type: feedback
---

`ktlintCheck` and `styleCheck` in a brief's `## Verify` line are root aggregate tasks that compile
and gate every module, not just the one named earlier in the command (e.g.
`:core:component:compileKotlinJvm ktlintCheck styleCheck`). If another module already has
uncommitted, broken work in the working tree (see [[feedback_check_working_tree_before_starting]]),
that module's compile/detekt failure surfaces under the same Verify run even though the brief's
diff never touches it.

**Why:** an already-staged, uncommitted change from earlier session work in an unrelated module had
broken a test's compile in that module. `:core:component:compileKotlinJvm ktlintCheck styleCheck`
therefore failed there, nothing in the current brief's own diff.

**How to apply:** when Verify fails on a module the brief's `## Files` never lists, isolate before
treating it as your own bug — rerun just the failing task in isolation
(`scripts/gradle-quiet.sh :failing:module:task`) to confirm it fails the same way with none of your
edits in play (compile it standalone; you don't need to revert anything to prove this). If it does,
it's pre-existing breakage — do not fix it (boundary: never edit tests, never widen the task).
Report the isolated Verify result for your own module (e.g. append `-x
:other:module:failingTask`) alongside the honest whole-command failure, so the report shows both
"my slice is clean" and "the stated Verify command still fails for an unrelated reason".
