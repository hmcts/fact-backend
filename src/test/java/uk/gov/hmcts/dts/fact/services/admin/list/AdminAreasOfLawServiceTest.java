package uk.gov.hmcts.dts.fact.services.admin.list;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.dts.fact.entity.AreaOfLaw;
import uk.gov.hmcts.dts.fact.exception.DuplicatedListItemException;
import uk.gov.hmcts.dts.fact.exception.NotFoundException;
import uk.gov.hmcts.dts.fact.repositories.AreasOfLawRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@ContextConfiguration(classes = AdminAreasOfLawService.class)
public class AdminAreasOfLawServiceTest {

    @Autowired
    private AdminAreasOfLawService areasOfLawService;

    @MockBean
    private AreasOfLawRepository areasOfLawRepository;


    @Test
    void shouldReturnAllAreasOfLaw() {
        final List<AreaOfLaw> mockAreasOfLaw = getTestAreaOfLawEntities();
        when(areasOfLawRepository.findAll()).thenReturn(mockAreasOfLaw);

        final List<uk.gov.hmcts.dts.fact.model.admin.AreaOfLaw> expectedResult = mockAreasOfLaw
            .stream()
            .map(uk.gov.hmcts.dts.fact.model.admin.AreaOfLaw::new)
            .collect(Collectors.toList());

        assertThat(areasOfLawService.getAllAreasOfLaw()).isEqualTo(expectedResult);
    }

    @Test
    void shouldReturnAnAreaOfLawForGivenId() {
        final AreaOfLaw mockAreaOfLaw = getTestAreaOfLawEntities().get(0);
        when(areasOfLawRepository.getOne(100)).thenReturn(mockAreaOfLaw);

        final uk.gov.hmcts.dts.fact.model.admin.AreaOfLaw expectedResult =
            new uk.gov.hmcts.dts.fact.model.admin.AreaOfLaw(mockAreaOfLaw);

        assertThat(areasOfLawService.getAreaOfLaw(100)).isEqualTo(expectedResult);
    }

    @Test
    void getAreaOfLawShouldThrowNotFoundExceptionIfIdDoesNotExist() {
        when(areasOfLawRepository.getOne(400)).thenThrow(javax.persistence.EntityNotFoundException.class);
        assertThatThrownBy(() -> areasOfLawService
            .getAreaOfLaw(400))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldUpdateAreaOfLaw() {
        final uk.gov.hmcts.dts.fact.model.admin.AreaOfLaw areaOfLaw =
            new uk.gov.hmcts.dts.fact.model.admin.AreaOfLaw(getTestAreaOfLawEntities().get(0));
        areaOfLaw.setExternalLink("https://something.else.link");
        areaOfLaw.setExternalLinkDescription("A different description");
        areaOfLaw.setExternalLinkDescriptionCy("A different description in Welsh");

        when(areasOfLawRepository.findById(areaOfLaw.getId()))
            .thenReturn(Optional.of(getTestAreaOfLawEntities().get(0)));
        when(areasOfLawRepository.save(any(AreaOfLaw.class)))
            .thenAnswer((Answer<AreaOfLaw>) invocation -> invocation.getArgument(0));

        assertThat(areasOfLawService.updateAreaOfLaw(areaOfLaw)).isEqualTo(areaOfLaw);
    }

    @Test
    void updateShouldThrowNotFoundExceptionWhenAreaOfLawDoesNotExist() {
        final AreaOfLaw testAreaOfLaw = getTestAreaOfLawEntities().get(0);
        when(areasOfLawRepository.findById(testAreaOfLaw.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> areasOfLawService
            .updateAreaOfLaw(new uk.gov.hmcts.dts.fact.model.admin.AreaOfLaw(testAreaOfLaw)))
            .isInstanceOf(NotFoundException.class);

        verify(areasOfLawRepository, never()).save(any());
    }

    @Test
    void shouldCreateAreaOfLaw() {
        final uk.gov.hmcts.dts.fact.model.admin.AreaOfLaw areaOfLaw =
            new uk.gov.hmcts.dts.fact.model.admin.AreaOfLaw();
        areaOfLaw.setName("New Area of Law");
        areaOfLaw.setExternalLink("https://newarea.of.law");
        areaOfLaw.setExternalLinkDescription("This is a new area of law");
        areaOfLaw.setExternalLinkDescriptionCy("This is a new area of law - welsh");
        areaOfLaw.setDisplayName("This is new area of law display name");
        areaOfLaw.setDisplayNameCy("This is new area of law display name - welsh");
        areaOfLaw.setDisplayExternalLink("https://external.newarea.of.law");

        when(areasOfLawRepository.save(any(AreaOfLaw.class))).thenAnswer((Answer<AreaOfLaw>)invocation -> invocation.getArgument(0));

        assertThat(areasOfLawService.createAreaOfLaw(areaOfLaw)).isEqualTo(areaOfLaw);
    }

    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    @Test
    void createShouldThrowDuplicatedListItemExceptionIfAreaOfLawAlreadyExists() {
        final List<AreaOfLaw> mockAreasOfLaw = getTestAreaOfLawEntities();
        when(areasOfLawRepository.findAll()).thenReturn(mockAreasOfLaw);

        final uk.gov.hmcts.dts.fact.model.admin.AreaOfLaw areaOfLaw =
            new uk.gov.hmcts.dts.fact.model.admin.AreaOfLaw(getTestAreaOfLawEntities().get(0));

        assertThatThrownBy(() -> areasOfLawService
            .createAreaOfLaw(areaOfLaw))
            .isInstanceOf(DuplicatedListItemException.class);
    }

    private List<AreaOfLaw> getTestAreaOfLawEntities() {
        return Arrays.asList(
            new AreaOfLaw(
                100,
                "Divorce",
                "https://divorce.test",
                "Information about getting a divorce",
                "Gwybodaeth ynglŷn â gwneud cais am ysgariad",
                "Divorce - alt",
                "Ysgariad alt",
                "Divorce - display",
                "Ysgariad display",
                "https://divorce.external.text"),
            new AreaOfLaw(
                200,
                "Tax",
                "https://tax.test",
                "Information about tax tribunals",
                "Gwybodaeth am tribiwnlysoedd treth",
                "Tax - alt",
                "Treth alt",
                "Tax - display",
                "Treth display",
                "https://tax.external.text"),
            new AreaOfLaw(
                300,
                "Employment",
                "https://employment.test",
                "Information about the Employment Tribunal",
                "Gwybodaeth ynglŷn â'r tribiwnlys cyflogaeth",
                "Employment - alt",
                "Honiadau yn erbyn cyflogwyr alt",
                "Employment - display",
                "Honiadau yn erbyn cyflogwyr display",
                "https://employment.external.text")
        );
    }
}
