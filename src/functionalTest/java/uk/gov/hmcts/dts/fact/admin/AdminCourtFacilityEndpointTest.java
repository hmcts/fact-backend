package uk.gov.hmcts.dts.fact.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.dts.fact.model.admin.Facility;
import uk.gov.hmcts.dts.fact.util.AdminFunctionalTestBase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.*;
import static uk.gov.hmcts.dts.fact.util.TestUtil.BEARER;
import static uk.gov.hmcts.dts.fact.util.TestUtil.objectMapper;

@ExtendWith(SpringExtension.class)
public class AdminCourtFacilityEndpointTest extends AdminFunctionalTestBase {

    private static final String ADMIN_COURTS_ENDPOINT = "/admin/courts/";
    private static final String FACILITIES_PATH = "facilities";
    private static final String AYLESBURY_COUNTY_COURT_AND_FAMILY_COURT_SLUG = "aylesbury-county-court-and-family-court/";
    private static final String GREENWICH_MAGISTRATES_COURT_SLUG = "greenwich-magistrate-court/";


    private static final String AYLESBURY_COURT_FACILITIES_PATH = ADMIN_COURTS_ENDPOINT + AYLESBURY_COUNTY_COURT_AND_FAMILY_COURT_SLUG
        + FACILITIES_PATH;
    private static final String COURT_NOT_FIND_PATH = ADMIN_COURTS_ENDPOINT + GREENWICH_MAGISTRATES_COURT_SLUG
        + FACILITIES_PATH;

    private static final String TEST_FACILITY_NAME1 = "Lift";
    private static final String TEST_FACILITY_NAME2 = "Refreshments";
    private static final String TEST_FACILITY_DESCRIPTION = "TEST";
    private static final String TEST_FACILITY_DESCRIPTION_CY = "TESTCY";


    @Test
    public void returnFacilitiesForTheCourt() {
        final var response = doGetRequest(
            AYLESBURY_COURT_FACILITIES_PATH,
            Map.of(AUTHORIZATION, BEARER + authenticatedToken)
        );
        assertThat(response.statusCode()).isEqualTo(OK.value());

        final List<Facility> facilities = response.body().jsonPath().getList(".", Facility.class);
        assertThat(facilities).hasSizeGreaterThan(1);
    }

    @Test
    public void shouldRequireATokenWhenGettingFacilities() {
        final var response = doGetRequest(AYLESBURY_COURT_FACILITIES_PATH);
        assertThat(response.statusCode()).isEqualTo(UNAUTHORIZED.value());
    }

    @Test
    public void shouldBeForbiddenForGettingFacilities() {
        final var response = doGetRequest(
            AYLESBURY_COURT_FACILITIES_PATH,
            Map.of(AUTHORIZATION, BEARER + forbiddenToken)
        );
        assertThat(response.statusCode()).isEqualTo(FORBIDDEN.value());
    }

    @Test
    public void shouldNotRetrieveFacilitiesWhenCourtSlugNotFound() {
        final var response = doGetRequest(COURT_NOT_FIND_PATH, Map.of(AUTHORIZATION, BEARER + authenticatedToken));
        assertThat(response.statusCode()).isEqualTo(NOT_FOUND.value());
    }

    @Test
    public void shouldUpdateCourtFacilities() throws JsonProcessingException {
        final List<Facility> currentCourtFacilities = getCurrentFacilities();
        final List<Facility> expectedFacilities = updateFacilities(currentCourtFacilities);
        final String updatedJson = objectMapper().writeValueAsString(expectedFacilities);
        final String originalJson = objectMapper().writeValueAsString(currentCourtFacilities);

        final var response = doPutRequest(
            AYLESBURY_COURT_FACILITIES_PATH,
            Map.of(AUTHORIZATION, BEARER + superAdminToken),
            updatedJson
        );
        assertThat(response.statusCode()).isEqualTo(OK.value());

        final List<Facility> updatedFacilities = response.body().jsonPath().getList(
            ".",
            Facility.class
        );
        assertThat(updatedFacilities).containsExactlyElementsOf(expectedFacilities);

        //clean up by removing added record
        final var cleanUpResponse = doPutRequest(
            AYLESBURY_COURT_FACILITIES_PATH,
            Map.of(AUTHORIZATION, BEARER + superAdminToken),
            originalJson
        );
        assertThat(cleanUpResponse.statusCode()).isEqualTo(OK.value());

        final List<Facility> cleanFacilities = cleanUpResponse.body().jsonPath().getList(
            ".",
            Facility.class
        );
        assertThat(cleanFacilities).containsExactlyElementsOf(currentCourtFacilities);
    }

    private List<Facility> getCurrentFacilities() {
        final var response = doGetRequest(
            AYLESBURY_COURT_FACILITIES_PATH,
            Map.of(AUTHORIZATION, BEARER + superAdminToken)
        );
        return response.body().jsonPath().getList(".", Facility.class);
    }

    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    private List<Facility> updateFacilities(final List<Facility> facilities) {
        final List<Facility> updatedFacilities = new ArrayList<>(facilities);
        Facility facility = new Facility();
        facility.setName(TEST_FACILITY_NAME1);
        facility.setDescription(TEST_FACILITY_DESCRIPTION);
        facility.setDescriptionCy(TEST_FACILITY_DESCRIPTION_CY);
        updatedFacilities.add(facility);

        return updatedFacilities;
    }

    private static String getTestFacility() throws JsonProcessingException {
        final List<Facility> facilities = Arrays.asList(
            new Facility(TEST_FACILITY_NAME1, TEST_FACILITY_DESCRIPTION, TEST_FACILITY_DESCRIPTION_CY),
            new Facility(TEST_FACILITY_NAME2, TEST_FACILITY_DESCRIPTION, TEST_FACILITY_DESCRIPTION_CY)

        );
        return objectMapper().writeValueAsString(facilities);
    }


}