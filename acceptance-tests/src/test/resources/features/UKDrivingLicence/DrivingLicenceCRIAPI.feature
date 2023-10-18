@drivinglicence_CRI_API
Feature: DrivingLicence CRI API

  @drivingLicenceCRI_API @pre-merge @dev
  Scenario: Acquire initial JWT and DVLA Driving Licence Happy path
    Given Driving Licence user has the user identity in the form of a signed JWT string for CRI Id driving-licence-cri-dev and row number 6
    And Driving Licence user sends a POST request to session endpoint
    And Driving Licence user gets a session-id
    When Driving Licence user sends a POST request to Driving Licence endpoint using jsonRequest DVLAValidJsonPayload and document checking route is dcs
    And Driving Licence user gets authorisation code
    And Driving Licence user sends a POST request to Access Token endpoint driving-licence-cri-dev
    Then User requests Driving Licence CRI VC
    And Driving Licence VC should contain validityScore 2 and strengthScore 3
    And Driving Licence VC should contain checkMethod data and identityCheckPolicy published in success checkDetails

  @drivingLicenceCRI_API @pre-merge @dev
  Scenario: Acquire initial JWT and DVA Driving Licence Happy path
    Given Driving Licence user has the user identity in the form of a signed JWT string for CRI Id driving-licence-cri-dev and row number 6
    And Driving Licence user sends a POST request to session endpoint
    And Driving Licence user gets a session-id
    When Driving Licence user sends a POST request to Driving Licence endpoint using jsonRequest DVAValidJsonPayload and document checking route is dcs
    And Driving Licence user gets authorisation code
    And Driving Licence user sends a POST request to Access Token endpoint driving-licence-cri-dev
    Then User requests Driving Licence CRI VC
    And Driving Licence VC should contain validityScore 2 and strengthScore 3
    And Driving Licence VC should contain checkMethod data and identityCheckPolicy published in success checkDetails


  @drivingLicenceCRI_API @pre-merge @dev
  Scenario: DVLA Driving Licence Retry Journey Happy Path
    Given Driving Licence user has the user identity in the form of a signed JWT string for CRI Id driving-licence-cri-dev and row number 6
    And Driving Licence user sends a POST request to session endpoint
    And Driving Licence user gets a session-id
    When Driving Licence user sends a POST request to Driving Licence endpoint using jsonRequest DVLAInvalidJsonPayload and document checking route is dcs
    Then Driving Licence check response should contain Retry value as true
    When Driving Licence user sends a POST request to Driving Licence endpoint using jsonRequest DVLAValidJsonPayload and document checking route is dcs
    And Driving Licence check response should contain Retry value as false
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
    When Driving Licence user sends a POST request to Driving Licence endpoint using jsonRequest DVAInvalidJsonPayload and document checking route is dcs
    Then Driving Licence check response should contain Retry value as true
    When Driving Licence user sends a POST request to Driving Licence endpoint using jsonRequest DVAValidJsonPayload and document checking route is dcs
    And Driving Licence check response should contain Retry value as false
    And Driving Licence user gets authorisation code
    And Driving Licence user sends a POST request to Access Token endpoint driving-licence-cri-dev
    Then User requests Driving Licence CRI VC
    And Driving Licence VC should contain validityScore 2 and strengthScore 3
    And Driving Licence VC should contain checkMethod data and identityCheckPolicy published in success checkDetails

  @drivingLicenceCRI_API @pre-merge @dev
  Scenario Outline: Test Driving Licence API falls back to DCS when an exception occurs in the request and if exception occurs again it is thrown correctly
    Given Driving Licence user has the user identity in the form of a signed JWT string for CRI Id driving-licence-cri-dev and row number 6
    And Driving Licence user sends a POST request to session endpoint
    And Driving Licence user gets a session-id
    When Driving Licence user sends a editable POST request to Driving Licence endpoint using jsonRequest <PassportJsonPayload> with edited fields <jsonEdits> and document checking route is direct
    Then Check response contains unexpected server error exception
    Examples:
      |PassportJsonPayload                   | jsonEdits |
      |DVLAValidJsonPayload                  | {"forenames": [] }  |

