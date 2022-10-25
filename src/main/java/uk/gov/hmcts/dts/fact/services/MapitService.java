package uk.gov.hmcts.dts.fact.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.dts.fact.mapit.MapitArea;
import uk.gov.hmcts.dts.fact.mapit.MapitClient;
import uk.gov.hmcts.dts.fact.mapit.MapitData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Service
public class MapitService {

    private final Logger logger;
    private final MapitClient mapitClient;

    @Autowired
    public MapitService(final Logger logger, final MapitClient mapitClient) {
        this.logger = logger;
        this.mapitClient = mapitClient;
    }

    public Optional<MapitData> getMapitData(final String postcode) {

        if (!postcode.isBlank()) {
            try {
                final MapitData mapitData = mapitClient.getMapitData(postcode);
                System.out.println(mapitData);

                @SuppressWarnings("unchecked")
                List<MapitArea> areaList = new ObjectMapper().treeToValue(mapitData.getAreas(), List.class) ;

                System.out.println("AREALIST " + areaList);


                final Map<String, MapitArea> mapitRegions = mapitClient.getMapitDataForRegions("ER,WAE");
                System.out.println(mapitRegions);

                final String match = mapitData.getMatchingRegionNumber(mapitRegions);

                mapitData.getMatchingRegionNameFromAreas(match);
                System.out.println(mapitData.getMatchingRegionNameFromAreas(match));

                if (mapitData.hasLatAndLonValues()) {
                    return Optional.of(mapitData);
                }
            } catch (final FeignException ex) {
                logger.warn("HTTP Status: {} Message: {}", ex.status(), ex.getMessage(), ex);
            } catch (final JsonProcessingException ex) {
                logger.warn("Error when transforming areas JsonNode to List Message: {}", ex.getMessage());
            }
        }

        return Optional.empty();
    }

    public Optional<MapitData> getMapitDataWithPartial(final String postcode) {

        if (!StringUtils.isBlank(postcode)) {
            try {
                final MapitData mapitData = mapitClient.getMapitDataWithPartial(postcode);

                if (mapitData.hasLatAndLonValues()) {
                    return Optional.of(mapitData);
                }
            } catch (final FeignException ex) {
                logger.warn("HTTP Status: {} Message: {}", ex.status(), ex.getMessage(), ex);
            }
        }

        return Optional.empty();
    }

    public Boolean localAuthorityExists(final String localAuthorityName) {

        if (StringUtils.isNotBlank(localAuthorityName)) {
            try {
                return mapitClient.getMapitDataForLocalAuthorities(localAuthorityName, "MTD,UTA,LBO,CTY")
                    .values()
                    .stream()
                    .anyMatch(la -> la.getName().equalsIgnoreCase(localAuthorityName));
            } catch (final FeignException ex) {
                logger.warn("Mapit API call (local authority validation) failed. HTTP Status: {} Message: {}", ex.status(), ex.getMessage(), ex);
                return false;
            }
        }

        return false;
    }
}
