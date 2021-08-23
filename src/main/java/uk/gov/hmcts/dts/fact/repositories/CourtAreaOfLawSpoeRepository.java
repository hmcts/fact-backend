package uk.gov.hmcts.dts.fact.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.hmcts.dts.fact.entity.CourtAreaOfLawSpoe;

import java.util.List;

public interface CourtAreaOfLawSpoeRepository extends JpaRepository<CourtAreaOfLawSpoe, Integer> {
    List<CourtAreaOfLawSpoe> getAllByCourtIdAndAreaOfLawId(Integer courtId, Integer areaOfLawId);
}
