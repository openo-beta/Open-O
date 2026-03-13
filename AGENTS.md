# OpenO EMR - Healthcare Electronic Medical Records System

> **DEVCONTAINER ENVIRONMENT NOTICE**: The `.claude/settings.json` grants extensive pre-approved permissions for **isolated devcontainer development only**. Do NOT use these defaults in production or any system with real patient data.

**PROJECT IDENTITY**: Always refer to this system as "OpenO EMR" or "OpenO" - NOT "OSCAR EMR" or "OSCAR McMaster"

## Core Context

**Domain**: Canadian healthcare EMR system with multi-jurisdictional compliance (BC, ON, generic)
**Stack**: Java 21, Spring 5.3.39, Hibernate 5.6.15, Maven 3, Tomcat 9.0.97, MariaDB/MySQL
**Regulatory**: HIPAA/PIPEDA compliance REQUIRED - PHI protection is CRITICAL

## Essential Commands

```bash
make clean                    # Clean project and remove deployed app
make install                  # Build and deploy without tests
make install --run-tests      # Build, test, and deploy (all tests)
make install --run-modern-tests     # Modern tests only (JUnit 5)
make install --run-legacy-tests     # Legacy tests only (JUnit 4)
make install --run-unit-tests       # Modern unit tests only
make install --run-integration-tests # Modern integration tests only
server start/stop/restart     # Tomcat management
server log                    # Tail application logs
db-connect                   # Connect to MariaDB as root
debug-on / debug-off         # Toggle DEBUG/INFO logging levels
```

## Critical Security Requirements

**MANDATORY for all code changes:**
- Use `Encode.forHtml()`, `Encode.forJavaScript()` etc. for ALL user inputs (context-appropriate OWASP encoding)
- Parameterized queries ONLY - never string concatenation
- ALL actions MUST include `SecurityInfoManager.hasPrivilege()` checks
- PHI (Patient Health Information) must NEVER be logged or exposed
- **Use `PathValidationUtils` for ALL file path operations** (see `docs/path-validation-utils.md`)

### PathValidationUtils - File Path Security

Use `PathValidationUtils` (`ca.openosp.openo.utility.PathValidationUtils`) for file operations involving user input:
```java
File safeFile = PathValidationUtils.validatePath(userFilename, allowedDir);
PathValidationUtils.validateExistingPath(file, allowedDir);
PathValidationUtils.validateUpload(uploadedFile);
File dest = PathValidationUtils.validateUpload(sourceFile, filename, destDir);
if (PathValidationUtils.isInAllowedTempDirectory(file)) { ... }
```

## Package Structure (2025 Migration)

**CRITICAL**: Use NEW namespace `ca.openosp.openo.*` for ALL code
- **Old**: `org.oscarehr.*`, `oscar.*` -> **New**: `ca.openosp.openo.*`
- **DAO Classes**: `ca.openosp.openo.commn.dao.*` (note: "commn" not "common")
- **Forms DAOs**: `ca.openosp.openo.commn.dao.forms.*`
- **Models**: `ca.openosp.openo.commn.model.*`
- **Exception**: `ProviderDao` at `ca.openosp.openo.dao.ProviderDao`
- **Test Utilities**: Remain at `org.oscarehr.common.dao.*` for backward compatibility

## Struts2 Migration Pattern ("2Action")

**CRITICAL PATTERN**: All new Struts2 actions use `*2Action.java` naming convention.

### 2Action Structure Template:
```java
public class Example2Action extends ActionSupport {
    HttpServletRequest request = ServletActionContext.getRequest();
    HttpServletResponse response = ServletActionContext.getResponse();

    private SecurityInfoManager securityInfoManager = SpringUtils.getBean(SecurityInfoManager.class);

    public String execute() {
        // MANDATORY security check
        if (!securityInfoManager.hasPrivilege(LoggedInInfo.getLoggedInInfoFromSession(request), "_object", "r", null)) {
            throw new SecurityException("missing required sec object");
        }
        // Business logic
        return "success";
    }
}
```

### 2Action Categories:
1. **Simple Execute**: Single `execute()` method (e.g., `AddTickler2Action`)
2. **Method-Based**: Route via `method` parameter (e.g., `CaseloadContent2Action`)
3. **Inheritance-Based**: Extend `EctDisplayAction` for encounter components

