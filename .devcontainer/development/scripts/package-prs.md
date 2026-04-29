# package-prs

Build a "package" branch by cherry-picking the merge commits of multiple source PRs into a single branch.

## What it does

Given a list of PR numbers from a source repo (e.g., `openo-beta/Open-O`), the script:

1. Fetches the source repo's branch
2. Resolves each PR to its merge commit on that branch via `git log` (not GitHub's API — see *Why* below)
3. Sanity-checks each commit's subject matches `Merge pull request #NNNN from <owner>/...`
4. Creates a fresh local branch off your specified base
5. Cherry-picks each merge commit using `git cherry-pick -m 1`
6. Aborts cleanly if any cherry-pick conflicts

## Why this approach

When packaging multiple PRs into a single branch (e.g., to sync `openo-beta/Open-O` fixes back to `open-osp/Open-O`), cherry-picking a PR's individual feature commits is fragile. Long-lived feature branches often have WIP commits authored against an older base, and cherry-picking those onto a different base produces conflicts that resolve toward stale code.

Cherry-picking the **merge commit** with `-m 1` (mainline = parent 1) captures only the PR's net contribution — exactly what landed on the source branch — without the intermediate WIP states.

The script also avoids two real bugs we hit doing this manually:

- **`gh pr view --json mergeCommit` can return wrong SHAs** when PRs merge close together (returns a sibling PR's SHA). The script uses `git log --grep` against the source branch instead, which is authoritative.
- **`git show <merge-sha>` shows a misleading combined diff** that includes inherited changes from the other parent. The script doesn't rely on `git show` for verification.

## Usage

```bash
package-prs --pr 2343,2344,2345 \
            --source-repo openo-beta/Open-O \
            --target-base upstream-base/main \
            --branch rx-bugfixes/20260428
```

### Flags

| Flag | Required | Default | Description |
|---|---|---|---|
| `--pr <N,N,...>` | yes | — | Comma-separated PR numbers from source repo |
| `--source-repo <owner/repo>` | yes | — | Source repo (e.g., `openo-beta/Open-O`) |
| `--target-base <ref>` | yes | — | Base for the new branch (e.g., `upstream-base/main`) |
| `--branch <name>` | yes | — | Name of the new local branch |
| `--source-branch <ref>` | no | `develop` | Source branch to search for merge commits |
| `--source-remote <name>` | no | `origin` | Local remote name for the source repo |
| `--dry-run` | no | off | Print the plan and exit without changes |
| `--force` | no | off | Overwrite the local branch if it already exists |
| `-h`, `--help` | — | — | Show usage |

### Dry-run

Always run with `--dry-run` first to verify the plan:

```bash
package-prs --pr 2343,2344,2261 \
            --source-repo openo-beta/Open-O \
            --target-base upstream-base/main \
            --branch test --dry-run
```

You'll get a table like:

```
PR      SHA         SUBJECT                                                  STATS
------------------------------------------------------------------------------------------------------------------------
2343    92c6707963  Merge pull request #2343 from openo-beta/...             2 files changed, 47 insertions(+), 106 deletions(-)
2344    4667eb4ab0  Merge pull request #2344 from openo-beta/...             4 files changed, 8 insertions(+), 15 deletions(-)
2261    b2765f4e4b  Merge pull request #2261 from openo-beta/...             15 files changed, 492 insertions(+), 45 deletions(-)
```

Verify each row's `#NNNN` matches the PR you intended. If a SHA looks suspicious (e.g., wrong subject), abort and investigate before re-running without `--dry-run`.

## What it doesn't do

- **Canary checks** — package-specific verification (e.g., "function X should still exist after these PRs apply") is your responsibility. Run them after.
- **Conflict resolution** — if a cherry-pick conflicts, the script aborts and exits non-zero. You resolve manually.
- **Push** — pushes are deliberate. The script prints suggested commands at the end; run them yourself.
- **Build/test** — invoke `make install --run-tests` (or similar) separately.

## Recovering from a failed cherry-pick

If the script aborts mid-run:

1. The branch exists with only the cherry-picks that succeeded (the failed one was aborted automatically).
2. Two paths forward:
   - **Abandon and retry**: `git branch -D <branch>` and re-run after fixing the underlying issue.
   - **Resolve manually**: re-run the failed cherry-pick yourself (`git cherry-pick -m 1 <sha>`), resolve conflicts, `git cherry-pick --continue`, then continue with the remaining PRs from the dry-run table.

## Examples

### Build the rx-bugfixes package

```bash
package-prs --pr 2343,2344,2345,2414,2404,2413,2415,2417,2261 \
            --source-repo openo-beta/Open-O \
            --target-base upstream-base/main \
            --branch rx-bugfixes/20260428
```

### Tickler package, dry-run first

```bash
# Preview
package-prs --pr 2410,2411,2412 \
            --source-repo openo-beta/Open-O \
            --target-base upstream-base/main \
            --branch tickler-bugfixes/20260428 \
            --dry-run

# Verify the table looks right, then re-run without --dry-run
```

### Different source remote

If your local remote for the source repo isn't named `origin`:

```bash
package-prs --pr 2343,2344 \
            --source-repo someorg/some-repo \
            --source-remote someorg \
            --target-base upstream-base/main \
            --branch fix-package
```

## Limitations

- **Devcontainer-only.** The script lives in `.devcontainer/development/scripts/` and is invokable as a bare command (`package-prs`) only inside the devcontainer. Outside the devcontainer, invoke via the explicit path.
- **GitHub merge-commit format only.** The script searches for subjects like `Merge pull request #NNNN from owner/branch`. PRs merged via "squash" or "rebase" strategies won't have that format and won't be findable. Use a different cherry-pick approach for those.
- **Owner check.** The script verifies merge subjects contain `from <owner>/...` to catch wrong-repo lookups. If your source repo uses unusual merge-commit formatting (e.g., merge messages edited manually), the check may fail; either adjust the merge messages or run cherry-picks manually.
- **No build/test integration.** This is intentional — packaging is a maintenance task, not a deploy task.

## When to use something else

- **Just one or two PRs**: usually faster to cherry-pick by hand.
- **Squash-merged PRs**: `git cherry-pick <squash-sha>` directly, no `-m 1` needed. The script's merge-commit subject lookup won't find squashed PRs.
- **Conflict-heavy package**: you're going to be resolving conflicts anyway; the script's value is on the happy path.