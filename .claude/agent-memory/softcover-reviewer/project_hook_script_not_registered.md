---
name: project_hook_script_not_registered
description: A new hook script in the rhaydus-kotlin plugin is not itself wired into its hooks/hooks.json — check the PreToolUse registration, not just the script
metadata:
  type: project
---

Adding a new script under `foundation/claude/plugins/rhaydus-kotlin/hooks/scripts/` (e.g. `doc-guard.sh`) does not
register it. The plugin's `hooks/hooks.json` (and `.claude/settings.local.json`) hold the actual `PreToolUse` /
`UserPromptSubmit` hook wiring by matcher (`Edit`, `Write`, `Bash`, `Agent`, …); every existing hook
(`quiet-gradle.sh`, `large-read-guard.sh`, `block-slow-gradle.sh`, `agent-brief-check.sh`,
`context-budget.sh`) has an entry there. A brief's "hook smoke cases passed" can mean the script was fed
crafted stdin JSON directly — which proves the script's own logic but not that Claude Code ever invokes it.

**Why:** caught a case where `doc-guard.sh` was fully implemented, tested via direct invocation, and the gate
job ran a merge-base equivalent (`checkDocBudgets`) successfully — everything *looked* done — but the hook
itself was dead code because no matcher pointed at it. Nothing in the diff or the gate output would reveal
this; only reading `hooks.json` does.

**How to apply:** whenever a scope includes a new or changed file under the plugin's `hooks/scripts/`, always read
`hooks/hooks.json` (and `.claude/settings.local.json`) as an integration point, even if neither file is
listed in `## Scope` — confirm the matcher and command path exist and match the new script's actual
filename/behavior (Edit/Write vs Bash matcher, etc.). Treat a missing registration as 🔴, not a nitpick: the
feature does not run at all. Registered is not loaded either: Claude Code runs the cached copy in
`installed_plugins.json`'s `installPath`, refreshed only by a reinstall at an unchanged version, so check the
cache holds the script before calling a hook live.
