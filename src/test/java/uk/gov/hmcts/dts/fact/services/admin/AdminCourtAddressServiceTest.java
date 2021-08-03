package uk.gov.hmcts.dts.fact.services.admin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.dts.fact.entity.AddressType;
import uk.gov.hmcts.dts.fact.entity.Court;
import uk.gov.hmcts.dts.fact.exception.NotFoundException;
import uk.gov.hmcts.dts.fact.mapit.MapitData;
import uk.gov.hmcts.dts.fact.model.admin.CourtAddress;
import uk.gov.hmcts.dts.fact.repositories.CourtAddressRepository;
import uk.gov.hmcts.dts.fact.repositories.CourtRepository;
import uk.gov.hmcts.dts.fact.services.MapitService;
import uk.gov.hmcts.dts.fact.services.admin.list.AdminAddressTypeService;
import uk.gov.hmcts.dts.fact.services.validation.ValidationService;

import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@ContextConfiguration(classes = AdminCourtAddressService.class)
@SuppressWarnings("PMD.TooManyMethods")
public class AdminCourtAddressServiceTest {
    private static final String COURT_SLUG = "court-slug";
    private static final int VISIT_US_ADDRESS_TYPE_ID = 5880;
    private static final int WRITE_TO_US_ADDRESS_TYPE_ID = 5881;
    private static final int VISIT_OR_CONTACT_US_ADDRESS_TYPE_ID = 5882;
    private static final String VISIT_US_ADDRESS_TYPE_NAME = "Visit us";
    private static final String WRITE_TO_US_ADDRESS_TYPE_NAME = "Write to us";
    private static final String VISIT_OR_CONTACT_US_ADDRESS_TYPE_NAME = "Visit or contact us";
    private static final String VISIT_US_ADDRESS_TYPE_NAME_CY = VISIT_US_ADDRESS_TYPE_NAME + " cy";
    private static final String WRITE_TO_US_ADDRESS_TYPE_NAME_CY = WRITE_TO_US_ADDRESS_TYPE_NAME + " cy";
    private static final String VISIT_OR_CONTACT_US_ADDRESS_TYPE_NAME_CY = VISIT_OR_CONTACT_US_ADDRESS_TYPE_NAME + " cy";

    private static final AddressType VISIT_US_ADDRESS_TYPE = new AddressType(VISIT_US_ADDRESS_TYPE_ID, VISIT_US_ADDRESS_TYPE_NAME, VISIT_US_ADDRESS_TYPE_NAME_CY);
    private static final AddressType WRITE_TO_US_ADDRESS_TYPE = new AddressType(WRITE_TO_US_ADDRESS_TYPE_ID, WRITE_TO_US_ADDRESS_TYPE_NAME, WRITE_TO_US_ADDRESS_TYPE_NAME_CY);
    private static final AddressType VISIT_OR_CONTACT_US_ADDRESS_TYPE = new AddressType(VISIT_OR_CONTACT_US_ADDRESS_TYPE_ID, VISIT_OR_CONTACT_US_ADDRESS_TYPE_NAME, VISIT_OR_CONTACT_US_ADDRESS_TYPE_NAME_CY);
    private static final Map<Integer, AddressType> ADDRESS_TYPE_MAP = Map.of(
        VISIT_US_ADDRESS_TYPE_ID, VISIT_US_ADDRESS_TYPE,
        WRITE_TO_US_ADDRESS_TYPE_ID, WRITE_TO_US_ADDRESS_TYPE,
        VISIT_OR_CONTACT_US_ADDRESS_TYPE_ID, VISIT_OR_CONTACT_US_ADDRESS_TYPE
    );

    private static final List<String> TEST_ADDRESS1 = singletonList("1 High Street");
    private static final List<String> TEST_ADDRESS2 = asList(
        "High Court",
        "2 Main Road"
    );

    private static final List<String> TEST_ADDRESS_CY1 = emptyList();
    private static final List<String> TEST_ADDRESS_CY2 = emptyList();

    private static final String TEST_TOWN1 = "London";
    private static final String TEST_TOWN2 = "Manchester";
    private static final String TEST_POSTCODE1 = "EC1A 1AA";
    private static final String TEST_POSTCODE2 = "M1 2AA";
    private static final String PARTIAL_POSTCODE = "EC1A";

