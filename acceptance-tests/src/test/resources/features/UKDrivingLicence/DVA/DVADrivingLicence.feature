@QualityGateSmokeTest @QualityGateRegressionTest @QualityGateIntegrationTest
Feature: DVA Driving Licence Test

  Background:
    Given I navigate to the IPV Core Stub
    And I click the Driving Licence CRI for the testEnvironment
    And I search for Driving Licence user number 5 in the Experian table
    And I assert the url path contains licence-issuer
    And I add a cookie to change the language to English
    Then I check the page title is Was your UK photocard driving licence issued by DVLA or DVA? – GOV.UK One Login
    And I should see DVA as an option
    And I click on DVA radio button and continue
    And I check the page title is Enter your details exactly as they appear on your UK driving licence – GOV.UK One Login
    And I see a form requesting DVA LicenceNumber

  @smoke-build @stub @uat @traffic
  Scenario Outline: DVA - Happy path
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain validity score 2, strength score 3 and type IdentityCheck
    And JSON response should contain personal number 12345678 same as given Driving Licence
    And JSON response should contain JTI field
    And The test is complete and I close the driver

    Examples:
      | DVADrivingLicenceSubject             |
      | DVADrivingLicenceSubjectHappyKenneth |

  @dev
  Scenario Outline: DVA - User attempts journey with invalid details and returns authorisation error
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Invalid response
    And JSON response should contain error description Authorization permission denied and status code as 302
    And The test is complete and I close the driver

    Examples:
      | DVADrivingLicenceSubject              |
      | DVADrivingLicenceSubjectUnhappySelina |

  @stub @traffic
  Scenario Outline: DVA - User enters invalid driving licence number
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    And User re-enters DVA license number as <InvalidLicenceNumber>
    When User clicks on continue
    Then Proper error message for dva Could not find your details is displayed
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci D02, validity score 0, strength score 3 and type IdentityCheck
    And JSON response should contain personal number 88776655 same as given Driving Licence
    And The test is complete and I close the driver

    Examples:
      | DVADrivingLicenceSubject           | InvalidLicenceNumber |
      | DVADrivingLicenceSubjectHappyBilly | 88776655             |

  @stub @uat @traffic
  Scenario Outline: DVA - User enters invalid first name and returns could not find your details error message
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    And User re-enters first name as <InvalidFirstName>
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci D02, validity score 0, strength score 3 and type IdentityCheck
    And The test is complete and I close the driver

    Examples:
      | DVADrivingLicenceSubject           | InvalidFirstName |
      | DVADrivingLicenceSubjectHappyBilly | SELINA           |

  @stub @uat @traffic
  Scenario Outline: DVA - User enters invalid last name and returns could not find your details error message
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    And User re-enters last name as <InvalidLastName>
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci D02, validity score 0, strength score 3 and type IdentityCheck
    And The test is complete and I close the driver

    Examples:
      | DVADrivingLicenceSubject           | InvalidLastName |
      | DVADrivingLicenceSubjectHappyBilly | KYLE            |

  @stub @traffic
  Scenario Outline: DVA - User enters invalid issue date and returns could not find your details error message
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    And User re-enters DVA issue day as <InvalidLicenceIssueDay>
    And User re-enters DVA issue month as <InvalidLicenceIssueMonth>
    And User re-enters DVA issue year as <InvalidLicenceIssueYear>
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci D02, validity score 0, strength score 3 and type IdentityCheck
    And The test is complete and I close the driver

    Examples:
      | DVADrivingLicenceSubject           | InvalidLicenceIssueDay | InvalidLicenceIssueMonth | InvalidLicenceIssueYear |
      | DVADrivingLicenceSubjectHappyBilly | 14                     | 09                       | 2019                    |

  @stub @traffic
  Scenario Outline: DVA - User enters invalid valid-to date and returns could not find your details error message
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    And User re-enters valid to day as <InvalidValidToDay>
    And User re-enters valid to month as <InvalidValidToMonth>
    And User re-enters valid to year as <InvalidValidToYear>
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci D02, validity score 0, strength score 3 and type IdentityCheck
    And The test is complete and I close the driver

    Examples:
      | DVADrivingLicenceSubject           | InvalidValidToDay | InvalidValidToMonth | InvalidValidToYear |
      | DVADrivingLicenceSubjectHappyBilly | 04                | 08                  | 2032               |

  @stub @traffic
  Scenario Outline: DVA - User enters invalid postcode and returns could not find your details error message
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    Given User re-enters postcode as <InvalidPostcode>
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci D02, validity score 0, strength score 3 and type IdentityCheck
    And The test is complete and I close the driver

    Examples:
      | DVADrivingLicenceSubject           | InvalidPostcode |
      | DVADrivingLicenceSubjectHappyBilly | E20 2AQ         |

  @stub @traffic
  Scenario Outline: DVA - User attempts invalid journey and retries with valid details
    Given User enters invalid Driving Licence DVA details
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User Re-enters DVA data as a <DVADrivingLicenceSubject>
    And User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain validity score 2, strength score 3 and type IdentityCheck
    And The test is complete and I close the driver

    Examples:
      | DVADrivingLicenceSubject           |
      | DVADrivingLicenceSubjectHappyBilly |

  @stub @traffic
  Scenario Outline: DVA - User attempts invalid journey and retries with invalid details
    Given User enters invalid Driving Licence DVA details
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User Re-enters DVA data as a <DVADrivingLicenceSubject>
    And User re-enters DVA license number as <InvalidLicenceNumber>
    And User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci D02, validity score 0, strength score 3 and type IdentityCheck
    And The test is complete and I close the driver

    Examples:
      | DVADrivingLicenceSubject           | InvalidLicenceNumber |
      | DVADrivingLicenceSubjectHappyBilly | 88776655             |

  @stub @uat @traffic
  Scenario: DVA - User attempts invalid journey and cancels after first attempt
    Given User enters invalid Driving Licence DVA details
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User click on ‘prove your identity another way' Link
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci D02, validity score 0, strength score 3 and type IdentityCheck
    And The test is complete and I close the driver

  @stub
  Scenario: DVA - User cancels before first attempt by clicking prove another way and returns an authorisation error
    Given User click on ‘prove your identity another way' Link
    Then I navigate to the Driving Licence verifiable issuer to check for a Invalid response
    And JSON response should contain error description Authorization permission denied and status code as 302
    And The test is complete and I close the driver

  @dev
  Scenario: DVA - User cancels before first attempt by clicking no driving licence and returns an authorisation error
    Given User click on ‘Back' Link
    When User click on I do not have a UK driving licence radio button
    Then I navigate to the Driving Licence verifiable issuer to check for a Invalid response
    And JSON response should contain error description Authorization permission denied and status code as 302
    And The test is complete and I close the driver

  @stub
  Scenario Outline: DVA - User enters invalid details and returns enter your details as it appears error message
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    And User re-enters last name as <InvalidLastName>
    And User re-enters first name as <InvalidFirstName>
    And User re-enters DVA birth day as <InvalidBirthDay>
    And User re-enters DVA birth month as <InvalidBirthMonth>
    And User re-enters DVA birth year as <InvalidBirthYear>
    And User re-enters DVA issue day as <InvalidIssueDay>
    And User re-enters DVA issue month as <InvalidIssueMonth>
    And User re-enters DVA issue year as <InvalidIssueYear>
    And User re-enters valid to day as <InvalidValidToDay>
    And User re-enters valid to month as <InvalidValidToMonth>
    And User re-enters valid to year as <InvalidValidToYear>
    And User re-enters DVA license number as <InvalidLicenceNumber>
    And User re-enters postcode as <InvalidPostCode>
    When User clicks on continue
    Then I check the page title is Error: Enter your details exactly as they appear on your UK driving licence – GOV.UK One Login
    And The test is complete and I close the driver

    Examples:
      | DVADrivingLicenceSubject           | InvalidLastName | InvalidFirstName | InvalidBirthDay | InvalidBirthMonth | InvalidBirthYear | InvalidIssueDay | InvalidIssueMonth | InvalidIssueYear | InvalidValidToDay | InvalidValidToMonth | InvalidValidToYear | InvalidLicenceNumber | InvalidPostCode |
      | DVADrivingLicenceSubjectHappyBilly |                 | PETER            | 11              | 10                | 1962             | 23              | 05                | 2018             | 09                | 12                  | 2062               | PARKE610112PBFGH     | NW3 5RG         |
      | DVADrivingLicenceSubjectHappyBilly | PARKER          |                  | 11              | 10                | 1962             | 23              | 05                | 2018             | 09                | 12                  | 2062               | PARKE610112PBFGH     | NW3 5RG         |
      | DVADrivingLicenceSubjectHappyBilly | PARKER          | PETER            |                 |                   |                  | 23              | 05                | 2018             | 09                | 12                  | 2062               | PARKE610112PBFGH     | NW3 5RG         |
      | DVADrivingLicenceSubjectHappyBilly | PARKER          | PETER            | 11              | 10                | 1962             |                 |                   |                  | 09                | 12                  | 2062               | PARKE610112PBFGH     | NW3 5RG         |
      | DVADrivingLicenceSubjectHappyBilly | PARKER          | PETER            | 11              | 10                | 1962             | 23              | 05                | 2018             |                   |                     |                    | PARKE610112PBFGH     | NW3 5RG         |
      | DVADrivingLicenceSubjectHappyBilly | PARKER          | PETER            | 11              | 10                | 1962             | 23              | 05                | 2018             | 09                | 12                  | 2062               |                      | NW3 5RG         |
      | DVADrivingLicenceSubjectHappyBilly | PARKER          | PETER            | 11              | 10                | 1962             | 23              | 05                | 2018             | 09                | 12                  | 2062               | PARKE610112PBFGH     | NW3 5RG         |
      | DVADrivingLicenceSubjectHappyBilly | PARKER          | PETER            | 11              | 10                | 1962             | 23              | 05                | 2018             | 09                | 12                  | 2062               | PARKE610112PBFGH     |                 |
      | DVADrivingLicenceSubjectHappyBilly | PARKER          | PETER987         | 11              | 10                | 1962             | 23              | 05                | 2018             | 09                | 12                  | 2062               | PARKE610112PBFGH     | NW3 5RG         |
      | DVADrivingLicenceSubjectHappyBilly | PARKER          | PETER%$@         | 11              | 10                | 1962             | 23              | 05                | 2018             | 09                | 12                  | 2062               | PARKE610112PBFGH     | NW3 5RG         |
      | DVADrivingLicenceSubjectHappyBilly | PARKER          | PETER            | @               | *&                | 19 7             | 23              | 05                | 2018             | 09                | 12                  | 2062               | PARKE610112PBFGH     | NW3 5RG         |
      | DVADrivingLicenceSubjectHappyBilly | PARKER          | PETER            | 51              | 71                | 198              | 23              | 05                | 2018             | 09                | 12                  | 2062               | PARKE610112PBFGH     | NW3 5RG         |
      | DVADrivingLicenceSubjectHappyBilly | PARKER          | PETER            | 11              | 10                | 1962             | &               | ^%                | %$ ^             | 09                | 12                  | 2062               | PARKE610112PBFGH     | NW3 5RG         |
      | DVADrivingLicenceSubjectHappyBilly | PARKER          | PETER            | 11              | 10                | 1962             | 23              | 05                | 2018             | !@                | %$                  | %^ *               | PARKE610112PBFGH     | NW3 5RG         |
      | DVADrivingLicenceSubjectHappyBilly | PARKER          | PETER            | 11              | 10                | 1962             | 23              | 05                | 2018             | 10                | 01                  | 2010               | PARKE610112PBFGH     | NW3 5RG         |
      | DVADrivingLicenceSubjectHappyBilly | PARKER          | PETER            | 11              | 10                | 1962             | 23              | 05                | 2018             | 09                | 12                  | 2062               | PARKE610112PB^&*     | NW3 5RG         |
      | DVADrivingLicenceSubjectHappyBilly | PARKER          | PETER            | 11              | 10                | 1962             | 23              | 05                | 2018             | 09                | 12                  | 2062               | PARKE610112PBFGH     | NW* ^%G         |
      | DVADrivingLicenceSubjectHappyBilly | PARKER          | PETER            | 11              | 10                | 1962             | 23              | 05                | 2018             | 09                | 12                  | 2062               | PARKE610112PBFGH     | CA 95128        |
