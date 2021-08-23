package uk.gov.hmcts.dts.fact.repositories;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import uk.gov.hmcts.dts.fact.entity.AreaOfLaw;
import uk.gov.hmcts.dts.fact.entity.Court;
import uk.gov.hmcts.dts.fact.entity.CourtAreaOfLaw;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CourtAreaOfLawRepositoryTest {

    @Autowired
    private CourtAreaOfLawRepository courtAreaOfLawRepository;

    @Autowired
    private AreasOfLawRepository areasOfLawRepository;

    @Autowired
    private CourtRepository courtRepository;

    @Test
    void shouldDeleteByCourtId() {

        final Optional<Court> court = courtRepository.findBySlug("plymouth-combined-court");
        assertThat(court).isPresent();
        final int courtId = court.get().getId();

        AreaOfLaw areaOfLaw = new AreaOfLaw(100_001, "Area of Law");
        AreaOfLaw areaOfLaw2 = new AreaOfLaw(100_002, "Area of Law 2");
        areasOfLawRepository.save(areaOfLaw);
        areasOfLawRepository.save(areaOfLaw2);
        areasOfLawRepository.flush();

        courtAreaOfLawRepository.save(new CourtAreaOfLaw(
            areaOfLaw, court.get()));
        courtAreaOfLawRepository.save(new CourtAreaOfLaw(
            areaOfLaw2, court.get()));
        courtAreaOfLawRepository.flush();

        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(courtAreaOfLawRepository.getCourtAreaOfLawByCourtId(courtId)).isNotEmpty();
        courtAreaOfLawRepository.deleteCourtAreaOfLawByCourtId(courtId);
        softly.assertThat(courtAreaOfLawRepository.getCourtAreaOfLawByCourtId(courtId)).isEmpty();
        softly.assertAll();
    }

}
