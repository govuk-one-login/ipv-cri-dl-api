package gov.di_ipv_drivingpermit.utilities;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PiiTermsCollector {

    // Exclude values too short or generic to be meaningful PII search terms
    private static final int MIN_TERM_LENGTH = 4;

    private PiiTermsCollector() {}

    public static Set<String> collectFromAllTestUsers() {
        TestDataCreator.createDefaultResponses();

        return Stream.of(
                        TestDataCreator.dvaTestUsers.values(),
                        TestDataCreator.dvlaTestUsers.values())
                .flatMap(Collection::stream)
                .flatMap(PiiTermsCollector::extractPiiTerms)
                .filter(term -> term != null && term.length() >= MIN_TERM_LENGTH)
                .collect(Collectors.toSet());
    }

    private static Stream<String> extractPiiTerms(TestInput user) {
        return Arrays.stream(
                new String[] {
                    user.getFirstName(),
                    user.getLastName(),
                    user.getMiddleNames(),
                    user.getLicenceNumber(),
                    user.getPostcode(),
                    fullDob(user)
                });
    }

    private static String fullDob(TestInput user) {
        if (user.getBirthYear() == null) return null;
        return user.getBirthYear() + "-" + pad(user.getBirthMonth()) + "-" + pad(user.getBirthDay());
    }

    private static String pad(String value) {
        if (value == null) return "00";
        return value.length() == 1 ? "0" + value : value;
    }
}
