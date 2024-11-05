Feature: DVLA Auth Source Driving Licence Test

#  @staging @integration @uat
  @build @smoke @stub
  Scenario Outline: DVLA Auth Source - Happy path
    Given I navigate to the IPV Core Stub and select Driving Licence CRI for the testEnvironment
    And I enter the context value check_details in the Input context value as a string
    And I enter the shared claims raw JSON <DVADrivingLicenceAuthSourceSubject> in the Input shared claims raw JSON
    And I add a cookie to change the language to English
    And I check the page title is Check your UK photocard driving licence details – Prove your identity – GOV.UK
    And User clicks selects the Radio Button
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain validity score 2, strength score 3 and type IdentityCheck
    And JSON response should contain personal number DECER607085K99AE same as given Driving Licence
    And JSON response should contain JTI field
    And The test is complete and I close the driver
    Examples:
      | DVADrivingLicenceAuthSourceSubject    |
      | DVLAAuthSourceValidKennethJsonPayload |
