#!/usr/bin/env bash
set -u

here=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
root=$(cd "$here/../.." && pwd)
hook="$here/../doc-guard.sh"
failures=0

b64dec() { printf '%s' "$1" | base64 -d; }

# Fixed fixture budgets shared by every case — see doc-guard.cases for the paths that
# exercise each rule (first-match-wins, `*` vs `**`, byte vs line units, the `## Now` ratchet).
budgets_content='fixture/special/exact.md            8B
fixture/special/*.md                1000B
docs/reference/*.md                 100B
docs/reference/**/*.md              20B
fixture/tiny.md                     20B
fixture/unicode.md                  10B
.claude/rules/*.md                  40L
## Now                              40L
'

run_case() {
  local name="$1" tool="$2" relpath="$3" old_b64="$4" arg1_b64="$5" arg2_b64="$6" \
    replace_all="$7" expected="$8" reason="$9"
  local fixture_root input output actual_decision actual_reason path

  fixture_root=$(mktemp -d)
  mkdir -p "$fixture_root/docs/working"
  printf '%s' "$budgets_content" > "$fixture_root/docs/doc-budgets.txt"
  printf '%s\n' "docs/working/now-test.md" > "$fixture_root/docs/working/ACTIVE.md"

  if [ "$tool" != "Bash" ] && [ "$old_b64" != "-" ]; then
    path="$fixture_root/$relpath"
    mkdir -p "$(dirname "$path")"
    b64dec "$old_b64" > "$path"
  fi

  case "$tool" in
    Write)
      input=$(jq -n --arg fp "$fixture_root/$relpath" --arg content "$(b64dec "$arg1_b64")" \
        '{tool_name: "Write", tool_input: {file_path: $fp, content: $content}}')
      ;;
    Edit)
      input=$(jq -n --arg fp "$fixture_root/$relpath" \
        --arg olds "$(b64dec "$arg1_b64")" --arg news "$(b64dec "$arg2_b64")" \
        --argjson all "$([ "$replace_all" = "true" ] && echo true || echo false)" \
        '{tool_name: "Edit", tool_input: {file_path: $fp, old_string: $olds, new_string: $news, replace_all: $all}}')
      ;;
    Bash)
      input=$(jq -n --arg cmd "$(b64dec "$arg1_b64")" '{tool_name: "Bash", tool_input: {command: $cmd}}')
      ;;
    EditNumberOldString)
      # old_string is a raw JSON number (not a string) — forces jq's split() to error out,
      # exercising the "doc-guard could not evaluate the edit" path rather than DOC_GUARD_NOT_FOUND.
      input=$(jq -n --arg fp "$fixture_root/$relpath" \
        --argjson olds "$(b64dec "$arg1_b64")" --arg news "$(b64dec "$arg2_b64")" \
        '{tool_name: "Edit", tool_input: {file_path: $fp, old_string: $olds, new_string: $news, replace_all: false}}')
      ;;
  esac

  output=$(printf '%s' "$input" | CLAUDE_PROJECT_DIR="$fixture_root" bash "$hook")
  rm -rf "$fixture_root"

  if [ -z "$output" ]; then
    actual_decision="allow"
    actual_reason=""
  else
    actual_decision=$(printf '%s' "$output" | jq -r '.hookSpecificOutput.permissionDecision // "allow"')
    actual_reason=$(printf '%s' "$output" | jq -r '.hookSpecificOutput.permissionDecisionReason // ""')
  fi

  if [ "$actual_decision" != "$expected" ]; then
    echo "FAIL  $name"
    echo "      expected decision: $expected"
    echo "      actual   decision: $actual_decision"
    echo "      reason: $actual_reason"
    failures=$((failures + 1))
    return
  fi

  if [ "$reason" != "-" ] && ! printf '%s' "$actual_reason" | grep -qF -- "$reason"; then
    echo "FAIL  $name"
    echo "      expected reason to contain: $reason"
    echo "      actual reason: $actual_reason"
    failures=$((failures + 1))
    return
  fi

  echo "ok    $name"
}

while IFS=$'\t' read -r name tool relpath old_b64 arg1_b64 arg2_b64 replace_all expected reason; do
  [ -z "$name" ] && continue
  case "$name" in "#"*) continue ;; esac
  run_case "$name" "$tool" "$relpath" "$old_b64" "$arg1_b64" "$arg2_b64" "$replace_all" "$expected" "$reason"
done < "$here/doc-guard.cases"

exit "$failures"
