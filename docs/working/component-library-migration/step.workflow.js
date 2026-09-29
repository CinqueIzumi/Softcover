export const meta = {
  name: 'component-library-step',
  description: 'Build one component-library step file unattended: implement, verify, test, review and snapshot per sub-step, then write convergence proposals',
  whenToUse: 'A step file whose ## Run mode says Workflow mode, after its Part 1 design is approved (family-procedure.md § Workflow mode)',
  phases: [
    { title: 'Preflight', detail: 'clean tree, approved shape complete' },
    { title: 'Build', detail: 'per sub-step: implement → test → review → snapshot' },
    { title: 'Converge', detail: 'Phase 3 audit, proposals written to the step file' },
  ],
}

const PLAN = 'docs/working/component-library-migration'
const JDK = 'JAVA_HOME=/Users/bartpeereboom/Library/Java/JavaVirtualMachines/jbr-21.0.11/Contents/Home'
const { stepFiles, subSteps, snapshotPrefix, convergence, resumeAfter } = args
const stepPaths = stepFiles.map(f => `${PLAN}/${f}`)
const stepList = stepPaths.map(p => `\`${p}\``).join(', ')
const MAX_FIX_ROUNDS = 2

const SNAPSHOT = {
  type: 'object',
  properties: { tree: { type: 'string' } },
  required: ['tree'],
}
const PREFLIGHT = {
  type: 'object',
  properties: { ok: { type: 'boolean' }, problems: { type: 'array', items: { type: 'string' } } },
  required: ['ok', 'problems'],
}
const IMPLEMENTED = {
  type: 'object',
  properties: {
    verifyPassed: { type: 'boolean' },
    filesChanged: { type: 'array', items: { type: 'string' } },
    needsTests: { type: 'array', items: { type: 'string' } },
    deferred: { type: 'array', items: { type: 'string' } },
    summary: { type: 'string' },
  },
  required: ['verifyPassed', 'filesChanged', 'needsTests', 'deferred', 'summary'],
}
const TESTED = {
  type: 'object',
  properties: {
    allPassed: { type: 'boolean' },
    failures: {
      type: 'array',
      items: {
        type: 'object',
        properties: {
          test: { type: 'string' },
          diagnosis: { type: 'string' },
          cause: { type: 'string', enum: ['production', 'test-side'] },
        },
        required: ['test', 'diagnosis', 'cause'],
      },
    },
    summary: { type: 'string' },
  },
  required: ['allPassed', 'failures', 'summary'],
}
const REVIEWED = {
  type: 'object',
  properties: {
    blockers: { type: 'array', items: { type: 'string' } },
    nonBlocking: { type: 'array', items: { type: 'string' } },
  },
  required: ['blockers', 'nonBlocking'],
}

const snapshot = (sub, ref) => agent(
  `Run exactly this in the repo root and return the printed tree id. Change nothing else.\n\n` +
  '```\n' +
  'S=$(mktemp -d) && GIT_INDEX_FILE=$S/idx git read-tree HEAD && GIT_INDEX_FILE=$S/idx git add -A && ' +
  't=$(GIT_INDEX_FILE=$S/idx git write-tree) && ' +
  (ref ? `git update-ref refs/${snapshotPrefix}-snapshots/${ref} "$t" && ` : '') +
  'rm -rf "$S" && echo "$t"\n' +
  '```',
  { label: `snapshot:${ref ?? sub}`, phase: sub, schema: SNAPSHOT, effort: 'low' },
).then(r => r.tree)

const implementBrief = (sub, extra) => `## Goal
Build sub-step ${sub} of the component-library migration, exactly as the step file's \`## Approved shape\` decides.
${extra ?? ''}

## Files
- The step file among ${stepList} whose \`## Sub-steps\` table lists ${sub}: read \`## Approved shape\`, \`## Run mode\` and ${sub}'s inventory table. Do not read other step files.
- \`${PLAN}/family-procedure.md\` § Phase 2 and § Standard acceptance, every sub-step.
- Every symbol in ${sub}'s inventory table, found by \`git grep -n "fun <Name>("\` (the path:line there is a starting address).

## Constraints
- Follow \`## Approved shape\` line by line. Where it does not decide something, choose the structurally clean option and name it in the report.
- A symbol that does not fit the approved shape stays as it is: add one line for it under the step file's \`## Deferred\` (edit markdown with Edit only) and list it in the report.
- Standard acceptance applies: fixtures (\`UiModelPreviews\`) and a \`GalleryRegistry\` entry per new component, KDoc via the \`document-code\` skill, \`components.md\` / \`module-structure.md\` updated.
- Never commit. Never touch the README's \`## Now\` / \`## Steps\` or the issue on GitHub.

## Verify
The family-procedure § Phase 2 Verify for every module touched, prefixed with \`${JDK}\`:
${JDK} scripts/gradle-quiet.sh :core:component:compileKotlinJvm :<feature>:compileKotlinJvm checkModuleGraph :<module>:projectHealth ktlintCheck styleCheck

## Report
≤150 words: Done / Files changed / Verify result / Left over / Needs tests (the files with new UI models, mappers or logic).`

const testBrief = (sub, files, extra) => `## Files
${files.map(f => `- \`${f}\` — unit under test (${sub})`).join('\n')}
- Audit the existing test files next to these for coverage gaps and close them in the same pass. No change-detector tests.
${extra ?? ''}

## Verify
${JDK} scripts/gradle-quiet.sh :<module>:testAndroidHostTest --tests "<each test class you wrote>"

