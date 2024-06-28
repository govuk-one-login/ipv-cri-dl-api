@drivinglicence_CRI_API
Feature: DVA CRI API

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

  #########  Direct connection tests ##########

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
