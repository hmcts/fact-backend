package uk.gov.hmcts.dts.fact.model.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.dts.fact.entity.CourtType;
import uk.gov.hmcts.dts.fact.entity.ServiceArea;
import uk.gov.hmcts.dts.fact.model.*;

import java.util.List;

import static com.fasterxml.jackson.databind.PropertyNamingStrategy.SnakeCaseStrategy;
import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.dts.fact.util.Utils.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(SnakeCaseStrategy.class)
@SuppressWarnings("PMD.TooManyFields")
@JsonPropertyOrder({"name", "name_cy", "slug", "info", "info_cy", "open", "directions", "directions_cy", "image_file", "lat", "lon",
    "urgent_message", "urgent_message_cy", "crown_location_code", "county_location_code", "magistrates_location_code", "areas_of_law",
    "types", "emails", "contacts", "opening_times", "facilities", "addresses", "gbs", "dx_number", "service_area",
    "in_person"})
public class AdminCourt {
    private String name;
    @JsonProperty("name_cy")
    private String nameCy;
    private String slug;
    private String info;
    @JsonProperty("info_cy")
    private String infoCy;
    private Boolean open;
    private String directions;
    @JsonProperty("directions_cy")
    private String directionsCy;
    private String imageFile;
    private Double lat;
    private Double lon;
    @JsonProperty("urgent_message")
    private String alert;
    @JsonProperty("urgent_message_cy")
    private String alertCy;
    private Integer crownLocationCode;
    private Integer countyLocationCode;
    private Integer magistratesLocationCode;
    private List<AreaOfLaw> areasOfLaw;
    @JsonProperty("types")
    private List<String> courtTypes;
    private List<Email> emails;
    private List<Contact> contacts;
    private List<OpeningTime> openingTimes;
    private List<Facility> facilities;
    private List<CourtAddress> addresses;
    private String gbs;
    @JsonProperty("dx_number")
    private List<String> dxNumbers;
    @JsonProperty("service_area")
    private List<String> serviceAreas;
    private Boolean inPerson;
    private Boolean accessScheme;

    public AdminCourt(uk.gov.hmcts.dts.fact.entity.Court courtEntity) {
        this.name = courtEntity.getName();
        this.nameCy = courtEntity.getNameCy();
        this.slug = courtEntity.getSlug();
        this.info = stripHtmlFromString(courtEntity.getInfo());
        this.infoCy = stripHtmlFromString(courtEntity.getInfoCy());
        this.open = courtEntity.getDisplayed();
        this.directions = courtEntity.getDirections();
        this.directionsCy = courtEntity.getDirectionsCy();
        this.imageFile = courtEntity.getImageFile();
        this.lat = courtEntity.getLat();
        this.lon = courtEntity.getLon();
        this.alert = courtEntity.getAlert();
        this.alertCy = courtEntity.getAlertCy();
        this.crownLocationCode = courtEntity.getNumber();
        this.countyLocationCode = courtEntity.getCciCode();
        this.magistratesLocationCode = courtEntity.getMagistrateCode();
        this.areasOfLaw = courtEntity
            .getAreasOfLaw()
            .stream()
            .map(areaOfLaw -> {
                AreaOfLaw areaOfLawObj = new AreaOfLaw(areaOfLaw);
                areaOfLawObj.setExternalLink(decodeUrlFromString(areaOfLawObj.getExternalLink()));
                return areaOfLawObj;
            })
            .collect(toList());
        this.contacts = courtEntity.getContacts().stream().filter(NAME_IS_NOT_DX)
            .map(Contact::new).collect(toList());
        this.courtTypes = courtEntity.getCourtTypes().stream().map(CourtType::getName).collect(toList());
        this.emails = courtEntity.getEmails().stream().map(Email::new).collect(toList());
        this.openingTimes = courtEntity.getOpeningTimes().stream().map(OpeningTime::new).collect(toList());
        this.facilities = courtEntity
            .getFacilities()
            .stream()
            .map(facility -> {
                Facility facilityObj = new Facility(facility);
                facilityObj.setDescription(stripHtmlFromString(facilityObj.getDescription()));
                return facilityObj;
            })
            .collect(toList());
        this.addresses = this.refactorAddressType(
            courtEntity.getAddresses().stream().map(CourtAddress::new).collect(toList()));
        this.gbs = courtEntity.getGbs();
        this.dxNumbers = courtEntity.getContacts().stream().filter(NAME_IS_DX).map(uk.gov.hmcts.dts.fact.entity.Contact::getNumber)
            .collect(toList());
        this.serviceAreas = courtEntity.getServiceAreas() == null ? null : courtEntity.getServiceAreas()
            .stream()
            .map(ServiceArea::getName)
            .collect(toList());
        this.inPerson = courtEntity.getInPerson() == null ? null : courtEntity.getInPerson().getIsInPerson();
        this.accessScheme = courtEntity.getInPerson() == null ? null : courtEntity.getInPerson().getAccessScheme();
    }

    private List<CourtAddress> refactorAddressType(List<CourtAddress> courtAddresses) {
        for (CourtAddress courtAddress : courtAddresses) {
            if (courtAddress.getAddressType().equals("Visit us or write to us")) {
                courtAddress.setAddressType("Visit or contact us");
            } else if (courtAddress.getAddressType().equals("Postal")) {
                courtAddress.setAddressType("Write to us");
            } else if (courtAddress.getAddressType().equals("Visiting")) {
                courtAddress.setAddressType("Visit us");
            }
        }
        return courtAddresses;
    }

}
