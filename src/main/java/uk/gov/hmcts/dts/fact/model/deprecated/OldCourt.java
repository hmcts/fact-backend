package uk.gov.hmcts.dts.fact.model.deprecated;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.dts.fact.entity.CourtContact;
import uk.gov.hmcts.dts.fact.entity.CourtEmail;
import uk.gov.hmcts.dts.fact.entity.CourtOpeningTime;
import uk.gov.hmcts.dts.fact.entity.CourtType;
import uk.gov.hmcts.dts.fact.model.Contact;
import uk.gov.hmcts.dts.fact.model.Email;
import uk.gov.hmcts.dts.fact.model.OpeningTime;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.dts.fact.util.Utils.chooseString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@SuppressWarnings("PMD.TooManyFields")
@JsonPropertyOrder({"name", "slug", "info", "open", "directions", "lat", "lon",
    "crown_location_code", "county_location_code", "cci_code", "magistrates_location_code", "areas_of_law",
    "types", "emails", "contacts", "opening_times", "facilities", "addresses", "gbs"})
public class OldCourt {
    private String name;
    private String slug;
    private String info;
    private Boolean open;
    private String directions;
    private Double lat;
    private Double lon;
    private Integer crownLocationCode;
    private Integer countyLocationCode;
    private Integer cciCode;
    private Integer magistratesLocationCode;
    private List<String> areasOfLaw;
    @JsonProperty("areas_of_law_spoe")
    private List<String> areasOfLawSpoe;
    @JsonProperty("types")
    private List<String> courtTypes;
    private List<Email> emails;
    private List<Contact> contacts;
    private List<OpeningTime> openingTimes;
    private List<OldFacility> facilities;
    private List<OldCourtAddress> addresses;
    private String gbs;

    public OldCourt(uk.gov.hmcts.dts.fact.entity.Court courtEntity) {
        this.name = chooseString(courtEntity.getNameCy(), courtEntity.getName());
        this.slug = courtEntity.getSlug();
        this.info = chooseString(courtEntity.getInfoCy(), courtEntity.getInfo());
        this.open = courtEntity.getDisplayed();
        this.directions = chooseString(courtEntity.getDirectionsCy(), courtEntity.getDirections());
        this.lat = courtEntity.getLat();
        this.lon = courtEntity.getLon();
        this.crownLocationCode = courtEntity.getNumber();
        this.countyLocationCode = courtEntity.getCciCode();
        this.cciCode = courtEntity.getCciCode();
        this.magistratesLocationCode = courtEntity.getMagistrateCode();
        this.areasOfLaw = courtEntity.getAreasOfLaw().stream().map(uk.gov.hmcts.dts.fact.entity.AreaOfLaw::getName)
            .collect(toList());
        this.areasOfLawSpoe = courtEntity.getAreasOfLawSpoe();
        this.courtTypes = courtEntity.getCourtTypes().stream().map(CourtType::getName).collect(toList());
        this.emails = ofNullable(courtEntity.getCourtEmails())
            .map(Collection::stream)
            .orElseGet(Stream::empty)
            .map(CourtEmail::getEmail)
            .map(Email::new).collect(toList());
        this.contacts = ofNullable(courtEntity.getCourtContacts())
            .map(Collection::stream)
            .orElseGet(Stream::empty)
            .map(CourtContact::getContact)
            .map(Contact::new)
            .collect(toList());
        this.openingTimes = ofNullable(courtEntity.getCourtOpeningTimes())
            .map(Collection::stream)
            .orElseGet(Stream::empty)
            .map(CourtOpeningTime::getOpeningTime)
            .map(OpeningTime::new).collect(toList());
        this.facilities = courtEntity.getFacilities().stream().map(OldFacility::new).collect(toList());
        this.addresses = courtEntity.getAddresses().stream().map(OldCourtAddress::new).collect(toList());
        this.gbs = courtEntity.getGbs();
    }
}
