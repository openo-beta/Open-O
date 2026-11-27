# Playwright MCP Server Configuration

## Chromium Executable Path Verification

The `.mcp.json` file in the repository root configures the Playwright MCP server with a specific Chromium executable path. This path must match the Chromium version installed by Playwright.

### Current Configuration

- **Playwright Version**: 1.65.1 (specified in `.devcontainer/development/Dockerfile`)
- **Chromium Revision**: 1255
- **Executable Path**: `/root/.cache/ms-playwright/chromium-1255/chrome-linux64/chrome`
- **Node.js**: 24.x (pinned in the Dockerfile)

> **Minimum version**: Playwright must be 1.60.0 or newer. Older versions hang right after the
> browser download reaches 100% when run on Node 24.16+ or Node 26 (the archive extraction stalls).
> See [microsoft/playwright#40724](https://github.com/microsoft/playwright/issues/40724).

### How to Verify the Path

If you need to verify or update the Chromium executable path, follow these steps:

1. **Check Playwright version** in the Dockerfile:
   ```bash
   grep "playwright@" .devcontainer/development/Dockerfile
   ```

2. **Find the Chromium revision** for that Playwright version:
   ```bash
   # For Playwright 1.65.1
   curl -s https://raw.githubusercontent.com/microsoft/playwright/v1.65.1/packages/playwright-core/browsers.json | grep -A 3 '"name": "chromium"'
   ```

3. **Verify the installed path** inside the devcontainer:
   ```bash
   ls -la /root/.cache/ms-playwright/
   ```

4. **Dry-run installation** to see what would be installed:
   ```bash
   npx playwright install chromium --dry-run
   ```

### Directory Structure

Playwright's Chromium installation follows this structure on Linux:
```
/root/.cache/ms-playwright/
└── chromium-{revision}/
    └── chrome-linux64/
        └── chrome          # The executable
```

**Note**: Since Playwright 1.57, Chromium is a Chrome for Testing build and unpacks to `chrome-linux64/` on x64 (`chrome-linux-arm64/` on arm64). Playwright 1.56 and earlier used `chrome-linux/`.

### Updating the Configuration

If you update the Playwright version in the Dockerfile, you must also update the Chromium path in `.mcp.json`:

1. Check the new Chromium revision from browsers.json (see step 2 above)
2. Update the `--executable-path` argument in `.mcp.json`:
   ```json
   {
     "mcpServers": {
       "playwright": {
         "args": [
           ...
           "--executable-path",
           "/root/.cache/ms-playwright/chromium-{NEW_REVISION}/chrome-linux64/chrome"
         ]
       }
     }
   }
   ```

### Playwright Version to Chromium Revision Mapping

| Playwright Version | Chromium Revision | Chromium Browser Version |
|-------------------|-------------------|--------------------------|
| 1.56.0            | 1194              | 141.0.7390.37            |
| 1.57.0            | 1200              | 143.0.7499.4             |
| 1.65.1            | 1255              | 159.0.8154.6             |

To find the mapping for other versions, check the official browsers.json file:
```bash
curl -s https://raw.githubusercontent.com/microsoft/playwright/v{VERSION}/packages/playwright-core/browsers.json
```
