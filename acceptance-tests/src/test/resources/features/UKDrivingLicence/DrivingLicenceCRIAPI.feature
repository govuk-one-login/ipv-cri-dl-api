@drivinglicence_CRI_API
Feature: DrivingLicence CRI API

  @intialJWT_DVLA_happy_path @drivingLicenceCRI_API @pre-merge @dev
  Scenario: Acquire initial JWT and DVLA Driving Licence Happy path
    Given Driving Licence user has the user identity in the form of a signed JWT string for CRI Id driving-licence-cri-dev and row number 6
    And Driving Licence user sends a POST request to session endpoint
    And Driving Licence user gets a session-id
    When Driving Licence user sends a POST request to Driving Licence endpoint using jsonRequest DVLAValidJsonPayload
    And Driving Licence user gets authorisation code
    And Driving Licence user sends a POST request to Access Token endpoint driving-licence-cri-dev
    Then User requests Driving Licence CRI VC
    And Driving Licence VC should contain validityScore 2 and strengthScore 3

  @drivingLicenceCRI_API @pre-merge @dev
  Scenario: Acquire initial JWT and DVA Driving Licence Happy path
    Given Driving Licence user has the user identity in the form of a signed JWT string for CRI Id driving-licence-cri-dev and row number 6
    And Driving Licence user sends a POST request to session endpoint
    And Driving Licence user gets a session-id
    When Driving Licence user sends a POST request to Driving Licence endpoint using jsonRequest DVAValidJsonPayload
    And Driving Licence user gets authorisation code
    And Driving Licence user sends a POST request to Access Token endpoint driving-licence-cri-dev
    Then User requests Driving Licence CRI VC
    And Driving Licence VC should contain validityScore 2 and strengthScore 3

  @drivingLicenceCRI_API @pre-merge @dev
  Scenario: DVLA Driving Licence Retry Journey Happy Path
    Given Driving Licence user has the user identity in the form of a signed JWT string for CRI Id driving-licence-cri-dev and row number 6
    And Driving Licence user sends a POST request to session endpoint
    And Driving Licence user gets a session-id
    When Driving Licence user sends a POST request to Driving Licence endpoint using jsonRequest DVLAInvalidJsonPayload
    Then Driving Licence check response should contain Retry value as true
    When Driving Licence user sends a POST request to Driving Licence endpoint using jsonRequest DVLAValidJsonPayload
    And Driving Licence check response should contain Retry value as false
    And Driving Licence user gets authorisation code
    And Driving Licence user sends a POST request to Access Token endpoint driving-licence-cri-dev
    Then User requests Driving Licence CRI VC
    And Driving Licence VC should contain validityScore 2 and strengthScore 3

  @drivingLicenceCRI_API @pre-merge @dev
  Scenario: DVA Driving Licence Retry Journey Happy Path
    Given Driving Licence user has the user identity in the form of a signed JWT string for CRI Id driving-licence-cri-dev and row number 6
    And Driving Licence user sends a POST request to session endpoint
    And Driving Licence user gets a session-id
    When Driving Licence user sends a POST request to Driving Licence endpoint using jsonRequest DVAInvalidJsonPayload
    Then Driving Licence check response should contain Retry value as true
    When Driving Licence user sends a POST request to Driving Licence endpoint using jsonRequest DVAValidJsonPayload
    And Driving Licence check response should contain Retry value as false
    And Driving Licence user gets authorisation code
    And Driving Licence user sends a POST request to Access Token endpoint driving-licence-cri-dev
    Then User requests Driving Licence CRI VC
    And Driving Licence VC should contain validityScore 2 and strengthScore 3

## Commenting these tests out until the endpoint is available
## These scenarios are to be tested with dvlaDirectEnabled parameter set to true as well when set to false
#  @drivingLicenceCRI_API @LIME-493
#  Scenario Outline: Acquire initial JWT and DVLA Driving Licence Happy path with dvla direct connection as document checking route
#    Given Driving Licence user has the user identity in the form of a signed JWT string for CRI Id driving-licence-cri-dev and row number 6
#    And Driving Licence user sends a POST request to session endpoint
#    And Driving Licence user gets a session-id
#    When Driving Licence user sends a POST request to DL endpoint using jsonRequest DVLAValidJsonPayload and document checking route is <docCheckRoute>
#    And Driving Licence user gets authorisation code
#    And Driving Licence user sends a POST request to Access Token endpoint driving-licence-cri-dev
#    Then User requests Driving Licence CRI VC
#    And Driving Licence VC should contain validityScore 2 and strengthScore 3
#    Examples:
#      |docCheckRoute|
#      |direct       |
#      |not-provided |
#
## These scenarios are to be tested with dvaDirectEnabled parameter set to true as well when set to false
#  @drivingLicenceCRI_API @LIME-493
#  Scenario Outline: Acquire initial JWT and DVA Driving Licence Happy path with dva direct connection as document checking route
#    Given Driving Licence user has the user identity in the form of a signed JWT string for CRI Id driving-licence-cri-dev and row number 6
#    And Driving Licence user sends a POST request to session endpoint
#    And Driving Licence user gets a session-id
#    When Driving Licence user sends a POST request to DL endpoint using jsonRequest DVAValidJsonPayload and document checking route is <docCheckRoute>
#    And Driving Licence user gets authorisation code
#    And Driving Licence user sends a POST request to Access Token endpoint driving-licence-cri-dev
#    Then User requests Driving Licence CRI VC
#    And Driving Licence VC should contain validityScore 2 and strengthScore 3
#    Examples:
#      |docCheckRoute|
#      |direct       |
#      |not-provided |