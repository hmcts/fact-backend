package uk.gov.hmcts.dts.fact.controllers.admin.list;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.dts.fact.exception.DuplicatedListItemException;
import uk.gov.hmcts.dts.fact.exception.NotFoundException;
import uk.gov.hmcts.dts.fact.model.admin.AreaOfLaw;
import uk.gov.hmcts.dts.fact.services.admin.list.AdminAreasOfLawService;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminAreasOfLawController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AdminAreasOfLawControllerTest {
    private static final String BASE_PATH = "/admin/areasOfLaw";

    @Autowired
    private transient MockMvc mockMvc;

    @MockBean
    private AdminAreasOfLawService adminAreasOfLawService;

    @Test
    void shouldReturnAllAreasOfLaw() throws Exception {

        final List<AreaOfLaw> mockAllAreasOfLaw = getAreasOfLaw();
        when(adminAreasOfLawService.getAllAreasOfLaw()).thenReturn(mockAllAreasOfLaw);

        final String allAreasOfLawJson = new ObjectMapper().writeValueAsString(mockAllAreasOfLaw);

        mockMvc.perform(get(BASE_PATH).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(allAreasOfLawJson));
    }

    @Test
    void shouldReturnAnAreaOfLaw() throws Exception {
        final AreaOfLaw areaOfLaw = getAreasOfLaw().get(0);
        when(adminAreasOfLawService.getAreaOfLaw(100)).thenReturn(areaOfLaw);

        final String areaOfLawJson = new ObjectMapper().writeValueAsString(areaOfLaw);

        mockMvc.perform(get(BASE_PATH + "/100").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(areaOfLawJson));
    }

    @Test
    void shouldReturnNotFoundWhenAreaOfLawIdNotFound() throws Exception {
        when(adminAreasOfLawService.getAreaOfLaw(400)).thenThrow(NotFoundException.class);

        mockMvc.perform(get(BASE_PATH + "/400").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    void updateAreaOfLawShouldUpdateAndReturnUpdatedAreaOfLaw() throws Exception {
        final AreaOfLaw areaOfLaw = getAreasOfLaw().get(0);
        areaOfLaw.setExternalLink("https://a.different.url");
        when(adminAreasOfLawService.updateAreaOfLaw(areaOfLaw)).thenReturn(areaOfLaw);

        final String areaOfLawJson = new ObjectMapper().writeValueAsString(areaOfLaw);

        mockMvc.perform(put(BASE_PATH)
                            .content(areaOfLawJson)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(areaOfLawJson));
    }

    @Test
    void updateAreaOfLawShouldReturnNotFoundForUnknownAreaOfLaw() throws Exception {
        final AreaOfLaw areaOfLaw = getAreasOfLaw().get(0);
        when(adminAreasOfLawService.updateAreaOfLaw(areaOfLaw)).thenThrow(NotFoundException.class);

        final String areaOfLawJson = new ObjectMapper().writeValueAsString(areaOfLaw);

        mockMvc.perform(put(BASE_PATH)
                            .content(areaOfLawJson)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    void createAreaOfLawShouldCreateAndReturnNewAreaOfLaw() throws Exception {
        final AreaOfLaw areaOfLaw = new AreaOfLaw(
            null,
            "Test Area Of Law",
            false,
            "https://new.test.areaoflaw",
            "Information about the new area of law",
            "Information about the new area of law - welsh",
            "alternativeName",
            "alternativeNameCy",
            "displayName",
            "displayNameCy",
            "https://new.test.external");
        when(adminAreasOfLawService.createAreaOfLaw(areaOfLaw)).thenReturn(areaOfLaw);

        final String areaOfLawJson = new ObjectMapper().writeValueAsString(areaOfLaw);

        mockMvc.perform(post(BASE_PATH)
                            .content(areaOfLawJson)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(areaOfLawJson));
    }

    @Test
    void createAreaOfLawShouldReturnConflictIfNameAlreadyExists() throws Exception {
        final AreaOfLaw areaOfLaw = getAreasOfLaw().get(0);
        when(adminAreasOfLawService.createAreaOfLaw(areaOfLaw)).thenThrow(DuplicatedListItemException.class);

        final String areaOfLawJson = new ObjectMapper().writeValueAsString(areaOfLaw);

        mockMvc.perform(post(BASE_PATH)
                            .content(areaOfLawJson)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isConflict());
    }

    private List<AreaOfLaw> getAreasOfLaw() {
        return Arrays.asList(
            new AreaOfLaw(
                100,
                "Divorce",
                false,
                "https://divorce.test",
                "Information about getting a divorce",
                "Gwybodaeth ynglŷn â gwneud cais am ysgariad",
                "Divorce - alternative",
                "Ysgariad",
                "Divorce - display",
                "Ysgariad",
                "https://divorce.external.text"),
            new AreaOfLaw(
                200,
                "Tax",
                false,
                "https://tax.test",
                "Information about tax tribunals",
                "Gwybodaeth am tribiwnlysoedd treth",
                "Tax - alternative",
                "Treth",
                "Tax - display",
                "Treth",
                "https://tax.external.text"),
            new AreaOfLaw(
                300,
                "Employment",
                false,
                "https://employment.test",
                "Information about the Employment Tribunal",
                "Gwybodaeth ynglŷn â'r tribiwnlys cyflogaeth",
                "Employment - alternative",
                "Honiadau yn erbyn cyflogwyr",
                "Employment - display",
                "Honiadau yn erbyn cyflogwyr",
                "https://employment.external.text")
        );
    }
}
