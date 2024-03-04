package uk.gov.di.ipv.cri.drivingpermit.api.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import uk.gov.di.ipv.cri.drivingpermit.library.config.GlobalConstants;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ThirdPartyAddress {

    private String postalCode;

    // Ignore sonar do not make this static
    private final String addressCountry = GlobalConstants.UK_DRIVING_PERMIT_ADDRESS_COUNTRY;

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getAddressCountry() {
        return addressCountry;
    }

    public void setAddressCountry(String addressCountry) {
        /* DO NOTHING */
    }
}