### Struts.xml Mapping:
```xml
<action name="login" class="ca.openosp.openo.login.Login2Action">
    <result name="provider" type="redirect">/provider/providercontrol.jsp</result>
    <result name="failure">/logout.jsp</result>
</action>
```
- Maintains `.do` extension for backward compatibility with legacy URLs
- Spring object factory: `<constant name="struts.objectFactory" value="spring"/>`

### OWASP Encoding Reference (for 2Action & JSP outputs):
- `Encode.forHtml()` - HTML body | `Encode.forHtmlAttribute()` - HTML attributes
- `Encode.forJavaScript()` - JS strings | `Encode.forJavaScriptAttribute()` - JS in HTML attrs
- `Encode.forUri()` / `Encode.forUriComponent()` - URL paths/params | `Encode.forCssString()` - CSS

## Healthcare Domain Context

**Core Medical Modules**: PMmodule (program/case mgmt), billing (BC, ON), prescription (ATC codes, interactions), lab (HL7, OLIS), prevention (immunizations), demographic (patient data, HIN)

**Standards & Protocols**: HL7 v2/v3, FHIR R4 (HAPI FHIR 5.4.0), SNOMED CT, ICD-9/ICD-10, ATC, DICOM

**Provincial Systems**: OLIS (Ontario Labs), Teleplan (BC MSP billing), MCEDT, DrugRef

## Development Workflow

**DevContainer**: Docker-based, debug port 8000, port 8080 for web, 3306 for DB

**Build & Deploy Cycle**:
1. `make clean` -> `make install --run-tests` -> `server log`
2. Quick iterations: `make install` (skips tests)
3. Debug logging: `debug-on` -> `server restart` -> `debug-off`

## Modern Test Framework (JUnit 5)

- **Location**: `src/test-modern/` (separate from legacy `src/test/`)
- **Stack**: JUnit 5, AssertJ, H2 in-memory database, BDD naming
- **Documentation**: `docs/test/modern-test-framework-complete.md`, `docs/test/test-writing-guide.md`
- **Unit Tests**: `OpenOUnitTestBase` for mocked tests without database
- **Manager Tests**: @Nested classes for organizing 100+ tests per manager

### Test Tags:
- **Type**: `@Tag("integration")`, `@Tag("unit")`, `@Tag("dao")`, `@Tag("manager")`
- **CRUD**: `@Tag("create")`, `@Tag("read")`, `@Tag("update")`, `@Tag("delete")`
- Run: `mvn test -Dgroups="unit"` | `make install --run-unit-tests`

### BDD Naming: `shouldReturnTicklerWhenValidIdProvided()` (pure camelCase)

### Writing Tests - CRITICAL:
1. **First examine the actual interface/class** being tested
2. **Only test methods that actually exist** - never invent method names
3. **Choose the right base class**:
   - `OpenOTestBase` - Integration tests (Spring context + database)
   - `OpenOUnitTestBase` - Unit tests (mocked SpringUtils, no database)
   - Domain-specific bases like `DemographicUnitTestBase`
4. Use `@PersistenceContext(unitName = "testPersistenceUnit")` for EntityManager (integration only)
5. **Manager unit tests**: Register SpringUtils mocks BEFORE creating static mocks (LogAction, etc.)
6. See `docs/test/test-writing-guide.md` for detailed patterns and static mocking

## Code Quality Standards

**Spring Integration Pattern**:
```java
private SomeManager someManager = SpringUtils.getBean(SomeManager.class);
```

**Documentation Standards**:
- JavaDoc required on all public classes/methods
- No @author tags (misleading post-migration); use @since with git history dates
- Document @param (with types), @return (with types), @throws
- Use @deprecated with migration guidance
- JSP: Add comment blocks after copyright headers

**Code Maintenance**: Project aggressively removes unused code. Don't assume legacy features exist - check current codebase. Recently removed: MyDrugRef, BORN integration, HealthSafety, legacy email.

## Architecture Patterns

### Layered Architecture
- **Web Layer**: Controllers (2Actions) handle HTTP requests
- **Service Layer**: `*Manager.java` classes for business logic
- **DAO Layer**: `*Dao.java` for database operations
- **Model Layer**: `ca.openosp.openo.commn.model.*` domain entities

### Spring Configuration
Multiple modular contexts: `applicationContext.xml` (core), `applicationContextREST.xml` (OAuth 1.0a), `applicationContextOLIS.xml`, `applicationContextHRM.xml`, `applicationContextCaisi.xml`, `applicationContextFax.xml`, `applicationContextJobs.xml`

