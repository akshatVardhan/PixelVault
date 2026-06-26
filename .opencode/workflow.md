# PixelVault — Branching & Workflow

## Branches

```
main           Production. Merged only via PR from staging.
  └─ staging   Integration branch. All tested code lands here. You test here before release.
       ├─ feature/*   New features. Branch off staging.
       └─ bugfix/*    Bug fixes. Branch off staging (or main for prod-critical hotfixes).
```

## Pipeline

```
Architect (plan)
  → Coder (creates feature/xxx from staging, implements code)
    → Tester (tests on feature/xxx)
      → [if fail] Debugger (fixes on bugfix/xxx or same branch) → back to Tester
      → [if pass] Tester merges directly to staging (no PR)
        → YOU manually test on staging
          → Reviewer (reviews staging, creates PR staging→main, assigns you)
            → YOU approve & merge the PR
```

## Role Responsibilities

| Agent | Branch | Action |
|-------|--------|--------|
| **Architect** | — | Produces plan. Never touches branches. |
| **Coder** | `feature/<name>` off `staging` | Implements code, commits, pushes. |
| **Tester** | `feature/<name>` | Runs tests. On pass: merges **directly** to `staging`. On fail: hands to Debugger. |
| **Debugger** | `bugfix/<name>` off `staging` (or `main` for hotfixes) | Fixes bug, hands back to Tester. |
| **Reviewer** | `staging` | Reviews `staging` branch. Creates real GitHub PR (`staging` → `main`), assigns user. Waits for user approval to merge. |

## Rules

1. **Never commit directly to `main` or `staging`** — all work happens on feature/bugfix branches.
2. **Tester is the gatekeeper for `staging`** — no code reaches staging without passing tests.
3. **Always create `feature/<name>` from latest `staging`** — rebase if behind.
4. **Reviewer always creates an actual GitHub PR** — use `gh pr create`. Assign the user as reviewer.
5. **You test on `staging`** before the reviewer creates the PR to `main`.
6. **Bugfix branches**: if a bug exists in `staging`, branch off `staging`. If it's a production bug on `main`, branch off `main` for a hotfix.
