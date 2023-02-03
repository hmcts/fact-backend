package uk.gov.hmcts.dts.fact.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.dts.fact.model.admin.CourtLock;
import uk.gov.hmcts.dts.fact.util.AdminFunctionalTestBase;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.*;
import static org.springframework.http.HttpStatus.*;
import static uk.gov.hmcts.dts.fact.util.TestUtil.*;

@ExtendWith(SpringExtension.class)
public class AdminCourtLockEndPointTest extends AdminFunctionalTestBase {

    private static final String ADMIN_COURTS_ENDPOINT = "/admin/courts/";
    private static final String COURT_LOCK_PATH = "/lock/";
    private static final String BARNSLEY_LAW_COURT_SLUG = "barnsley-law-courts";
    private static final String BARNSLEY_LAW_COURT_LOCK_PATH = ADMIN_COURTS_ENDPOINT
        + BARNSLEY_LAW_COURT_SLUG + COURT_LOCK_PATH;
    private static final String USER_EMAIL = "Test";
    private static final LocalDateTime TEST_LOCK_ACQUIRED =
        LocalDateTime.of(2001, 8, 28, 20, 20);


    /************************************************************* Get Request Tests. ***************************************************************/

    @Test
    public void returnLockForTheCourt() {
        final Response response = doGetRequest(
            BARNSLEY_LAW_COURT_LOCK_PATH,
            Map.of(AUTHORIZATION, BEARER + authenticatedToken)
        );
        assertThat(response.statusCode()).isEqualTo(OK.value());

    }

    @Test
    public void shouldRequireATokenWhenGettingLocksForTheCourt() {
        final Response response = doGetRequest(BARNSLEY_LAW_COURT_LOCK_PATH);
        assertThat(response.statusCode()).isEqualTo(UNAUTHORIZED.value());
    }

    @Test
    public void shouldBeForbiddenForGettingLocksForTheCourt() {
        final Response response = doGetRequest(
            BARNSLEY_LAW_COURT_LOCK_PATH,
            Map.of(AUTHORIZATION, BEARER + forbiddenToken)
        );
        assertThat(response.statusCode()).isEqualTo(FORBIDDEN.value());
    }

    /************************************************************* POST request tests section. ***************************************************************/

    //    @Test
    //    public void shouldCreateLock() throws JsonProcessingException {
    //
    //        final CourtLock expectedCourtLock = createCourtLock();
    //        final String newCourtLockJson = objectMapper().registerModule(new JavaTimeModule()).writeValueAsString(expectedCourtLock);
    //
    //        final var response = doPostRequest(
    //            BARNSLEY_LAW_COURT_LOCK_PATH,
    //            Map.of(AUTHORIZATION, BEARER + superAdminToken),
    //            newCourtLockJson
    //        );
    //
    //        assertThat(response.statusCode()).isEqualTo(CREATED.value());
    //        final CourtLock createdCourtLock = response.as(CourtLock.class);
    //        assertThat(createdCourtLock.getUserEmail()).isEqualTo(expectedCourtLock.getUserEmail());
    //
    //        // delete request test
    //        final var cleanUpResponse = doDeleteRequest(
    //            BARNSLEY_LAW_COURT_LOCK_PATH + createdCourtLock.getUserEmail(),
    //            Map.of(AUTHORIZATION, BEARER + superAdminToken),""
    //        );
    //
    //        assertThat(cleanUpResponse.statusCode()).isEqualTo(OK.value());
    //        final List<CourtLock> currentCourtLock = getCurrentLock();
    //        assertThat(currentCourtLock).isEmpty();
    //
    //    }

    @Test
    public void shouldBeForbiddenForCreatingCourtLock() throws JsonProcessingException {
        final CourtLock expectedCourtLock = createCourtLock();
        final String newCourtLockJson = objectMapper().registerModule(new JavaTimeModule()).writeValueAsString(expectedCourtLock);

        final Response response = doPostRequest(
            BARNSLEY_LAW_COURT_LOCK_PATH,
            Map.of(AUTHORIZATION, BEARER + forbiddenToken), newCourtLockJson
        );
        assertThat(response.statusCode()).isEqualTo(FORBIDDEN.value());
    }

    @Test
    public void shouldRequireATokenWhenCreatingCourtLock() throws JsonProcessingException {
        final CourtLock expectedCourtLock = createCourtLock();
        final String newCourtLockJson = objectMapper().registerModule(new JavaTimeModule()).writeValueAsString(expectedCourtLock);

        final Response response = doPostRequest(
            BARNSLEY_LAW_COURT_LOCK_PATH, newCourtLockJson
        );
        assertThat(response.statusCode()).isEqualTo(UNAUTHORIZED.value());
    }

    //    @Test
    //    public void shouldNotCreateCourtLockIfAlreadyExist() throws JsonProcessingException {
    //        final CourtLock expectedCourtLock = createCourtLock();
    //        final String courtLockJson = objectMapper().registerModule(new JavaTimeModule()).writeValueAsString(expectedCourtLock);
    //        final CourtLock alreadyExistCourtLock = createCourtLock();
    //        alreadyExistCourtLock.setUserEmail("xyz");
    //        final String courtLockJsonExisted = objectMapper().registerModule(new JavaTimeModule()).writeValueAsString(alreadyExistCourtLock);
    //
    //        final var response = doPostRequest(
    //            BARNSLEY_LAW_COURT_LOCK_PATH,
    //            Map.of(AUTHORIZATION, BEARER + superAdminToken),
    //            courtLockJson
    //        );
    //        assertThat(response.statusCode()).isEqualTo(CREATED.value());
    //
    //        final var responseAlreadyExist = doPostRequest(
    //            BARNSLEY_LAW_COURT_LOCK_PATH,
    //            Map.of(AUTHORIZATION, BEARER + superAdminToken),
    //            courtLockJsonExisted
    //        );
    //
    //        assertThat(responseAlreadyExist.statusCode()).isEqualTo(CONFLICT.value());
    //
    //        // cleanup step
    //        final var cleanUpResponse = doDeleteRequest(
    //            BARNSLEY_LAW_COURT_LOCK_PATH + expectedCourtLock.getUserEmail(),
    //            Map.of(AUTHORIZATION, BEARER + superAdminToken),""
    //        );
    //
    //        assertThat(cleanUpResponse.statusCode()).isEqualTo(OK.value());
    //        final List<CourtLock> currentCourtLock = getCurrentLock();
    //        assertThat(currentCourtLock).isEmpty();
    //    }

    /************************************************************* Delete request test section. ***************************************************************/


    @Test
    public void shouldRequireATokenWhenDeletingCourtLock() {
        final var response = doDeleteRequest(
            BARNSLEY_LAW_COURT_LOCK_PATH + USER_EMAIL,
            ""
        );
        assertThat(response.statusCode()).isEqualTo(UNAUTHORIZED.value());
    }


    /************************************************************* Shared utility methods. ***************************************************************/

    //    private List<CourtLock> getCurrentLock() {
    //        final var response = doGetRequest(
    //            BARNSLEY_LAW_COURT_LOCK_PATH,
    //            Map.of(AUTHORIZATION, BEARER + superAdminToken)
    //        );
    //        assertThat(response.statusCode()).isEqualTo(OK.value());
    //        return response.body().jsonPath().getList(".", CourtLock.class);
    //    }

    private CourtLock createCourtLock() {
        return new CourtLock(1, TEST_LOCK_ACQUIRED, USER_EMAIL, BARNSLEY_LAW_COURT_SLUG);
    }

}