### REST API & Web Services
- **OAuth 1.0a**: Migrating from CXF OAuth2 to ScribeJava (`OscarOAuthDataProvider`, `OAuth1Executor`)
- **25+ API endpoints**: DemographicService, ScheduleService, PrescriptionService, LabService, PreventionService, etc.
- **SOAP**: CXF-based for provincial healthcare system integration

## Database Schema & Migrations

**Schema**: MariaDB/MySQL, dating back to 2006
**Migration Pattern**: Date-based SQL scripts (`update-YYYY-MM-DD-description.sql`)
**Core files**: `database/mysql/oscarinit_2025.sql` (current schema), `oscardata.sql` (reference data), province-specific `oscarinit_bc.sql`/`oscarinit_on.sql`

**Key Tables**: `demographic` (50+ fields, HIN), `allergies`, `appointment`, `casemgmt_note`, `prevention`, `drugs`, `measurementType`, `billing`
**Audit Pattern**: Every table includes `lastUpdateUser`, `lastUpdateDate`

## Issue Management

**Commit Format**: Conventional Commits - `feat:`, `fix:`, `chore:`, `update:`

**Issue Labels** (required): Type (`type: bug/feature/security/maintenance/test/documentation/regression`) + Priority (`priority: critical/high/medium/low`)
**Status Labels**: `status: needs-triage`, `status: confirmed`, `status: pending-verification`, `status: verified-fixed`, `status: fix-failed`

**Automated Lifecycle**: PRs referencing issues (`fixes #123`) trigger verification workflow - reporter is notified, and status labels auto-update based on reporter feedback.

## AI Workflow Guidelines

### Task Handling
1. **Simple Questions**: Answer directly, reference specific files and line numbers
2. **Straightforward Changes** (1-3 files): Create feature branch, implement, create PR
3. **Complex Changes**: Ask clarifying questions first, plan, proceed after approval

### Branch & PR Rules
- **Protected**: `develop`, `main`, `experimental` - no direct commits
- Feature branches: `claude/issue-<number>-<timestamp>`
- PRs target `develop`, include tests, reference issues (`fixes #123`), add "Generated with Claude Code"

### Security Checklist (Every Code Change)
- [ ] Context-appropriate OWASP encoding for user inputs
- [ ] Parameterized SQL queries
- [ ] `SecurityInfoManager.hasPrivilege()` checks in all actions
- [ ] `PathValidationUtils` for file operations
- [ ] No PHI in logs or error messages

### Tool Permissions & Safety
Permissions are configured in `.claude/settings.json` with three tiers: ALLOW (core workflow), ASK (confirmation required), DENY (blocked).
- **Allowed**: git operations, `gh pr create/view/list/diff/checks`, `gh issue view/list/comment`, file read/write within repo
- **Blocked**: destructive operations (`rm -rf`, force push, `git rebase`, `git reset --hard`), `gh pr merge`, repo management, GitHub API write methods, credential manipulation
- **Protected paths**: `.git/`, `.github/`, `database/` - write-denied
- @claude triggers restricted to OWNER/MEMBER/COLLABORATOR

### Interacting with Claude
- **On PRs**: `@claude review`, `@claude fix the lint errors`, `@claude explain this change`
- **On Issues**: `@claude investigate this bug`, `@claude implement this feature`, `@claude add labels`

## Key Code References

| Area | Key Files |
|------|-----------|
| Security | `managers/SecurityInfoManager.java`, `utility/LoggedInInfo.java`, `utility/PathValidationUtils.java` |
| Spring | `applicationContext*.xml`, `utility/SpringUtils.java` |
| Struts | `WEB-INF/classes/struts.xml`, `*/web/*2Action.java` |
| 2Action Examples | `tickler/pageUtil/AddTickler2Action.java`, `caseload/CaseloadContent2Action.java` |
| CSRF | `Owasp.CsrfGuard.properties`, `app/CSRFPreservingFilter.java` |
| Models | `commn/model/Demographic.java`, `commn/model/Allergies.java` |
| DAOs | `commn/dao/DemographicDao.java`, `dao/ProviderDao.java` |
| Tests | `src/test-modern/`, `docs/test/test-writing-guide.md`, `docs/test/modern-test-framework-complete.md` |
| Database | `database/mysql/oscarinit_2025.sql`, `database/mysql/updates/` |
| DevContainer | `.devcontainer/devcontainer.json`, `.devcontainer/development/scripts/` |

All paths under `ca.openosp.openo` package unless otherwise noted. Use search tools to discover specific files.
