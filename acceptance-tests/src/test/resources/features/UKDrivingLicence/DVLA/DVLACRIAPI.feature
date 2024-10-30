@drivinglicence_CRI_API
Feature: DrivingLicence CRI API

  @drivingLicenceCRI_API @pre-merge @dev
  Scenario Outline: DVLA Driving Licence - Auth Source Happy path
    Given DVLA Driving Licence with a signed JWT string with <context>, <personalNumber>, <expiryDate>, <issueDate>, <issueNumber>, <issuedBy> and <fullAddress> for CRI Id driving-licence-cri-dev and JSON Shared Claims 197
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
      | context       | personalNumber   | expiryDate | issueDate  | issueNumber | issuedBy | fullAddress                | JSONPayloadRequest          |
      | check_details | DOE99751010AL9OD | 2022-02-02 | 2012-02-02 | 13          | DVLA     | 8 HADLEY ROAD BATH TB2 5AA | DVLAValidKennethJsonPayload |
      | check_details | DOE99751010AL9OD | 2022-02-02 | 2012-02-02 | 13          | DVLA     | 8 HADLEY ROAD BATH BA2 5AA | DVLAValidKennethJsonPayload |

  @drivingLicenceCRI_API @pre-merge @dev
  Scenario: DVLA Driving Licence Happy path
    Given Driving Licence user has the user identity in the form of a signed JWT string for CRI Id driving-licence-cri-dev and row number 6
    And Driving Licence user sends a POST request to session endpoint
    And Driving Licence user gets a session-id
    When Driving Licence user sends a POST request to Driving Licence endpoint using jsonRequest DVLAValidKennethJsonPayload
    And Driving Licence user gets authorisation code
    And Driving Licence user sends a POST request to Access Token endpoint driving-licence-cri-dev
    Then User requests Driving Licence CRI VC
    And Driving Licence VC should contain validityScore 2 and strengthScore 3
    And Driving Licence VC should contain checkMethod data and identityCheckPolicy published in success checkDetails
    And Driving Licence VC should contain JTI field

  @drivingLicenceCRI_API @pre-merge @dev
  Scenario: DVLA Password rotation check
    Given Driving Licence CRI is functioning as expected for CRI Id driving-licence-cri-dev
    And The secret has been created
    Then The DVLA password should be valid and rotated within the specified window

  @drivingLicenceCRI_API @pre-merge @dev
  Scenario: DVLA Driving Licence Retry Journey Happy Path
    Given Driving Licence user has the user identity in the form of a signed JWT string for CRI Id driving-licence-cri-dev and row number 6
    And Driving Licence user sends a POST request to session endpoint
    And Driving Licence user gets a session-id
    When Driving Licence user sends a POST request to Driving Licence endpoint using jsonRequest DVLAInvalidJsonPayload
    Then Driving Licence check response should contain Retry value as true
    When Driving Licence user sends a POST request to Driving Licence endpoint using jsonRequest DVLAValidKennethJsonPayload
    And Driving Licence check response should contain Retry value as false
    And Driving Licence user gets authorisation code
    And Driving Licence user sends a POST request to Access Token endpoint driving-licence-cri-dev
    Then User requests Driving Licence CRI VC
    And Driving Licence VC should contain validityScore 2 and strengthScore 3
    And Driving Licence VC should contain checkMethod data and identityCheckPolicy published in success checkDetails

  @drivingLicenceCRI_API @pre-merge @dev
  Scenario: DVLA Driving Licence user fails first attempt with but VC is still created
    Given Driving Licence user has the user identity in the form of a signed JWT string for CRI Id driving-licence-cri-dev and row number 6
    And Driving Licence user sends a POST request to session endpoint
    And Driving Licence user gets a session-id
    When Driving Licence user sends a POST request to Driving Licence endpoint using jsonRequest DVLAInvalidJsonPayload
    Then Driving Licence check response should contain Retry value as true
    And Driving Licence user gets authorisation code
    And Driving Licence user sends a POST request to Access Token endpoint driving-licence-cri-dev
    Then User requests Driving Licence CRI VC
    And Driving Licence VC should contain ci D02, validityScore 0 and strengthScore 3
    And Driving Licence VC should contain checkMethod data and identityCheckPolicy published in failed checkDetails

