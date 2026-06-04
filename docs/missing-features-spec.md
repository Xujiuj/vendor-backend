# Spec: Vendor Backend Missing Features

Updated: 2026-06-05

## Objective

Finish the vendor-side central backend for customer, License, factor, template, and renewal operations. The vendor backend owns central operational metadata and signing workflows. It must never host enterprise-local business data, enterprise runtime License state, or enterprise-local workflow results.

Current baseline:

- Manual License issue service exists.
- License issue hardening is complete:
  - vendor customer is resolved by `customerId`
  - disabled/inactive customers are rejected
  - duplicate issue is blocked
  - revoked-history reissue is blocked until a dedicated workflow exists
  - request customer code/name are not trusted
  - generic issue failures return sanitized message
- Phase 0 verification passed with 9 vendor License issue tests.

## Tech Stack

- Java 17
- Spring Boot 3.5.14
- MyBatis-Plus 3.5.16
- Sa-Token 1.45.0
- Maven multi-module project based on RuoYi-Vue-Plus
- Primary module for new vendor business code: `ruoyi-modules/carbon-vendor`

## Commands

Run from `vendor-backend`.

```powershell
rtk mvn -pl ruoyi-modules/carbon-vendor -am "-DskipTests=false" "-Dtest=CvLicenseIssueServiceTest,CvLicenseIssueControllerTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
rtk mvn -pl ruoyi-modules/carbon-vendor -am "-DskipTests=false" test
rtk mvn -pl ruoyi-admin -am package -DskipTests
```

Cross-repository boundary checks are run from the original parent workspace when available:

```powershell
rtk python tools\verify_backend_module_boundaries.py
rtk python tools\verify_sql_boundaries.py
```

## Project Structure

```text
ruoyi-admin/                      Spring Boot application entrypoint
ruoyi-common/                     Shared RuoYi common modules
ruoyi-modules/carbon-vendor/      Vendor-owned carbon operations domain
script/sql/mysql/                 MySQL development schema and seed scripts
docs/                             Vendor backend specs and task plans
```

## Missing Features

### VB-1: Vendor Factor Lifecycle

Vendor factor catalog/version lifecycle is not yet implemented.

Requirements:

- Vendor owns factor catalog, factor version governance, release, freeze, scope, and distribution metadata.
- Enterprise effective factor results and local runtime status must never be displayed or stored as vendor-local workflow data.
- Version state transitions must be explicit:
  - draft
  - released
  - frozen
  - disabled or retired, if needed by existing schema
- Release/freeze operations must be audited.
- Customer or edition scope must be represented as vendor-side entitlement metadata only.

### VB-2: License-Scoped Factor API

The API for `Authorization: Bearer {licenseId}` factor reads is missing.

Requirements:

- Valid license returns the customer-authorized factor version.
- Expired license returns frozen version or rejects update according to the lifecycle policy.
- Unknown/revoked license is rejected.
- API does not read enterprise runtime state.
- API returns only factor metadata and values needed for enterprise-side cache/sync.

### VB-3: Revoke/Reissue Workflow

C1 intentionally blocks reissue when revoked history exists. A dedicated workflow is still missing.

Requirements:

- Reissue must be a separate audited action.
- Reissue must preserve old issue history.
- Reissue must define whether installId can change.
- Reissue must not expose private key material.

### VB-4: Report Template Lifecycle and Distribution

Vendor owns Power BI template assets and distribution metadata, but the full lifecycle is not yet frozen.

Requirements:

- Vendor may upload, version, publish, disable, and distribute `.pbix` templates.
- Vendor must not connect to enterprise SQL Server to read enterprise business data.
- Template distribution records may reference customers, editions, or License entitlements.

### VB-5: Renewal / Payment Callback to License Issue

Manual issue exists; automatic renewal/payment callback issue is not implemented.

Requirements:

- Payment callbacks must be idempotent.
- Automatic issue must reuse the same License payload contract and key provider as manual issue.
- Failed automatic issue must be auditable and retryable.
- Renewal must not create enterprise-local workflow data in vendor backend.

## Code Style

Follow existing RuoYi layering:

```java
@RequiredArgsConstructor
@Service
public class CvExampleServiceImpl implements ICvExampleService {

    private final CvCustomerMapper customerMapper;

    @Override
    public CvResult operate(CvRequest request) {
        var customer = customerMapper.selectById(request.getCustomerId());
        // Vendor service resolves canonical vendor-side facts.
        return CvResult.success(customer.getCustomerCode());
    }
}
```

Conventions:

- Controllers adapt HTTP and current operator identity.
- Services own lifecycle transitions and trust-boundary decisions.
- Never return raw exception messages to clients for signing, key, or crypto paths.
- Tests must forge request-supplied facts when validating server-side canonical data.

## Testing Strategy

- Service tests for lifecycle transitions and edge cases.
- Controller tests for HTTP shape and operator identity handling.
- Security-oriented tests for sanitization and private-key non-exposure.
- Tests must pass with `-DskipTests=false`.

## Boundaries

- Always:
  - Keep vendor business code inside `ruoyi-modules/carbon-vendor`.
  - Resolve customer facts from vendor master data.
  - Sanitize signing/key failure messages.
  - Preserve audit records for release/freeze/revoke/reissue operations.
- Ask first:
  - License payload schema changes.
  - New payment provider integrations.
  - New dependencies.
  - Changing factor expiration/frozen-version policy.
- Never:
  - Read enterprise-local business tables.
  - Store enterprise runtime License state as vendor workflow state.
  - Expose `privateKeyRef` or private key material.
  - Trust request customer code/name for License payload facts.

## Success Criteria

- Factor lifecycle supports release/freeze/scope with tests.
- License-scoped factor API returns authorized factor data and rejects invalid access.
- Reissue workflow is audited and does not mutate old issue history.
- Report template lifecycle can publish/distribute templates without enterprise data access.
- Renewal/payment callback path can issue or queue License creation idempotently.

## Open Questions

- Should expired License factor API return frozen factors or reject refresh?
- What is the approved revoke/reissue business process?
- Which payment providers are in scope for the first callback slice?
