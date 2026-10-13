#!/usr/bin/env bash
set -u

input=$(cat)
root="${CLAUDE_PROJECT_DIR:-$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)}"

QUIET_GRADLE="$root/scripts/gradle-quiet.sh" python3 -c '
import json
import os
import sys

data = json.load(sys.stdin)
tool_input = data.get("tool_input") or {}
cmd = tool_input.get("command") or ""
if "GRADLE_VERBOSE=1" in cmd or "gradle-quiet.sh" in cmd:
    sys.exit(0)

token = "./gradlew"
boundary_before = " \t\n;&|()"
boundary_after = " \t\n;&|)"
quiet = os.environ["QUIET_GRADLE"]
out = []
quote = None
i = 0
changed = False
while i < len(cmd):
    ch = cmd[i]
    if quote:
        if ch == "\\" and quote == "\"" and i + 1 < len(cmd):
            out.append(cmd[i:i + 2])
            i += 2
            continue
        if ch == quote:
            quote = None
        out.append(ch)
        i += 1
        continue
    if ch == "\\" and i + 1 < len(cmd):
        out.append(cmd[i:i + 2])
        i += 2
        continue
    if ch in "\"'"'"'":
        quote = ch
        out.append(ch)
        i += 1
        continue
    end = i + len(token)
    if (
        cmd.startswith(token, i)
        and (i == 0 or cmd[i - 1] in boundary_before)
        and (end == len(cmd) or cmd[end] in boundary_after)
    ):
        out.append(quiet)
        i = end
        changed = True
        continue
    out.append(ch)
    i += 1

if changed:
    updated = dict(tool_input, command="".join(out))
    print(json.dumps({"hookSpecificOutput": {"hookEventName": "PreToolUse", "updatedInput": updated}}))
' <<< "$input"
exit 0
