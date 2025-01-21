Feature: DVA Auth Source Driving Licence Test

  @build @smoke @stub @staging @integration @uat @traffic
  Scenario Outline: DVA Auth Source - Happy path
    Given I navigate to the IPV Core Stub and select Driving Licence CRI for the testEnvironment
    And I enter the context value <contextValue> in the Input context value as a string
    And I enter the shared claims raw JSON <DVADrivingLicenceAuthSourceSubject> in the Input shared claims raw JSON
    And I add a cookie to change the language to English
    And I check the page title is Check your UK photocard driving licence details – Prove your identity – GOV.UK
    And User clicks selects the Yes Radio Button
    When User clicks on continue
    And I check the page title is We need to check your driving licence details – Prove your identity – GOV.UK
    And User clicks the DVA consent checkbox
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain validity score 2, strength score 3 and type IdentityCheck
    And JSON response should contain personal number <personalNumber> same as given Driving Licence
    And JSON response should contain JTI field
    And The test is complete and I close the driver
    Examples:
      | contextValue  | DVADrivingLicenceAuthSourceSubject   | personalNumber |
      | check_details | DVAAuthSourceValidKennethJsonPayload | 12345678       |

  @build @smoke @stub @staging @integration @uat
  Scenario Outline: DVA Auth Source - Validation Test - Invalid Context Values
    Given I navigate to the IPV Core Stub and select Driving Licence CRI for the testEnvironment
    And I enter the context value <contextValue> in the Input context value as a string
    And I enter the shared claims raw JSON <DVADrivingLicenceAuthSourceSubject> in the Input shared claims raw JSON
    Then I navigate to the Driving Licence verifiable issuer to check for a Invalid response
    And JSON response should contain error description Invalid Context field value and status code as 302
    And The test is complete and I close the driver
    Examples:
      | contextValue  | DVADrivingLicenceAuthSourceSubject |
      | check_detail  | DVAAuthSourceValidBillyJsonPayload |
      | invalid_value | DVAAuthSourceValidBillyJsonPayload |

  @build @smoke @stub @staging @integration @uat @traffic
  Scenario Outline: DVA Auth Source - Validation Test - Missing context field directs to default DVA journey
    Given I navigate to the IPV Core Stub and select Driving Licence CRI for the testEnvironment
    And I enter the context value <contextValue> in the Input context value as a string
    And I enter the shared claims raw JSON <DVADrivingLicenceAuthSourceSubject> in the Input shared claims raw JSON
    And I add a cookie to change the language to English
    Then I check the page title is Who was your UK driving licence issued by? – Prove your identity – GOV.UK
    And I should see DVA as an option
    And I click on DVA radio button and Continue
    And I check the page title is Enter your details exactly as they appear on your UK driving licence – Prove your identity – GOV.UK
    And I see a form requesting DVA LicenceNumber
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain validity score 2, strength score 3 and type IdentityCheck
    And JSON response should contain personal number <personalNumber> same as given Driving Licence
    And JSON response should contain JTI field
    And The test is complete and I close the driver
    Examples:
      | contextValue | DVADrivingLicenceAuthSourceSubject   | personalNumber | DVADrivingLicenceSubject             |
      |              | DVAAuthSourceValidKennethJsonPayload | 12345678       | DVADrivingLicenceSubjectHappyKenneth |

  @build @smoke @stub @staging @integration @uat
  Scenario Outline: DVA Auth Source - Raw JSON Object Validation Tests - Missing Address field in Claimset
    Given I navigate to the IPV Core Stub and select Driving Licence CRI for the testEnvironment
    And I enter the context value <contextValue> in the Input context value as a string
    And I enter the shared claims raw JSON <DVADrivingLicenceAuthSourceSubject> in the Input shared claims raw JSON
    Then I navigate to the Driving Licence verifiable issuer to check for a Invalid response
    And JSON response should contain error description Internal server error and status code as 302
    And The test is complete and I close the driver
    Examples:
      | contextValue  | DVADrivingLicenceAuthSourceSubject     |
      | check_details | DVAAuthSourceInvalidKennethJsonPayload |

  @build @smoke @stub
  Scenario Outline: DVA Auth Source - Negative Scenario - Postcode does not match the DVA Stub expected value
    Given I navigate to the IPV Core Stub and select Driving Licence CRI for the testEnvironment
    And I enter the context value <contextValue> in the Input context value as a string
    And I enter the shared claims raw JSON <DVADrivingLicenceAuthSourceSubject> in the Input shared claims raw JSON
    And I add a cookie to change the language to English
    And I check the page title is Check your UK photocard driving licence details – Prove your identity – GOV.UK
    And User clicks selects the Yes Radio Button
    When User clicks on continue
    And I check the page title is We need to check your driving licence details – Prove your identity – GOV.UK
    And User clicks the DVA consent checkbox
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Invalid response
    And JSON response should contain error description Authorization permission denied and status code as 302
    And The test is complete and I close the driver
    Examples:
      | contextValue  | DVADrivingLicenceAuthSourceSubject   |
      | check_details | DVAAuthSourceInvalidBillyJsonPayload |

  @staging @integration @uat
  Scenario Outline: DVA Auth Source - Negative Scenario UAT Stub - Postcode does not match the DVA Stub expected value
    Given I navigate to the IPV Core Stub and select Driving Licence CRI for the testEnvironment
    And I enter the context value <contextValue> in the Input context value as a string
    And I enter the shared claims raw JSON <DVADrivingLicenceAuthSourceSubject> in the Input shared claims raw JSON
    And I add a cookie to change the language to English
    And I check the page title is Check your UK photocard driving licence details – Prove your identity – GOV.UK
    And User clicks selects the Yes Radio Button
    When User clicks on continue
    And I check the page title is We need to check your driving licence details – Prove your identity – GOV.UK
    And User clicks the DVA consent checkbox
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain validity score 0, strength score 3 and type IdentityCheck
    And JSON response should contain personal number <personalNumber> same as given Driving Licence
    And JSON response should contain JTI field
    And The test is complete and I close the driver
    Examples:
      | contextValue  | DVADrivingLicenceAuthSourceSubject   | personalNumber |
      | check_details | DVAAuthSourceInvalidBillyJsonPayload | 55667788       |

  @build @smoke @stub @staging @integration @uat
  Scenario Outline: DVA Auth Source - Happy path - User selects No on the check your details are correct page
    Given I navigate to the IPV Core Stub and select Driving Licence CRI for the testEnvironment
    And I enter the context value <contextValue> in the Input context value as a string
    And I enter the shared claims raw JSON <DVADrivingLicenceAuthSourceSubject> in the Input shared claims raw JSON
    And I add a cookie to change the language to English
    And I check the page title is Check your UK photocard driving licence details – Prove your identity – GOV.UK
    And User clicks selects the No Radio Button
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Invalid response
    And JSON response should contain error description Authorization permission denied and status code as 302
    And The test is complete and I close the driver
    Examples:
      | contextValue  | DVADrivingLicenceAuthSourceSubject   |
      | check_details | DVAAuthSourceValidBillyJsonPayload   |
      | check_details | DVAAuthSourceValidKennethJsonPayload |

  @build @smoke @stub @staging @integration @uat
  Scenario Outline: DVA Auth Source - Error Validation Text - Fail to provide consent
    Given I navigate to the IPV Core Stub and select Driving Licence CRI for the testEnvironment
    And I enter the context value <contextValue> in the Input context value as a string
    And I enter the shared claims raw JSON <DVADrivingLicenceAuthSourceSubject> in the Input shared claims raw JSON
    And I add a cookie to change the language to English
    And I check the page title is Check your UK photocard driving licence details – Prove your identity – GOV.UK
    And User clicks selects the Yes Radio Button
    When User clicks on continue
    And I check the page title is We need to check your driving licence details – Prove your identity – GOV.UK
    When User clicks on continue
    And I see the DVA give your consent error in the summary as Error: You must give your consent to continue
    And The test is complete and I close the driver
    Examples:
      | contextValue  | DVADrivingLicenceAuthSourceSubject   |
      | check_details | DVAAuthSourceValidBillyJsonPayload   |
      | check_details | DVAAuthSourceValidKennethJsonPayload |
