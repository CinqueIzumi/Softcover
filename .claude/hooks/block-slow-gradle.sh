#!/usr/bin/env bash
# PreToolUse(Bash) guard: block long-running Gradle tasks that trigger iOS/native release
# framework linking or full multi-target assembly. These take 45+ minutes and are never
# wanted except a deliberate PR acceptance build.
#
# NOT blocked: `compileKotlinIos*` klib compiles. Those are the *compilation* half of an iOS
# target and finish in seconds — they are how you verify iOS `actual` implementations without
# ever linking a framework. Blocking them made iOS source unverifiable and pushed whole
# adoption batches into "can't check that here" territory. Linking, framework assembly,
# simulator test runs, and publishing stay blocked.
#
# Bypass: prefix the command with ACCEPTANCE_BUILD=1 to allow anything through.
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

# Only the Gradle invocation itself carries task names. Everything downstream of a pipe or a
# command separator is someone's `grep`/`echo`, and matching task keywords in *that* text is how
# `... | grep -E "SUCCESSFUL|FAILED"; echo "build done"` used to get blocked. Split the command on
# separators and inspect only the segments that actually invoke Gradle.
gradle_segments=$(
  printf '%s' "$cmd" |
    sed -E 's/(\|\||&&|;|\|)/\n/g' |
    grep -E 'gradlew|(^|[[:space:]])gradle[[:space:]]'
)

block=0
reason=""

check_segment() {
  local seg="$1" tok low task

  # assemble: allow only assembleDebug* variants; block bare `assemble` and any non-debug variant.
  while IFS= read -r tok; do
    [ -z "$tok" ] && continue
    low=$(printf '%s' "$tok" | tr '[:upper:]' '[:lower:]')
    case "$low" in
      assembledebug*) : ;;
      assemble*)
        block=1
        reason="an aggregate / non-debug assemble task ('$tok')"
        return
        ;;
    esac
  done < <(printf '%s' "$seg" | grep -oiE '(^|[[:space:]:])assemble[a-z0-9]*' | sed -E 's/^[[:space:]:]+//')

  # bare `build` task (not /build/ paths or --build-cache flags).
  if printf '%s' "$seg" | grep -Eiq '(^|[[:space:]:])build([[:space:]]|$)'; then
    block=1
    reason="the aggregate 'build' task"
    return
  fi

  # Unconditionally slow: release linking, framework assembly, Xcode integration, publishing.
  if printf '%s' "$seg" | grep -Eiq 'linkrelease|linkdebug|releaseframework|binaries|embedandsign|bundlerelease|publish'; then
    block=1
    reason="an iOS/native link, release-framework, or publish task"
    return
  fi

  # Any remaining iOS/native task is allowed only if it is a klib *compile*. A simulator test run
  # (`iosSimulatorArm64Test`) boots a device and is not.
  while IFS= read -r tok; do
    [ -z "$tok" ] && continue
    task=$(printf '%s' "$tok" | sed -E 's/.*://')            # strip a :module:path prefix
    low=$(printf '%s' "$task" | tr '[:upper:]' '[:lower:]')
    case "$low" in
      compilekotlinios*|compileiosmainkotlinmetadata*) : ;;  # fast klib compile — allowed
      *)
        block=1
        reason="a non-compile iOS/native task ('$task')"
        return
        ;;
    esac
  done < <(printf '%s' "$seg" | tr ' ' '\n' | grep -iE 'ios(arm64|simulatorarm64|x64)|iosmain')
}

while IFS= read -r seg; do
  [ -z "$seg" ] && continue
  [ "$block" -eq 1 ] && break
  check_segment "$seg"
done <<< "$gradle_segments"

if [ "$block" -eq 1 ]; then
  cat >&2 <<MSG
BLOCKED: this Gradle command runs $reason, which triggers iOS/native release framework
linking or full multi-target assembly (45+ min) and is never wanted here. Use a lightweight
task instead:
  - ./gradlew assembleDebug                     (Android debug across all modules)
  - ./gradlew :<module>:compileDebug*           (compile a single module)
  - ./gradlew :<module>:test*HostTest           (run a module's host unit tests)
  - ./gradlew :<module>:compileKotlinIosSimulatorArm64   (verify iOS actuals — allowed, seconds)
If you genuinely need the full acceptance build (e.g. before opening a PR), prefix it:
  ACCEPTANCE_BUILD=1 ./gradlew <task>
MSG
  exit 2
fi

exit 0