    private static final int ADDRESS_COUNT = 2;
    private static final CourtAddress WRITE_TO_US_ADDRESS = new CourtAddress(WRITE_TO_US_ADDRESS_TYPE_ID, TEST_ADDRESS1, TEST_ADDRESS_CY1, TEST_TOWN1, null, TEST_POSTCODE1);
    private static final CourtAddress VISIT_US_ADDRESS = new CourtAddress(VISIT_US_ADDRESS_TYPE_ID, TEST_ADDRESS2, TEST_ADDRESS_CY2, TEST_TOWN2, null, TEST_POSTCODE2);
    private static final List<CourtAddress> EXPECTED_ADDRESSES = asList(WRITE_TO_US_ADDRESS, VISIT_US_ADDRESS);

    private static final Court MOCK_COURT = mock(Court.class);
    private static final List<uk.gov.hmcts.dts.fact.entity.CourtAddress> COURT_ADDRESSES_ENTITY = asList(
        new uk.gov.hmcts.dts.fact.entity.CourtAddress(MOCK_COURT, WRITE_TO_US_ADDRESS_TYPE, TEST_ADDRESS1, TEST_ADDRESS_CY1, TEST_TOWN1, null, TEST_POSTCODE1),
        new uk.gov.hmcts.dts.fact.entity.CourtAddress(MOCK_COURT, VISIT_US_ADDRESS_TYPE, TEST_ADDRESS2, TEST_ADDRESS_CY2, TEST_TOWN2, null, TEST_POSTCODE2));

    private static final Double LATITUDE = 1.0;
    private static final Double LONGITUDE = 2.0;

    private static final String NOT_FOUND = "Not found: ";

    @Autowired
    private AdminCourtAddressService adminCourtAddressService;

    @MockBean
    private CourtRepository courtRepository;

    @MockBean
    private CourtAddressRepository courtAddressRepository;

    @MockBean
    private AdminAddressTypeService adminAddressTypeService;

    @MockBean
    private AdminService adminService;

    @MockBean
    private MapitService mapitService;

    @MockBean
    private ValidationService validationService;

    @Mock
    private MapitData mapitData;

    @Test
    void shouldReturnAllCourtAddresses() {
        when(MOCK_COURT.getAddresses()).thenReturn(COURT_ADDRESSES_ENTITY);
        when(courtRepository.findBySlug(COURT_SLUG)).thenReturn(Optional.of(MOCK_COURT));

        final List<CourtAddress> results = adminCourtAddressService.getCourtAddressesBySlug(COURT_SLUG);
        assertThat(results).hasSize(ADDRESS_COUNT);
        assertThat(results.get(0)).isEqualTo(VISIT_US_ADDRESS);
        assertThat(results.get(1)).isEqualTo(WRITE_TO_US_ADDRESS);
    }

    @Test
    void shouldSortAllVisitUsAddressesFirst() {
        final List<uk.gov.hmcts.dts.fact.entity.CourtAddress> courtAddresses = asList(
            new uk.gov.hmcts.dts.fact.entity.CourtAddress(MOCK_COURT, WRITE_TO_US_ADDRESS_TYPE, emptyList(), emptyList(), null, null, null),
            new uk.gov.hmcts.dts.fact.entity.CourtAddress(MOCK_COURT, VISIT_US_ADDRESS_TYPE, emptyList(), emptyList(), null, null, null),
            new uk.gov.hmcts.dts.fact.entity.CourtAddress(MOCK_COURT, VISIT_OR_CONTACT_US_ADDRESS_TYPE, emptyList(), emptyList(), null, null, null),
            new uk.gov.hmcts.dts.fact.entity.CourtAddress(MOCK_COURT, WRITE_TO_US_ADDRESS_TYPE, emptyList(), emptyList(), null, null, null),
            new uk.gov.hmcts.dts.fact.entity.CourtAddress(MOCK_COURT, WRITE_TO_US_ADDRESS_TYPE, emptyList(), emptyList(), null, null, null),
            new uk.gov.hmcts.dts.fact.entity.CourtAddress(MOCK_COURT, VISIT_US_ADDRESS_TYPE, emptyList(), emptyList(), null, null, null)
        );
        when(MOCK_COURT.getAddresses()).thenReturn(courtAddresses);
        when(courtRepository.findBySlug(COURT_SLUG)).thenReturn(Optional.of(MOCK_COURT));

        final List<CourtAddress> results = adminCourtAddressService.getCourtAddressesBySlug(COURT_SLUG);
        assertThat(results).hasSize(courtAddresses.size());
        assertThat(results.subList(0, 3).stream().map(CourtAddress::getAddressTypeId))
            .allMatch(c -> c.equals(VISIT_US_ADDRESS_TYPE_ID) || c.equals(VISIT_OR_CONTACT_US_ADDRESS_TYPE_ID));
        assertThat(results.subList(3, 6).stream().map(CourtAddress::getAddressTypeId))
            .allMatch(c -> c.equals(WRITE_TO_US_ADDRESS_TYPE_ID));
    }

