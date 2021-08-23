package uk.gov.hmcts.dts.fact.model.deprecated;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.dts.fact.entity.Contact;
import uk.gov.hmcts.dts.fact.entity.Court;
import uk.gov.hmcts.dts.fact.entity.CourtContact;
import uk.gov.hmcts.dts.fact.entity.CourtType;
import uk.gov.hmcts.dts.fact.model.AreaOfLaw;
import uk.gov.hmcts.dts.fact.model.CourtAddress;
import uk.gov.hmcts.dts.fact.util.Utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.dts.fact.util.Utils.chooseString;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@SuppressWarnings("PMD.TooManyFields")
@JsonPropertyOrder({"name", "lat", "lon", "number", "cci_code", "magistrate_code", "slug", "types", "address",
    "areas_of_law", "areas_of_law_spoe", "displayed", "hide_aols", "dx_number", "distance"})
public class CourtWithDistance {
    private String name;
    private Double lat;
    private Double lon;
    @JsonProperty("number")
    private Integer crownLocationCode;
    @JsonProperty("cci_code")
    private Integer countyLocationCode;
    @JsonProperty("magistrate_code")
    private Integer magistratesLocationCode;
    private String slug;
    @JsonProperty("areas_of_law_spoe")
    private List<String> areasOfLawSpoe;
    @JsonProperty("types")
    private List<String> courtTypes;
    private List<CourtAddress> addresses;
    private List<AreaOfLaw> areasOfLaw;
    private Boolean displayed;
    private Boolean hideAols;
    private String dxNumber;
    private BigDecimal distance;


    public CourtWithDistance(final Court courtEntity) {
        this.name = chooseString(courtEntity.getNameCy(), courtEntity.getName());
        this.lat = courtEntity.getLat();
        this.lon = courtEntity.getLon();
        this.crownLocationCode = courtEntity.getNumber();
        this.countyLocationCode = courtEntity.getCciCode();
        this.magistratesLocationCode = courtEntity.getMagistrateCode();
        this.slug = courtEntity.getSlug();
        this.courtTypes = courtEntity.getCourtTypes().stream().map(CourtType::getName).sorted().collect(toList());
        this.addresses = courtEntity.getAddresses().stream().map(CourtAddress::new).collect(toList());
        this.areasOfLaw = courtEntity.getAreasOfLaw().stream().map(AreaOfLaw::new).collect(toList());
        this.areasOfLawSpoe = courtEntity.getAreasOfLawSpoe();
        this.displayed = courtEntity.getDisplayed();
        this.hideAols = courtEntity.getHideAols();
        final List<Contact> contacts = ofNullable(courtEntity.getCourtContacts())
            .map(Collection::stream)
            .orElseGet(Stream::empty)
            .map(CourtContact::getContact)
            .collect(toList());
        this.dxNumber = this.getDxNumber(contacts);
    }

    public CourtWithDistance(final uk.gov.hmcts.dts.fact.entity.CourtWithDistance courtWithDistanceEntity) {
        this.name = chooseString(courtWithDistanceEntity.getNameCy(), courtWithDistanceEntity.getName());
        this.lat = courtWithDistanceEntity.getLat();
        this.lon = courtWithDistanceEntity.getLon();
        this.crownLocationCode = courtWithDistanceEntity.getNumber();
        this.countyLocationCode = courtWithDistanceEntity.getCciCode();
        this.magistratesLocationCode = courtWithDistanceEntity.getMagistrateCode();
        this.slug = courtWithDistanceEntity.getSlug();
        this.courtTypes = courtWithDistanceEntity.getCourtTypes().stream().map(CourtType::getName).sorted().collect(toList());
        this.addresses = courtWithDistanceEntity.getAddresses().stream().map(CourtAddress::new).collect(toList());
        this.areasOfLaw = courtWithDistanceEntity.getAreasOfLaw().stream().map(AreaOfLaw::new).collect(toList());
        this.areasOfLawSpoe = courtWithDistanceEntity.getAreasOfLawSpoe();
        this.displayed = courtWithDistanceEntity.getDisplayed();
        this.hideAols = courtWithDistanceEntity.getHideAols();
        this.dxNumber = this.getDxNumber(courtWithDistanceEntity.getContacts());
        this.distance = BigDecimal.valueOf(courtWithDistanceEntity.getDistance()).setScale(2, RoundingMode.HALF_UP);
    }

    private String getDxNumber(final List<Contact> contacts) {
        return contacts.stream()
            .filter(Utils.NAME_IS_DX)
            .map(Contact::getNumber)
            .findFirst()
            .orElse(null);
    }
}
