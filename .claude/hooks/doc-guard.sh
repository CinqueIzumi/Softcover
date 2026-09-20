#!/usr/bin/env bash
# Denies markdown edits that break a doc budget, its ratchet, or the reference no-history rule.
set -u

input=$(cat)
root="${CLAUDE_PROJECT_DIR:-$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)}"
budgets_file="$root/docs/doc-budgets.txt"
active_file="$root/docs/working/ACTIVE.md"
history_pattern='\b(used to|previously|no longer|was (changed|replaced|renamed)|we decided|(that|this|which) replaced|originally)\b'

deny() {
  jq -n --arg reason "$1" \
    '{hookSpecificOutput: {hookEventName: "PreToolUse", permissionDecision: "deny", permissionDecisionReason: $reason}}'
  exit 0
}

file_bytes() {
  if [ -f "$1" ]; then wc -c < "$1" | tr -d ' '; else echo 0; fi
}

file_lines() {
  if [ -f "$1" ]; then grep -c '' "$1"; else echo 0; fi
}

glob_to_ere() {
  local glob="$1" pattern="^" i=0 len ch
  len=${#glob}
  while [ "$i" -lt "$len" ]; do
    if [ "${glob:$i:2}" = "**" ]; then
      pattern="${pattern}.*"
      i=$((i + 2))
      continue
    fi
    ch="${glob:$i:1}"
    case "$ch" in
      '*') pattern="${pattern}[^/]*" ;;
      '.' | '^' | '$' | '+' | '?' | '(' | ')' | '[' | ']' | '{' | '}' | '|' | '\')
        pattern="${pattern}\\${ch}" ;;
      *) pattern="${pattern}${ch}" ;;
    esac
    i=$((i + 1))
  done
  printf '%s$' "$pattern"
}

parse_limit() {
  case "$1" in
    *KB) printf '%s B' "$(( ${1%KB} * 1024 ))" ;;
    *B) printf '%s B' "${1%B}" ;;
    *L) printf '%s L' "${1%L}" ;;
  esac
}

find_budget() {
  local relpath="$1" line glob token regex
  while IFS= read -r line || [ -n "$line" ]; do
    case "$line" in
      "## Now"*) continue ;;
      "#"*) continue ;;
      "") continue ;;
    esac
    glob=$(printf '%s' "$line" | awk '{print $1}')
    token=$(printf '%s' "$line" | awk '{print $NF}')
    regex=$(glob_to_ere "$glob")
    if [[ "$relpath" =~ $regex ]]; then
      printf '%s %s' "$glob" "$token"
      return 0
    fi
  done < "$budgets_file"
  return 1
}

now_limit_token() {
  grep '^## Now' "$budgets_file" | awk '{print $NF}'
}

extract_now_section() {
  [ -f "$1" ] || return 0
  awk '
    /^## Now[[:space:]]*$/ { capture=1; print; next }
    capture && /^## / { exit }
    capture { print }
  ' "$1"
}

