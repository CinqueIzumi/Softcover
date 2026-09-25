---
name: project_hook_script_not_registered
description: A new .claude/hooks/*.sh file is not itself wired into .claude/settings.json — check the PreToolUse registration, not just the script
metadata:
  type: project
---

Adding a new `.claude/hooks/*.sh` script (e.g. `doc-guard.sh` for the Step 04b doc-budgets ratchet) does not
register it. `.claude/settings.json` (and `.claude/settings.local.json`) hold the actual `PreToolUse` /
`UserPromptSubmit` hook wiring by matcher (`Edit`, `Write`, `Bash`, `Agent`, …); every existing hook
(`quiet-gradle.sh`, `large-read-guard.sh`, `block-slow-gradle.sh`, `agent-brief-check.sh`,
`context-budget.sh`) has an entry there. A brief's "hook smoke cases passed" can mean the script was fed
crafted stdin JSON directly — which proves the script's own logic but not that Claude Code ever invokes it.

**Why:** caught a case where `doc-guard.sh` was fully implemented, tested via direct invocation, and the gate
job ran a merge-base equivalent (`checkDocBudgets`) successfully — everything *looked* done — but the hook
itself was dead code because no matcher pointed at it. Nothing in the diff or the gate output would reveal
this; only reading `settings.json` does.

**How to apply:** whenever a scope includes a new or changed file under `.claude/hooks/`, always read
`.claude/settings.json` (and `.claude/settings.local.json`) as an integration point, even if neither file is
listed in `## Scope` — confirm the matcher and command path exist and match the new script's actual
filename/behavior (Edit/Write vs Bash matcher, etc.). Treat a missing registration as 🔴, not a nitpick: the
feature does not run at all.
