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

# Rule: no fully-qualified references inline — import and use the short name.
# (Imprecise: KDoc/@see tags and string literals may match — review each.)
check_fq_refs() {
    local out
    out=$(grep -HnE '(androidx|java|kotlin|kotlinx|nl\.rhaydus)\.[a-z][a-zA-Z._]+\.[A-Z]' "${targets[@]}" 2>/dev/null \
        | grep -vE ':[0-9]+:[[:space:]]*(import |package |\*|//|/\*|@)')
    if [ -n "$out" ]; then
        section "[advisory] Inline fully-qualified reference — add an import, use the short name (§Imports)"
        printf '%s\n' "$out"
        advisory_hits=$((advisory_hits + 1))
    fi
}

# Rule: one top-level type per file (data-source interface+Impl colocation is the sanctioned exception).
check_one_type_per_file() {
    local f count out=""
    for f in "${targets[@]}"; do
        count=$(grep -cE '^(class |data class |enum class |sealed class |sealed interface |interface |object )' "$f" 2>/dev/null)
        if [ "${count:-0}" -gt 1 ]; then
            out+="$f: $count top-level types"$'\n'
        fi
    done
    if [ -n "$out" ]; then
        section "[advisory] More than one top-level type in a file — one type per file, named after it (§Files & Organization)"
        printf '%s' "$out"
        advisory_hits=$((advisory_hits + 1))
    fi
}

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

# Rule: project imports (nl.rhaydus.*) are alphabetical within their group.
check_import_order() {
    local f block out=""
    for f in "${targets[@]}"; do
        block=$(grep -E '^import nl\.rhaydus' "$f" 2>/dev/null)
        [ -z "$block" ] && continue
        if ! printf '%s\n' "$block" | sort -c >/dev/null 2>&1; then
            out+="$f: nl.rhaydus.* import block is not alphabetically sorted"$'\n'
        fi
    done
    if [ -n "$out" ]; then
        section "[advisory] Project import block out of alphabetical order (§Imports)"
        printf '%s' "$out"
        advisory_hits=$((advisory_hits + 1))
    fi
}

# ----- run --------------------------------------------------------------------------------

check_fq_refs
check_one_type_per_file
check_import_order
check_unguarded_flow_terminal

if [ "$error_hits" -gt 0 ]; then
    printf '\nstyle-check: %d error-tier rule(s), %d advisory rule(s) with findings.\n' "$error_hits" "$advisory_hits"
    exit 2
fi

if [ "$advisory_hits" -gt 0 ]; then
    printf '\nstyle-check: %d advisory rule(s) with findings (review; some recipes have known false positives).\n' "$advisory_hits"
fi

exit 0
