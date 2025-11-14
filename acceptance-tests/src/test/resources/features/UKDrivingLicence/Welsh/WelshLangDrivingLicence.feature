Feature: Driving License Language Test

  Background:
    Given I navigate to the IPV Core Stub
    And I click the Driving Licence CRI for the testEnvironment
    Then I search for Driving Licence user number 5 in the Experian table
    And I assert the url path contains licence-issuer
    And I add a cookie to change the language to Welsh
    And I assert the URL is valid in Welsh

  @Language-regression
  Scenario Outline: Language Title validation Welsh DVLA
    Given I click on DVLA radio button and Parhau
    Then User clicks language toggle and switches to English
    And I check the page title is Enter your details exactly as they appear on your UK driving licence – GOV.UK One Login
    Then User enters DVLA data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain validity score 2, strength score 3 and type IdentityCheck
    And JSON response should contain personal number DECER607085K99AE same as given Driving Licence
    And The test is complete and I close the driver

    Examples:
      | DrivingLicenceSubject             |
      | DrivingLicenceSubjectHappyKenneth |

  @Language-regression
  Scenario Outline: Language Title validation Welsh DVA
    Given I click on DVA radio button and Parhau
    Then User clicks language toggle and switches to English
    And I check the page title is Enter your details exactly as they appear on your UK driving licence – GOV.UK One Login
    Then User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain validity score 2, strength score 3 and type IdentityCheck
    And JSON response should contain personal number 55667788 same as given Driving Licence
    And JSON response should contain JTI field
    And The test is complete and I close the driver

    Examples:
      | DVADrivingLicenceSubject           |
      | DVADrivingLicenceSubjectHappyBilly |
