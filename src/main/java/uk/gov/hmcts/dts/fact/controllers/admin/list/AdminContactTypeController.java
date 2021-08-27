package uk.gov.hmcts.dts.fact.controllers.admin.list;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.dts.fact.config.security.Role;
import uk.gov.hmcts.dts.fact.model.admin.ContactType;
import uk.gov.hmcts.dts.fact.services.admin.list.AdminContactTypeService;

import java.net.URI;
import java.util.List;

import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.dts.fact.services.admin.AdminRole.FACT_ADMIN;
import static uk.gov.hmcts.dts.fact.services.admin.AdminRole.FACT_SUPER_ADMIN;

@RestController
@RequestMapping(
    path = "/admin/contactTypes",
    produces = {MediaType.APPLICATION_JSON_VALUE}
)
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class AdminContactTypeController {

    private final AdminContactTypeService adminContactTypeService;

    @Autowired
    public AdminContactTypeController(AdminContactTypeService adminContactTypeService) {
        this.adminContactTypeService = adminContactTypeService;
    }

    @GetMapping()
    @ApiOperation("Return all contact types")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successful", response = ContactType.class, responseContainer = "List"),
        @ApiResponse(code = 401, message = "Unauthorized"),
        @ApiResponse(code = 403, message = "Forbidden")
    })
    @Role({FACT_ADMIN, FACT_SUPER_ADMIN})
    public ResponseEntity<List<ContactType>> getAllContactTypes() {
        return ok(adminContactTypeService.getAllContactTypes());
    }


    @GetMapping(path = "/{id}")
    @ApiOperation("Get contact type")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successful", response = ContactType.class),
        @ApiResponse(code = 401, message = "Unauthorized"),
        @ApiResponse(code = 403, message = "Forbidden"),
        @ApiResponse(code = 404, message = "Contact type not found")
    })
    @Role({FACT_ADMIN, FACT_SUPER_ADMIN})
    public ResponseEntity<ContactType> getContactType(@PathVariable Integer id) {
        return ok(adminContactTypeService.getContactType(id));
    }

    @PostMapping()
    @ApiOperation("Create contact type")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Created", response = ContactType.class),
        @ApiResponse(code = 401, message = "Unauthorized"),
        @ApiResponse(code = 403, message = "Forbidden"),
        @ApiResponse(code = 409, message = "Contact type already exists")
    })
    @Role({FACT_SUPER_ADMIN})
    public ResponseEntity<ContactType> createContactType(@RequestBody ContactType contactType) {
        return created(URI.create(StringUtils.EMPTY)).body(adminContactTypeService.createContactType(contactType));
    }

    @PutMapping()
    @ApiOperation("Update contact type")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successful", response = ContactType.class),
        @ApiResponse(code = 401, message = "Unauthorized"),
        @ApiResponse(code = 403, message = "Forbidden"),
        @ApiResponse(code = 404, message = "Contact type not found")
    })
    @Role({FACT_SUPER_ADMIN})
    public ResponseEntity<ContactType> updateContactType(@RequestBody ContactType contactType) {
        return ok(adminContactTypeService.updateContactType(contactType));
    }

    @DeleteMapping("/{contactTypeId}")
    @ApiOperation("Delete contact type")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successful", response = ContactType.class),
        @ApiResponse(code = 401, message = "Unauthorized"),
        @ApiResponse(code = 403, message = "Forbidden"),
        @ApiResponse(code = 404, message = "Contact type not found"),
        @ApiResponse(code = 409, message = "Contact type in use")
    })
    @Role({FACT_SUPER_ADMIN})
    public ResponseEntity deleteContactType(@PathVariable Integer contactTypeId) {
        adminContactTypeService.deleteContactType(contactTypeId);
        return ok().body(contactTypeId);
    }

}


