package gov.di_ipv_drivingpermit.utilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class ObjectMapperFactory {
    public static final ObjectMapper MAPPER =
            new ObjectMapper().registerModule(new JavaTimeModule());

    private ObjectMapperFactory() {}
}
