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
 *
 *  Integration test harness for RV Notification Callback (CO-7339)
 */

public class notificationTest
{
    private static RequestSpecification requestSpec;

    private static final String AUTHENTICATION_TOKEN = "<INSERT-KEY>";

    private static final String CHECK_UUID = "19779c90-3ed5-419d-98ca-e14accd01da5";

    private static final String APPLICANT_UUID = "190cb624-6a61-4f39-ad1a-e33afca07179";

    private static final String BASE_URI = "https://qa.contego.com";

    private static final String SVC_URL = "/webhook/rv/notificationresult/rvnotify";

    @BeforeClass
    public static void setup()
    {
        // Create standard request for use across tests
        requestSpec = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setBaseUri(BASE_URI)
                .setBasePath(SVC_URL)
                // log request and response for better debugging.
                // You can also only log if a requests fails.
                .addFilter(new ResponseLoggingFilter())
                .addFilter(new RequestLoggingFilter())
                .build();
    }

    @Test
    public void givenValidNotification_whenPosted_thenCreateResource()
    {
        // Given
        Map<String, String> dummyNote = getDummyNote();

        // When
        Response response = createNotificationResource(AUTHENTICATION_TOKEN, dummyNote);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SC_CREATED);
    }

    @Test
    public void givenInvalidNotificationData_whenPosted_thenUnprocessableEntity()
    {
        // Given
        Map<String, String> dummyNote = new HashMap<>();
        dummyNote.put("incorrect_uuid", CHECK_UUID);
        dummyNote.put("applicant_uuid", APPLICANT_UUID);

        // When
        Response response = createNotificationResource(AUTHENTICATION_TOKEN, dummyNote);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SC_UNPROCESSABLE_ENTITY);
    }

    @Test
    public void givenEmptyNotification_whenPosted_thenBadRequest()
    {
        // Given
        Map<String, String> emptyNote = new HashMap<>();

        // When
        Response response = createNotificationResource(AUTHENTICATION_TOKEN, emptyNote);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void givenMissingCheckId_whenPosted_thenBadRequest()
    {
        // Given
        Map<String, String> invalidNote = new HashMap<>();
        invalidNote.put("applicant_uuid", APPLICANT_UUID);

        // When
        Response response = createNotificationResource(AUTHENTICATION_TOKEN, invalidNote);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void givenMissingApplicantId_whenPosted_thenBadRequest()
    {
        // Given
        Map<String, String> invalidNote = new HashMap<>();
        invalidNote.put("check_uuid", APPLICANT_UUID);

        // When
        Response response = createNotificationResource(AUTHENTICATION_TOKEN, invalidNote);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SC_CREATED);
    }

    @Test
    public void givenNull_whenPosted_thenBadRequest()
    {
        // Given
        Map<String, String> invalidNote = createNote(null, "SomeKey");

        // When
        Response response = createNotificationResource(AUTHENTICATION_TOKEN, invalidNote);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void givenInvalidAuthenticationToken_whenRequestSecuredResource_thenUnauthorized()
    {
        // Given
        Map<String, String> dummyNote = getDummyNote();
        String wrongKey = Base64.encodeBase64String("TheWrongKey".getBytes());

        // When
        Response response = createNotificationResource(wrongKey, dummyNote);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SC_UNAUTHORIZED);
    }


    @Test
    public void givenNoAuthenticationToken_whenRequestSecuredResource_thenUnauthorized()
    {
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
    public void givenNoAuthenticationToken_whenRequestSecuredResource_thenUnauthorized_Style2() {

        Map<String, String> dummyNote = getDummyNote();

        given() // custom POST - no auth token
                .header("Authorization", "Basic ")
                .spec(requestSpec)
                .body(dummyNote)
        .when()
                .post()
        .then()
                .assertThat()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);
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

    @Test
    public void givenNoAuthorizationHeader_whenRequestSecuredResource_thenUnauthorized_Style2() {

        Map<String, String> dummyNote = getDummyNote();

        given() // Custom POST - no authorization header
                .spec(requestSpec)
                .body(dummyNote)
        .when()
                .post()
        .then()
                .assertThat()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    /**
     * Creates a new RV Notification resource on the Northrow Server.
     *
     * @param authToken - they authorization API token
     * @param params -  the map of key value pairs representing the
     *                  RV notification
     * @return the HTTP response
     */
    private Response createNotificationResource(String authToken, Map<String, String> params)
    {
        String value = "Basic " + Base64.encodeBase64String(authToken.getBytes());

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
        return createNote(CHECK_UUID, APPLICANT_UUID);
    }

    /**
     * Creates a new map of key-value pairs representing the RV notification
     *
     * @param check_uuid  the UUID for the check in NR
     * @param applicant_uuid  the UUID for the applicant (possibly optional)
     * @return a map of key-value pairs
     */
    private Map<String, String> createNote(String check_uuid, String applicant_uuid) {
        Map<String, String> params = new HashMap<>();
        params.put("check_uuid", check_uuid);
        params.put("applicant_uuid", applicant_uuid);
        return params;
    }
}