#########  DVLA direct connection tests ##########
  @drivingLicenceCRI_API @pre-merge @dev
  Scenario: DVLA Driving Licence Happy path with dvla direct connection as document checking route
    Given Driving Licence user has the user identity in the form of a signed JWT string for CRI Id driving-licence-cri-dev and row number 6
    And Driving Licence user sends a POST request to session endpoint
    And Driving Licence user gets a session-id
    When Driving Licence user sends a POST request to Driving Licence endpoint using jsonRequest DVLAValidKennethJsonPayload and document checking route is direct
    And Driving Licence user gets authorisation code
    And Driving Licence user sends a POST request to Access Token endpoint driving-licence-cri-dev
    Then User requests Driving Licence CRI VC
    And Driving Licence VC should contain validityScore 2 and strengthScore 3
    And Driving Licence VC should contain checkMethod data and identityCheckPolicy published in success checkDetails

  @drivingLicenceCRI_API @pre-merge @dev @dvlaDirect
  Scenario Outline: Test Driving Licence API falls back to DCS when user is marked as unverified during the DVAD fallback window and the DCS request succeeds
    Given Driving Licence user has the user identity in the form of a signed JWT string for CRI Id driving-licence-cri-dev and row number 6
    And Driving Licence user sends a POST request to session endpoint
    And Driving Licence user gets a session-id
    When Driving Licence user sends a POST request to Driving Licence endpoint using jsonRequest <PassportJsonPayload> and document checking route is direct
    And Driving Licence user gets authorisation code
    And Driving Licence user sends a POST request to Access Token endpoint driving-licence-cri-dev
    Then User requests Driving Licence CRI VC
    And Driving Licence VC should contain validityScore 2 and strengthScore 3
    And Driving Licence VC should contain <checkDetails> checkDetails
    Examples:
      |PassportJsonPayload                         | checkDetails |
      |DVLA-DCS-InvalidJsonPayload                 | success      |

  @drivingLicenceCRI_API @pre-merge @dev @dvlaDirect
  Scenario Outline: Test passport API falls back to DCS when the request throws an exception during the DVAD fallback window and the DCS request succeeds
    Given Driving Licence user has the user identity in the form of a signed JWT string for CRI Id driving-licence-cri-dev and row number 6
    And Driving Licence user sends a POST request to session endpoint
    And Driving Licence user gets a session-id
    When Driving Licence user sends a POST request to Driving Licence endpoint using jsonRequest <PassportJsonPayload> and document checking route is direct
    And Driving Licence user gets authorisation code
    And Driving Licence user sends a POST request to Access Token endpoint driving-licence-cri-dev
    Then User requests Driving Licence CRI VC
    And Driving Licence VC should contain validityScore 2 and strengthScore 3
    And Driving Licence VC should contain <checkDetails> checkDetails
    Examples:
      |PassportJsonPayload                             | checkDetails |
      |DVLAServerErrorInvalidJsonPayload               | success      |

  @drivingLicenceCRI_API @pre-merge @dev
  Scenario: DVLA Driving Licence user fails first attempt with dvla direct as document checking route but VC is still created
    Given Driving Licence user has the user identity in the form of a signed JWT string for CRI Id driving-licence-cri-dev and row number 6
    And Driving Licence user sends a POST request to session endpoint
    And Driving Licence user gets a session-id
    When Driving Licence user sends a POST request to Driving Licence endpoint using jsonRequest DVLAInvalidJsonPayload and document checking route is direct
    Then Driving Licence check response should contain Retry value as true
    And Driving Licence user gets authorisation code
    And Driving Licence user sends a POST request to Access Token endpoint driving-licence-cri-dev
    Then User requests Driving Licence CRI VC
    And Driving Licence VC should contain ci D02, validityScore 0 and strengthScore 3
    And Driving Licence VC should contain checkMethod data and identityCheckPolicy published in failed checkDetails

#########  DVA direct connection tests ##########
  @drivingLicenceCRI_API @pre-merge @dev
  Scenario: DVA Driving Licence Happy path with dva direct connection as document checking route
    Given Driving Licence user has the user identity in the form of a signed JWT string for CRI Id driving-licence-cri-dev and row number 6
    And Driving Licence user sends a POST request to session endpoint
    And Driving Licence user gets a session-id
    When Driving Licence user sends a POST request to Driving Licence endpoint using jsonRequest DVAValidKennethJsonPayload and document checking route is direct
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
    When Driving Licence user sends a POST request to Driving Licence endpoint using jsonRequest DVAInvalidJsonPayload and document checking route is direct
    Then Driving Licence check response should contain Retry value as true
    When Driving Licence user sends a POST request to Driving Licence endpoint using jsonRequest DVAValidKennethJsonPayload and document checking route is direct
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
    When Driving Licence user sends a POST request to Driving Licence endpoint using jsonRequest DVAInvalidJsonPayload and document checking route is direct
    Then Driving Licence check response should contain Retry value as true
    And Driving Licence user gets authorisation code
    And Driving Licence user sends a POST request to Access Token endpoint driving-licence-cri-dev
    Then User requests Driving Licence CRI VC
    And Driving Licence VC should contain ci D02, validityScore 0 and strengthScore 3
    And Driving Licence VC should contain checkMethod data and identityCheckPolicy published in failed checkDetails

