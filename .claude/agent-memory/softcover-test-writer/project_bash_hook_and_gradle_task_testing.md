---
name: project_bash_hook_and_gradle_task_testing
description: Patterns for testing a Claude-Code bash hook (PreToolUse) and a Gradle build-logic task
metadata:
  type: project
---

Techniques for `.claude/hooks/doc-guard.sh` and `build-logic/.../CheckDocBudgetsTask.kt`:

- **Bash hook test harness.** A base64-encoded TSV cases file (name, tool, relpath, old content, arg1, arg2,
  replace_all, expected, reason-substring) plus a runner that decodes each field, builds the `tool_input` JSON
  the hook reads, writes fixtures under a fresh `mktemp -d` per case (as `CLAUDE_PROJECT_DIR`), and checks
  `hookSpecificOutput.permissionDecision`. Base64 avoids TSV/quoting escapes. Check byte-sensitive fixtures
  with `wc -c` before encoding them.

- **Gradle task test without GradleRunner.** For a `DefaultTask` with a public `@TaskAction`,
  `ProjectBuilder` (from `testImplementation(gradleTestKit())`) plus `project.tasks.create(...)` lets you set
  properties and call the action directly — fast, and it runs the real production code.

- **Git merge-base fixtures are single-branch.** In a one-branch temp repo `merge-base == HEAD`, so only
  commit-then-modify makes "current" differ from "baseline". For a **new-file-over-budget** case, commit a
  placeholder, then write the target file only into the working tree so `git show <merge-base>:path` is null.

- **`doc-guard.sh` checks your own Bash commands too.** A literal `cp ... docs/reference/foo.md`-style string
  inline in a Bash heredoc is denied by the hook under test. Put such strings in a script created via `Write`
  and only `bash`-invoke that file.

- **The cases harness applies Edit by substring.** `compute_new_content` splits the whole file on
  `old_string` with jq's `split($olds)`, so `"- x"` also matches the start of an earlier `"- xxxx…"` bullet and
  the fixture comes out wrong. Anchor `old_string` on a unique run (e.g. `"## Now\n- x"`).
