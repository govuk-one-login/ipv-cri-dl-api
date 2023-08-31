@DVA_Direct_API
Feature: DVA Direct API

  @drivingLicenceCRI_API @pre-merge @dev
  Scenario: Acquire initial JWT and DVA Direct Happy path
    Given Driving Licence user has the user identity in the form of a signed JWT string for CRI Id driving-licence-cri-shared-dev and row number 6
    And Driving Licence user sends a POST request to session endpoint
    And Driving Licence user gets a session-id
    When Driving Licence user sends a POST request to DVA Direct endpoint using jsonRequest DVAValidJsonPayload
    And Driving Licence user gets authorisation code
    And Driving Licence user sends a POST request to Access Token endpoint driving-licence-cri-shared-dev
    Then User requests Driving Licence CRI VC
    And Driving Licence VC should contain validityScore 2 and strengthScore 3

  @drivingLicenceCRI_API @pre-merge @dev
  Scenario: DVA Retry Journey Happy Path
    Given Driving Licence user has the user identity in the form of a signed JWT string for CRI Id driving-licence-cri-shared-dev and row number 6
    And Driving Licence user sends a POST request to session endpoint
    And Driving Licence user gets a session-id
    When Driving Licence user sends a POST request to DVA Direct endpoint using jsonRequest DVAValidJsonPayload
    Then Driving Licence check response should contain Retry value as true
    When Driving Licence user sends a POST request to DVA Direct endpoint using jsonRequest DVAValidJsonPayload
    And Driving Licence check response should contain Retry value as false
    And Driving Licence user gets authorisation code
    And Driving Licence user sends a POST request to Access Token endpoint driving-licence-cri-shared-dev
    Then User requests Driving Licence CRI VC
    And Driving Licence VC should contain validityScore 2 and strengthScore 3

