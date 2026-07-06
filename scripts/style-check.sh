#!/usr/bin/env bash
#
# style-check.sh — deterministic enforcement of the mechanical CODE_STYLE_GUIDE.md rules.
#
# Ports the code-reviewer's documented grep recipes (see
# .claude/agent-memory/code-reviewer/style_conventions.md) into a script so the
# "patient layout sweep" stops depending on an LLM remembering to run it.
#
# Usage:
#   scripts/style-check.sh [file ...]    # check the given .kt/.kts files
#   scripts/style-check.sh               # check changed files (git diff + staged)
#
# Output: findings grouped by rule, as  <file>:<line>: <code>  with a one-line reminder.
#
# Exit codes:
#   0  no findings, or advisory-only findings
#   2  at least one ERROR-tier finding (use for a CI / pre-commit gate)
#
# Severity:
#   ERROR    — high-precision rule, safe to gate a build on (currently: boolean ! negation).
#   ADVISORY — useful but grep-imprecise (multi-arg wrapping, FQ refs, one-type-per-file,
#              import order, composable blank lines, unguarded terminal flow reads). Surfaced
#              for review, never gates CI.
#              The PostToolUse hook surfaces ALL findings on files Claude just edited, so the
#              advisory rules are still enforced in practice via the on-touch policy.

set -u

# ----- resolve target files -----------------------------------------------------------------

files=()
if [ "$#" -gt 0 ]; then
    for f in "$@"; do
        files+=("$f")
    done
else
    while IFS= read -r f; do
        [ -n "$f" ] && files+=("$f")
    done < <(
        {
            git diff --name-only --diff-filter=ACMR
            git diff --name-only --cached --diff-filter=ACMR
            git ls-files --others --exclude-standard
        } 2>/dev/null | sort -u
    )
fi

# keep only existing .kt/.kts files outside build/
targets=()
for f in "${files[@]:-}"; do
    case "$f" in
        *build/*) continue ;;
        *.kt | *.kts) ;;
        *) continue ;;
    esac
    [ -f "$f" ] && targets+=("$f")
done

if [ "${#targets[@]}" -eq 0 ]; then
    exit 0
fi

error_hits=0
advisory_hits=0

# print a section header once, lazily, when its first hit appears
section() {
    printf '\n%s\n' "$1"
}

# ----- ADVISORY tier ------------------------------------------------------------------------
#
# Note: the layout rules the custom ktlint ruleset (:ktlint-rules) owns — multi-arg one-per-line
# wrapping, trailing commas, super/Timber.e blank lines, region flush, brace blank lines, sibling
# composable blank lines, and boolean `!` → `.not()` — are auto-fixed/gated by `./gradlew
# ktlintFormat` / `ktlintCheck` and are intentionally NOT duplicated here. This script covers only
# the rules ktlint does not.
#
# The five greppable recipes this script used to carry — inline fully-qualified references,
# one-type-per-file, project-import ordering, inline mockk stubs, and bare runCatching in a use case —
# were promoted to blocking rules in the foundation `nl.rhaydus:ktlint-rules` ruleset (F7) and are now
# gated by `ktlintCheck`, so they were retired from here. Only the unguarded-terminal-flow-read recipe
# remains: its type-resolved counterpart lives in `nl.rhaydus:detekt-rules` (F1), which Softcover has
# not wired yet, so this advisory recipe stays until that detekt rule is adopted.

# Rule: a terminal flow read must never be able to crash the app.
# `.first()` / `.single()` throw on an empty flow, and any terminal re-throws an upstream error
# (DataStore / network / Apollo / repository). A bare `.first(` / `.single(` is a crash risk —
# guard it (`.firstOrNull()` + default + `.catch` / cancellation-aware `runCatching`) or consume
# the flow reactively via a TOAD Collector. Production source only (test code controls its flows).
# (Imprecise: List/Iterable `.first()`/`.single()` also match — review each; confirm not empty.)
check_unguarded_flow_terminal() {
    local f out=""
    for f in "${targets[@]}"; do
        case "$f" in
            */src/*[Tt]est*/*) continue ;; # production source sets only (commonMain/androidMain/…)
        esac
        local hits
        hits=$(grep -HnE '\.(first|single)[[:space:]]*[({]' "$f" 2>/dev/null)
        [ -n "$hits" ] && out+="$hits"$'\n'
    done
    if [ -n "$out" ]; then
        section "[advisory] Terminal flow read (.first()/.single()) can crash on an empty/erroring flow — guard it (.firstOrNull() + default + .catch / runCatching) or consume via a Collector; never let a flow read crash the app (§Error Handling)"
        printf '%s' "$out"
        advisory_hits=$((advisory_hits + 1))
    fi
}

# ----- run --------------------------------------------------------------------------------

check_unguarded_flow_terminal

if [ "$error_hits" -gt 0 ]; then
    printf '\nstyle-check: %d error-tier rule(s), %d advisory rule(s) with findings.\n' "$error_hits" "$advisory_hits"
    exit 2
fi

if [ "$advisory_hits" -gt 0 ]; then
    printf '\nstyle-check: %d advisory rule(s) with findings (review; some recipes have known false positives).\n' "$advisory_hits"
fi

exit 0
