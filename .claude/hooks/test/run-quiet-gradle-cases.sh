#!/usr/bin/env bash
set -u

here=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
root=$(cd "$here/../../.." && pwd)
quiet="$root/scripts/gradle-quiet.sh"
failures=0

while IFS=$'\t' read -r command expected; do
  [ -z "$command" ] && continue
  [ "$expected" = "=" ] && expected="$command"
  expected="${expected//@Q@/$quiet}"
  input=$(jq -n --arg c "$command" '{tool_input: {command: $c, description: "d", timeout: 5}}')
  output=$(printf '%s' "$input" | CLAUDE_PROJECT_DIR="$root" bash "$here/../quiet-gradle.sh")
  if [ -z "$output" ]; then
    actual="$command"
  else
    actual=$(printf '%s' "$output" | jq -r '.hookSpecificOutput.updatedInput.command')
    kept=$(printf '%s' "$output" | jq -r '.hookSpecificOutput.updatedInput | "\(.description)/\(.timeout)"')
    [ "$kept" = "d/5" ] || { echo "FAIL (fields dropped): $command"; failures=$((failures + 1)); continue; }
  fi
  if [ "$actual" = "$expected" ]; then
    echo "ok    $command"
  else
    echo "FAIL  $command"
    echo "      expected: $expected"
    echo "      actual:   $actual"
    failures=$((failures + 1))
  fi
done < "$here/quiet-gradle.cases"

exit "$failures"
