#!/usr/bin/env bash
#
# kt-style-hook.sh — Claude Code PostToolUse adapter for style-check.sh.
#
# Wired in .claude/settings.json as a PostToolUse hook on Edit|Write. Reads the hook JSON on
# stdin, pulls out the edited file path, and runs style-check.sh on it. If the file has any
# style findings, it prints them to stderr and exits 2 so Claude sees them as feedback and
# fixes real violations (per the on-touch compliance policy) before moving on.
#
# Exit 0 (silent) for non-Kotlin files, missing files, or a clean check.

input=$(cat)

file=$(printf '%s' "$input" | python3 -c "import sys, json; print(json.load(sys.stdin).get('tool_input', {}).get('file_path', ''))" 2>/dev/null)

case "$file" in
    *.kt | *.kts) ;;
    *) exit 0 ;;
esac

[ -f "$file" ] || exit 0

dir=$(cd "$(dirname "$0")" && pwd)

findings=$("$dir/style-check.sh" "$file" 2>/dev/null)

if [ -n "$findings" ]; then
    {
        echo "style-check flagged candidates in the file you just edited ($file)."
        echo "Review each — some advisory recipes have known false positives — and fix real violations per CODE_STYLE_GUIDE.md:"
        echo
        echo "$findings"
    } >&2
    exit 2
fi

exit 0
