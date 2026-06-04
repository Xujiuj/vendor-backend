# Subagent Plan: Vendor Backend

Updated: 2026-06-05

## Overview

This plan decomposes the vendor backend missing features into small, reviewable subagent tasks. Each implementation task must pass spec review and quality review before the next dependent task starts.

## Architecture Decisions

- Vendor backend owns central customer, License, factor, template, and renewal metadata.
- Vendor backend does not own enterprise-local runtime state or enterprise-local business data.
- Factor lifecycle work may start now because License issue hardening has passed review.
- License-scoped factor API waits for factor lifecycle state semantics.

## Phase 1: Factor Lifecycle Foundation

### Task VB-1: Factor Lifecycle State Model

**Description:** Define and implement factor version state transitions for draft, released, frozen, and disabled/retired if needed by current schema.

**Acceptance criteria:**
- [ ] Invalid state transitions are rejected.
- [ ] Release/freeze operations are auditable.
- [ ] No enterprise-local state is read or written.

**Verification:**
- [ ] `rtk mvn -pl ruoyi-modules/carbon-vendor -am "-DskipTests=false" "-Dtest=*Factor*Lifecycle*Test" "-Dsurefire.failIfNoSpecifiedTests=false" test`

**Dependencies:** None

**Files likely touched:**
- `ruoyi-modules/carbon-vendor/src/main/java/org/dromara/carbon/vendor/**`
- `ruoyi-modules/carbon-vendor/src/test/java/org/dromara/carbon/vendor/**`

**Estimated scope:** M

### Task VB-2: Factor Open Scope Rules

**Description:** Add vendor-side customer/edition/license entitlement scope for released factor versions.

**Acceptance criteria:**
- [ ] Scope is vendor metadata only.
- [ ] Unauthorized customer cannot see unscoped factor version.
- [ ] Tests cover customer-scoped and edition-scoped cases.

**Verification:**
- [ ] `rtk mvn -pl ruoyi-modules/carbon-vendor -am "-DskipTests=false" "-Dtest=*Factor*Scope*Test" "-Dsurefire.failIfNoSpecifiedTests=false" test`

**Dependencies:** VB-1

**Estimated scope:** M

## Phase 2: License-Scoped Factor API

### Task VB-3: Bearer License Factor Read API

**Description:** Add `Authorization: Bearer {licenseId}` factor read API that returns authorized factor versions.

**Acceptance criteria:**
- [ ] Valid License returns authorized latest factor version.
- [ ] Invalid or unknown License is rejected.
- [ ] Expired License follows frozen-version policy or explicit rejection policy.
- [ ] API response contains no enterprise-local runtime data.

**Verification:**
- [ ] `rtk mvn -pl ruoyi-modules/carbon-vendor -am "-DskipTests=false" "-Dtest=*Factor*Api*Test,*LicenseIssue*Test" "-Dsurefire.failIfNoSpecifiedTests=false" test`

**Dependencies:** VB-1, VB-2

**Estimated scope:** M

## Phase 3: License Operations Completion

### Task VB-4: Audited Revoke/Reissue Workflow

**Description:** Replace the current revoked-history blocker with an explicit audited reissue workflow.

**Acceptance criteria:**
- [ ] Reissue is a separate action.
- [ ] Old issue history is preserved.
- [ ] Install ID change rules are explicit.
- [ ] Private key material remains internal.

**Verification:**
- [ ] `rtk mvn -pl ruoyi-modules/carbon-vendor -am "-DskipTests=false" "-Dtest=*License*Reissue*Test,CvLicenseIssueServiceTest" "-Dsurefire.failIfNoSpecifiedTests=false" test`

**Dependencies:** None

**Estimated scope:** M

### Task VB-5: Renewal Callback Issue Queue

**Description:** Add the first idempotent renewal/payment callback slice that can queue or perform License issue.

**Acceptance criteria:**
- [ ] Duplicate callback does not duplicate License issue.
- [ ] Failed issue is auditable and retryable.
- [ ] Manual issue path remains unchanged.

**Verification:**
- [ ] `rtk mvn -pl ruoyi-modules/carbon-vendor -am "-DskipTests=false" "-Dtest=*Renewal*Test,*LicenseIssue*Test" "-Dsurefire.failIfNoSpecifiedTests=false" test`

**Dependencies:** VB-4 preferred

**Estimated scope:** M

## Phase 4: Report Template Lifecycle

### Task VB-6: Report Template Publish/Distribution

**Description:** Implement vendor-owned report template versioning and customer distribution metadata.

**Acceptance criteria:**
- [ ] Vendor can publish/disable template metadata.
- [ ] Distribution references customer/license entitlement metadata.
- [ ] No enterprise data source connection is introduced.

**Verification:**
- [ ] `rtk mvn -pl ruoyi-modules/carbon-vendor -am "-DskipTests=false" "-Dtest=*ReportTemplate*Test" "-Dsurefire.failIfNoSpecifiedTests=false" test`

**Dependencies:** None

**Estimated scope:** M

## Checkpoint

After VB-1, VB-2, VB-3:

- [ ] `rtk mvn -pl ruoyi-modules/carbon-vendor -am "-DskipTests=false" test`
- [ ] `rtk mvn -pl ruoyi-admin -am package -DskipTests`
- [ ] Parent boundary scripts pass when parent workspace is available.

## Risks and Mitigations

| Risk | Impact | Mitigation |
| --- | --- | --- |
| Factor API leaks enterprise runtime state | High | API reads only vendor License/factor metadata |
| Reissue mutates old issue history | High | VB-4 requires append-only audit |
| Payment callback duplicates issues | High | VB-5 requires idempotency key |
| Tests skipped by root Maven property | Medium | Always pass `-DskipTests=false` |

## Parallelization

- VB-1 and VB-6 can run in parallel.
- VB-2 waits for VB-1.
- VB-3 waits for VB-1 and VB-2.
- VB-4 can run in parallel with factor lifecycle if it does not touch the same License issue files as another active task.
- VB-5 waits for VB-4 or an explicit product decision to keep manual fallback only.
