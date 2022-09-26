package uk.gov.di.ipv.cri.drivingpermit.api.persistence.item;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

@DynamoDbBean
public class ContraindicationMappingItem {
    private String thirdPartyId;
    private String contraindicationCode;
}
