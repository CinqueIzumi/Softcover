#!/usr/bin/env bash
set -u

input=$(cat)
agent=$(printf '%s' "$input" | jq -r '.tool_input.subagent_type // ""')
prompt=$(printf '%s' "$input" | jq -r '.tool_input.prompt // ""')

case "$agent" in
  softcover-implementer)
    required=("## Goal" "## Files" "## Verify" "## Report")
    template='## Goal
What the change does and why, in one or two sentences.

## Files
- `path/to/File.kt:120-180` — what to change there

## Contract (optional)
UiState / UiAction / UiEvent shape or public signature, when decided.

## Constraints (optional)
What must not change; decisions taken; the governing design-system section.

## Verify
scripts/gradle-quiet.sh :module:compileKotlinJvm

## Report
≤150 words: Done / Files changed / Verify result / Left over / Needs tests.'
    ;;
  softcover-test-writer)
    required=("## Files" "## Verify" "## Report")
    template='## Files
- `path/to/Unit.kt` — the unit under test
- `path/to/UnitTest.kt:40-95` — construction sites to fix / where to add

## Verify
scripts/gradle-quiet.sh :module:testAndroidHostTest --tests "nl.rhaydus.softcover.feature.x.YTest"

## Report
≤150 words: tests added / pass or fail with names and diagnosis.'
    ;;
  softcover-reviewer)
    required=("## Scope")
    template='## Scope
Commit range (main..HEAD), staged, unstaged, or a file list.

## Gates (optional)
The gate result you already have.

## Verify (optional)
One narrow command the reviewer may run if no gate result is given.

## Focus (optional)
What the change is meant to do; areas to look at hardest.'
    ;;
  *) exit 0 ;;
esac

missing=()
for section in "${required[@]}"; do
  printf '%s\n' "$prompt" | grep -Eq "^${section}[[:space:]]*$" || missing+=("$section")
done
[ "${#missing[@]}" -eq 0 ] && exit 0

joined=$(printf '%s, ' "${missing[@]}")
reason="$agent brief is missing: ${joined%, }. Re-issue with this template (docs/reference/agent-briefs.md):

$template"
jq -n --arg reason "$reason" '{hookSpecificOutput: {hookEventName: "PreToolUse", permissionDecision: "deny", permissionDecisionReason: $reason}}'
exit 0
