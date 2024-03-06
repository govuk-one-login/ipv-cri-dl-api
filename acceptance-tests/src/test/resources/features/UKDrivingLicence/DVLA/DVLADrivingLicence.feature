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

  @DVLADrivingLicence_test @build @staging @integration @smoke
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

  @DVLADrivingLicence_test @build @staging @integration
  #Scenario Outline: DVLA - Details page unhappy path with IncorrectDrivingLicenceNumber
  Scenario Outline: DVLA - Unhappy Path - User enters invalid driving licence number
    Given User enters DVLA data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci D02, validity score 0, strength score 3 and type IdentityCheck
    And JSON response should contain personal number PARKE610112PBFGI same as given Driving Licence
    And The test is complete and I close the driver
    Examples:
      | DrivingLicenceSubject         |
      | IncorrectDrivingLicenceNumber |

  @DVLADrivingLicence_test @build @staging @integration @smoke
  #Scenario Outline: DVLA Driving Licence details page unhappy path when licence number date format does not match with User's Date Of Birth
  Scenario Outline: DVLA - Unhappy Path - User enters driving licence number and date of birth in incorrect format which returns validation error
    Given User enters DVLA data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then I see the licence number error in the summary as Enter the number exactly as it appears on your driving licence
    #And I see check date of birth sentence as Error:Check you have entered your date of birth correctly
    And The test is complete and I close the driver
    Examples:
      | DrivingLicenceSubject               |
      | DrivingLicenceNumberWithNumericChar |

  @DVLADrivingLicence_test @build @staging @integration
  #Scenario Outline: DVLA Driving Licence details page unhappy path with IncorrectDateOfBirth
  Scenario Outline: DVLA - Unhappy Path - User enters invalid date of birth and returns field validation error
    Given User enters DVLA data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then I see check date of birth sentence as Check you have entered your date of birth correctly
    Then I see enter the date as it appears above the field as Error:Check you have entered your date of birth correctly
    And The test is complete and I close the driver
    Examples:
      | DrivingLicenceSubject |
      | IncorrectDateOfBirth  |

  @DVLADrivingLicence_test @build @staging @integration
  #Scenario Outline: DVLA Driving Licence details page unhappy path with IncorrectLastName
  Scenario Outline: DVLA - Unhappy Path - User enters invalid last name and returns could not find your details error message
    Given User enters DVLA data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci D02, validity score 0, strength score 3 and type IdentityCheck
    And The test is complete and I close the driver
    Examples:
      | DrivingLicenceSubject |
      | IncorrectLastName     |

  @DVLADrivingLicence_test @build @staging @integration
  #Scenario Outline: DVLA Driving Licence details page unhappy path with IncorrectIssueDate
  Scenario Outline: DVLA - Unhappy Path - User enters invalid issue date and returns could not find your details error message
    Given User enters DVLA data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci D02, validity score 0, strength score 3 and type IdentityCheck
    And The test is complete and I close the driver
    Examples:
      | DrivingLicenceSubject |
      | IncorrectIssueDate    |

  @DVLADrivingLicence_test @build @staging @integration
  #Scenario Outline: DVLA Driving Licence details page unhappy path with IncorrectValidToDate
  Scenario Outline: DVLA - Unhappy Path - User enters invalid valid-to date and returns could not find your details error message
    Given User enters DVLA data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci D02, validity score 0, strength score 3 and type IdentityCheck
    And The test is complete and I close the driver
    Examples:
      | DrivingLicenceSubject |
      | IncorrectValidToDate  |

  @DVLADrivingLicence_test @build @staging @integration
  #Scenario Outline: DVLA Driving Licence details page unhappy path with IncorrectIssueNumber
  Scenario Outline: DVLA - Unhappy Path - User enters invalid issue number and returns could not find your details error message
    Given User enters DVLA data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci D02, validity score 0, strength score 3 and type IdentityCheck
    And The test is complete and I close the driver
    Examples:
      | DrivingLicenceSubject |
      | IncorrectIssueNumber  |

  @DVLADrivingLicence_test @build @staging @integration @smoke
  #Scenario Outline: DVLA Driving Licence Retry Test Happy Path
  Scenario Outline: DVLA - Unhappy Path - User attempts invalid journey and retries with valid details
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

  @DVLADrivingLicence_test @build @staging @integration @smoke
  #Scenario Outline: DVLA Driving Licence User failed second attempt
  Scenario Outline: DVLA - Unhappy Path - User attempts invalid journey and retries with valid details
    Given User enters invalid Driving Licence DVLA details
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User Re-enters DVLA data as a <DrivingLicenceSubject>
    And User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci D02, validity score 0, strength score 3 and type IdentityCheck
    And The test is complete and I close the driver
    Examples:
      | DrivingLicenceSubject         |
      | IncorrectDrivingLicenceNumber |

  @DVLADrivingLicence_test @build @staging @integration @smoke
  #Scenario: DVLA Driving Licence User cancels after failed first attempt
  Scenario: DVLA - Unhappy Path - User attempts invalid journey and cancels after first attempt
    Given User enters invalid Driving Licence DVLA details
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User click on ‘prove your identity another way' Link
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci D02, validity score 0, strength score 3 and type IdentityCheck
    And The test is complete and I close the driver

  @DVLADrivingLicence_test @smoke
  #cenario: DVLA Driving Licence User cancels before first attempt via prove your identity another way route
  Scenario: DVLA - Unhappy Path - User cancels before first attempt by clicking prove another way and returns an authorisation error
    Given User click on ‘prove your identity another way' Link
    Then I navigate to the Driving Licence verifiable issuer to check for a Invalid response
    And JSON response should contain error description Authorization permission denied and status code as 302
    And The test is complete and I close the driver

  @DVLADrivingLicence_test @smoke
  #Scenario: DVLA Driving Licence User cancels before first attempt via I do not have a UK driving licence route
  Scenario: DVLA - Unhappy Path - User cancels before first attempt by clicking no driving licence and returns an authorisation error
    Given User click on ‘Back' Link
    When User click on I do not have a UK driving licence radio button
    Then I navigate to the Driving Licence verifiable issuer to check for a Invalid response
    And JSON response should contain error description Authorization permission denied and status code as 302
    And The test is complete and I close the driver

  @DVLADrivingLicence_test @build
  #Scenario: DVLA Password rotation check
  Scenario: DVLA - Unhappy Path - Password rotation check
    Given User enters DVLA data as a DrivingLicenceSubjectHappyKenneth
    When User clicks on continue
    And I navigate to the Driving Licence verifiable issuer to check for a Valid response
    Then The secret has been created
    Then The DVLA password should be valid and rotated within the specified window

  @DVLADrivingLicence_test @build
  #Scenario Outline: DVLA Error tab title validation
  Scenario Outline: DVLA - Unhappy Path - User enters invalid details and returns enter your details as it appears error message
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
  @DVLADrivingLicence_test @build @staging @integration @dvlaDirect @cat
  #Scenario: DVLA Driving Licence privacy notice link to consent
  Scenario: DVLA - Unhappy Path - User consents to have DL checked and navigates to DVLA privacy notice
    Then I see the consent section Allow DVLA to check your driving licence details
    And I see the sentence DVLA needs your consent to check your driving licence details before you can continue. They will make sure your licence has not been cancelled or reported as lost or stolen.
    And I see the second line To find out more about how your driving licence details will be used, you can read:
    And I see privacy notice link the GOV.UK One Login privacy notice (opens in a new tab)
    Then I see the DVLA privacy notice link the DVLA privacy notice (opens in a new tab)
    And The test is complete and I close the driver