routing_hint() {
  case "$1" in
    docs/reference/design-system/components.md)
      printf '%s' "a component contract goes in KDoc on its UI model; components.md keeps one line per component" ;;
    docs/reference/design-system/*)
      printf '%s' "rewrite the entry to the current truth (≤12 lines)" ;;
    docs/working/*)
      printf '%s' "working docs hold state and one-line decisions; reasoning goes in the PR description" ;;
    *)
      printf '%s' "see the routing table in .claude/rules/docs.md" ;;
  esac
}

check_budget() {
  local relpath="$1" new_file="$2" old_file="$3" budget glob token value unit new_size old_size

  budget=$(find_budget "$relpath") || return 0
  glob="${budget% *}"
  token="${budget##* }"
  read -r value unit <<< "$(parse_limit "$token")"

  if [ "$unit" = "L" ]; then
    new_size=$(file_lines "$new_file")
    old_size=$(file_lines "$old_file")
  else
    new_size=$(file_bytes "$new_file")
    old_size=$(file_bytes "$old_file")
  fi

  if [ "$new_size" -gt "$value" ] && [ "$new_size" -gt "$old_size" ]; then
    deny "$relpath exceeds its budget ($glob $token): ${old_size}${unit} → ${new_size}${unit} (limit ${value}${unit}). $(routing_hint "$relpath")"
  fi
}

check_now_section() {
  local relpath="$1" new_file="$2" old_file="$3" path
  [ -f "$active_file" ] || return 0
  grep -qxF -- "$relpath" "$active_file" || return 0

  local now_limit new_now old_now new_tmp old_tmp
  now_limit=$(now_limit_token)
  [ -n "$now_limit" ] || return 0
  now_limit="${now_limit%L}"

  new_tmp=$(mktemp)
  old_tmp=$(mktemp)
  extract_now_section "$new_file" > "$new_tmp"
  extract_now_section "$old_file" > "$old_tmp"
  new_now=$(file_lines "$new_tmp")
  old_now=$(file_lines "$old_tmp")
  rm -f "$new_tmp" "$old_tmp"

  if [ "$new_now" -gt "$now_limit" ] && [ "$new_now" -gt "$old_now" ]; then
    deny "$relpath ## Now section exceeds its budget (## Now ${now_limit}L): ${old_now}L → ${new_now}L (limit ${now_limit}L). $(routing_hint "$relpath")"
  fi
}

check_history() {
  local relpath="$1" new_file="$2" old_file="$3" line
  case "$relpath" in
    docs/reference/*) ;;
    *) return 0 ;;
  esac

  while IFS= read -r line || [ -n "$line" ]; do
    [ -n "$line" ] || continue
    grep -qxF -- "$line" "$old_file" 2>/dev/null && continue
    case "$line" in
      *'<!-- history-ok -->'*) continue ;;
    esac
    if printf '%s' "$line" | grep -iEq "$history_pattern"; then
      deny "$relpath adds a history line: \"$line\" — rewrite to present tense, or mark the line \`<!-- history-ok -->\`."
    fi
  done < "$new_file"
}

handle_markdown_write() {
  local path old_file rc relpath
  path=$(printf '%s' "$input" | jq -r '.tool_input.file_path // ""')
  case "$path" in
    "$root"/*) ;;
    *) return 0 ;;
  esac
  case "$(printf '%s' "$path" | tr '[:upper:]' '[:lower:]')" in
    *.md) ;;
    *) return 0 ;;
  esac

  if [ -f "$path" ]; then old_file="$path"; else old_file="/dev/null"; fi

  tmp_new=$(mktemp)
  tmp_err=$(mktemp)
  trap 'rm -f "$tmp_new" "$tmp_err"' EXIT
  printf '%s' "$input" | jq -j --rawfile old "$old_file" '
    .tool_input as $ti
    | ($old) as $o
    | if .tool_name == "Write" then
        ($ti.content // "")
      else
        ($ti.old_string // "") as $olds
        | ($ti.new_string // "") as $news
        | ($ti.replace_all // false) as $all
        | ($o | split($olds)) as $parts
        | if ($parts|length) < 2 then error("DOC_GUARD_NOT_FOUND")
          elif $all then ($parts | join($news))
          else ($parts[0] + $news + ($parts[1:] | join($olds)))
          end
      end
  ' > "$tmp_new" 2> "$tmp_err"
  rc=$?
  if [ "$rc" -ne 0 ]; then
    grep -q DOC_GUARD_NOT_FOUND "$tmp_err" && return 0
    deny "doc-guard could not evaluate the edit: $(tr '\n' ' ' < "$tmp_err")"
  fi

  relpath="${path#"$root"/}"
  check_budget "$relpath" "$tmp_new" "$old_file"
  check_now_section "$relpath" "$tmp_new" "$old_file"
  check_history "$relpath" "$tmp_new" "$old_file"
}

handle_bash() {
  local command
  command=$(printf '%s' "$input" | jq -r '.tool_input.command // ""')

  if printf '%s' "$command" | grep -qE '>{1,2}[[:space:]]*[^[:space:]&][^[:space:]]*\.md([[:space:]]|[;&|]|$)'; then
    deny "edit markdown with Edit/Write so doc-guard sees the change"
  fi

  if printf '%s' "$command" | grep -qE '(^|[^[:alnum:]_])tee([[:space:]]|$)'; then
    printf '%s' "$command" | grep -qE '\.md([[:space:]]|[;&|]|$)' &&
      deny "edit markdown with Edit/Write so doc-guard sees the change"
  fi

  if printf '%s' "$command" | grep -qE '(^|[^[:alnum:]_])(sed|perl)([^;&|]*)[[:space:]]-i([[:space:]]|$)'; then
    printf '%s' "$command" | grep -qE '\.md([[:space:]]|[;&|]|$)' &&
      deny "edit markdown with Edit/Write so doc-guard sees the change"
  fi

  if printf '%s' "$command" | grep -qE '(^|[^[:alnum:]_])(python3?|node|ruby|perl)([[:space:]].*)?[[:space:]](-c|-e)([[:space:]]|$)'; then
    printf '%s' "$command" | grep -q '\.md' &&
      deny "edit markdown with Edit/Write so doc-guard sees the change"
  fi

  if printf '%s' "$command" | grep -qE '(^|[^[:alnum:]_])(python3?|node|ruby|perl)[[:space:]][^|;&]*<<'; then
    printf '%s' "$command" | grep -q '\.md' &&
      deny "edit markdown with Edit/Write so doc-guard sees the change"
  fi

  if printf '%s' "$command" | grep -qE '(^|[^[:alnum:]_])cp([[:space:]]|$)'; then
    case "$(printf '%s' "$command" | awk '{print $NF}')" in
      *.md) deny "edit markdown with Edit/Write so doc-guard sees the change" ;;
    esac
  fi

  if printf '%s' "$command" | grep -qE '(^|[^[:alnum:]_])dd([[:space:]]|$)'; then
    printf '%s' "$command" | grep -qE '(^|[[:space:]])of=[^[:space:]]*\.md([[:space:]]|$)' &&
      deny "edit markdown with Edit/Write so doc-guard sees the change"
  fi

  if printf '%s' "$command" | grep -qE '(^|[^[:alnum:]_])rsync([[:space:]]|$)'; then
    case "$(printf '%s' "$command" | awk '{print $NF}')" in
      *.md) deny "edit markdown with Edit/Write so doc-guard sees the change" ;;
    esac
  fi

  if printf '%s' "$command" | grep -qE '(^|[^[:alnum:]_])awk([[:space:]].*)?[[:space:]]-i[[:space:]]+inplace'; then
    printf '%s' "$command" | grep -q '\.md' &&
      deny "edit markdown with Edit/Write so doc-guard sees the change"
  fi
}

tool_name=$(printf '%s' "$input" | jq -r '.tool_name // ""')
case "$tool_name" in
  Edit | Write) handle_markdown_write ;;
  Bash) handle_bash ;;
esac
exit 0
