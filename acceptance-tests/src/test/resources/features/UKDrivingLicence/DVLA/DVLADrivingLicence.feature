Feature: Driving Licence Test

  Background:
    Given I navigate to the IPV Core Stub
    And I click the Driving Licence CRI for the testEnvironment
    And I search for Driving Licence user number 5 in the Experian table
    Then I check the page title is Who was your UK driving licence issued by? – Prove your identity – GOV.UK
    And I should see DVLA as an option
    And I click on DVLA radio button and Continue
    And I check the page title is Enter your details exactly as they appear on your UK driving licence – Prove your identity – GOV.UK
    And I see a form requesting DVLA LicenceNumber

  @build @staging @integration @smoke @stub @uat
  Scenario Outline: DVLA - Happy path
    Given User enters DVLA data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain validity score 2, strength score 3 and type IdentityCheck
    And JSON response should contain personal number DECER607085K99AE same as given Driving Licence
    And The test is complete and I close the driver
    Examples:
      | DrivingLicenceSubject             |
      | DrivingLicenceSubjectHappyKenneth |

  @build @staging @integration @stub @uat
  Scenario Outline: DVLA - User enters invalid driving licence number
    Given User enters DVLA data as a <DrivingLicenceSubject>
    And User re-enters license number as <InvalidLicenceNumber>
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci D02, validity score 0, strength score 3 and type IdentityCheck
    And JSON response should contain personal number PARKE610112PBFGI same as given Driving Licence
    And The test is complete and I close the driver
    Examples:
      | DrivingLicenceSubject           | InvalidLicenceNumber |
      | DrivingLicenceSubjectHappyPeter | PARKE610112PBFGI     |

  @build @staging @integration @smoke @stub @uat
  Scenario Outline: DVLA - User enters driving licence number and date of birth in incorrect format which returns validation error
    Given User enters DVLA data as a <DrivingLicenceSubject>
    And User re-enters license number as <InvalidLicenceNumber>
    When User clicks on continue
    Then I see the licence number error in the summary as Enter the number exactly as it appears on your driving licence
    #And I see check date of birth sentence as Error:Check you have entered your date of birth correctly
    And The test is complete and I close the driver
    Examples:
      | DrivingLicenceSubject           | InvalidLicenceNumber |
      | DrivingLicenceSubjectHappyPeter | 1234567890111213     |

  @build @staging @integration @stub @uat
  Scenario Outline: DVLA - User enters invalid date of birth and returns field validation error
    Given User enters DVLA data as a <DrivingLicenceSubject>
    And User re-enters birth day as <InvalidBirthDay>
    And User re-enters birth month as <InvalidBirthMonth>
    And User re-enters birth year as <InvalidBirthYear>
    When User clicks on continue
    Then I see check date of birth sentence as Check you have entered your date of birth correctly
    Then I see enter the date as it appears above the field as Error:Check you have entered your date of birth correctly
    And The test is complete and I close the driver
    Examples:
      | DrivingLicenceSubject             | InvalidBirthDay | InvalidBirthMonth | InvalidBirthYear |
      | DrivingLicenceSubjectHappyKenneth | 12              | 08                | 1985             |

  @build @staging @integration @stub @uat
  Scenario Outline: DVLA - User enters invalid last name and returns could not find your details error message
    Given User enters DVLA data as a <DrivingLicenceSubject>
    And User re-enters last name as <InvalidLastName>
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci D02, validity score 0, strength score 3 and type IdentityCheck
    And The test is complete and I close the driver
    Examples:
      | DrivingLicenceSubject             | InvalidLastName |
      | DrivingLicenceSubjectHappyKenneth | KYLE            |

  @build @staging @integration @stub @uat
  Scenario Outline: DVLA - User enters invalid issue date and returns could not find your details error message
    Given User enters DVLA data as a <DrivingLicenceSubject>
    And User re-enters issue day as <InvalidLicenceIssueDay>
    And User re-enters issue month as <InvalidLicenceIssueMonth>
    And User re-enters issue year as <InvalidLicenceIssueYear>
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci D02, validity score 0, strength score 3 and type IdentityCheck
    And The test is complete and I close the driver
    Examples:
      | DrivingLicenceSubject             | InvalidLicenceIssueDay | InvalidLicenceIssueMonth | InvalidLicenceIssueYear |
      | DrivingLicenceSubjectHappyKenneth | 14                     | 09                       | 2019                    |

  @build @staging @integration @stub @uat
  Scenario Outline: DVLA - User enters invalid valid-to date and returns could not find your details error message
    Given User enters DVLA data as a <DrivingLicenceSubject>
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
      | DrivingLicenceSubject             | InvalidValidToDay | InvalidValidToMonth | InvalidValidToYear |
      | DrivingLicenceSubjectHappyKenneth | 04                | 08                  | 2032               |

  @build @staging @integration @stub @uat
  Scenario Outline: DVLA - User enters invalid issue number and returns could not find your details error message
    Given User enters DVLA data as a <DrivingLicenceSubject>
    And User re-enters issue day as <InvalidLicenceIssueDay>
    And User re-enters issue month as <InvalidLicenceIssueMonth>
    And User re-enters issue year as <InvalidLicenceIssueYear>
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci D02, validity score 0, strength score 3 and type IdentityCheck
    And The test is complete and I close the driver
    Examples:
      | DrivingLicenceSubject             | InvalidLicenceIssueDay | InvalidLicenceIssueMonth | InvalidLicenceIssueYear |
      | DrivingLicenceSubjectHappyKenneth | 14                     | 09                       | 2019                    |

  @build @staging @integration @smoke @stub @uat
  Scenario Outline: DVLA - User attempts invalid journey and retries with valid details
    Given User enters invalid Driving Licence DVLA details
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User Re-enters DVLA data as a <DrivingLicenceSubject>
    And User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain validity score 2, strength score 3 and type IdentityCheck
    And The test is complete and I close the driver
    Examples:
      | DrivingLicenceSubject             |
      | DrivingLicenceSubjectHappyKenneth |

  @build @staging @integration @smoke @stub @uat
  Scenario Outline: DVLA - User attempts invalid journey and retries with valid details
    Given User enters invalid Driving Licence DVLA details
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User Re-enters DVLA data as a <DrivingLicenceSubject>
    And User re-enters license number as <InvalidLicenceNumber>
    And User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci D02, validity score 0, strength score 3 and type IdentityCheck
    And The test is complete and I close the driver
    Examples:
      | DrivingLicenceSubject           | InvalidLicenceNumber |
      | DrivingLicenceSubjectHappyPeter | PARKE610112PBFGI     |

  @build @staging @integration @smoke @stub @uat
  Scenario: DVLA - User attempts invalid journey and cancels after first attempt
    Given User enters invalid Driving Licence DVLA details
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User click on ‘prove your identity another way' Link
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci D02, validity score 0, strength score 3 and type IdentityCheck
    And The test is complete and I close the driver

  @smoke
  Scenario: DVLA - User cancels before first attempt by clicking prove another way and returns an authorisation error
    Given User click on ‘prove your identity another way' Link
    Then I navigate to the Driving Licence verifiable issuer to check for a Invalid response
    And JSON response should contain error description Authorization permission denied and status code as 302
    And The test is complete and I close the driver

  @smoke
  Scenario: DVLA - User cancels before first attempt by clicking no driving licence and returns an authorisation error
    Given User click on ‘Back' Link
    When User click on I do not have a UK driving licence radio button
    Then I navigate to the Driving Licence verifiable issuer to check for a Invalid response
    And JSON response should contain error description Authorization permission denied and status code as 302
    And The test is complete and I close the driver

  @build @stub
  Scenario: DVLA - Password rotation check
    Given User enters DVLA data as a DrivingLicenceSubjectHappyKenneth
    When User clicks on continue
    And I navigate to the Driving Licence verifiable issuer to check for a Valid response
    Then The secret has been created
    Then The DVLA password should be valid and rotated within the specified window

  @build @stub
  Scenario Outline: DVLA - User enters invalid details and returns enter your details as it appears error message
    Given User enters DVLA data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then I check the page title is Error: Enter your details exactly as they appear on your UK driving licence – Prove your identity – GOV.UK
    And The test is complete and I close the driver
    Examples:
      | DrivingLicenceSubject                 |
      | NoLastName                            |
      | NoFirstName                           |
      | NoDateOfBirth                         |
      | NoIssueDate                           |
      | NoValidToDate                         |
      | NoDrivingLicenceNumber                |
      | NoIssueNumber                         |
      | NoPostcode                            |
      | InvalidFirstNameWithNumbers           |
      | InvalidFirstNameWithSpecialCharacters |
      | DateOfBirthWithSpecialCharacters      |
      | InvalidDateOfBirth                    |
      | IssueDateWithSpecialCharacters        |
      | ValidToDateWithSpecialCharacters      |
      | ValidToDateInPast                     |
      | DrivingLicenceNumberWithSpecialChar   |
      | IssueNumberWithSpecialChar            |
      | PostcodeWithSpecialChar               |
      | InternationalPostcode                 |


    ###########  DVLA Field Validations ##########
  #not existing in front end repo
  @build @staging @integration @dvlaDirect @stub @uat
  Scenario: DVLA - User consents to have DL checked and navigates to DVLA privacy notice
    Then I see the consent section Allow DVLA to check your driving licence details
    And I see the sentence DVLA needs your consent to check your driving licence details before you can continue. They will make sure your licence has not been cancelled or reported as lost or stolen.
    And I see the second line To find out more about how your driving licence details will be used, you can read:
    And I see privacy notice link the GOV.UK One Login privacy notice (opens in a new tab)
    Then I see the DVLA privacy notice link the DVLA privacy notice (opens in a new tab)
    And The test is complete and I close the driver

  @build @stub @Language-regression
  Scenario Outline: Language Title validation
    Given User clicks on language toggle and switches to Welsh
    Then I check the page title is Rhowch eich manylion yn union fel maent yn ymddangos ar eich trwydded yrru – Profi pwy ydych chi – GOV.UK
    Then User enters DVLA data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain validity score 2, strength score 3 and type IdentityCheck
    And JSON response should contain personal number DECER607085K99AE same as given Driving Licence
    And The test is complete and I close the driver
    Examples:
      | DrivingLicenceSubject             |
      | DrivingLicenceSubjectHappyKenneth |