#########  Direct connection tests ##########

  @drivingLicenceCRI_API @pre-merge @dev
  Scenario: DVLA Driving Licence Happy path with dvla direct connection as document checking route
    Given Driving Licence user has the user identity in the form of a signed JWT string for CRI Id driving-licence-cri-dev and row number 6
    And Driving Licence user sends a POST request to session endpoint
    And Driving Licence user gets a session-id
    When Driving Licence user sends a POST request to Driving Licence endpoint using jsonRequest DVLAValidKennethJsonPayload
    And Driving Licence user gets authorisation code
    And Driving Licence user sends a POST request to Access Token endpoint driving-licence-cri-dev
    Then User requests Driving Licence CRI VC
    And Driving Licence VC should contain validityScore 2 and strengthScore 3
    And Driving Licence VC should contain checkMethod data and identityCheckPolicy published in success checkDetails

  @drivingLicenceCRI_API @pre-merge @dev
  Scenario: DVLA Driving Licence user fails first attempt with dvla direct as document checking route but VC is still created
    Given Driving Licence user has the user identity in the form of a signed JWT string for CRI Id driving-licence-cri-dev and row number 6
    And Driving Licence user sends a POST request to session endpoint
    And Driving Licence user gets a session-id
    When Driving Licence user sends a POST request to Driving Licence endpoint using jsonRequest DVLAInvalidJsonPayload
    Then Driving Licence check response should contain Retry value as true
    And Driving Licence user gets authorisation code
    And Driving Licence user sends a POST request to Access Token endpoint driving-licence-cri-dev
    Then User requests Driving Licence CRI VC
    And Driving Licence VC should contain ci D02, validityScore 0 and strengthScore 3
    And Driving Licence VC should contain checkMethod data and identityCheckPolicy published in failed checkDetails

  @drivingLicenceCRI_API @pre-merge @dev
  Scenario Outline: Test Driving Licence API handles errors on the match endpoint with an OAuth Server Error
    Given Driving Licence user has the user identity in the form of a signed JWT string for CRI Id driving-licence-cri-dev and row number 6
    And Driving Licence user sends a POST request to session endpoint
    And Driving Licence user gets a session-id
    When Driving Licence user sends a editable POST request to Driving Licence endpoint using jsonRequest <JsonPayload> with edited fields <jsonEdits>
    Then Check response contains unexpected server error exception containing debug error code <cri_internal_error_code> and debug error message <cri_internal_error_message>
    Examples:
      | JsonPayload                 | jsonEdits                                                                  | cri_internal_error_code | cri_internal_error_message                                |
      | DVLAValidKennethJsonPayload | {"surname": "Unauthorized", "drivingLicenceNumber" : "UNAUT123456AB1AB"}   | 1316                    | error dvla expired token recovery failed                  |
      | DVLAValidKennethJsonPayload | {"surname": "Forbidden", "drivingLicenceNumber" : "FORBI123456AB1AB"}      | 1314                    | error match endpoint returned unexpected http status code |
      | DVLAValidKennethJsonPayload | {"surname": "TooManyRequest", "drivingLicenceNumber" : "TOOMA123456AB1AB"} | 1314                    | error match endpoint returned unexpected http status code |
      | DVLAValidKennethJsonPayload | {"surname": "ServerError", "drivingLicenceNumber" : "SERVE500456AB1AB"}    | 1314                    | error match endpoint returned unexpected http status code |
      | DVLAValidKennethJsonPayload | {"surname": "ServerError", "drivingLicenceNumber" : "SERVE502456AB1AB"}    | 1314                    | error match endpoint returned unexpected http status code |
      | DVLAValidKennethJsonPayload | {"surname": "ServerError", "drivingLicenceNumber" : "SERVE504456AB1AB"}    | 1314                    | error match endpoint returned unexpected http status code |

  @drivingLicenceCRI_API @pre-merge @dev
  Scenario Outline: Test Driving Licence API handles 404 on the match endpoint
    Given Driving Licence user has the user identity in the form of a signed JWT string for CRI Id driving-licence-cri-dev and row number 6
    And Driving Licence user sends a POST request to session endpoint
    And Driving Licence user gets a session-id
    When Driving Licence user sends a editable POST request to Driving Licence endpoint using jsonRequest <JsonPayload> with edited fields <jsonEdits>
    Then Driving Licence check response should contain Retry value as true
    And Driving Licence user gets authorisation code
    And Driving Licence user sends a POST request to Access Token endpoint driving-licence-cri-dev
    Then User requests Driving Licence CRI VC
    And Driving Licence VC should contain ci D02, validityScore 0 and strengthScore 3
    And Driving Licence VC should contain checkMethod data and identityCheckPolicy published in failed checkDetails
    Examples:
      | JsonPayload                 | jsonEdits                                                                 |
      | DVLAValidKennethJsonPayload | {"surname": "CannotBeFound", "drivingLicenceNumber" : "CANNO123456AB1AB"} |

  @drivingLicenceCRI_API @pre-merge @dev
  Scenario Outline: DVLA Driving Licence Un-Happy path with invalid sessionId on Driving Licence Endpoint
    Given Driving Licence user has the user identity in the form of a signed JWT string for CRI Id driving-licence-cri-dev and row number 6
    And Driving Licence user sends a POST request to session endpoint
    And Driving Licence user gets a session-id
    When Driving Licence user sends a POST request to Driving Licence endpoint with a invalid <invalidHeaderValue> value using jsonRequest DVAInvalidJsonPayload
    Examples:
      | invalidHeaderValue |
      | mismatchSessionId  |
      | malformedSessionId |
      | missingSessionId   |
      | noSessionHeader    |

  @drivingLicenceCRI_API @pre-merge @dev
  Scenario: DVLA Driving Licence Un-Happy path with invalid authCode on Credential Issuer Endpoint
    Given Driving Licence user has the user identity in the form of a signed JWT string for CRI Id driving-licence-cri-dev and row number 6
    And Driving Licence user sends a POST request to session endpoint
    And Driving Licence user gets a session-id
    When Driving Licence user sends a POST request to Driving Licence endpoint using jsonRequest DVLAValidKennethJsonPayload
    And Driving Licence user gets authorisation code
    And Driving Licence user sends a POST request to Access Token endpoint driving-licence-cri-dev
    Then User requests Driving Licence CRI VC from the Credential Issuer Endpoint with a invalid Bearer Token value
