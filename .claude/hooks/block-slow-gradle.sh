#!/usr/bin/env bash
# PreToolUse(Bash) guard: block long-running Gradle tasks that trigger iOS/native release
# framework linking or full multi-target assembly. These take 45+ minutes and are never
# wanted except a deliberate PR acceptance build.
#
# Bypass: prefix the command with ACCEPTANCE_BUILD=1 to allow the full build through.
set -u

input=$(cat)
cmd=$(printf '%s' "$input" | jq -r '.tool_input.command // ""')

# Only act on Gradle invocations (gradlew, or a bare `gradle ` token — not paths like ~/.gradle/).
if ! printf '%s' "$cmd" | grep -Eq 'gradlew|(^|[[:space:]])gradle[[:space:]]'; then
  exit 0
fi

# Explicit bypass for the deliberate acceptance build.
if printf '%s' "$cmd" | grep -q 'ACCEPTANCE_BUILD=1'; then
  exit 0
fi

block=0
reason=""

# assemble: allow only assembleDebug* variants; block bare `assemble` and any non-debug variant.
while IFS= read -r tok; do
  [ -z "$tok" ] && continue
  low=$(printf '%s' "$tok" | tr '[:upper:]' '[:lower:]')
  case "$low" in
    assembledebug*) : ;;
    assemble*)
      block=1
      reason="an aggregate / non-debug assemble task ('$tok')"
      ;;
  esac
done < <(printf '%s' "$cmd" | grep -oiE '(^|[[:space:]:])assemble[a-z0-9]*' | sed -E 's/^[[:space:]:]+//')

# bare `build` task (not /build/ paths or --build-cache flags).
if [ "$block" -eq 0 ] && printf '%s' "$cmd" | grep -Eiq '(^|[[:space:]:])build([[:space:]]|$)'; then
  block=1
  reason="the aggregate 'build' task"
fi

# Native / iOS / release-link / publish tasks.
if [ "$block" -eq 0 ] && printf '%s' "$cmd" | grep -Eiq 'linkrelease|releaseframework|iosarm64|iossimulatorarm64|iosx64|binaries|embedandsign|bundlerelease|publish'; then
  block=1
  reason="an iOS/native link, release-framework, or publish task"
fi

if [ "$block" -eq 1 ]; then
  cat >&2 <<MSG
BLOCKED: this Gradle command runs $reason, which triggers iOS/native release framework
linking or full multi-target assembly (45+ min) and is never wanted here. Use a lightweight
task instead:
  - ./gradlew assembleDebug                  (Android debug across all modules)
  - ./gradlew :<module>:compileDebug*        (compile a single module)
  - ./gradlew :<module>:test*HostTest        (run a module's host unit tests)
If you genuinely need the full acceptance build (e.g. before opening a PR), prefix it:
  ACCEPTANCE_BUILD=1 ./gradlew <task>
MSG
  exit 2
fi

exit 0
