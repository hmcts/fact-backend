package uk.gov.hmcts.dts.fact.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.hmcts.dts.fact.entity.CourtContact;

import java.util.List;

public interface CourtContactRepository extends JpaRepository<CourtContact, Integer> {

    List<CourtContact> getCourtContactByContactId(Integer contactTypeId);
}
