package uk.gov.hmcts.dts.fact.services.search;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.dts.fact.entity.ServiceArea;
import uk.gov.hmcts.dts.fact.mapit.MapitData;

import java.util.Optional;

@Component
public class FamilySearchFactory {

    private static final String LOCAL_AUTHORITY_CATCHMENT_METHOD = "local-authority";

    private final FamilyRegionalSearch familyRegionalSearch;
    private final FamilyNonRegionalSearch familyNonRegionalSearch;
    private final DefaultSearch defaultSearch;

    public FamilySearchFactory(final FamilyRegionalSearch familyRegionalSearch,
                               final FamilyNonRegionalSearch familyNonRegionalSearch,
                               final DefaultSearch defaultSearch) {
        this.familyRegionalSearch = familyRegionalSearch;
        this.familyNonRegionalSearch = familyNonRegionalSearch;
        this.defaultSearch = defaultSearch;
    }

    public Search getSearchFor(final ServiceArea serviceArea, final MapitData mapitData) {

        final Optional<String> localAuthorityOptional = mapitData.getLocalAuthority();

        if (LOCAL_AUTHORITY_CATCHMENT_METHOD.equals(serviceArea.getCatchmentMethod())
            && localAuthorityOptional.isPresent()) {

            if (serviceArea.isRegional()) {
                return familyRegionalSearch;
            } else {
                return familyNonRegionalSearch;
            }
        }

        return defaultSearch;
    }
}
