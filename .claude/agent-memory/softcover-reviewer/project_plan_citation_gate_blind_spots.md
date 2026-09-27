---
name: project_plan_citation_gate_blind_spots
description: history of blind spots found in the plan-citation gate (doc-guard.sh + CheckDocBudgetsTask); all closed as of the char-literal/YAML-quote fix
metadata:
  type: project
---

All blind spots previously recorded against this gate are closed as of the round reviewed 2026-09-28
(char-literal tracking + YAML quote-aware `#` detection). Do not re-flag any of the items below without
re-verifying fresh — this file exists to save re-deriving the reasoning if a *new* gap surfaces in a future
change to the same extractors, not as a standing list of open issues.

**Closed, in order:**
1. `config/detekt/detekt.yml`'s own stray "S4-5b"/"S4-1" citations, and the gate not scanning `.yml`/`.kts`
   at all — closed when the gate was first extended to `config/**`, `*.yml`/`*.yaml`, `*.kts`.
2. No single-quote/char-literal awareness in `kotlinStyleComment`/`extract_code_comment` — a `"` inside a
   char literal like `'"'` used to flip `inString` with nothing to flip it back, silently swallowing a real
   trailing comment. Closed by adding parallel `inChar`/`in_chr` tracking (mirrors the string-tracking
   branches exactly, including the `\'`-escape and 2-char skip) in both the Kotlin (`kotlinStyleComment`,
   `CheckDocBudgetsTask.kt`) and bash (`extract_code_comment`, `doc-guard.sh`) versions. Verified: both
   `val c = '"' // S4-1` and `val c = '\'' // D17` now correctly deny, in the awk trace and in
   `CheckDocBudgetsTaskTest.kt`'s "char literal holding a double quote" / "...holding an escaped quote" tests.
3. `yamlStyleComment`/`check_yaml_comments` (`${line#*#}`) had zero string-literal awareness — any `#`
   anywhere on a YAML line was treated as a comment start, demonstrably wrong on
   `.github/workflows/issue-status.yml:101`/`:181` (`echo "#$number is not an issue…"`). Closed by adding
   single-quote (`''`-doubling escape) and double-quote (`\"`-escape) tracking, AND requiring `#` to be at
   line-start or preceded by whitespace (the actual YAML comment rule) in both `yamlStyleComment`
   (`CheckDocBudgetsTask.kt`) and `extract_yaml_comment` (`doc-guard.sh`). Verified against the exact
   `issue-status.yml` lines by hand-tracing the quote state machine (the `"` before `#$number` opens a
   string that isn't closed until the line's trailing `"`, so `#` is correctly seen as inside the string),
   and against `doc-guard.cases`' `yaml_hash_inside_double_quote_allow` / `yaml_hash_inside_single_quote_allow`
   / `yaml_hash_no_whitespace_before_allow` / `yaml_hash_after_quote_and_space_deny`.

**Parity check performed:** bash (`extract_code_comment`/`extract_yaml_comment`, awk) and Kotlin
(`kotlinStyleComment`/`yamlStyleComment`) were compared branch-by-branch; both pairs are structurally
identical (same state-machine branches in the same order, same escape/doubling semantics). Both extractors
now carry KDoc (Kotlin side) describing the quote/char-literal handling and its single-line-only scope.

**Why:** a citation gate's whole job is correct detection; a quote-unaware split is a silent bypass
(false negative) or a silent false-deny (false positive) depending on which side is wrong. Worth re-tracing
by hand whenever this state machine changes, since the failure mode is invisible until an adversarial or
just-unlucky line shows up. [[normative-rules-are-not-negotiable]]

**How to apply:** if this gate's extractors change again, re-run the branch-by-branch bash/Kotlin comparison
above before trusting a "gates passed" claim — a passing test suite only covers the cases someone thought to
write. Re-derive at least one live-file trace (a real line in the actual tree, not just a fixture) the way
`issue-status.yml:101` was traced here.
