#!/usr/bin/env bash
set -u

here=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
hook="$here/../agent-brief-check.sh"
failures=0

check() {
  local name=$1 agent=$2 prompt=$3 expected=$4
  local input output actual
  input=$(jq -n --arg a "$agent" --arg p "$prompt" '{tool_name: "Agent", tool_input: {description: "d", prompt: $p, subagent_type: $a}}')
  output=$(printf '%s' "$input" | bash "$hook")
  if [ -z "$output" ]; then
    actual=allow
  else
    actual=$(printf '%s' "$output" | jq -r '.hookSpecificOutput.permissionDecision')
  fi
  if [ "$actual" = "$expected" ]; then
    echo "ok    $name"
  else
    echo "FAIL  $name (expected $expected, got $actual)"
    failures=$((failures + 1))
  fi
}

full_impl=$'## Goal\nx\n\n## Files\n- a.kt\n\n## Verify\nscripts/gradle-quiet.sh x\n\n## Report\n150 words'
full_test=$'## Files\n- a.kt\n\n## Verify\nx\n\n## Report\ny'

check "implementer bare prompt denied" softcover-implementer "add a button" deny
check "implementer full brief allowed" softcover-implementer "$full_impl" allow
check "implementer missing Report denied" softcover-implementer $'## Goal\nx\n## Files\ny\n## Verify\nz' deny
check "implementer inline mention not a heading" softcover-implementer "needs ## Goal ## Files ## Verify ## Report" deny
check "test-writer full brief allowed" softcover-test-writer "$full_test" allow
check "test-writer missing Verify denied" softcover-test-writer $'## Files\na\n## Report\nb' deny
check "reviewer with Scope allowed" softcover-reviewer $'## Scope\nmain..HEAD' allow
check "reviewer bare prompt denied" softcover-reviewer "review the last commit" deny
check "Explore passes through" Explore "find things" allow
check "no subagent_type passes through" "" "anything" allow

reason=$(jq -n --arg p "x" '{tool_input: {prompt: $p, subagent_type: "softcover-reviewer"}}' | bash "$hook" | jq -r '.hookSpecificOutput.permissionDecisionReason')
case "$reason" in
  *"## Scope"*"Commit range"*) echo "ok    denial embeds the template" ;;
  *) echo "FAIL  denial embeds the template"; failures=$((failures + 1)) ;;
esac

exit "$failures"