    @Test
    void shouldReturnNotFoundWhenRetrievingAddressesForNonExistentCourt() {
        when(courtRepository.findBySlug(COURT_SLUG)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminCourtAddressService.getCourtAddressesBySlug(COURT_SLUG))
            .isInstanceOf(NotFoundException.class)
            .hasMessage(NOT_FOUND + COURT_SLUG);
    }

    @Test
    void shouldUpdateCourtAddresses() {
        when(courtRepository.findBySlug(COURT_SLUG)).thenReturn(Optional.of(MOCK_COURT));
        when(adminAddressTypeService.getAddressTypeMap()).thenReturn(Map.of(VISIT_US_ADDRESS_TYPE_ID, VISIT_US_ADDRESS_TYPE,
                                                                            WRITE_TO_US_ADDRESS_TYPE_ID, WRITE_TO_US_ADDRESS_TYPE));
        when(courtAddressRepository.saveAll(any())).thenReturn(COURT_ADDRESSES_ENTITY);

        final List<CourtAddress> results = adminCourtAddressService.updateCourtAddresses(COURT_SLUG, EXPECTED_ADDRESSES);
        assertThat(results).hasSize(ADDRESS_COUNT);
        assertThat(results.get(0)).isEqualTo(VISIT_US_ADDRESS);
        assertThat(results.get(1)).isEqualTo(WRITE_TO_US_ADDRESS);

        verify(courtAddressRepository).deleteAll(any());
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingAddressesForNonExistentCourt() {
        when(courtRepository.findBySlug(COURT_SLUG)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminCourtAddressService.updateCourtAddresses(COURT_SLUG, any()))
            .isInstanceOf(NotFoundException.class)
            .hasMessage(NOT_FOUND + COURT_SLUG);
    }

    @Test
    void validateCourtPostcodesShouldReturnNothingForValidPostcodes() {
        when(adminAddressTypeService.getAddressTypeMap()).thenReturn(ADDRESS_TYPE_MAP);
        when(validationService.validateFullPostcodes(asList(TEST_POSTCODE1, TEST_POSTCODE2))).thenReturn(emptyList());
        when(mapitService.getMapitData(TEST_POSTCODE2)).thenReturn(Optional.of(mapitData));
        when(mapitData.getLat()).thenReturn(LATITUDE);
        when(mapitData.getLon()).thenReturn(LONGITUDE);

        assertThat(adminCourtAddressService.validateCourtAddressPostcodes(COURT_SLUG, EXPECTED_ADDRESSES)).isEmpty();
        verify(adminService).updateCourtLatLon(COURT_SLUG, LATITUDE, LONGITUDE);
    }

    @Test
    void validateCourtPostcodesShouldReturnAllInvalidPostcodes() {
        when(adminAddressTypeService.getAddressTypeMap()).thenReturn(ADDRESS_TYPE_MAP);
        when(validationService.validateFullPostcodes(asList(TEST_POSTCODE1, TEST_POSTCODE2))).thenReturn(asList(TEST_POSTCODE1, TEST_POSTCODE2));
        when(mapitService.getMapitData(TEST_POSTCODE2)).thenReturn(Optional.empty());

        assertThat(adminCourtAddressService.validateCourtAddressPostcodes(COURT_SLUG, EXPECTED_ADDRESSES)).containsExactly(TEST_POSTCODE1, TEST_POSTCODE2);
        verifyNoInteractions(mapitService);
        verifyNoInteractions(adminService);
    }

    @Test
    void validateCourtPostcodesShouldReturnInvalidVisitUsPostcodeOnly() {
        when(adminAddressTypeService.getAddressTypeMap()).thenReturn(ADDRESS_TYPE_MAP);
        when(validationService.validateFullPostcodes(asList(TEST_POSTCODE1, TEST_POSTCODE2))).thenReturn(singletonList(TEST_POSTCODE2));

        assertThat(adminCourtAddressService.validateCourtAddressPostcodes(COURT_SLUG, EXPECTED_ADDRESSES)).containsExactly(TEST_POSTCODE2);
        verifyNoInteractions(mapitService);
        verifyNoInteractions(adminService);
    }

    @Test
    void validateCourtPostcodesShouldReturnInvalidWriteToUsPostcodeOnly() {
        when(adminAddressTypeService.getAddressTypeMap()).thenReturn(ADDRESS_TYPE_MAP);
        when(validationService.validateFullPostcodes(asList(TEST_POSTCODE1, TEST_POSTCODE2))).thenReturn(singletonList(TEST_POSTCODE1));

        assertThat(adminCourtAddressService.validateCourtAddressPostcodes(COURT_SLUG, EXPECTED_ADDRESSES)).containsExactly(TEST_POSTCODE1);
        verifyNoInteractions(mapitService);
        verifyNoInteractions(adminService);
    }

    @Test
    void validateCourtPostcodesShouldReturnInvalidPartialPostcode() {
        final List<CourtAddress> testAddresses = singletonList(new CourtAddress(WRITE_TO_US_ADDRESS_TYPE_ID, TEST_ADDRESS1, TEST_ADDRESS_CY1, TEST_TOWN1, null, PARTIAL_POSTCODE));
        when(adminAddressTypeService.getAddressTypeMap()).thenReturn(ADDRESS_TYPE_MAP);
        when(validationService.validateFullPostcodes(singletonList(PARTIAL_POSTCODE))).thenReturn(singletonList(PARTIAL_POSTCODE));

        assertThat(adminCourtAddressService.validateCourtAddressPostcodes(COURT_SLUG, testAddresses)).containsExactly(PARTIAL_POSTCODE);
        verifyNoInteractions(mapitService);
        verifyNoInteractions(adminService);
    }

    @Test
    void validateCourtPostcodesShouldReturnNothingForEmptyAddress() {
        assertThat(adminCourtAddressService.validateCourtAddressPostcodes(COURT_SLUG, emptyList())).isEmpty();

        verifyNoInteractions(validationService);
        verifyNoInteractions(mapitService);
        verifyNoInteractions(adminService);
    }

    @Test
    void validateCourtPostcodesShouldRemoveLatLonForAddressWithMissingPostcode() {
        final List<CourtAddress> testAddresses = singletonList(new CourtAddress(VISIT_OR_CONTACT_US_ADDRESS_TYPE_ID, TEST_ADDRESS1, TEST_ADDRESS_CY1, TEST_TOWN1, null, ""));
        when(adminAddressTypeService.getAddressTypeMap()).thenReturn(ADDRESS_TYPE_MAP);
        assertThat(adminCourtAddressService.validateCourtAddressPostcodes(COURT_SLUG, testAddresses)).isEmpty();

        verifyNoInteractions(validationService);
        verifyNoInteractions(mapitService);
        verify(adminService).removeCourtLatLon(COURT_SLUG);

    }

    @Test
    void validateCourtPostcodesShouldRemoveLatLonForWriteToUsAddressesOnly() {
        final List<CourtAddress> testAddresses = asList(
            new CourtAddress(WRITE_TO_US_ADDRESS_TYPE_ID, TEST_ADDRESS1, TEST_ADDRESS_CY1, TEST_TOWN1, null, TEST_POSTCODE1),
            new CourtAddress(WRITE_TO_US_ADDRESS_TYPE_ID, TEST_ADDRESS2, TEST_ADDRESS_CY2, TEST_TOWN2, null, TEST_POSTCODE2)
        );
        when(adminAddressTypeService.getAddressTypeMap()).thenReturn(ADDRESS_TYPE_MAP);
        when(validationService.validateFullPostcodes(asList(TEST_POSTCODE1, TEST_POSTCODE2))).thenReturn(emptyList());

        assertThat(adminCourtAddressService.validateCourtAddressPostcodes(COURT_SLUG, testAddresses)).isEmpty();
        verifyNoInteractions(mapitService);
        verify(adminService).removeCourtLatLon(COURT_SLUG);
    }
}