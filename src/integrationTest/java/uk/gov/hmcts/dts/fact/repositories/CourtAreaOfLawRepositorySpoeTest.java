package uk.gov.hmcts.dts.fact.repositories;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import uk.gov.hmcts.dts.fact.entity.CourtAreaOfLawSpoe;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CourtAreaOfLawRepositorySpoeTest {

    @Autowired
    private CourtAreaOfLawSpoeRepository courtAreaOfLawSpoeRepository;

    @Test
    void shouldFindAllByAreaOfLawIdAndCourtId() {

        List<CourtAreaOfLawSpoe> courtAreaOfLawSpoeList = courtAreaOfLawSpoeRepository.findAll();

        // Use the first result as the basis for testing the repo method that has been added
        assertThat(courtAreaOfLawSpoeList).isNotEmpty();

        // Check we can use the above on our custom jpa method
        CourtAreaOfLawSpoe courtAreaOfLawSpoe = courtAreaOfLawSpoeList.get(0);
        List<CourtAreaOfLawSpoe> courtAreaOfLawSpoeListBySearch =
            courtAreaOfLawSpoeRepository.getAllByCourtIdAndAreaOfLawId(courtAreaOfLawSpoe.getCourt().getId(),
                                                                       courtAreaOfLawSpoe.getAreaOfLaw().getId());
        assertThat(courtAreaOfLawSpoeListBySearch).isNotEmpty();
    }
}
