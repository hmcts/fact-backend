package uk.gov.hmcts.dts.fact.admin;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.dts.fact.model.admin.AddressType;
import uk.gov.hmcts.dts.fact.model.admin.County;
import uk.gov.hmcts.dts.fact.util.AdminFunctionalTestBase;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.*;
import static uk.gov.hmcts.dts.fact.util.TestUtil.BEARER;

@ExtendWith(SpringExtension.class)
public class AdminCountyEndpointTest extends AdminFunctionalTestBase {

    private static final String COUNTIES_ENDPOINT = "/admin/counties";

    @Test
    public void shouldGetAllCounties() {
        final Response response = doGetRequest(COUNTIES_ENDPOINT, Map.of(AUTHORIZATION, BEARER + authenticatedToken));
        assertThat(response.statusCode()).isEqualTo(OK.value());

        final List<County> counties = response.body().jsonPath().getList(".", County.class);
        assertThat(counties).hasSizeGreaterThan(1);
    }

    @Test
    public void shouldRequireATokenWhenGettingAllCounties() {
        final Response response = doGetRequest(COUNTIES_ENDPOINT);
        assertThat(response.statusCode()).isEqualTo(UNAUTHORIZED.value());
    }

    @Test
    public void shouldBeForbiddenForGettingAllCounties() {
        final Response response = doGetRequest(COUNTIES_ENDPOINT, Map.of(AUTHORIZATION, BEARER + forbiddenToken));
        assertThat(response.statusCode()).isEqualTo(FORBIDDEN.value());
    }
}
