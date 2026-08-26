package ca.openosp.openo.integration.dhdr;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

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
 *
 * <p>A third is covered here because it cannot be seen from the audit row at all: the response has to
 * survive being audited. Reading a CXF entity closes it, so a response audited without being buffered
 * first is unreadable by the caller that asked for it.
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
    @DisplayName("should not read the response body when the caller does not opt in")
    void shouldNotReadBody_whenCallerDoesNotOptIn() {
      OMDGatewayTransactionLog log = new OMDGatewayTransactionLog();
      Response response = response(200, "req-1");

      OmdGateway.completeLog(log, response, false);

      // The OAuth token endpoints reach completeLog through the two-argument overload; their bodies
      // carry access and refresh tokens, which must never be persisted to the audit table.
      assertThat(log.getDataRecieved()).isNull();
      verify(response, never()).readEntity(String.class);
    }

    @Test
    @DisplayName("should not capture a body into the audit row by default")
    void shouldNotCaptureBody_whenUsingTheDefaultOverload() {
      OMDGatewayTransactionLog log = new OMDGatewayTransactionLog();

      OmdGateway.completeLog(log, response(200, "req-1"));

      assertThat(log.getDataRecieved()).isNull();
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
  @DisplayName("entity survives being audited")
  class EntityAvailability {

    @Test
    @DisplayName("should buffer the entity before reading it when the caller opts in")
    void shouldBufferEntity_beforeReadingBody() {
      OMDGatewayTransactionLog log = new OMDGatewayTransactionLog();
      Response response = response(200, "req-1");

      OmdGateway.completeLog(log, response, true);

      // Order is the whole point: buffering after the read is too late, the stream is already closed.
      InOrder inOrder = inOrder(response);
      inOrder.verify(response).bufferEntity();
      inOrder.verify(response).readEntity(String.class);
    }

    @Test
    @DisplayName("should buffer the entity on a failure the caller did not opt in to")
    void shouldBufferEntity_whenFailureAndCallerDidNotOptIn() {
      OMDGatewayTransactionLog log = new OMDGatewayTransactionLog();
      Response response = response(400, "req-1");

      OmdGateway.completeLog(log, response, false);

      // A failed token call is read for its error here and read again by the OAuth caller.
      verify(response).bufferEntity();
    }

    @Test
    @DisplayName("should buffer the entity even when the caller does not opt in")
    void shouldBufferEntity_whenCallerDoesNotOptIn() {
      OMDGatewayTransactionLog log = new OMDGatewayTransactionLog();
      Response response = response(200, "req-1");

      OmdGateway.completeLog(log, response, false);

      // DHDRManager.search2 and the DHIR retrievals read the body after this returns; nothing here
      // knows whether a given caller will, so the entity is left re-readable for all of them.
      // Whether this method reads it is a separate property, asserted in ResponsePayloadCapture.
      verify(response).bufferEntity();
    }

    @Test
    @DisplayName("should still record the outcome when the entity cannot be buffered")
    void shouldRecordOutcome_whenEntityCannotBeBuffered() {
      OMDGatewayTransactionLog log = new OMDGatewayTransactionLog();
      Response response = response(503, "req-1");
      doThrow(new IllegalStateException("Entity is not available")).when(response).bufferEntity();

      OmdGateway.completeLog(log, response, true);

      // An audit row is owed for the call whether or not its body can be recovered (DHDR15.01).
      assertThat(log.getSuccess()).isFalse();
      assertThat(log.getResultCode()).isEqualTo(503);
      assertThat(log.getxRequestId()).isEqualTo("req-1");
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
