package net.tangly.bus.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AddressTest {
    static final String STREET = "STREET";
    static final String EXTENDED = "EXTENDED";
    public static final String POBOX = "POBOX";
    public static final String POSTCODE = "POSTCODE";
    public static final String LOCALITY = "LOCALITY";
    public static final String REGION = "REGION";
    public static final String COUNTRY = "COUNTRY";

    @Test
    void buildAdressTest() {
        Address address =
                Address.builder().street(STREET).extended(EXTENDED).poBox(POBOX).locality(LOCALITY).postcode(POSTCODE).region(REGION).country(COUNTRY)
                        .build();
        assertThat(address.street()).isEqualTo(STREET);
        assertThat(address.extended()).isEqualTo(EXTENDED);
        assertThat(address.poBox()).isEqualTo(POBOX);
        assertThat(address.locality()).isEqualTo(LOCALITY);
        assertThat(address.region()).isEqualTo(REGION);
        assertThat(address.country()).isEqualTo(COUNTRY);
    }

    @Test
    void buildAddressFromString() {
        Address original =
                Address.builder().street(STREET).extended(EXTENDED).poBox(POBOX).locality(LOCALITY).postcode(POSTCODE).region(REGION).country(COUNTRY)
                        .build();
        Address copy = Address.of(original.text());
        assertThat(copy).isEqualTo(original);
        assertThat(copy.hashCode()).isEqualTo(original.hashCode());
    }

    @Test
    void testEmptyAddressTextAndOf() {
        Address original = Address.builder().build();
        String foo = original.text();
        Address copy = Address.of(original.text());
        assertThat(copy).isEqualTo(original);
        assertThat(copy.hashCode()).isEqualTo(original.hashCode());
    }
}
