# ROADMAP.md generation

`ROADMAP.md` at the repo root is a **build output, not a document.** Do not hand-edit it.

The roadmap itself lives in GitHub Issues; the public view is assembled from the
`description` field of each open milestone, so the two cannot drift.

```bash
python3 scripts/roadmap/generate_roadmap.py            # preview to stdout
python3 scripts/roadmap/generate_roadmap.py --write    # write ROADMAP.md
python3 scripts/roadmap/generate_roadmap.py --check    # exit 1 if stale (used by CI)
```

Reads only public data, so no token is needed locally — one is used in CI purely to raise
the rate limit.

## How it fits together

| Piece | Role |
|---|---|
| Milestone `description` (on GitHub) | One section of the public roadmap — including "Under consideration", which is a milestone too |
| `header.md` | The static intro and caveats block |
| `versionName` in `app/build.gradle.kts` | Fills the "Current release" line |

`.github/workflows/roadmap.yml` runs this on any milestone change — and on a push to `main`
touching `app/build.gradle.kts` or `scripts/roadmap/**`, since the "Current release" line comes
from `versionName` and the intro from `header.md` — then commits the result to `main`. The in-app Roadmap screen fetches the raw file at runtime, so a milestone copy edit
reaches users with no app release. On a pull request the same workflow runs `--check`, which
fails if someone hand-edited the file — that guard is what keeps "generated" true.

## Two behaviours worth knowing

**Closing a milestone removes its section.** The generator reads *open* milestones only, so
closing one on release drops it from the public roadmap. That's intended — shipped releases
leave the roadmap — but it makes closing a milestone a user-visible act.

**It refuses to run on a partial set.** If any open milestone has an empty description the
generator exits rather than silently deleting whole releases from the public file. Give the
milestone a description, or pass `--allow-partial` if the omission is deliberate.

## What stays hand-written

Only `header.md`, and within it one sentence describing what the current release *shipped* —
that's release history rather than plan, which is why it doesn't belong on a milestone.
Update it at release time; the version number itself is read from Gradle.

## The one residual risk

Everything about the *plan* is derived, but `header.md`'s "what the current release shipped"
sentence is prose no machine can regenerate. The version number beside it is read from Gradle
and a push to `app/build.gradle.kts` regenerates the file, so the number cannot go stale — but
the sentence can. It is the only line in `ROADMAP.md` that relies on someone remembering, and
release time is when to check it.
