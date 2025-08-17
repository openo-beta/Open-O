# MeasurementHL7Uploader API

## Overview
The MeasurementHL7Uploader is a Struts 2 action that allows external systems to upload patient measurements via HL7 ORU^R01 messages. This API endpoint processes HL7 messages containing observation results and stores them as measurements in the OpenO system.

## Endpoint
```
POST /MeasurementHL7Uploader.do
```

## Authentication
- Requires a valid OpenO session with `_measurement` write privileges
- Optional password protection via `oscar.measurements.hl7.password` property

## Configuration Properties
The following properties can be set in oscar.properties:

| Property | Description | Default |
|----------|-------------|---------|
| `oscar.measurements.hl7.defaultProviderNo` | Provider number to use when not specified in HL7 | `999998` |
| `oscar.measurements.hl7.password` | Optional password for additional security | `null` |
| `oscar.measurements.hl7.datetime.format` | Date/time format for parsing HL7 timestamps | `yyyyMMddHHmmss` |

## Request Parameters

### File Upload
- **Parameter**: `importFile`
- **Type**: File upload (multipart/form-data)
- **Description**: HL7 ORU^R01 message file containing measurement data

### Direct Message
- **Parameter**: `hl7msg`
- **Type**: String
- **Description**: HL7 message content as text (alternative to file upload)

### Password (optional)
- **Parameter**: `password`
- **Type**: String
- **Description**: Required if `oscar.measurements.hl7.password` is configured

## HL7 Message Format

### Supported Version
- HL7 v2.3 ORU^R01 (Observation Result) messages

### Required Segments
- **MSH**: Message header
- **PID**: Patient identification (must match existing demographic)
- **OBR**: Observation request
- **OBX**: Observation results (measurements)

### Example HL7 Message
```
MSH|^~\&|SENDING_APP|SENDING_FACILITY|OPENO|FACILITY|20240115120000||ORU^R01|MSG00001|P|2.3|||
PID|||12345^^^OPENO||DOE^JOHN||19700101|M|||
OBR|1|||PANEL^Lab Panel|||20240115120000|||||||||||||||F|||
OBX|1|NM|BP_SYSTOLIC^Blood Pressure Systolic||120|mmHg|90-120||||F|||20240115120000||
OBX|2|NM|BP_DIASTOLIC^Blood Pressure Diastolic||80|mmHg|60-80||||F|||20240115120000||
```

## Response

### Success Response
- **HTTP Status**: 200 OK
- **Body**: HTML page with success message

### Error Response
- **HTTP Status**: 500 Internal Server Error
- **Body**: Error message describing the issue

### Common Error Scenarios
1. **Authentication Error**: Missing session or insufficient privileges
2. **Invalid HL7**: Malformed HL7 message structure
3. **Patient Not Found**: PID segment references non-existent demographic
4. **Password Mismatch**: Incorrect password when password protection is enabled

## Measurement Mapping

The action maps HL7 OBX segments to OpenO measurements:

| HL7 Field | OpenO Field | Notes |
|-----------|-------------|-------|
| OBX-3 (Observation Identifier) | Measurement Type | Must match existing measurement type in OpenO |
| OBX-5 (Observation Value) | Data Field | Numeric or text value |
| OBX-6 (Units) | Units | Optional |
| OBX-14 (Date/Time) | Date Observed | Falls back to current date if not provided |
| PID-3 (Patient ID) | Demographic No | Must match existing patient |

## Security Considerations

1. **Session Security**: Requires authenticated OpenO session
2. **Password Protection**: Optional additional password can be configured
3. **Provider Validation**: Uses logged-in provider or configured default
4. **Input Validation**: HL7 messages are parsed and validated before processing

## Usage Example

### cURL Example
```bash
curl -X POST \
  -F "importFile=@measurements.hl7" \
  -F "password=your_password" \
  --cookie "JSESSIONID=your_session_id" \
  https://your-openo-instance/MeasurementHL7Uploader.do
```

### Programmatic Upload (Java)
```java
// Prepare HL7 message
String hl7Message = "MSH|^~\\&|...";

// Upload via HTTP client
HttpPost post = new HttpPost("https://openo/MeasurementHL7Uploader.do");
MultipartEntity entity = new MultipartEntity();
entity.addPart("hl7msg", new StringBody(hl7Message));
entity.addPart("password", new StringBody(password));
post.setEntity(entity);

// Execute with session cookie
HttpResponse response = httpClient.execute(post);
```

## Notes

- This action appears to be orphaned and not referenced in the current UI
- Consider using the REST API endpoints for measurement upload if available
- The action was migrated from Struts 1 to Struts 2 but may not be actively maintained
- Test thoroughly before using in production

## Related Files
- **Action Class**: `/src/main/java/oscar/oscarEncounter/oscarMeasurements/hl7/MeasurementHL7Uploader2Action.java`
- **Configuration**: `/src/main/webapp/WEB-INF/classes/struts.xml`