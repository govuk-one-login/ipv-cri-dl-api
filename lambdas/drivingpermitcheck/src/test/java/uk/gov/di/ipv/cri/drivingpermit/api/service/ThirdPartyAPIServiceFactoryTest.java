// package uk.gov.di.ipv.cri.drivingpermit.api.service;
//
// import com.fasterxml.jackson.databind.ObjectMapper;
// import org.apache.http.HttpException;
// import org.apache.http.impl.client.CloseableHttpClient;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import uk.gov.di.ipv.cri.common.library.util.EventProbe;
// import uk.gov.di.ipv.cri.drivingpermit.api.service.configuration.ConfigurationService;
// import uk.gov.di.ipv.cri.drivingpermit.api.service.configuration.DcsConfiguration;
// import uk.gov.di.ipv.cri.drivingpermit.api.service.dcs.DcsThirdPartyDocumentGateway;
// import uk.gov.di.ipv.cri.drivingpermit.api.service.dva.DvaThirdPartyDocumentGateway;
// import uk.gov.di.ipv.cri.drivingpermit.api.service.dvla.DvlaThirdPartyDocumentGateway;
// import uk.gov.di.ipv.cri.drivingpermit.library.dva.configuration.DvaConfiguration;
// import uk.gov.di.ipv.cri.drivingpermit.library.dvla.configuration.DvlaConfiguration;
//
// import java.io.IOException;
// import java.security.KeyStoreException;
// import java.security.NoSuchAlgorithmException;
// import java.security.cert.CertificateException;
//
// import static org.junit.jupiter.api.Assertions.assertNotNull;
// import static org.junit.jupiter.api.Assertions.assertTrue;
// import static org.mockito.Mockito.when;
//
// @ExtendWith(MockitoExtension.class)
// class ThirdPartyAPIServiceFactoryTest {
//
//    @Mock DrivingPermitServiceFactory mockDrivingPermitServiceFactory;
//    @Mock ConfigurationService mockConfigurationService;
//
//    @Mock DcsConfiguration mockDcsConfiguration;
//    @Mock DvaConfiguration mockDvaConfiguration;
//    @Mock DvlaConfiguration mockDvlaConfiguration;
//
//    @Mock ObjectMapper mockObjectMapper;
//    @Mock EventProbe mockEventProbe;
//    @Mock CloseableHttpClient mockCloseableHttpClient;
//
//    private ThirdPartyAPIServiceFactory thirdPartyAPIServiceFactory;
//
//    @BeforeEach
//    void setUp()
//            throws CertificateException, HttpException, NoSuchAlgorithmException,
// KeyStoreException,
//                    IOException {
//
//        when(mockDrivingPermitServiceFactory.getConfigurationService())
//                .thenReturn(mockConfigurationService);
//        when(mockConfigurationService.isDcsPerformanceStub()).thenReturn(false);
//        when(mockConfigurationService.isDvaPerformanceStub()).thenReturn(false);
//        when(mockConfigurationService.isDvlaPerformanceStub()).thenReturn(false);
//
//        // DCS
//        when(mockDrivingPermitServiceFactory.getObjectMapper()).thenReturn(mockObjectMapper);
//        when(mockDrivingPermitServiceFactory.getEventProbe()).thenReturn(mockEventProbe);
//        when(mockDrivingPermitServiceFactory.generateDcsHttpClient(mockConfigurationService,
// true))
//                .thenReturn(mockCloseableHttpClient);
//
//        // DVA
//        when(mockDrivingPermitServiceFactory.getObjectMapper()).thenReturn(mockObjectMapper);
//        when(mockDrivingPermitServiceFactory.getEventProbe()).thenReturn(mockEventProbe);
//        when(mockDrivingPermitServiceFactory.generateDvaHttpClient(mockConfigurationService,
// true))
//                .thenReturn(mockCloseableHttpClient);
//
//        // DVLA
//        when(mockDrivingPermitServiceFactory.getEventProbe()).thenReturn(mockEventProbe);
//        when(mockDrivingPermitServiceFactory.generateDvlaHttpClient())
//                .thenReturn(mockCloseableHttpClient);
//
//        when(mockConfigurationService.getDvlaConfiguration()).thenReturn(mockDvlaConfiguration);
//        when(mockDvlaConfiguration.getTokenEndpoint()).thenReturn("TOKEN_END_POINT");
//        when(mockDvlaConfiguration.getTokenTableName()).thenReturn("TOKEN_TABLE");
//        when(mockDvlaConfiguration.getMatchEndpoint()).thenReturn("DRIVER_MATCH_ENDPOINT");
//
//        thirdPartyAPIServiceFactory =
//                new ThirdPartyAPIServiceFactory(mockDrivingPermitServiceFactory);
//    }
//
//    @Test
//    void shoudReturnDcsThirdPartyService() {
//        ThirdPartyAPIService thirdPartyAPIService =
//                thirdPartyAPIServiceFactory.getDcsThirdPartyAPIService();
//        assertNotNull(thirdPartyAPIService);
//        assertTrue(thirdPartyAPIService instanceof DcsThirdPartyDocumentGateway);
//    }
//
//    @Test
//    void shoudReturnDvaThirdPartyService() {
//        ThirdPartyAPIService thirdPartyAPIService =
//                thirdPartyAPIServiceFactory.getDvaThirdPartyAPIService();
//        assertNotNull(thirdPartyAPIService);
//        assertTrue(thirdPartyAPIService instanceof DvaThirdPartyDocumentGateway);
//    }
//
//    @Test
//    void shoudReturnDvlaThirdPartyService() {
//        ThirdPartyAPIService thirdPartyAPIService =
//                thirdPartyAPIServiceFactory.getDvlaThirdPartyAPIService();
//        assertNotNull(thirdPartyAPIService);
//        assertTrue(thirdPartyAPIService instanceof DvlaThirdPartyDocumentGateway);
//    }
// }
