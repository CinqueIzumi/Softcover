#!/usr/bin/env bash
set -u

cat > /dev/null

root="${CLAUDE_PROJECT_DIR:-$(pwd)}"
active="$root/docs/working/ACTIVE.md"
cap=60

[ -s "$active" ] || exit 0

blocks=""
while IFS= read -r path || [ -n "$path" ]; do
  path=$(printf '%s' "$path" | sed -E 's/^[[:space:]]+//; s/[[:space:]]+$//')
  [ -z "$path" ] && continue

  file="$root/$path"
  if [ ! -f "$file" ]; then
    blocks+=$'\n\n'"### $path"$'\n'"(listed in ACTIVE.md but the file does not exist)"
    continue
  fi

  now=$(sed -n '/^## Now[[:space:]]*$/,/^## /p' "$file" | sed '1d; /^## /d')
  [ -z "$now" ] && continue

  lines=$(printf '%s\n' "$now" | wc -l | tr -d ' ')
  if [ "$lines" -gt "$cap" ]; then
    now=$(printf '%s\n' "$now" | head -n "$cap")
    now+=$'\n'"(Now block is $lines lines, cut at $cap; the convention is at most 40. Trim it with /handoff.)"
  fi

  blocks+=$'\n\n'"### $path"$'\n'"$now"
done < "$active"

[ -z "$blocks" ] && exit 0

context="Active work (docs/working/ACTIVE.md). Read only the step file the Next line names.$blocks"
jq -n --arg ctx "$context" '{hookSpecificOutput: {hookEventName: "SessionStart", additionalContext: $ctx}}'
exit 0
