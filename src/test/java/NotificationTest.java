import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpStatus;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by IntelliJ IDEA.
 * User: David Smiles
 * Date: 11/06/2019
 * Time: 04:21 PM
 * <p>
 * Integration tests for the Notification Callbacks endpoint
 */
public class NotificationTest {

    private static RequestSpecification requestSpec;

    private static String authenticationToken;

    private static String verificationUUID;

    private static String applicantUUID;

    private static final String BASE_URI = "https://idvserver.testlab.local";

    private static final String SVC_URL = "/webhook/idv/notificationresult/idvnotify";

    @BeforeClass
    public static void setup() {

        authenticationToken = System.getenv("AUTHENTICATION_TOKEN");
        if (authenticationToken == null) {
            throw new IllegalArgumentException("Authentication token has not been defined");
        }

        verificationUUID = System.getenv("VERIFICATION_UUID");
        if (verificationUUID == null) {
            throw new IllegalArgumentException("Verification UUID has not been defined");
        }

        applicantUUID = System.getenv("APPLICANT_UUID");
        if (applicantUUID == null) {
            throw new IllegalArgumentException("Applicant UUID has not been defined");
        }

        requestSpec = new RequestSpecBuilder()
            .setContentType(ContentType.JSON)
            .setBaseUri(BASE_URI)
            .setBasePath(SVC_URL)
            .addFilter(new ResponseLoggingFilter())
            .addFilter(new RequestLoggingFilter())
            .build();
    }

    @Test
    public void givenValidNotification_whenPosted_thenCreateResource() {
        // Given
        Map<String, String> dummyNote = getDummyNote();

        // When
        Response response = createNotificationResource(authenticationToken, dummyNote);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SC_CREATED);
    }

    @Test
    public void givenInvalidNotificationData_whenPosted_thenUnprocessableEntity() {
        // Given
        Map<String, String> dummyNote = new HashMap<>();
        dummyNote.put("incorrect_uuid", verificationUUID);
        dummyNote.put("applicant_uuid", applicantUUID);

        // When
        Response response = createNotificationResource(authenticationToken, dummyNote);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SC_UNPROCESSABLE_ENTITY);
    }

    @Test
    public void givenEmptyNotification_whenPosted_thenBadRequest() {
        // Given
        Map<String, String> emptyNote = new HashMap<>();

        // When
        Response response = createNotificationResource(authenticationToken, emptyNote);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void givenMissingVerificationId_whenPosted_thenBadRequest() {
        // Given
        Map<String, String> invalidNote = new HashMap<>();
        invalidNote.put("applicant_uuid", applicantUUID);

        // When
        Response response = createNotificationResource(authenticationToken, invalidNote);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void givenMissingApplicantId_whenPosted_thenBadRequest() {
        // Given
        Map<String, String> invalidNote = new HashMap<>();
        invalidNote.put("verification_uuid", applicantUUID);

        // When
        Response response = createNotificationResource(authenticationToken, invalidNote);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SC_CREATED);
    }

    @Test
    public void givenNull_whenPosted_thenBadRequest() {
        // Given
        Map<String, String> invalidNote = createNote(null, "SomeUUID");

        // When
        Response response = createNotificationResource(authenticationToken, invalidNote);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void givenInvalidAuthenticationToken_whenRequestSecuredResource_thenUnauthorized() {
        // Given
        Map<String, String> dummyNote = getDummyNote();
        String wrongKey = Base64.encodeBase64String("TheWrongToken".getBytes());

        // When
        Response response = createNotificationResource(wrongKey, dummyNote);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SC_UNAUTHORIZED);
    }


    @Test
    public void givenNoAuthenticationToken_whenRequestSecuredResource_thenUnauthorized() {
        // Given
        Map<String, String> dummyNote = getDummyNote();

        // When - Custom POST - no auth token
        Response response = given()
            .header("Authorization", "Basic ")
            .spec(requestSpec)
            .body(dummyNote)
            .when()
            .post()
            .andReturn();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    public void givenNoAuthorizationHeader_whenRequestSecuredResource_thenUnauthorized() {

        // Given
        Map<String, String> dummyNote = getDummyNote();

        // When - Custom POST - no authorisation header
        Response response = given()
            .spec(requestSpec)
            .body(dummyNote)
            .when()
            .post()
            .andReturn();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SC_UNAUTHORIZED);
    }

    /**
     * Creates a new Notification resource on the server.
     *
     * @param token  the authentication token
     * @param params the map of key value pairs representing the notification
     * @return the HTTP response
     */
    private Response createNotificationResource(String token, Map<String, String> params) {
        String value = "Basic " + Base64.encodeBase64String(token.getBytes());

        return given()
            .header("Authorization", value)
            .spec(requestSpec)
            .body(params)
            .when()
            .post()
            .then()
            .extract()
            .response();
    }

    /**
     * @return Returns a pre-canned notification
     */
    private Map<String, String> getDummyNote() {
        return createNote(verificationUUID, applicantUUID);
    }

    /**
     * Creates a new map of key-value pairs representing the notification
     *
     * @param verificationUuid  the UUID for a particular verification
     * @param applicantUuid     the UUID for a particular applicant
     * @return a map of key-value pairs
     */
    private Map<String, String> createNote(String verificationUuid, String applicantUuid) {
        Map<String, String> params = new HashMap<>();
        params.put("verification_uuid", verificationUuid);
        params.put("applicant_uuid", applicantUuid);
        return params;
    }
}
