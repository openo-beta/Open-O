# PathValidationUtils - Secure File Path Validation

## Overview

`PathValidationUtils` is a centralized utility class for validating file paths to prevent path traversal attacks in OpenO EMR. It provides consistent, robust validation across the entire codebase.

**Package**: `ca.openosp.openo.utility.PathValidationUtils`
**Since**: 2025-12-09

## Why Use PathValidationUtils?

Before PathValidationUtils, path validation was implemented inconsistently across the codebase with various patterns:
- Manual `canonicalPath.startsWith(baseDir + File.separator)` checks
- Custom sanitization methods in individual files
- Inconsistent temp directory validation

PathValidationUtils provides:
- **Consistency**: Single source of truth for path validation
- **Security**: Comprehensive validation including hidden file rejection, filename sanitization
- **Maintainability**: Easier to fix issues across all uses
- **Robustness**: Handles edge cases like symlinks, temp directories, Tomcat work directories

## API Reference

### validatePath(String userProvidedFileName, File allowedDir)

**Use for**: Validating user-provided filenames before creating/reading files in a directory.

**What it does**:
1. Sanitizes the filename (strips path components, rejects hidden files)
2. Validates the resulting path is within the allowed directory

```java
// Example: Secure file download
File directory = new File(documentDir);
File safeFile = PathValidationUtils.validatePath(userProvidedFilename, directory);
// Now safe to read from safeFile
```

**Returns**: `File` - The validated file path

**Throws**: `SecurityException` if validation fails

### validateExistingPath(File file, File allowedDir)

**Use for**: Validating internal/application-created paths before access or deletion.

**What it does**:
1. Validates the file's canonical path is within the allowed directory
2. Does NOT sanitize or reconstruct the path

```java
// Example: Validate before file deletion
File fileToDelete = new File(filePath);
File docDir = new File(documentDir);
PathValidationUtils.validateExistingPath(fileToDelete, docDir);
// Now safe to delete
```

**Returns**: `File` - The same file if valid

**Throws**: `SecurityException` if the file is outside the allowed directory

### validateUpload(File sourceFile)

**Use for**: Validating uploaded source files from Struts2/Tomcat.

**What it does**:
1. Verifies source file exists and is a regular file
2. Validates the source is from an allowed temp location

```java
// Example: Validate upload before processing
PathValidationUtils.validateUpload(uploadedFile);
InputStream is = new FileInputStream(uploadedFile);
// Now safe to read content
```

**Throws**: `SecurityException` if validation fails

### validateUpload(File sourceFile, String userProvidedFileName, File destinationDir)

**Use for**: Complete end-to-end upload validation.

**What it does**:
1. Validates source file exists and is from an allowed location
2. Sanitizes the user-provided filename
3. Validates the destination path is within the allowed directory

```java
// Example: Complete upload workflow
File destination = PathValidationUtils.validateUpload(
    uploadedFile,
    originalFilename,
    new File(documentDir)
);
FileUtils.copyFile(uploadedFile, destination);
```

**Returns**: `File` - The validated destination path

**Throws**: `SecurityException` if any validation fails

### isInAllowedTempDirectory(File file)

**Use for**: Checking if a file is in an allowed system temp directory.

**What it does**:
- Checks if file is within:
  - `java.io.tmpdir` (System temp directory)
  - `catalina.base/work` (Tomcat work directory)
  - `catalina.home/work` (Tomcat home work directory)

```java
// Example: Validate temp file location
if (!PathValidationUtils.isInAllowedTempDirectory(tempFile)) {
    throw new SecurityException("Invalid temp file location");
}
```

**Returns**: `boolean` - true if in allowed temp directory

## Migration Guide

### Before (Old Pattern)
```java
// Manual validation - inconsistent and error-prone
String canonicalPath = file.getCanonicalPath();
String baseCanonical = baseDir.getCanonicalPath();
if (!canonicalPath.startsWith(baseCanonical + File.separator)) {
    throw new SecurityException("Path traversal detected");
}
```

### After (Using PathValidationUtils)
```java
// Clean, consistent, and robust
PathValidationUtils.validateExistingPath(file, baseDir);
```

### Common Migration Patterns

#### 1. Download/Read Operations
```java
// Old
String sanitizedFilename = filename.replaceAll("[\\/]", "");
File file = new File(dir, sanitizedFilename);
if (!file.getCanonicalPath().startsWith(dir.getCanonicalPath() + File.separator)) {
    throw new SecurityException("Invalid path");
}

// New
File file = PathValidationUtils.validatePath(filename, new File(dir));
```

