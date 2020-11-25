package uk.gov.hmcts.dts.fact.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.dts.fact.config.security.RolesProvider;
import uk.gov.hmcts.dts.fact.entity.Court;
import uk.gov.hmcts.dts.fact.exception.NotFoundException;
import uk.gov.hmcts.dts.fact.model.CourtReference;
import uk.gov.hmcts.dts.fact.model.admin.CourtGeneral;
import uk.gov.hmcts.dts.fact.model.admin.CourtInfo;
import uk.gov.hmcts.dts.fact.repositories.CourtRepository;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class AdminService {

    private final CourtRepository courtRepository;

    private final RolesProvider rolesProvider;

    @Autowired
    public AdminService(final CourtRepository courtRepository, final RolesProvider rolesProvider) {
        this.courtRepository = courtRepository;
        this.rolesProvider = rolesProvider;
    }

    public List<CourtReference> getAllCourts() {
        return courtRepository
            .findAll()
            .stream()
            .map(CourtReference::new)
            .collect(toList());
    }

    public CourtGeneral getCourtGeneralBySlug(String slug) {
        return courtRepository
            .findBySlug(slug)
            .map(CourtGeneral::new)
            .orElseThrow(() -> new NotFoundException(slug));
    }

    public Court getCourtEntityBySlug(String slug) {
        return courtRepository.findBySlug(slug).orElseThrow(() -> new NotFoundException(slug));
    }

    public CourtGeneral saveGeneral(String slug, CourtGeneral courtGeneral) {
        Court court = getCourtEntityBySlug(slug);
        court.setAlert(courtGeneral.getAlert());
        court.setAlertCy(courtGeneral.getAlertCy());
        if (rolesProvider.getRoles().contains("fact-super-admin")) {
            court.setInfo(courtGeneral.getInfo());
            court.setInfoCy(courtGeneral.getInfoCy());
        }
        Court updatedCourt = courtRepository.save(court);
        return new CourtGeneral(updatedCourt);
    }

    public void updateAllCourts(CourtInfo info) {
        List<Court> courts = courtRepository.findAll();

        for (Court c : courts) {
            c.setInfo(info.getInfo());
            c.setInfoCy(info.getInfoCy());
        }

        courtRepository.saveAll(courts);
    }
}
