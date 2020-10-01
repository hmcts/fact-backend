package uk.gov.hmcts.dts.fact.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.dts.fact.exception.SlugNotFoundException;
import uk.gov.hmcts.dts.fact.model.CourtReference;
import uk.gov.hmcts.dts.fact.repositories.CourtRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourtService {

    private static final int MIN_COUNT = 2;

    @Autowired
    CourtRepository courtRepository;

    public uk.gov.hmcts.dts.fact.model.Court getCourtBySlug(String slug) {
        return courtRepository
            .findBySlug(slug)
            .map(this::mapCourt)
            .orElseThrow(() -> new SlugNotFoundException(slug));
    }

    public List<CourtReference> getCourtByNameOrAddressOrPostcodeOrTown(String query) {
        return courtRepository
            .queryBy(query, getCourtNameRegex(query))
            .stream()
            .map(court -> new CourtReference(court.getName(), court.getSlug()))
            .collect(Collectors.toList());
    }

    private uk.gov.hmcts.dts.fact.model.Court mapCourt(uk.gov.hmcts.dts.fact.entity.Court courtEntity) {
        return new uk.gov.hmcts.dts.fact.model.Court(courtEntity);
    }

    private String getCourtNameRegex(String query) {
        String[] wordSplit = query.split("-|\\s");
        if (wordSplit.length >= MIN_COUNT) {
            return "%" + query + "%";
        } else {
            return "\\b" + query + "\\b";
        }
    }
}