#### 2. Upload Validation
```java
// Old
String tempDir = System.getProperty("java.io.tmpdir");
String canonicalPath = uploadedFile.getCanonicalPath();
if (!canonicalPath.startsWith(new File(tempDir).getCanonicalPath() + File.separator)) {
    throw new SecurityException("Invalid upload source");
}

// New
PathValidationUtils.validateUpload(uploadedFile);
```

#### 3. File Deletion
```java
// Old
String canonicalPath = fileToDelete.getCanonicalPath();
if (!canonicalPath.startsWith(allowedDir.getCanonicalPath() + File.separator)) {
    throw new SecurityException("Cannot delete file outside directory");
}

// New
PathValidationUtils.validateExistingPath(fileToDelete, allowedDir);
```

#### 4. Multi-Directory Validation
```java
// Old
boolean isAllowed = canonicalPath.startsWith(docDir + File.separator) ||
                    canonicalPath.startsWith(tempDir + File.separator);
if (!isAllowed) throw new SecurityException("Invalid path");

// New
try {
    PathValidationUtils.validateExistingPath(file, new File(docDir));
} catch (SecurityException e) {
    if (!PathValidationUtils.isInAllowedTempDirectory(file)) {
        throw new SecurityException("Invalid path");
    }
}
```

## Security Features

### Filename Sanitization
- Strips path components (directory prefixes)
- Rejects hidden files (starting with `.`)
- Uses Apache Commons IO FilenameUtils for cross-platform support

### Path Traversal Prevention
- Uses canonical path resolution to follow symlinks
- Validates containment with proper separator handling
- Rejects attempts to escape directories

### Temp Directory Handling
- Recognizes system temp directory
- Recognizes Tomcat work directories (where Struts2 stores uploads)
- Thread-safe lazy initialization of allowed directories

## Best Practices

1. **Always use PathValidationUtils** for any file operations involving user input
2. **Use `validatePath()`** for user-provided filenames (creates new safe paths)
3. **Use `validateExistingPath()`** for internal paths (validates existing paths)
4. **Use `validateUpload()`** for file uploads
5. **Handle SecurityException** gracefully - don't expose internal paths in error messages
6. **Log security violations** for monitoring

## Testing

Tests are located at:
`src/test-modern/java/ca/openosp/openo/utility/PathValidationUtilsTest.java`

Run tests with:
```bash
mvn test -Dtest=PathValidationUtilsTest
```

## SAST False Positives (Snyk Code)

Snyk Code flags path traversal findings (CWE-22) in files that use `PathValidationUtils` because its inter-procedural taint analysis does not propagate sanitization through custom utility method boundaries. Internally, `PathValidationUtils` uses `File.getCanonicalPath()` + `String.startsWith()` — a pattern Snyk recognizes as a sanitizer — but only when inlined at the call site, not when called through a wrapper method.

**These findings are false positives.** The validation is present and effective; Snyk simply cannot see it.

For per-finding triage details, see `docs/security/snyk-false-positives.md`.

### Recommended triage process

1. **New Snyk Code scan** — filter for Path Traversal (CWE-22) findings
2. **Check the call site** — if the file operation is guarded by a `PathValidationUtils` method, a `IncomingDocUtil` validated path, or a hardcoded whitelist/map lookup, mark as ignored with reason: *"Sanitized by PathValidationUtils — see docs/path-validation-utils.md"*
3. **Investigate unguarded sites** — any file operation using HTTP params without `PathValidationUtils` is a real finding and should be fixed
4. **Snyk web UI** — mark false positives as ignored there so the dashboard stays clean across scans (finding IDs in `.snyk` files can drift after code changes)

### If Snyk Custom Rules are available (Team/Enterprise plan)

Register `PathValidationUtils` methods as sanitizers in the Snyk web UI                                                                   (**Org Settings → Snyk Code → Custom Rules**) to eliminate false positives at the engine level. Create sanitizer rules for:

- `PathValidationUtils.validatePath()`                                                                                                               
- `PathValidationUtils.validateExistingPath()`                                                                                                       
- `PathValidationUtils.validateUpload()`
- `PathValidationUtils.isInAllowedTempDirectory()`

## Related Documentation

- [OWASP Path Traversal](https://owasp.org/www-community/attacks/Path_Traversal)
- [CWE-22: Improper Limitation of a Pathname](https://cwe.mitre.org/data/definitions/22.html)
