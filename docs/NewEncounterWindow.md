# New Encounter Window Documentation

## Overview

The New Encounter Window in OSCAR EMR is a complex interface that allows healthcare providers to document patient encounters. It consists of multiple components including CPP (Cumulative Patient Profile) sections, encounter notes, and various navigation elements.

## Architecture

### Main Components

1. **Main Layout**: `/casemgmt/newEncounterLayout.jsp`
   - Provides the overall structure and container divs
   - Includes the CPP boxes and main note area
   - Contains the floating overlay forms for editing

2. **JavaScript Controllers**: 
   - `/js/newCaseManagementView.js.jsp` - Main view logic
   - `/casemgmt/newEncounterLayout.js.jsp` - Layout-specific functions

3. **Backend Actions** (Struts 2):
   - `CaseManagementEntry2Action.java` - Handles note saving and editing
   - `CaseManagementView2Action.java` - Handles view rendering and data retrieval

## CPP (Cumulative Patient Profile) Sections

The CPP sections are displayed in a grid layout with four main areas:

- **divR1I1**: Social History (top-left)
- **divR1I2**: Medical History (top-right)
- **divR2I1**: Ongoing Concerns (bottom-left)
- **divR2I2**: Reminders (bottom-right)

### CPP Loading Process

1. On page load, `showIssueNotes()` or `showCustomIssueNotes()` is called
2. Each CPP section is loaded via AJAX from `CaseManagementView.do?method=listNotes`
3. The response populates individual divs with the content from `/casemgmt/viewNotes.jsp`

```javascript
// Example URL for loading Social History
ctx + "/CaseManagementView.do?hc=996633&method=listNotes&providerNo=" + providerNo + 
"&demographicNo=" + demographicNo + "&issue_code=SocHistory&title=" + socHistoryLabel + "&cmd=divR1I1"
```

## Note Saving Mechanisms

### 1. CPP Note Saves (Issue Notes)

CPP notes use a floating overlay form (`#showEditNote`) that appears when editing items in the CPP sections.

**Process:**
1. User clicks edit on a CPP item
2. Overlay form appears with the note content
3. Form submission calls `updateCPPNote()` JavaScript function
4. AJAX request sent to `CaseManagementEntry.do?method=issueNoteSave`
5. Backend saves the note and redirects to reload just that CPP section

**Key Parameters:**
- `containerDiv`: Identifies which CPP section (divR1I1, divR1I2, etc.)
- `reloadUrl`: URL to reload just that specific CPP section
- `value`: The note text
- `noteId`: ID of existing note (0 for new notes)

### 2. Main Encounter Note Saves

Main encounter notes are saved differently:

**Methods:**
- `save`: Standard save
- `ajaxsave`: AJAX-based save
- `saveAndExit`: Save and close encounter

**Process:**
1. Note content from main text area
2. Form submission via `saveNoteAjax()` function
3. Request to `CaseManagementEntry.do` with appropriate method
4. Updates the notes section (`#notCPP` div)

## AJAX Update Flow

### CPP Section Updates

```
User Action → JavaScript (updateCPPNote) → Backend (issueNoteSave) → Redirect with ajaxview → listNotes → viewNotes.jsp → Update Div
```

The fix implemented ensures that CPP saves only reload the specific section:

```java
// Check if this is an AJAX request from CPP overlay
if (containerDiv != null && !containerDiv.isEmpty() && 
    reloadUrl != null && !reloadUrl.isEmpty() &&
    (containerDiv.startsWith("divR") || containerDiv.contains("cpp"))) {
    
    // Build redirect URL with ajaxview parameter
    StringBuilder redirectUrl = new StringBuilder();
    redirectUrl.append(request.getContextPath());
    redirectUrl.append("/CaseManagementView.do?");
    redirectUrl.append("method=listNotes");
    redirectUrl.append("&ajaxview=listNotes");  // Critical parameter!
    redirectUrl.append("&issue_code=").append(issueCode);
    redirectUrl.append("&title=").append(URLEncoder.encode(title, "UTF-8"));
    redirectUrl.append("&cmd=").append(containerDiv);
    redirectUrl.append("&demographicNo=").append(demo);
    redirectUrl.append("&providerNo=").append(providerNo);
    
    response.sendRedirect(redirectUrl.toString());
    return null;
}
```

