Feature: DVLA Auth Source Driving Licence Test

  @build @smoke @stub @staging @integration @uat @traffic
  Scenario Outline: DVLA Auth Source - Happy path
    Given I navigate to the IPV Core Stub and select Driving Licence CRI for the testEnvironment
    And I enter the context value <contextValue> in the Input context value as a string
    And I enter the shared claims raw JSON <DVLADrivingLicenceAuthSourceSubject> in the Input shared claims raw JSON
    And I add a cookie to change the language to English
    And I check the page title is Check your UK photocard driving licence details – Prove your identity – GOV.UK
    And User clicks selects the Yes Radio Button
    When User clicks on continue
    And I check the page title is We need to check your driving licence details – Prove your identity – GOV.UK
    And User clicks the DVLA consent checkbox
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain validity score 2, strength score 3 and type IdentityCheck
    And JSON response should contain personal number DECER607085K99AE same as given Driving Licence
    And JSON response should contain JTI field
    And The test is complete and I close the driver
    Examples:
      | contextValue  | DVLADrivingLicenceAuthSourceSubject   |
      | check_details | DVLAAuthSourceValidKennethJsonPayload |

  @build @smoke @stub @staging @integration @uat
  Scenario Outline: DVLA Auth Source - Validation Test - Invalid Context Values
    Given I navigate to the IPV Core Stub and select Driving Licence CRI for the testEnvironment
    And I enter the context value <contextValue> in the Input context value as a string
    And I enter the shared claims raw JSON <DVLADrivingLicenceAuthSourceSubject> in the Input shared claims raw JSON
    Then I navigate to the Driving Licence verifiable issuer to check for a Invalid response
    And JSON response should contain error description Invalid Context field value and status code as 302
    And The test is complete and I close the driver
    Examples:
      | contextValue  | DVLADrivingLicenceAuthSourceSubject   |
      | check_detail  | DVLAAuthSourceValidKennethJsonPayload |
      | invalid_value | DVLAAuthSourceValidKennethJsonPayload |

  @build @smoke @stub @staging @integration @uat @traffic
  Scenario Outline: DVLA Auth Source - Validation Test - Missing context field directs to default DVLA journey
    Given I navigate to the IPV Core Stub and select Driving Licence CRI for the testEnvironment
    And I enter the context value <contextValue> in the Input context value as a string
    And I enter the shared claims raw JSON <DVLADrivingLicenceAuthSourceSubject> in the Input shared claims raw JSON
    And I add a cookie to change the language to English
    Then I check the page title is Was your UK photocard driving licence issued by DVLA or DVA? – Prove your identity – GOV.UK
    And I should see DVLA as an option
    And I click on DVLA radio button and Continue
    And I check the page title is Enter your details exactly as they appear on your UK driving licence – Prove your identity – GOV.UK
    And I see a form requesting DVLA LicenceNumber
    Given User enters DVLA data as a <DVLADrivingLicenceSubject>
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain validity score 2, strength score 3 and type IdentityCheck
    And JSON response should contain personal number <personalNumber> same as given Driving Licence
    And JSON response should contain JTI field
    And The test is complete and I close the driver
    Examples:
      | contextValue | DVLADrivingLicenceAuthSourceSubject   | personalNumber   | DVLADrivingLicenceSubject         |
      |              | DVLAAuthSourceValidKennethJsonPayload | DECER607085K99AE | DrivingLicenceSubjectHappyKenneth |

  @build @smoke @stub @staging @integration @uat
  Scenario Outline: DVLA Auth Source - Raw JSON Object Validation Tests - Missing Address field in Claimset
    Given I navigate to the IPV Core Stub and select Driving Licence CRI for the testEnvironment
    And I enter the context value <contextValue> in the Input context value as a string
    And I enter the shared claims raw JSON <DVLADrivingLicenceAuthSourceSubject> in the Input shared claims raw JSON
    Then I navigate to the Driving Licence verifiable issuer to check for a Invalid response
    And JSON response should contain error description Unexpected server error and status code as 302
    And The test is complete and I close the driver
    Examples:
      | contextValue  | DVLADrivingLicenceAuthSourceSubject              |
      | check_details | DVLAAuthSourceInvalidKennethJsonPayloadNoAddress |


  @build @smoke @stub @staging @integration @uat
  Scenario Outline: DVLA Auth Source - Happy path
    Given I navigate to the IPV Core Stub and select Driving Licence CRI for the testEnvironment
    And I enter the context value <contextValue> in the Input context value as a string
    And I enter the shared claims raw JSON <DVLADrivingLicenceAuthSourceSubject> in the Input shared claims raw JSON
    And I add a cookie to change the language to English
    And I check the page title is Check your UK photocard driving licence details – Prove your identity – GOV.UK
    And User clicks selects the No Radio Button
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Invalid response
    And JSON response should contain error description Authorization permission denied and status code as 302
    And The test is complete and I close the driver
    Examples:
      | contextValue  | DVLADrivingLicenceAuthSourceSubject   |
      | check_details | DVLAAuthSourceValidKennethJsonPayload |


  @build @smoke @stub @staging @integration @uat
  Scenario Outline: DVLA Auth Source - Error Validation Text - Fail to provide consent
    Given I navigate to the IPV Core Stub and select Driving Licence CRI for the testEnvironment
    And I enter the context value <contextValue> in the Input context value as a string
    And I enter the shared claims raw JSON <DVLADrivingLicenceAuthSourceSubject> in the Input shared claims raw JSON
    And I add a cookie to change the language to English
    And I check the page title is Check your UK photocard driving licence details – Prove your identity – GOV.UK
    And User clicks selects the Yes Radio Button
    When User clicks on continue
    And I check the page title is We need to check your driving licence details – Prove your identity – GOV.UK
    When User clicks on continue
    And I see the give your consent error in the summary as Error: You must give your consent to continue
    And The test is complete and I close the driver
    Examples:
      | contextValue  | DVLADrivingLicenceAuthSourceSubject   |
      | check_details | DVLAAuthSourceValidKennethJsonPayload |
