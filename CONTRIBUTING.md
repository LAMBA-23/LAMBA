# Contributing to LAMBA

This guide describes how the LAMBA team contributes to the repository. It is
intended for current team members, reviewers, and future maintainers.

For the broader workflow and completion rules, also see:

- [Development process](docs/development-process.md)
- [Definition of Done](docs/definition-of-done.md)
- [Testing overview](docs/testing.md)
- [Pull request template](.github/pull_request_template.md)

## Before You Start

1. Choose an existing GitHub issue or create a new one using the repository issue
   templates.
2. Make sure the issue has a clear description, expected evidence, and acceptance
   criteria or course-task deliverables.
3. Assign an implementer and a reviewer. The reviewer must be a different team
   member from the implementer.
4. Confirm whether the work is product-facing, documentation-only, testing,
   deployment, or course evidence.

Do not start untracked work from private chat context only. The issue should be
the inspectable source of scope for the PR.

## Branches

Create a dedicated branch for each issue or tightly related task.

Recommended branch naming:

```text
<issue-number>-short-description
```

Examples:

```text
245-decimal-fuel-liters
246-trip-odometer-start-end
docs/week6-report
fix/statistics-response-format
```

Use `docs/` for documentation-only work and `fix/` for bug fixes when there is
no issue-number branch convention available. Keep branches focused so review and
CI evidence stay easy to inspect.

## Making Changes

- Keep changes scoped to the linked issue.
- Follow the existing code style and file organization.
- Prefer existing helpers, patterns, and documented APIs over new conventions.
- Update documentation when setup, API behavior, workflow, deployment,
  verification, or customer-facing instructions change.
- Add or update tests when the changed behavior needs automated coverage.
- Do not mix unrelated cleanup with feature, bug, or documentation work.

## Verification Before PR

Run the checks that match the files you changed.

Backend checks from the `backend/` directory:

```bash
python -m ruff check app tests
python -m ruff format --check app tests
python -m coverage run -m pytest tests
python -m coverage report --include="app/*" --fail-under=30
python -m pip check
```

Android check from the repository root:

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

Markdown and documentation checks:

```text
Run the repository Link Check workflow in CI, or manually verify changed links
when local Lychee is not available.
```

For documentation-only course tasks, automated product tests may be not
applicable. In that case, record manual link checking and document review in the
PR.

## Pull Requests

Open a pull request from the issue branch into `main`.

Every PR should:

- use the repository PR template;
- summarize what changed;
- list testing or manual verification performed;
- verify the linked issue acceptance criteria or deliverables;
- select exactly one changelog option;
- confirm no secrets or private evidence were committed;
- include `Closes #<issue-number>` in the related issue section.

The PR author should not approve their own PR. At least one different team
member should review the PR before merge.

## Changelog

Update [CHANGELOG.md](CHANGELOG.md) under `[Unreleased]` for user-visible product
changes.

A changelog entry is usually not required for:

- pure internal refactoring;
- course-task evidence;
- documentation maintenance that does not change user-facing behavior;
- test-only changes.

When the changelog is not applicable, select the matching option in the PR
template and briefly explain why if needed.

## Secrets and Private Evidence

Never commit:

- `.env` files;
- API keys, tokens, passwords, or private credentials;
- private customer access instructions;
- private recordings or exact private recording timecodes;
- private customer-identifying information;
- screenshots or artifacts that expose private credentials or private customer
  data.

Use sanitized public evidence in the repository. Keep private submission
evidence in the required private Moodle or instructor-only channel.

## Review Checklist

Before requesting review, confirm that:

- the PR is linked to the correct issue;
- the branch contains only relevant changes;
- acceptance criteria or deliverables are visibly addressed;
- changed links work;
- required checks pass or are clearly marked as not applicable;
- no secrets or private evidence are included;
- documentation and changelog updates are handled where needed.

## After Merge

After a PR is merged:

- confirm the linked issue closes or is moved to the correct status;
- check that CI remains green on `main`;
- delete the feature branch when it is no longer needed;
- update related maintained documentation if the merge changes setup, workflow,
  access, deployment, or verification expectations.