# ##########  DVA Field Validations ##########

  # not existing in front end repo
  @uat @stub
  Scenario: DVA - User consents to have DL checked and navigates to DVA privacy notice
    Then I see the DVA consent section Allow DVA to check your driving licence details
    And I see the Consent sentence in DVA page DVA needs your consent to check your driving licence details before you can continue. They will make sure your licence has not been cancelled or reported as lost or stolen.
    And I see the Consent second line in DVA page To find out more about how your driving licence details will be used, you can read:
    And I see privacy DVA notice link the GOV.UK One Login privacy notice (opens in a new tab)
    Then I see the DVA privacy notice link the DVA privacy notice (opens in a new tab)
    And The test is complete and I close the driver

  # not existing in front end repo
  @uat @traffic
  Scenario Outline: DVA - User attempts journey with invalid details and clicks on prove another way and generates a VC
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    And User re-enters DVA license number as <InvalidLicenceNumber>
    When User clicks on continue
    And I check the page title is Enter your details exactly as they appear on your UK driving licence – GOV.UK One Login
    When User click on ‘prove your identity another way' Link
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON response should contain personal number 88776655 same as given Driving Licence
    And The test is complete and I close the driver

    Examples:
      | DVADrivingLicenceSubject           | InvalidLicenceNumber |
      | DVADrivingLicenceSubjectHappyBilly | 88776655             |

  @stub @uat
  Scenario Outline: DVA - User attempts journey with consent checkbox unselected and returns error
    Given User enters DVA data as a <DrivingLicenceSubject>
    And DVA consent checkbox is unselected
    When User clicks on continue
    When I can see an error box highlighted red
    And I check the page title is Enter your details exactly as they appear on your UK driving licence – GOV.UK One Login
    Then User can see the DVA consent error in summary as You must give your consent to continue
    And User can see the DVA consent error on the checkbox as Error:You must give your consent to continue
    And The test is complete and I close the driver

    Examples:
      | DrivingLicenceSubject              |
      | DVADrivingLicenceSubjectHappyBilly |

  @stub @Language-regression @traffic
  Scenario Outline: Language Title validation
    Given User clicks on language toggle and switches to Welsh
    Then I check the page title is Rhowch eich manylion yn union fel maent yn ymddangos ar eich trwydded yrru – GOV.UK One Login
    Then User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain validity score 2, strength score 3 and type IdentityCheck
    And JSON response should contain personal number 12345678 same as given Driving Licence
    And JSON response should contain JTI field
    And The test is complete and I close the driver

    Examples:
      | DVADrivingLicenceSubject             |
      | DVADrivingLicenceSubjectHappyKenneth |
