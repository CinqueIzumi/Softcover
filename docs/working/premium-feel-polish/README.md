# Premium-feel polish

Single-screen polish that makes the app feel premium: motion, haptics, depth and missing affordances on one
screen at a time. The shared motion, press, haptics and transition contracts are already in the component
library; every row here builds on them and never adds a second system. The work list is
[`findings.md`](findings.md). Nothing is filed as a GitHub issue. Not scheduled: starts when the user picks it
up.

> [!WARNING]
> **Do not build anything from `findings.md` as it stands.** The first attempt applied these rows straight
> from the audit. It produced changes the user did not want and a long tail of UI bugs, and all of it was
> discarded after a device check. Before any implementer brief:
>
> 1. **Re-audit.** The findings are from 2026-10 and the app has moved on. Check every row against the
>    current code: drop the ones already done or no longer true, fix the paths, and sweep every screen and
>    sheet again for new findings.
> 2. **Walk every row with the user.** One row at a time: what changes on screen, on which platforms, and
>    whether it adds UI no screen had before. The user approves, rewrites or cuts each one. Nothing is
>    approved by default, and an approval from the old audit does not carry over.
> 3. **Build in small groups.** One screen group per round, each checked on a device by the user before
>    the next starts.

## Now

- **State:** written 2026-10-13 from the approved surface rows of the premium-feel audit; nothing built.
- **Next:** step 0 below: re-audit `findings.md` against the current code and sweep for new findings.
- **Open questions:** none.
- **Uncommitted:** this file and `findings.md`.

## Steps

- [ ] **0. Re-audit:** parallel `general-purpose` agents per feature check every row against the code and sweep for new findings; `findings.md` is rewritten to the result.
- [ ] **1. Walk the findings with the user:** every row approved, rewritten or cut, with the verdict recorded on the row.

Then one `softcover-implementer` brief per screen group, with tests through `softcover-test-writer`, then a
`softcover-reviewer` round and a device check by the user before the group's box is ticked.

| Group | Rows | Status |
|---|---|---|
| Library | S1–S7, O19, O33 | [ ] |
| Reading and Focus Mode | S8–S17, O16–O18, O34, O37 | [ ] |
| Book detail | S18–S26, O21–O25 | [ ] |
| Explore | S28–S33, O26–O28 | [ ] |
| Profile | S34, S36 | [ ] |
| Onboarding | S37, S42, S44, O39, O40 | [ ] |
| Settings | S38–S41, S43, S45 | [ ] |
| Scanner | S46–S48, O31 | [ ] |
| Share | S49, S50 | [ ] |
| Desktop | S51, O30 | [ ] |
| Lists | O20 | [ ] |

## Decisions

- All motion is gated by `playDecorativeMotion()`; haptics are Android and iOS only; predictive back is Android only.
- A row that adds UI no screen had before is checked against the preserve-existing-UI rule with the user first.
