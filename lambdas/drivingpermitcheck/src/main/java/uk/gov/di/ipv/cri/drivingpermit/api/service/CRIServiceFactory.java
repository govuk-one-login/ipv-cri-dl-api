package uk.gov.di.ipv.cri.drivingpermit.api.service;

import org.apache.http.HttpException;
import software.amazon.awssdk.regions.Region;
import uk.gov.di.ipv.cri.common.library.annotations.ExcludeFromGeneratedCoverageReport;
import uk.gov.di.ipv.cri.drivingpermit.api.gateway.ThirdPartyDocumentGateway;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

public class CRIServiceFactory {
    private final CommonServiceFactory commonServiceFactory;
    private final IdentityVerificationService identityVerificationService;
    private final ContraindicationMapper contraindicationMapper;
    private final DcsCryptographyService dcsCryptographyService;
    private final FormDataValidator formDataValidator;
    private final Region awsRegion = Region.of(System.getenv("AWS_REGION"));

    public CRIServiceFactory(CommonServiceFactory commonServiceFactory)
            throws NoSuchAlgorithmException, InvalidKeyException, CertificateException,
                    InvalidKeySpecException, KeyStoreException, IOException, HttpException {
        this.commonServiceFactory = commonServiceFactory;
        this.formDataValidator = new FormDataValidator();
        this.dcsCryptographyService =
                new DcsCryptographyService(commonServiceFactory.getConfigurationService());
        this.contraindicationMapper =
                new ContraIndicatorRemoteMapper(commonServiceFactory.getConfigurationService());
        this.identityVerificationService = createIdentityVerificationService(commonServiceFactory);
    }

    /** Creates services used by this CRI */
    @ExcludeFromGeneratedCoverageReport
    CRIServiceFactory(
            CommonServiceFactory commonServiceFactory,
            DcsCryptographyService dcsCryptographyService,
            ContraindicationMapper contraindicationMapper,
            FormDataValidator formDataValidator)
            throws NoSuchAlgorithmException, InvalidKeyException, CertificateException,
                    InvalidKeySpecException {
        this.commonServiceFactory = commonServiceFactory;
        this.dcsCryptographyService = dcsCryptographyService;
        this.contraindicationMapper = contraindicationMapper;
        this.formDataValidator = formDataValidator;
        this.identityVerificationService = createIdentityVerificationService(commonServiceFactory);
    }

    private IdentityVerificationService createIdentityVerificationService(
            CommonServiceFactory commonServiceFactory) {

        ThirdPartyDocumentGateway thirdPartyGateway =
                new ThirdPartyDocumentGateway(
                        commonServiceFactory.getObjectMapper(),
                        this.dcsCryptographyService,
                        commonServiceFactory.getConfigurationService(),
                        commonServiceFactory.getHttpRetryer(),
                        commonServiceFactory.getEventProbe());

        return new IdentityVerificationService(
                thirdPartyGateway,
                this.formDataValidator,
                this.contraindicationMapper,
                commonServiceFactory.getAuditService(),
                commonServiceFactory.getConfigurationService(),
                commonServiceFactory.getObjectMapper(),
                commonServiceFactory.getEventProbe());
    }

    private static final char[] password = "password".toCharArray();

    public IdentityVerificationService getIdentityVerificationService() {
        return this.identityVerificationService;
    }

    public ContraindicationMapper getContraindicationMapper() {
        return contraindicationMapper;
    }
}
