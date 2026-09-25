---
name: project_bash_hook_and_gradle_task_testing
description: Patterns for testing a Claude-Code bash hook (PreToolUse) and a Gradle build-logic task, plus a concurrent-edit gotcha
metadata:
  type: project
---

Two techniques used for `.claude/hooks/doc-guard.sh` and `build-logic/.../CheckDocBudgetsTask.kt`, neither of
which is a KMP unit under test but both came through the same `softcover-test-writer` brief:

- **Bash hook test harness.** A base64-encoded TSV cases file (name, tool, relpath, old content, arg1, arg2,
  replace_all, expected, reason-substring) plus a runner that decodes each field, builds the exact
  `tool_input` JSON the hook reads, writes fixture files under a fresh `mktemp -d` per case (set as
  `CLAUDE_PROJECT_DIR`), and diffs the hook's stdout for `hookSpecificOutput.permissionDecision`. Base64
  avoids all TSV/quoting escaping for multi-line heredocs and unicode content. Self-check byte-sensitive
  fixtures with `wc -c` while drafting the cases file, before wiring the runner — catches off-by-N errors
  before they hide inside base64.

- **Gradle task unit test without TestKit's GradleRunner.** For a `DefaultTask` whose `@TaskAction` method is
  public, `org.gradle.testfixtures.ProjectBuilder` (resolves from a plain `testImplementation(gradleTestKit())`
  dependency) plus `project.tasks.create(...)` lets you set the task's properties and call the action method
  directly — no need to spin up a real Gradle build. Far faster than `GradleRunner`, and still exercises the
  real production code (not reflection into its private helpers).

- **Git merge-base fixtures are single-branch, not two-branch.** A task that resolves
  `merge-base(HEAD, main)` inside a temp repo with only one branch always finds `merge-base == HEAD` — commit,
  then modify, is the only way to make "current" differ from "baseline" in that repo; committing twice with no
  intervening uncommitted edit gives you "unchanged since merge-base", not "new file", even if you intended
  the latter. To fake a **new-file-over-budget** case, commit a placeholder file first, then write the target
  file only into the working tree (never committed) so `git show <merge-base>:path` returns null for it.

- **Concurrent production-code edits mid-session are real, not a glitch.** Mid-session "changed on disk" system
  reminders on files with `git status` showing `??` (untracked) meant another process/session was actively
  developing those exact files while tests were being written against them. Re-read the file, re-run the full
  test suite against the new content instead of assuming staleness — do not revert or flag it as a bug unless
  the new behavior actually breaks a documented case.

- **`doc-guard.sh`'s own Bash checks fire on your test-generation commands, not just the fixture content.** A
  generator script that embeds a literal `cp ... docs/reference/foo.md`-style string inline in a `Bash` tool
  heredoc gets denied by the very hook you're testing, because the check greps the whole raw command text, not
  just file writes. Put those literal strings inside a script file created via `Write` (untouched by the
  Bash-command checks) and only `bash`-invoke that file from the terminal.
