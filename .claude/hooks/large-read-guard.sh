#!/usr/bin/env bash
set -u

input=$(cat)
root="${CLAUDE_PROJECT_DIR:-$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)}"
max_lines=600

[ -n "$(printf '%s' "$input" | jq -r '.agent_id // ""')" ] && exit 0
[ -n "$(printf '%s' "$input" | jq -r '.tool_input.limit // ""')" ] && exit 0

path=$(printf '%s' "$input" | jq -r '.tool_input.file_path // ""')
case "$path" in
  "$root"/*) ;;
  *) exit 0 ;;
esac
[ -f "$path" ] || exit 0

case "$(printf '%s' "$path" | tr '[:upper:]' '[:lower:]')" in
  *.png | *.jpg | *.jpeg | *.gif | *.webp | *.bmp | *.ico | *.pdf | *.ipynb) exit 0 ;;
esac
grep -Iq . "$path" 2>/dev/null || exit 0

lines=$(wc -l < "$path" | tr -d ' ')
[ "$lines" -gt "$max_lines" ] || exit 0

relative="${path#"$root"/}"
reason="$relative has $lines lines. grep for the symbol and Read with offset/limit, or delegate the work (softcover-implementer / Explore)."
jq -n --arg reason "$reason" '{hookSpecificOutput: {hookEventName: "PreToolUse", permissionDecision: "deny", permissionDecisionReason: $reason}}'
exit 0
