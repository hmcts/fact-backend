package uk.gov.hmcts.dts.fact.services.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.dts.fact.entity.Court;
import uk.gov.hmcts.dts.fact.entity.CourtAreaOfLaw;
import uk.gov.hmcts.dts.fact.exception.NotFoundException;
import uk.gov.hmcts.dts.fact.model.admin.AreaOfLaw;
import uk.gov.hmcts.dts.fact.repositories.CourtAreaOfLawRepository;
import uk.gov.hmcts.dts.fact.repositories.CourtAreaOfLawSpoeRepository;
import uk.gov.hmcts.dts.fact.repositories.CourtRepository;

import java.util.*;

import static java.util.stream.Collectors.toList;

@Service
public class AdminCourtAreasOfLawService {

    private final CourtRepository courtRepository;
    private final CourtAreaOfLawRepository courtAreaOfLawRepository;
    private final CourtAreaOfLawSpoeRepository courtAreaOfLawSpoeRepository;

    @Autowired
    public AdminCourtAreasOfLawService(final CourtRepository courtRepository, final CourtAreaOfLawRepository courtAreaOfLawRepository,
                                       final CourtAreaOfLawSpoeRepository courtAreaOfLawSpoeRepository) {
        this.courtRepository = courtRepository;
        this.courtAreaOfLawRepository = courtAreaOfLawRepository;
        this.courtAreaOfLawSpoeRepository = courtAreaOfLawSpoeRepository;
    }

    public List<AreaOfLaw> getCourtAreasOfLawBySlug(final String slug) {

        Court court = courtRepository.findBySlug(slug).orElseThrow(() -> new NotFoundException(slug));
        return courtAreaOfLawRepository.getCourtAreaOfLawByCourtId(court.getId())
            .stream()
            .map(aol -> new AreaOfLaw(aol.getAreaOfLaw(),
                                      courtAreaOfLawSpoeRepository.getAllByCourtIdAndAreaOfLawId(court.getId(), aol.getAreaOfLaw().getId())
                                          .stream()
                                          .findAny()
                                          .isPresent()))
            .collect(toList());
    }

    @Transactional()
    public List<AreaOfLaw> updateAreasOfLawForCourt(final String slug, final List<AreaOfLaw> areasOfLaw) {
        final Court courtEntity = courtRepository.findBySlug(slug)
            .orElseThrow(() -> new NotFoundException(slug));

        List<uk.gov.hmcts.dts.fact.entity.AreaOfLaw> newAreasOfLawList = getNewAreasOfLaw(areasOfLaw);
        List<CourtAreaOfLaw> newCourtAreasOfLawList = getNewCourtAreasOfLaw(courtEntity, newAreasOfLawList);

        courtAreaOfLawRepository.deleteCourtAreaOfLawByCourtId(courtEntity.getId());

        return courtAreaOfLawRepository
            .saveAll(newCourtAreasOfLawList)
            .stream()
            .map(aol -> new AreaOfLaw(aol.getAreaOfLaw(),
                                      courtAreaOfLawSpoeRepository.getAllByCourtIdAndAreaOfLawId(courtEntity.getId(), aol.getAreaOfLaw().getId())
                                          .stream()
                                          .findAny()
                                          .isPresent()))
            .collect(toList());
    }

    private List<uk.gov.hmcts.dts.fact.entity.AreaOfLaw> getNewAreasOfLaw(final List<AreaOfLaw> areasOfLaw) {
        return areasOfLaw.stream()
            .map(e -> new uk.gov.hmcts.dts.fact.entity.AreaOfLaw(e.getId(), e.getName())).collect(toList());
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private List<CourtAreaOfLaw> getNewCourtAreasOfLaw(final Court court,
                                                       final List<uk.gov.hmcts.dts.fact.entity.AreaOfLaw> areasOfLaw) {
        final List<CourtAreaOfLaw> newCourtAreasOfLawList = new ArrayList<>();
        for (uk.gov.hmcts.dts.fact.entity.AreaOfLaw areaOfLaw : areasOfLaw) {
            newCourtAreasOfLawList.add(new CourtAreaOfLaw(areaOfLaw, court));
        }
        return newCourtAreasOfLawList;
    }
}






