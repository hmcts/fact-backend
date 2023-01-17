package uk.gov.hmcts.dts.fact.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import javax.persistence.*;

@SuppressWarnings("PMD.ExcessiveParameterList")
@Entity
@Table(name = "search_courtaddress")
@Getter
@Setter
@NoArgsConstructor
public class CourtAddress {
    @Id()
    @SequenceGenerator(name = "seq-gen-address", sequenceName = "search_courtaddress_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq-gen-address")
    private Integer id;
    @ManyToOne
    @JoinColumn(name = "court_id")
    private Court court;
    @OneToOne
    @JoinColumn(name = "address_type_id")
    private AddressType addressType;
    private String address;
    private String addressCy;
    private String townName;
    private String townNameCy;
    @OneToOne
    @JoinColumn(name = "county_id")
    private County county;
    private String postcode;

    @OneToMany
    @JoinColumn(name = "address_id")
    private List<CourtSecondaryAddressType> courtSecondaryAddressType;

    private Integer sortOrder;

    public CourtAddress(final Court court, final AddressType addressType, final List<String> addressLines, final List<String> addressLinesCy,
                        final String townName, final String townNameCy, final County county, final String postcode, final Integer sortOrder) {
        this.court = court;
        this.addressType = addressType;
        this.address = CollectionUtils.isEmpty(addressLines) ? "" : convertAddressLines(addressLines);
        this.addressCy = CollectionUtils.isEmpty(addressLinesCy) ? "" : convertAddressLines(addressLinesCy);
        this.townName = townName;
        this.townNameCy = townNameCy;
        this.county = county;
        this.postcode = postcode;
        this.sortOrder = sortOrder;
    }

    private String convertAddressLines(final List<String> addressLines) {
        final StringBuilder builder = new StringBuilder();
        final int lastLine = addressLines.size() - 1;
        for (int i = 0; i < lastLine; i++) {
            builder.append(addressLines.get(i))
                .append(System.lineSeparator());
        }
        builder.append(addressLines.get(lastLine));
        return builder.toString();
    }

    @PrePersist
    @PreUpdate
    @PreRemove
    public void updateTimestamp() {
        court.setUpdatedAt(Timestamp.from(Instant.now()));
    }
}
