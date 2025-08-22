# OpenO Resources Directory Documentation

This directory contains all non-Java resources for the OpenO application including Spring configuration, message bundles, form templates, data files, and legacy libraries. Resources are loaded from the classpath at runtime and deployed to `WEB-INF/classes/` in the WAR file.

**For a high-level overview of this directory's purpose and importance, see:** [`/docs/resources-directory.md`](../../docs/resources-directory.md)

## Table of Contents
- [Spring Configuration Files](#spring-configuration-files)
- [Properties Files](#properties-files)
- [Oscar Subdirectory](#oscar-subdirectory)
- [Hibernate Mappings](#hibernate-mappings)
- [Legacy HAPI Libraries](#legacy-hapi-libraries)
- [Data Files](#data-files)
- [Other Resources](#other-resources)

## Spring Configuration Files

### Core Application Context Files

#### `applicationContext.xml`
Main Spring configuration that imports all other Spring contexts. Loads via web.xml using the pattern `classpath:applicationContext*.xml`.
- Imports: `spring_jpa.xml`, `spring_hibernate.xml`, `spring_managers.xml`, `spring_ws.xml`
- Defines core beans and datasource configuration
- Sets up component scanning base packages

#### Module-Specific Contexts (Loaded by wildcard)

- **`applicationContextBORN.xml`** - Better Outcomes Registry & Network integration for prenatal/child health data
- **`applicationContextBORN18M.xml`** - BORN 18-month well-baby visit reporting
- **`applicationContextCaisi.xml`** - Client Access to Integrated Services and Information (CAISI) module configuration
- **`applicationContextEmeraldA04.xml`** - Emerald EMR A04 patient registration message handling
- **`applicationContextERx.xml`** - Electronic prescribing (eRx) configuration
- **`applicationContextFax.xml`** - Fax integration services configuration
- **`applicationContextHRM.xml`** - Hospital Report Manager configuration for receiving hospital reports
- **`applicationContextJobs.xml`** - Scheduled jobs and background task configuration
- **`applicationContextOLIS.xml`** - Ontario Laboratories Information System integration
- **`applicationContextORN.xml`** - Ontario Renal Network integration
- **`applicationContextREST.xml`** - RESTful web services configuration

### Imported Spring Configuration

#### `spring_hibernate.xml`
Hibernate SessionFactory configuration
- Bean: `sessionFactory` (class: `SpringHibernateLocalSessionFactoryBean`)
- Loads all `*.hbm.xml` mappings from specified directories
- Configures Hibernate properties and transaction management

#### `spring_jpa.xml`
JPA and DAO configuration
- Component scanning for DAO packages
- Repository configurations
- Entity manager setup (Note: persistence.xml exists but appears unused)

#### `spring_managers.xml`
Business logic manager beans
- Component scanning for service/manager classes
- Configures managers for various modules

#### `spring_ws.xml`
Web services configuration
- CXF web services setup
- REST endpoint configuration
- Component scanning for web service classes

### Other XML Configuration

#### `cxf.xml`
Apache CXF web services framework configuration for SOAP/REST services

#### `log4j.xml`, `log4j2.xml`
Logging configuration files for Log4j framework
- Log levels, appenders, and output formats
- Module-specific logging configurations

#### `label.xml`, `label-appt.xml`
Label printing configurations
- Patient label formats
- Appointment label templates

#### `ckd.xml`
Chronic Kidney Disease flowsheet configuration (root level copy)

## Properties Files

### Message Resource Bundles

#### `MessageResources.properties`
Main application message bundle for internationalization (i18n)
- UI labels, error messages, validation messages
- Default English messages

#### `MessageResources_casemgmt.properties`
Case management module specific messages
- Case management UI labels
- Clinical note messages

#### `MessageResources_mcedt.properties`
Medical Care Electronic Data Transfer messages for Ontario billing

#### `HelpResources.properties`
Help text and documentation strings displayed in the UI

### Configuration Properties

#### `buildNumber.properties`
Build metadata (typically generated during build process)
- Build version, timestamp, commit hash

#### `caisi_issues_dx.properties`
CAISI module diagnostic codes and issue mappings

#### `billing_invoice_email.properties`
**REQUIRED** - Billing invoice email configuration
- Loaded by `BillingONManager` at startup (line 73)
- Even though email functionality is disabled, the file must exist or startup fails

#### `pref.defaults`
Default user preferences
- Contains: `pref.fax=905-555-5555` (default fax number)

## Oscar Subdirectory

### `/oscar/form/` - Form Resources
See detailed [Form Resources Documentation](README-form-resources.md)

Contains:
- `/prop/` - PDF templates and print configurations (140+ files)
- `/bcar2020/` - BC Antenatal Record 2020 (Jasper Reports)
- `/rourke2017/`, `/rourke2020/` - Rourke Baby Record forms
- `/dataFiles/` - Mental health form data files

### `/oscar/oscarRx/` - Prescription Module Resources

#### `/oscar/oscarRx/images/`
Icons and images for the prescription module UI

#### `/oscar/oscarRx/data_extract_*.xml`
Ontario Drug Benefit (ODB) formulary data
- **Current**: `data_extract_20250730.xml` - Edition 43 (July 2025)
- **Old**: `data_extract_20181217.xml` - Edition 42 (Dec 2018) - should be removed
- Contains drug coverage information, Limited Use codes
- Loaded by `LimitedUseLookup.java` (hardcoded to load 20250730 version)

#### `/oscar/oscarRx/RenalDosing.xml`
Renal dosing adjustments for medications based on kidney function

### `/oscar/oscarEncounter/` - Clinical Encounter Resources

#### `/oscar/oscarEncounter/oscarMeasurements/flowsheets/`
Clinical flowsheet configurations and decision support rules

**XML Flowsheet Definitions:**
- `chf.xml` - Congestive Heart Failure flowsheet
- `ckd.xml` - Chronic Kidney Disease flowsheet
- `copd.xml` - Chronic Obstructive Pulmonary Disease flowsheet
- `dm2.xml` - Diabetes Mellitus Type 2 flowsheet
- `hiv.xml` - HIV care flowsheet
- Additional disease-specific flowsheets

**Drools Rule Files (`*.drl`):**
- `chf.drl`, `ckd.drl` - Disease-specific rules
- `/decisionSupport/` subdirectory contains measurement-specific rules:
  - `INR.drl` - International Normalized Ratio monitoring
  - `diab-A1C.drl` - Hemoglobin A1C thresholds
  - `diab-ACR.drl` - Albumin-Creatinine Ratio
  - `diab-BMI.drl` - Body Mass Index calculations
  - `diab-BP.drl` - Blood Pressure targets
  - `diab-C-*.drl` - Cholesterol level rules
  - `diab-EFGR.drl` - Estimated GFR kidney function
  - Additional diabetes management rules

These Drools rules provide clinical decision support by evaluating measurements and setting indication colors (HIGH/LOW/NORMAL).

**HTML Flowsheet Templates (`/html/` subdirectory):**
- `inr.html` - INR monitoring flowsheet HTML template
- `physicalFunction.html` - Physical function assessment flowsheet

#### `/oscar/oscarEncounter/oscarConsultationRequest/`
Consultation request templates and configuration

### `/oscar/oscarBilling/` - Billing Module Resources

#### `/oscar/oscarBilling/ca/` - Canadian Billing
- `/data/` - Billing data files
  - `SupServiceCodeAssocDAO.jbx` - Service code associations

#### `/oscar/oscarBilling/ca/on/reports/` - Ontario Billing Reports
- `end_year_statement_report.jasper` - Year-end statement main report
- `end_year_statement_subreport.jasper` - Year-end statement subreport
- Compiled Jasper Reports for billing summaries

#### `/oscar/oscarBilling/ca/bc/reports/` - BC Billing Reports
- `broadcastmessages.jrxml` - Broadcast message report template
- `csv_rep_account_rec.jrxml` - Account receivable CSV report
- `csv_rep_invoice.jrxml` - Invoice CSV report template
- `practsum.jrxml` - Practitioner summary report
- Additional Jasper Report templates for BC billing

### `/oscar/oscarReport/` - Reporting Resources

#### `/oscar/oscarReport/ClinicalReports/`
- `ClinicalReports.xml` - Clinical report definitions and configurations

#### `/oscar/oscarReport/oscarMeasurements/data/`
- `example.xml` - Example measurement report data

#### `/oscar/oscarReport/pageUtil/`
- `billDaySheet.xml` - Daily billing sheet report configuration
- `labDaySheet.xml` - Daily lab sheet report configuration

### `/oscar/oscarLab/` - Laboratory Module Resources

#### `/oscar/oscarLab/ca/` - Canadian Lab Standards
Lab interface configurations and templates

### `/oscar/oscarPrevention/` - Prevention Module Resources
Immunization and prevention tracking configurations

### `/oscar/oscarDemographic/` - Demographics Resources
Patient demographic templates and configurations

### `/oscar/oscarSecurity/` - Security Resources
Security configuration files

### `/oscar/oscarWorkflow/` - Workflow Resources

#### `/oscar/oscarWorkflow/rules/`
Workflow rule definitions

### `/oscar/documentManager/` - Document Management Resources
- `masterPrintStylesheet.css` - Master CSS for document printing

### `/oscar/eform/` - Electronic Forms Resources
- `apconfig.xml` - Auto-populate configuration for eForms

## Hibernate Mappings

### `/com/quatro/model/` - Quatro Model Mappings

#### Security Model (`/security/`)
- `SecProvider.hbm.xml` - Provider security settings
- `Secobjectname.hbm.xml` - Securable object definitions
- `Secobjprivilege.hbm.xml` - Object privilege mappings
- `Secprivilege.hbm.xml` - Privilege definitions
- `Secrole.hbm.xml` - Security role definitions
- `Secuserrole.hbm.xml` - User-role associations
- `UserAccessValue.hbm.xml` - User access control values

#### Lookup Tables
- `FieldDefValue.hbm.xml` - Field definition metadata
- `LookupCodeValue.hbm.xml` - Lookup code values
- `LookupTableDefValue.hbm.xml` - Lookup table definitions
- `LstOrgcd.hbm.xml` - Organization codes

### `/org/caisi/model/` - CAISI Model Mappings
Contains Hibernate mappings for CAISI module entities

### `/org/oscarehr/` - Core Model Mappings
- `/PMmodule/model/` - Program Management module entities
- `/casemgmt/model/` - Case Management entities
- `/common/model/` - Common/shared entities

## Legacy HAPI Libraries

### `/hapi_libs/fork/`
Contains legacy HAPI 0.5.1 (2005-era) HL7 parsing libraries required for specific lab interfaces

#### Files:
- **`hacked_patched_hapi-0.5.1.jar`** - Modified HAPI library with custom patches
- **`commons-logging-1.1.1.jar`** - Required dependency
- **`readme.txt`** - Documentation explaining why these can't be upgraded

#### Why These Exist:
- MDS (Medical Diagnostic Services) labs require HAPI 0.5.1 specifically
- IHA (Interior Health Authority) requires these exact versions
- Dynamically loaded via `URLClassLoader` to avoid conflicts with modern HAPI
- Loaded by: `MessageUploader.java`, `Hl7textResultsData.java`

## Data Files and Schemas

### `/indicatorXMLTemplates/`
- **`IndicatorXMLTemplate.xml`** - Template for health indicator reporting
- **`IndicatorXMLTemplate1.xml`** - Alternate indicator template  
- **`IndicatorXMLTemplateSchema.xsd`** - XML Schema definition for indicator templates

### `/cihi/` - Canadian Institute for Health Information
- **`ontariomd_cihi.xsd`** - CIHI data submission schema
- **`ontariomd_cihi_dt.xsd`** - CIHI data type definitions

### `/k2a/` - K2A Integration
- **`dsGuideline.xsd`** - Decision support guideline schema

### `/omdDataMigration/` - OntarioMD Data Migration
- **`EMR_Data_Migration_Schema.xsd`** - Main migration schema
- **`EMR_Data_Migration_Schema_DT.xsd`** - Migration data type definitions
Used for standardized data migration between EMR systems

### `/xsd/hrm/` - Hospital Report Manager Schemas
- `/1.1.2/` - HRM version 1.1.2 schemas
  - `hrm.xsdconfig` - Schema configuration
  - `ontariomd_hrm.xsd` - HRM message schema
  - `ontariomd_hrm_dt.xsd` - HRM data types

### `/org/oscarehr/` - Core Module Resources

#### `/org/oscarehr/integration/fhir/filters/`
FHIR resource filtering configurations
- `/born/resource_attribute.filter` - BORN data filtering rules
- `/dhir/resource_attribute.filter` - Drug & Health Information Repository filters

#### `/org/oscarehr/olis/`
- **`response.xsd`** - OLIS response message schema

#### `/org/oscarehr/common/web/`
- **`BillingInvoiceTemplate.jrxml`** - Billing invoice Jasper Report template
- **`DxResearchReport.jrxml`** - Diagnosis research report template

#### `/org/oscarehr/casemgmt/`, `/org/oscarehr/PMmodule/`
Module-specific resources and configurations

### `/loginResource/`
- **`openosp_logo.png`** - OpenOSP logo for login page

### Other Data Files

#### `iso-3166-2.json`
ISO 3166-2 country subdivision codes (provinces/states) in JSON format

### Email Templates (Note: All currently disabled in code)
- `billing_invoice_email_notification_template.txt`
- `tickler_email_notification_template.txt`
- `wait_list_daily_admission_email_notification_template.txt`
- `wait_list_immediate_admission_email_template.txt`
- `wait_list_immediate_vacancy_email_template.txt`
- `wait_list_received_a_new_app_template.txt`


## Other Resources

### Text Files
Various `.txt` files throughout containing configuration data, templates, and documentation

### Images and Icons
Image resources for UI components scattered in module-specific directories

## Important Notes

### Dynamic Loading Pattern
Many resources are loaded dynamically at runtime based on:
- Form names passed as parameters
- Lab type configurations
- User preferences
- Provincial configurations (BC vs ON)

### Version Management
Multiple versions of forms and configurations are maintained because:
1. Historical patient data must be viewable with original form versions
2. Different provinces update standards at different times
3. Regulatory compliance requires specific versions
4. Medical standards evolve (e.g., Rourke 2006 → 2009 → 2017 → 2020)

### Classpath Loading
Resources are loaded using various mechanisms:
- Spring's `classpath:` prefix
- `Class.getResourceAsStream()`
- `ClassLoader.getResource()`
- Direct file system access (for configurable paths)

### Cannot Delete "Old" Files
Many seemingly outdated files must be retained:
- Form configurations for all years (2006-2020)
- Legacy HAPI libraries (required for specific labs)
- Multiple versions of standards and protocols

## Maintenance Guidelines

### When Adding Resources
1. Place in appropriate subdirectory by function
2. Follow existing naming conventions
3. Document in this README
4. Consider version management needs
5. **IMPORTANT**: Keep all .md documentation files at the resources root directory to avoid unexpected classpath loading issues

### When Removing Resources
1. Verify no dynamic loading by name
2. Check for hardcoded references
3. Ensure not needed for historical data
4. Test with various provincial configurations

### Regular Maintenance
1. Update ODB formulary XML annually
2. Verify email templates if email feature is re-enabled
3. Test form PDF generation after library updates
4. Maintain backward compatibility

## Files That Can Be Safely Removed

Based on analysis, these files are not being used:

### Confirmed Unused
- `/oscar/oscarRx/data_extract_20181217.xml` - Old ODB file, superseded by 2025 version

### Email Templates (Currently Disabled but Templates Can Be Removed)
The email template loading code is commented out. These template files could be removed:
- `billing_invoice_email_notification_template.txt`
- `tickler_email_notification_template.txt`
- `wait_list_daily_admission_email_notification_template.txt`
- `wait_list_immediate_admission_email_template.txt`
- `wait_list_immediate_vacancy_email_template.txt`
- `wait_list_received_a_new_app_template.txt`

**Note**: `billing_invoice_email.properties` must remain even though unused - required for startup

## Related Documentation
- `/docs/legacy-hapi-dynamic-loading.md` - HAPI library isolation
- `/docs/txt-files-analysis.md` - Analysis of .txt file usage
- `README-form-resources.md` - Detailed form resources documentation
- Build configuration in `/pom.xml`
- Web application configuration in `/src/main/webapp/WEB-INF/web.xml`