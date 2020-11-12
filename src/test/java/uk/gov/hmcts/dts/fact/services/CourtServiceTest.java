package uk.gov.hmcts.dts.fact.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.dts.fact.entity.AreaOfLaw;
import uk.gov.hmcts.dts.fact.entity.Court;
import uk.gov.hmcts.dts.fact.exception.NotFoundException;
import uk.gov.hmcts.dts.fact.mapit.Coordinates;
import uk.gov.hmcts.dts.fact.model.CourtReference;
import uk.gov.hmcts.dts.fact.model.CourtReferenceWithDistance;
import uk.gov.hmcts.dts.fact.model.deprecated.CourtWithDistance;
import uk.gov.hmcts.dts.fact.model.deprecated.OldCourt;
import uk.gov.hmcts.dts.fact.repositories.CourtRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = CourtService.class)
class CourtServiceTest {

    private static final String SOME_SLUG = "some-slug";
    private static final String AREA_OF_LAW_NAME = "AreaOfLawName";

    @Autowired
    private CourtService courtService;

    @MockBean
    private CourtRepository courtRepository;

    @MockBean
    private MapitService mapitService;

    @Test
    void shouldThrowSlugNotFoundException() {
        when(courtRepository.findBySlug(any())).thenReturn(empty());
        assertThrows(NotFoundException.class, () -> courtService.getCourtBySlug("some-slug"));
    }

    @Test
    void shouldReturnOldCourtObject() {
        final Court court = mock(Court.class);
        when(courtRepository.findBySlug(SOME_SLUG)).thenReturn(Optional.of(court));
        assertThat(courtService.getCourtBySlugDeprecated(SOME_SLUG)).isInstanceOf(OldCourt.class);
    }

    @Test
    void shouldReturnCourtObject() {
        final Court court = mock(Court.class);
        when(courtRepository.findBySlug(SOME_SLUG)).thenReturn(Optional.of(court));
        assertThat(courtService.getCourtBySlug(SOME_SLUG)).isInstanceOf(uk.gov.hmcts.dts.fact.model.Court.class);
    }

    @Test
    void shouldReturnListOfCourtReferenceObject() {
        final String query = "London";
        final Court court = mock(Court.class);

        when(courtRepository.queryBy(query)).thenReturn(singletonList(court));
        final List<CourtReference> results = courtService.getCourtByNameOrAddressOrPostcodeOrTown(query);
        assertThat(results.size()).isEqualTo(1);
        assertThat(results.get(0)).isInstanceOf(CourtReference.class);
    }

    @Test
    void shouldReturnListOfCourts() {
        final String query = "London";
        final Court court = mock(Court.class);

        when(courtRepository.queryBy(query)).thenReturn(singletonList(court));
        final List<CourtWithDistance> results = courtService.getCourtsByNameOrAddressOrPostcodeOrTown(query);
        assertThat(results.get(0)).isInstanceOf(CourtWithDistance.class);
        assertThat(results.size()).isEqualTo(1);
    }

    @Test
    void shouldReturnListOfTenCourts() {

        final Coordinates coordinates = mock(Coordinates.class);
        when(mapitService.getCoordinates(any())).thenReturn(Optional.of(coordinates));

        final List<uk.gov.hmcts.dts.fact.entity.CourtWithDistance> courts = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            courts.add(mock(uk.gov.hmcts.dts.fact.entity.CourtWithDistance.class));
        }

        when(courtRepository.findNearest(anyDouble(), anyDouble())).thenReturn(courts);

        final List<CourtWithDistance> results = courtService.getNearestCourtsByPostcode("OX1 1RZ");
        assertThat(results.size()).isEqualTo(10);
    }

    @Test
    void shouldReturnEmptyListOfCourtsIfNoCoordinates() {

        when(mapitService.getCoordinates(any())).thenReturn(empty());

        final List<CourtWithDistance> results = courtService.getNearestCourtsByPostcode("JE3 4BA");
        assertThat(results.isEmpty()).isTrue();
        verifyNoInteractions(courtRepository);
    }

    @Test
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    void shouldFilterSearchByAreaOfLaw() {

        final Coordinates coordinates = mock(Coordinates.class);
        when(mapitService.getCoordinates(any())).thenReturn(Optional.of(coordinates));

        final List<uk.gov.hmcts.dts.fact.entity.CourtWithDistance> courts = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            final uk.gov.hmcts.dts.fact.entity.CourtWithDistance mock = mock(uk.gov.hmcts.dts.fact.entity.CourtWithDistance.class);
            final AreaOfLaw areaOfLaw = new AreaOfLaw();
            if (i % 4 == 0) {
                areaOfLaw.setName("AreaOfLawName");
            }
            final List<AreaOfLaw> areasOfLaw = singletonList(areaOfLaw);
            when(mock.getAreasOfLaw()).thenReturn(areasOfLaw);
            courts.add(mock);
        }

        when(courtRepository.findNearest(anyDouble(), anyDouble())).thenReturn(courts);

        final List<CourtWithDistance> results = courtService.getNearestCourtsByPostcodeAndAreaOfLaw(
            "OX2 1RZ",
            AREA_OF_LAW_NAME
        );
        assertThat(results.size()).isEqualTo(5);
    }

    @Test
    void shouldReturnEmptyListForFilterSearchByAreaOfLawIfNoCoordinates() {

        when(mapitService.getCoordinates(any())).thenReturn(empty());

        final List<CourtWithDistance> results = courtService.getNearestCourtsByPostcodeAndAreaOfLaw(
            "JE2 4BA",
            AREA_OF_LAW_NAME
        );

        assertThat(results.isEmpty()).isTrue();
        verifyNoInteractions(courtRepository);
    }

    @Test
    void shouldFilterSearchByAreaOfLawWithPostcode() {
        final Coordinates coordinates = mock(Coordinates.class);
        when(mapitService.getCoordinates(any())).thenReturn(Optional.of(coordinates));

        final List<uk.gov.hmcts.dts.fact.entity.CourtWithDistance> courts = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            final uk.gov.hmcts.dts.fact.entity.CourtWithDistance mock = mock(uk.gov.hmcts.dts.fact.entity.CourtWithDistance.class);
            final AreaOfLaw areaOfLaw = new AreaOfLaw();
            if (i % 4 == 0) {
                areaOfLaw.setName("AreaOfLawName");
            }
            final List<AreaOfLaw> areasOfLaw = singletonList(areaOfLaw);
            when(mock.getAreasOfLaw()).thenReturn(areasOfLaw);
            courts.add(mock);
        }

        when(courtRepository.findNearest(anyDouble(), anyDouble())).thenReturn(courts);

        final List<CourtReferenceWithDistance> results = courtService.getNearestCourtsByPostcodeAndAreaOfLawSearch(
            "OX2 1RZ",
            AREA_OF_LAW_NAME
        );
        assertThat(results.size()).isEqualTo(5);
        assertThat(results.get(0)).isInstanceOf(CourtReferenceWithDistance.class);
    }

    @Test
    void shouldReturnEmptyListForFilterSearchByAreaOfLawWithPostcodeIfNoCoordinates() {
        when(mapitService.getCoordinates(any())).thenReturn(empty());

        final List<CourtReferenceWithDistance> results = courtService.getNearestCourtsByPostcodeAndAreaOfLawSearch(
            "JE2 4BA",
            AREA_OF_LAW_NAME
        );

        assertThat(results.isEmpty()).isTrue();
        verifyNoInteractions(courtRepository);
    }
}
