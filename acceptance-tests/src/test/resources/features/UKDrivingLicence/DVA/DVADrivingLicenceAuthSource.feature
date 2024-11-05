Feature: DVA Auth Source Driving Licence Test

#  @staging @integration
  @build @smoke @stub @uat
  Scenario Outline: DVA Auth Source - Happy path
    Given I navigate to the IPV Core Stub and select Driving Licence CRI for the testEnvironment
    And I enter the context value <contextValue> in the Input context value as a string
    And I enter the shared claims raw JSON <DVADrivingLicenceAuthSourceSubject> in the Input shared claims raw JSON
    And I add a cookie to change the language to English
    And I check the page title is Check your UK photocard driving licence details – Prove your identity – GOV.UK
    And User clicks selects the Radio Button
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain validity score 2, strength score 3 and type IdentityCheck
    And JSON response should contain personal number <personalNumber> same as given Driving Licence
    And JSON response should contain JTI field
    And The test is complete and I close the driver
    Examples:
      | contextValue  | DVADrivingLicenceAuthSourceSubject   | personalNumber |
      | check_details | DVAAuthSourceValidBillyJsonPayload   | 55667788       |
      | check_details | DVAAuthSourceValidKennethJsonPayload | 12345678       |