### Main Note Updates

Main notes update the entire notes section without affecting CPP areas.

## Forms and Hidden Fields

### CPP Edit Form (`frmIssueNotes`)
```html
<form id="frmIssueNotes" action="" method="post" onsubmit="return updateCPPNote();">
    <input type="hidden" id="reloadUrl" name="reloadUrl" value="">
    <input type="hidden" id="containerDiv" name="containerDiv" value="">
    <input type="hidden" id="issueChange" name="issueChange" value="">
    <input type="hidden" id="archived" name="archived" value="false">
    <!-- Note content and other fields -->
</form>
```

## Struts 2 Migration Considerations

When migrating from Struts 1 to Struts 2:

1. **Action Mappings**: Defined in `/WEB-INF/classes/struts.xml`
2. **Result Types**: 
   - `listCPPNotes` → redirects to `/CaseManagementView.do`
   - `listNotes` → renders `/casemgmt/viewNotes.jsp`
   - `ajaxView` → renders `/casemgmt/ChartNotes.jsp`

3. **Request Handling**: Must carefully handle AJAX vs full page requests
   - AJAX requests need partial content responses
   - Full page requests need complete page responses
   - The `ajaxview` parameter is crucial for determining response type

4. **Key Discovery**: CaseManagementView2Action uses the `ajaxview` parameter to determine whether to return:
   - Full page: when `ajaxview` is null/empty → returns "page.newcasemgmt.view"
   - Partial content: when `ajaxview` has a value → returns that value as the result
   - For CPP sections: `ajaxview=listNotes` → returns "listNotes" → renders `/casemgmt/viewNotes.jsp`

## Common Issues and Solutions

### Issue: Malformed Page After CPP Save
**Cause**: The action was returning a full page redirect instead of just the CPP content
**Solution**: Detect AJAX CPP requests and handle them differently by including the `ajaxview` parameter

**Technical Details**:
1. CPP saves include `containerDiv` and `reloadUrl` parameters
2. The fix detects these parameters and builds a redirect URL with `ajaxview=listNotes`
3. The `ajaxview` parameter tells CaseManagementView2Action to return partial content
4. Without this parameter, the view() method returns the full page layout

**Code Location**: `CaseManagementEntry2Action.issueNoteSave()` lines 1218-1308

### Issue: JavaScript Errors on Save
**Cause**: Response format mismatch between expected AJAX response and actual response
**Solution**: Ensure proper content-type and response format for AJAX requests

## Key JavaScript Functions

- `showIssueNotes()`: Loads all CPP sections
- `updateCPPNote()`: Handles CPP note saves
- `saveNoteAjax()`: Handles main encounter note saves
- `loadDiv()`: Generic function to load content into a div via AJAX
- `notifyDivLoaded()`: Callback after div content is loaded

## Security Considerations

1. All actions check for valid session and user permissions
2. Provider numbers and demographic numbers are validated
3. Note locking prevents concurrent edits
4. Audit logging tracks all note modifications

## Best Practices

1. Always preserve AJAX behavior for CPP updates to maintain user experience
2. Use proper null checks when handling request parameters
3. Maintain backward compatibility when modifying save mechanisms
4. Test both CPP and main note saves after any changes
5. Ensure proper error handling for failed AJAX requests

## Debugging Tips

1. Check browser console for JavaScript errors
2. Use browser dev tools to inspect AJAX requests/responses
3. Verify that hidden form fields are properly populated
4. Check server logs for backend errors
5. Ensure Struts action mappings are correct