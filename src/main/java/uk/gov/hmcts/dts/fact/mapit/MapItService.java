package uk.gov.hmcts.dts.fact.mapit;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.dts.fact.exception.MapitUsageException;

import java.io.IOException;
import java.util.Arrays;

@Component
public class MapItService {
    private static final String QUOTA = "quota";
    private static final String LIMIT = "limit";
    private static final String CURRENT = "current";

    private final MapitValidator mapitValidater;

    @Autowired
    public MapItService(MapitValidator mapitValidater) {
        this.mapitValidater = mapitValidater;
    }

    @Value("${mapit.url}")
    private String mapitUrl;

    @Value("${mapit.endpoint.quota}")
    private String mapitQuotaPath;

    @Value("${mapit.key}")
    private String mapitKey;

    public boolean isUp() throws IOException {
        final String fullPath = mapitUrl + mapitQuotaPath + "?api_key=" + mapitKey;
        final ResponseEntity<JsonNode> response = new RestTemplate().getForEntity(fullPath, JsonNode.class);

        final JsonNode responseBody = response.getBody();
        if (responseBody != null) {
            final JsonNode quota = responseBody.get(QUOTA);
            if (quota != null) {
                validateQuotaLimit(quota);
            }
        }
        return response.getStatusCode().equals(HttpStatus.OK);
    }

    private void validateQuotaLimit(final JsonNode quota) {
        final int limit = quota.get(LIMIT).asInt();
        // Mapit quota limit will be zero if a valid Mapit key has been configured. If no key is supplied and the
        // limit hasn't been reached, throw an exception so the Mapit service can be marked as 'down' for health check
        if (limit != 0 && limit <= quota.get(CURRENT).asInt()) {
            throw new MapitUsageException();
        }
    }

    /**
     * Accepts an array of strings and checks for each if mapit data exists.
     * @param postcodes An array of strings which are postcodes
     * @return An array of strings which indicate which postcodes have failed to return
     * geographical information
     */
    public String[] validatePostcodes(String[] postcodes) {
        return Arrays.stream(postcodes)
            .filter(postcode -> !mapitValidater.postcodeDataExists(postcode))
            .toArray(String[]::new);
    }
}
