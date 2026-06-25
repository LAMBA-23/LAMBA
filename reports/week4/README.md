# Week 4 Report - CI and Quality Automation

This is the public Week 4 report section for Assignment 4 repository automation evidence. It documents the CI configuration work completed for Part 8 and the public evidence that must be included before submission.

## 1. Assignment scope

- Assignment/week: Week 4 / Assignment 4
- Part covered here: Part 8 - Configure CI
- Related issue: [#135](https://github.com/LAMBA-23/LAMBA/issues/135)
- Related branch: `135-configure-backend-ci`

## 2. CI configuration evidence

- Backend CI workflow: [`.github/workflows/backend-ci.yml`](../../.github/workflows/backend-ci.yml)
- Link-check workflow: [`.github/workflows/lychee.yml`](../../.github/workflows/lychee.yml)
- Testing status artifact: [`docs/testing.md`](../../docs/testing.md)
- Definition of Done update: [`docs/definition-of-done.md`](../../docs/definition-of-done.md)

## 3. Required CI links

Add the public GitHub Actions links here after pushing the branch and getting the first successful runs:

- Pull request CI run: `TODO after PR creation`
- Latest `main` backend CI run: `TODO after merge or protected-branch run`
- Latest `main` Lychee run: `TODO before final submission`

## 4. Branch protection or rules evidence

The report must include inspectable evidence that the protected default branch enforces the required review workflow.

- Existing branch protection screenshot from Week 2: [`reports/week2/images/branch-protection-rule.png`](../week2/images/branch-protection-rule.png)
- Recommended Week 4 evidence: add a fresh screenshot from the repository branch protection or rules settings if anything changed since Week 2

### Embedded branch protection evidence

![Branch protection evidence](../week2/images/branch-protection-rule.png)

## 5. Testing-report screenshots

The Week 4 public report must include screenshots showing the testing status evidence.

Recommended screenshots:

- `docs/testing.md` critical modules and coverage table
- `docs/testing.md` CI and QA status table
- Successful GitHub Actions `Backend CI` run
- Successful GitHub Actions `Link Check` run

Place the screenshots under `reports/week4/images/` and embed them here.

### Screenshot placeholders

- `TODO`: add `reports/week4/images/testing-report-coverage.png`
- `TODO`: add `reports/week4/images/testing-report-ci-status.png`
- `TODO`: add `reports/week4/images/backend-ci-success.png`
- `TODO`: add `reports/week4/images/lychee-success.png`

## 6. Current local verification evidence

The backend CI workflow was verified locally before push using the same command categories as the GitHub Actions job:

- `python -m ruff check backend/app backend/tests`
- `python -m ruff format --check backend/app backend/tests`
- `python -m coverage run -m pytest backend/tests`
- `python -m coverage report --include='backend/app/*'`
- `python -m pip check`

Current local results:

- backend tests: `32 passed`
- backend total coverage: `89%`
- critical module coverage:
  - `backend/app/main.py`: `95%`
  - `backend/app/chat_parser.py`: `59%`
  - `backend/app/database.py`: `100%`
- dependency health check: `No broken requirements found`

## 7. Remaining submission actions

Before final Week 4 submission, replace the `TODO` placeholders in this report with:

1. Real GitHub Actions run links
2. Fresh screenshots from the successful CI runs
3. Branch protection or rules evidence that matches the current repository settings
4. Any updated evidence from teammate-owned Assignment 4 documents once their PRs are merged
