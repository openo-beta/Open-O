package ca.openosp.openo.integration.dhdr;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import ca.openosp.openo.commn.model.OMDGatewayTransactionLog;

/**
 * Unit tests for {@link OmdGateway#completeLog}, which stamps the outcome of a gateway call onto its
 * audit row (DHDR15.01: transaction status, return code, the service's message on failure, and the
 * service-generated transaction identifiers).
 *
 * <p>Two behaviours here are load-bearing rather than incidental. A failed response must be recorded
 * as a failure even when the service omits the {@code X-Request-Id} correlation header, and a
 * successful response body must be captured only when the caller opts in - the OAuth token endpoints
 * share this method and return credentials in the body.
 */
@Tag("unit")
@Tag("dhdr")
class OmdGatewayCompleteLogUnitTest {

  private static final String BODY = "{\"resourceType\":\"Bundle\"}";

  /**
   * Builds a mocked JAX-RS response.
   *
   * @param status int the HTTP status to report
   * @param requestId String the {@code X-Request-Id} header value, or {@code null} to omit it
   * @return Response the mocked response
   */
  private Response response(int status, String requestId) {
    Response response = mock(Response.class);
    MultivaluedMap<String, Object> headers = new MultivaluedHashMap<String, Object>();
    when(response.getStatus()).thenReturn(status);
    when(response.getHeaders()).thenReturn(headers);
    when(response.getHeaderString("X-Request-Id")).thenReturn(requestId);
    when(response.readEntity(String.class)).thenReturn(BODY);
    return response;
  }

  @Nested
  @DisplayName("transaction status (DHDR15.01 h, i)")
  class TransactionStatus {

    @Test
    @DisplayName("should record failure when the service fails and omits the correlation header")
    void shouldRecordFailure_whenServiceFailsWithoutRequestIdHeader() {
      OMDGatewayTransactionLog log = new OMDGatewayTransactionLog();

      OmdGateway.completeLog(log, response(500, null), true);

      assertThat(log.getSuccess()).isFalse();
      assertThat(log.getResultCode()).isEqualTo(500);
      assertThat(log.getError()).isEqualTo(BODY);
    }

    @Test
    @DisplayName("should record failure when the service fails and supplies the correlation header")
    void shouldRecordFailure_whenServiceFailsWithRequestIdHeader() {
      OMDGatewayTransactionLog log = new OMDGatewayTransactionLog();

      OmdGateway.completeLog(log, response(404, "req-1"), true);

      assertThat(log.getSuccess()).isFalse();
      assertThat(log.getError()).isEqualTo(BODY);
      assertThat(log.getxRequestId()).isEqualTo("req-1");
    }

    @Test
    @DisplayName("should record success when the service returns a 2xx")
    void shouldRecordSuccess_whenServiceReturnsOk() {
      OMDGatewayTransactionLog log = new OMDGatewayTransactionLog();

      OmdGateway.completeLog(log, response(200, "req-1"), true);

      assertThat(log.getSuccess()).isTrue();
      assertThat(log.getError()).isNull();
      assertThat(log.getEnded()).isNotNull();
    }

    @Test
    @DisplayName("should treat a redirect as a failed transaction")
    void shouldRecordFailure_whenServiceReturnsRedirect() {
      OMDGatewayTransactionLog log = new OMDGatewayTransactionLog();

      OmdGateway.completeLog(log, response(302, null), true);

      assertThat(log.getSuccess()).isFalse();
    }
  }

  @Nested
  @DisplayName("response payload capture")
  class ResponsePayloadCapture {

    @Test
    @DisplayName("should capture the response body when the caller opts in")
    void shouldCaptureBody_whenCallerOptsIn() {
      OMDGatewayTransactionLog log = new OMDGatewayTransactionLog();

      OmdGateway.completeLog(log, response(200, "req-1"), true);

      assertThat(log.getDataRecieved()).isEqualTo(BODY);
    }

    @Test
    @DisplayName("should not persist the response body when the caller does not opt in")
    void shouldNotPersistBody_whenCallerDoesNotOptIn() {
      OMDGatewayTransactionLog log = new OMDGatewayTransactionLog();
      Response response = response(200, "req-1");

      OmdGateway.completeLog(log, response, false);

      // The OAuth token endpoints opt out explicitly; their bodies carry access and refresh tokens,
      // which must never be persisted to the audit table. The body is still read into a local so the
      // EHR result code can be extracted from it, so what matters here is that it is not stored.
      assertThat(log.getDataRecieved()).isNull();
    }

    @Test
    @DisplayName("should capture the response body by default, which the gateway GET and POST rely on")
    void shouldCaptureBody_whenUsingTheDefaultOverload() {
      OMDGatewayTransactionLog log = new OMDGatewayTransactionLog();

      OmdGateway.completeLog(log, response(200, "req-1"));

      // doGet and doPost use the two-argument form and depend on this default to record the DHDR
      // response body (DHDR15.01). The four credential-bearing callers pass false explicitly, so
      // flipping this default would silently stop the audit table holding that evidence.
      assertThat(log.getDataRecieved()).isEqualTo(BODY);
    }

    @Test
    @DisplayName("should not capture response headers when the caller does not opt in")
    void shouldNotCaptureHeaders_whenCallerDoesNotOptIn() {
      OMDGatewayTransactionLog log = new OMDGatewayTransactionLog();

      // A token-endpoint response may set a session cookie; its headers stay out of the audit row.
      OmdGateway.completeLog(log, response(200, "req-1"), false);

      assertThat(log.getHeaders()).isNull();
    }

    @Test
    @DisplayName("should capture response headers when the caller opts in")
    void shouldCaptureHeaders_whenCallerOptsIn() {
      OMDGatewayTransactionLog log = new OMDGatewayTransactionLog();

      OmdGateway.completeLog(log, response(200, "req-1"), true);

      assertThat(log.getHeaders()).isNotNull();
    }

    @Test
    @DisplayName("should still record the service message when a failure is not opted in")
    void shouldRecordError_whenFailureAndCallerDidNotOptIn() {
      OMDGatewayTransactionLog log = new OMDGatewayTransactionLog();

      OmdGateway.completeLog(log, response(400, "req-1"), false);

      assertThat(log.getError()).isEqualTo(BODY);
    }
  }

  @Nested
  @DisplayName("service-generated transaction identifiers (DHDR15.01 c)")
  class TransactionIdentifiers {

    @Test
    @DisplayName("should record the correlation identifiers the service supplies")
    void shouldRecordCorrelationIds_whenServiceSuppliesThem() {
      OMDGatewayTransactionLog log = new OMDGatewayTransactionLog();
      Response response = response(200, "req-1");
      when(response.getHeaderString("X-LobTxId")).thenReturn("lob-1");
      when(response.getHeaderString("X-Correlation-Id")).thenReturn("corr-1");

      OmdGateway.completeLog(log, response, true);

      assertThat(log.getxRequestId()).isEqualTo("req-1");
      assertThat(log.getxLobTxId()).isEqualTo("lob-1");
      assertThat(log.getxCorrelationId()).isEqualTo("corr-1");
    }

    @Test
    @DisplayName("should record the lob identifier even when the request identifier is absent")
    void shouldRecordLobId_whenRequestIdIsAbsent() {
      OMDGatewayTransactionLog log = new OMDGatewayTransactionLog();
      Response response = response(200, null);
      when(response.getHeaderString("X-LobTxId")).thenReturn("lob-1");

      OmdGateway.completeLog(log, response, true);

      assertThat(log.getxRequestId()).isNull();
      assertThat(log.getxLobTxId()).isEqualTo("lob-1");
    }
  }
}
