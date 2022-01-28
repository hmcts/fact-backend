package uk.gov.hmcts.dts.fact.controllers.admin;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.dts.fact.config.security.Role;
import uk.gov.hmcts.dts.fact.model.admin.AreaOfLaw;
import uk.gov.hmcts.dts.fact.services.admin.AdminCourtSpoeAreasOfLawService;

import java.util.List;

import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.dts.fact.services.admin.AdminRole.FACT_SUPER_ADMIN;

@RestController
@RequestMapping(
    path = "/admin/courts",
    produces = {MediaType.APPLICATION_JSON_VALUE}
)
public class AdminCourtSpoeAreasOfLawController {
    private final AdminCourtSpoeAreasOfLawService adminCourtAreasOfLawSpoeService;

    @Autowired
    public AdminCourtSpoeAreasOfLawController(AdminCourtSpoeAreasOfLawService adminService) {
        this.adminCourtAreasOfLawSpoeService = adminService;
    }

    @GetMapping(path = "/SpoeAreasOfLaw")
    @ApiOperation("Return all spoe areas of law")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successful", response = AreaOfLaw.class, responseContainer = "List"),
        @ApiResponse(code = 401, message = "Unauthorized"),
        @ApiResponse(code = 403, message = "Forbidden")
    })
    @Role({FACT_SUPER_ADMIN})
    public ResponseEntity<List<AreaOfLaw>> getAllAreasOfLaw() {
        return ok(adminCourtAreasOfLawSpoeService.getAllSpoeAreasOfLaw());
    }

    @GetMapping(path = "/{slug}/SpoeAreasOfLaw")
    @ApiOperation("Find the spoe areas of law for a court")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successful", response = AreaOfLaw.class, responseContainer = "List"),
        @ApiResponse(code = 401, message = "Unauthorized"),
        @ApiResponse(code = 403, message = "Forbidden"),
        @ApiResponse(code = 404, message = "Court not Found")
    })
    @Role({FACT_SUPER_ADMIN})
    public ResponseEntity<List<AreaOfLaw>> getCourtAreasOfLaw(@PathVariable String slug) {
        return ok(adminCourtAreasOfLawSpoeService.getCourtSpoeAreasOfLawBySlug(slug));
    }

    @PutMapping(path = "/{slug}/SpoeAreasOfLaw")
    @ApiOperation("Update the spoe areas of law for a court")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successful", response = AreaOfLaw.class, responseContainer = "List"),
        @ApiResponse(code = 401, message = "Unauthorized"),
        @ApiResponse(code = 403, message = "Forbidden"),
        @ApiResponse(code = 404, message = "Court not Found")
    })
    @Role({FACT_SUPER_ADMIN})
    public ResponseEntity<List<AreaOfLaw>> updateCourtAreasOfLaw(@PathVariable String slug,
                                                                 @RequestBody List<AreaOfLaw> areasOfLaw) {
        return ok(adminCourtAreasOfLawSpoeService.updateSpoeAreasOfLawForCourt(slug, areasOfLaw));
    }
}