## Report
≤150 words: tests added / pass or fail. For every failure, give the test name, your diagnosis, and whether the cause is production behaviour or test-side.`

const reviewBrief = (sub, fromTree, toTree) => `## Scope
Sub-step ${sub}: \`git diff ${fromTree} ${toTree}\` (both are tree objects; the diff includes new files).

## Gates
The implementer reports Verify green for the modules touched.

## Focus
The change implements ${sub} per \`## Approved shape\` in the step file among ${stepList} that lists ${sub}. Check it follows that shape, component-contract R1–R11, and family-procedure § Standard acceptance. Report blockers separately from non-blocking findings.`

phase('Preflight')
const pre = await agent(
  `Read-only check in the repo root. Report ok=false with a problem line for each failure:\n` +
  (resumeAfter
    ? `1. \`refs/${snapshotPrefix}-snapshots/${resumeAfter.toLowerCase()}\` exists (\`git rev-parse --verify\`).\n`
    : `1. \`git status --porcelain\` is empty.\n`) +
  `2. In ${stepList}, \`## Approved shape\` exists, contains no line marked OPEN, and is not a placeholder.\n` +
  `3. Every sub-step in ${JSON.stringify(subSteps)} is a row in one of those files' \`## Sub-steps\` tables, still unticked.\n` +
  `4. No ref exists under refs/${snapshotPrefix}-snapshots/ for those sub-steps (\`git for-each-ref\`).`,
  { label: 'preflight', schema: PREFLIGHT, effort: 'low' },
)
if (!pre.ok) return { status: 'preflight-failed', problems: pre.problems }

let prevTree = resumeAfter
  ? `refs/${snapshotPrefix}-snapshots/${resumeAfter.toLowerCase()}`
  : await snapshot('Preflight', null)
const done = []

for (const sub of subSteps) {
  phase(sub)
  log(`${sub}: implementing`)

  let impl = await agent(implementBrief(sub), { label: `implement:${sub}`, phase: sub, agentType: 'softcover-implementer', schema: IMPLEMENTED })
  for (let round = 0; impl && !impl.verifyPassed && round < MAX_FIX_ROUNDS; round++) {
    impl = await agent(implementBrief(sub, `Verify is red after the previous round: ${impl.summary}. Make it green without narrowing the approved shape.`),
      { label: `implement:${sub}:verify-fix-${round + 1}`, phase: sub, agentType: 'softcover-implementer', schema: IMPLEMENTED })
  }
  if (!impl || !impl.verifyPassed) {
    return { status: 'stopped', at: sub, reason: 'Verify still red after fix rounds', last: impl?.summary, done }
  }

  if (impl.needsTests.length) {
    log(`${sub}: tests for ${impl.needsTests.length} file(s)`)
    let tests = await agent(testBrief(sub, impl.needsTests), { label: `test:${sub}`, phase: sub, agentType: 'softcover-test-writer', schema: TESTED })
    const production = tests?.failures.filter(f => f.cause === 'production') ?? []
    if (production.length) {
      return { status: 'stopped', at: sub, reason: 'test failure points at production behaviour', failures: production, done }
    }
    if (tests && !tests.allPassed) {
      tests = await agent(testBrief(sub, impl.needsTests, `- Fix round: these failures are test-side fallout — fix the tests, not production: ${JSON.stringify(tests.failures)}`),
        { label: `test:${sub}:fix`, phase: sub, agentType: 'softcover-test-writer', schema: TESTED })
    }
    if (!tests || !tests.allPassed) {
      return { status: 'stopped', at: sub, reason: 'tests still failing after one fix round', failures: tests?.failures, done }
    }
  }

  let candidate = await snapshot(sub, null)
  let review = await agent(reviewBrief(sub, prevTree, candidate), { label: `review:${sub}`, phase: sub, agentType: 'softcover-reviewer', schema: REVIEWED })
  for (let round = 0; review && review.blockers.length && round < MAX_FIX_ROUNDS; round++) {
    log(`${sub}: ${review.blockers.length} blocker(s), fix round ${round + 1}`)
    impl = await agent(implementBrief(sub, `Fix these reviewer blockers, and keep Verify green:\n${review.blockers.map(b => `- ${b}`).join('\n')}`),
      { label: `implement:${sub}:review-fix-${round + 1}`, phase: sub, agentType: 'softcover-implementer', schema: IMPLEMENTED })
    candidate = await snapshot(sub, null)
    review = await agent(reviewBrief(sub, prevTree, candidate), { label: `review:${sub}:${round + 2}`, phase: sub, agentType: 'softcover-reviewer', schema: REVIEWED })
  }
  if (!review || review.blockers.length) {
    return { status: 'stopped', at: sub, reason: 'reviewer blockers remain after fix rounds', blockers: review?.blockers, done }
  }

  await agent(
    `In the step file among ${stepList} whose \`## Sub-steps\` table lists ${sub}, tick that row's status (\`[ ]\` → \`[x]\`). Use the Edit tool. Change nothing else.`,
    { label: `tick:${sub}`, phase: sub, effort: 'low' },
  )
  prevTree = await snapshot(sub, sub.toLowerCase())
  done.push({ sub, summary: impl.summary, deferred: impl.deferred, nonBlocking: review.nonBlocking })
  log(`${sub}: done, snapshot refs/${snapshotPrefix}-snapshots/${sub.toLowerCase()}`)
}

if (!convergence) return { status: 'built', done }

phase('Converge')
const proposals = await agent(
  `Run \`${PLAN}/family-procedure.md\` § Phase 3 (convergence pass) over the family as it now stands in the working tree, for ${stepList}. ` +
  `Audit every layer it lists. Write the proposals — structural changes first, then pairwise merges and fixes, or "nothing to converge" and why — under each step file's \`## Convergence proposals\` (Edit tool only). ` +
  `Change no code. Return a ≤150-word summary.`,
  { label: 'convergence-audit', phase: 'Converge' },
)
return { status: 'awaiting-convergence-approval', done, proposals }
