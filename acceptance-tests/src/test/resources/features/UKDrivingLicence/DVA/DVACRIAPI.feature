@drivingLicence_CRI_API
Feature: DVA CRI API

  @drivingLicenceCRI_API @pre-merge @dev
  Scenario Outline: DVA Driving Licence - Auth Source Happy path
    Given DVA Driving Licence with a signed JWT string with <context>, <personalNumber>, <expiryDate>, <issueDate>, <issuedBy> and <fullAddress> for CRI Id driving-licence-cri-dev and JSON Shared Claims 197
    And Driving Licence user sends a POST request to session endpoint
    And Driving Licence user gets a session-id
    And Driving Licence user sends a GET request to the personInfo endpoint
    When Driving Licence user sends a POST request to Driving Licence endpoint using updated jsonRequest returned from the personInfo Table <JSONPayloadRequest>
    And Driving Licence user gets authorisation code
    And Driving Licence user sends a POST request to Access Token endpoint driving-licence-cri-dev
    Then User requests Driving Licence CRI VC
    And Driving Licence VC should contain validityScore 2 and strengthScore 3
    And Driving Licence VC should contain checkMethod data and identityCheckPolicy published in success checkDetails
    And Driving Licence VC should contain JTI field

    Examples:
      | context       | personalNumber | expiryDate | issueDate  | issuedBy | fullAddress                         | JSONPayloadRequest             |
      | check_details | 12345678       | 2042-10-01 | 2018-04-19 | DVA      | 8 HADLEY ROAD BATH BA2 5AA          | DVAAuthValidKennethJsonPayload |
      | check_details | 55667788       | 2042-10-01 | 2018-04-19 | DVA      | 70 OLD BAKERS COURT BELFAST NW3 5RG | DVAAuthValidBillyJsonPayload   |

  @drivingLicenceCRI_API @pre-merge @dev
  Scenario Outline: DVA Driving Licence - Auth Source Retry Journey Happy Path
    Given DVA Driving Licence with a signed JWT string with <context>, <personalNumber>, <expiryDate>, <issueDate>, <issuedBy> and <fullAddress> for CRI Id driving-licence-cri-dev and JSON Shared Claims 197
    And Driving Licence user sends a POST request to session endpoint
    And Driving Licence user gets a session-id
    When Driving Licence user sends a POST request to Driving Licence endpoint using updated jsonRequest returned from the personInfo Table <JSONPayloadRequest>
    Then Driving Licence check response should contain Retry value as false
    Then Check response contains unexpected server error exception containing debug error code <cri_internal_error_code> and debug error message <cri_internal_error_message>

    Examples:
      | context       | personalNumber | expiryDate | issueDate  | issuedBy | fullAddress                | JSONPayloadRequest              | cri_internal_error_code | cri_internal_error_message    |
      | check_details | 12345678       | 2042-10-01 | 2018-04-19 | DVA      | 8 HADLEY ROAD BATH BA2 5AA | DVAAuthSourceInvalidJsonPayload | 1229                    | Failed to unwrap DVA response |
      | check_details | 66778899       | 2042-10-01 | 2018-04-19 | DVA      | 8 HADLEY ROAD BATH NW3 5RG | DVAAuthSourceInvalidJsonPayload | 1229                    | Failed to unwrap DVA response |

  @drivingLicenceCRI_API @pre-merge @dev
  Scenario Outline: DVA Driving Licence - Auth Source Negative Scenario - Missing Address field in payload
    Given DVA Driving Licence with a signed JWT string with <context>, <personalNumber>, <expiryDate>, <issueDate>, <issuedBy> and <fullAddress> for CRI Id driving-licence-cri-dev and JSON Shared Claims 197
    And Driving Licence user sends a POST request to session endpoint
    And Driving Licence user gets a session-id
    When Driving Licence user sends a POST request to Driving Licence endpoint using jsonRequest <JSONPayloadRequest>
    Then Check response contains unexpected server error exception containing debug error code <cri_internal_error_code> and debug error message <cri_internal_error_message>

    Examples:
      | context       | personalNumber | expiryDate | issueDate  | issuedBy | fullAddress | JSONPayloadRequest                     | cri_internal_error_code | cri_internal_error_message  |
      | check_details | 66778899       | 2042-10-01 | 2018-04-19 | DVA      |             | DVAAuthSourceInvalidAddressJsonPayload | 1001                    | Form Data failed validation |

  @drivingLicenceCRI_API @pre-merge @dev
  Scenario: DVA Driving Licence Happy path
    Given Driving Licence user has the user identity in the form of a signed JWT string for CRI Id driving-licence-cri-dev and row number 6
    And Driving Licence user sends a POST request to session endpoint
    And Driving Licence user gets a session-id
    When Driving Licence user sends a POST request to Driving Licence endpoint using jsonRequest DVAValidKennethJsonPayload
    And Driving Licence user gets authorisation code
    And Driving Licence user sends a POST request to Access Token endpoint driving-licence-cri-dev
    Then User requests Driving Licence CRI VC
    And Driving Licence VC should contain validityScore 2 and strengthScore 3
    And Driving Licence VC should contain checkMethod data and identityCheckPolicy published in success checkDetails

  @drivingLicenceCRI_API @pre-merge @dev
  Scenario: DVA Driving Licence Retry Journey Happy Path
    Given Driving Licence user has the user identity in the form of a signed JWT string for CRI Id driving-licence-cri-dev and row number 6
    And Driving Licence user sends a POST request to session endpoint
    And Driving Licence user gets a session-id
    When Driving Licence user sends a POST request to Driving Licence endpoint using jsonRequest DVAInvalidJsonPayload
    Then Driving Licence check response should contain Retry value as true
    When Driving Licence user sends a POST request to Driving Licence endpoint using jsonRequest DVAValidKennethJsonPayload
    And Driving Licence check response should contain Retry value as false
    And Driving Licence user gets authorisation code
    And Driving Licence user sends a POST request to Access Token endpoint driving-licence-cri-dev
    Then User requests Driving Licence CRI VC
    And Driving Licence VC should contain validityScore 2 and strengthScore 3
    And Driving Licence VC should contain checkMethod data and identityCheckPolicy published in success checkDetails

  # ########  Direct connection tests ##########
  @drivingLicenceCRI_API @pre-merge @dev
  Scenario: DVA Driving Licence Happy path with dva direct connection as document checking route
    Given Driving Licence user has the user identity in the form of a signed JWT string for CRI Id driving-licence-cri-dev and row number 6
    And Driving Licence user sends a POST request to session endpoint
    And Driving Licence user gets a session-id
    When Driving Licence user sends a POST request to Driving Licence endpoint using jsonRequest DVAValidKennethJsonPayload
    And Driving Licence user gets authorisation code
    And Driving Licence user sends a POST request to Access Token endpoint driving-licence-cri-dev
    Then User requests Driving Licence CRI VC
    And Driving Licence VC should contain validityScore 2 and strengthScore 3
    And Driving Licence VC should contain checkMethod data and identityCheckPolicy published in success checkDetails

  @drivingLicenceCRI_API @pre-merge @dev
  Scenario: DVA Driving Licence Retry Journey Happy Path with dva direct connection as document checking route
    Given Driving Licence user has the user identity in the form of a signed JWT string for CRI Id driving-licence-cri-dev and row number 6
    And Driving Licence user sends a POST request to session endpoint
    And Driving Licence user gets a session-id
    When Driving Licence user sends a POST request to Driving Licence endpoint using jsonRequest DVAInvalidJsonPayload
    Then Driving Licence check response should contain Retry value as true
    When Driving Licence user sends a POST request to Driving Licence endpoint using jsonRequest DVAValidKennethJsonPayload
    And Driving Licence user gets authorisation code
    And Driving Licence user sends a POST request to Access Token endpoint driving-licence-cri-dev
    Then User requests Driving Licence CRI VC
    And Driving Licence VC should contain validityScore 2 and strengthScore 3
    And Driving Licence VC should contain checkMethod data and identityCheckPolicy published in success checkDetails

  @drivingLicenceCRI_API @pre-merge @dev
  Scenario: DVA Driving Licence user fails first attempt with dva direct as document checking route but VC is still created
    Given Driving Licence user has the user identity in the form of a signed JWT string for CRI Id driving-licence-cri-dev and row number 6
    And Driving Licence user sends a POST request to session endpoint
    And Driving Licence user gets a session-id
    When Driving Licence user sends a POST request to Driving Licence endpoint using jsonRequest DVAInvalidJsonPayload
    Then Driving Licence check response should contain Retry value as true
    And Driving Licence user gets authorisation code
    And Driving Licence user sends a POST request to Access Token endpoint driving-licence-cri-dev
    Then User requests Driving Licence CRI VC
    And Driving Licence VC should contain ci D02, validityScore 0 and strengthScore 3
    And Driving Licence VC should contain checkMethod data and identityCheckPolicy published in failed checkDetails

  @drivingLicenceCRI_API @pre-merge @dev
  Scenario Outline: DVA Driving Licence Un-Happy path with invalid sessionId on Driving Licence Endpoint
    Given Driving Licence user has the user identity in the form of a signed JWT string for CRI Id driving-licence-cri-dev and row number 6
    And Driving Licence user sends a POST request to session endpoint
    And Driving Licence user gets a session-id
    When Driving Licence user sends a POST request to Driving Licence endpoint with a invalid <invalidHeaderValue> value using jsonRequest DVAInvalidJsonPayload

    Examples:
      | invalidHeaderValue |
      | invalidSessionId   |
      | malformedSessionId |
      | missingSessionId   |
      | noSessionHeader    |

  @drivingLicenceCRI_API @pre-merge @dev
  Scenario: DVA Driving Licence Un-Happy path with invalid authCode on Credential Issuer Endpoint
    Given Driving Licence user has the user identity in the form of a signed JWT string for CRI Id driving-licence-cri-dev and row number 6
    And Driving Licence user sends a POST request to session endpoint
    And Driving Licence user gets a session-id
    When Driving Licence user sends a POST request to Driving Licence endpoint using jsonRequest DVAValidKennethJsonPayload
    And Driving Licence user gets authorisation code
    And Driving Licence user sends a POST request to Access Token endpoint driving-licence-cri-dev
    Then User requests Driving Licence CRI VC from the Credential Issuer Endpoint with a invalid Bearer Token value
