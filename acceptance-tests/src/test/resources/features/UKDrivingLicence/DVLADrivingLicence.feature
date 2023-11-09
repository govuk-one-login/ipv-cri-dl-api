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
  Scenario Outline:  DVLA Driving Licence details page happy path
    Given User enters DVLA data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain validity score 2, strength score 3 and type IdentityCheck
    And JSON response should contain personal number DECER607085K99AE same as given Driving Licence
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject             |
      |DrivingLicenceSubjectHappyKenneth   |

  @DVLADrivingLicence_test @build @staging @integration
  Scenario Outline: DVLA Driving Licence details page unhappy path with IncorrectDrivingLicenceNumber
    Given User enters DVLA data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci D02, validity score 0, strength score 3 and type IdentityCheck
    And JSON response should contain personal number PARKE610112PBFGI same as given Driving Licence
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject      |
      |IncorrectDrivingLicenceNumber |

  @DVLADrivingLicence_test @build @staging @integration @smoke
  Scenario Outline: DVLA Driving Licence details page unhappy path when licence number date format does not match with User's Date Of Birth
    Given User enters DVLA data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then I see the licence number error in the summary as Enter the number exactly as it appears on your driving licence
    #And I see check date of birth sentence as Error:Check you have entered your date of birth correctly
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject      |
      |DrivingLicenceNumberWithNumericChar |

  @DVLADrivingLicence_test @build @staging @integration
  Scenario Outline: DVLA Driving Licence details page unhappy path with IncorrectDateOfBirth
    Given User enters DVLA data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then I see check date of birth sentence as Check you have entered your date of birth correctly
    Then I see enter the date as it appears above the field as Error:Check you have entered your date of birth correctly
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |IncorrectDateOfBirth |

  @DVLADrivingLicence_test @build @staging @integration
  Scenario Outline: DVLA Driving Licence details page unhappy path with IncorrectLastName
    Given User enters DVLA data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci D02, validity score 0, strength score 3 and type IdentityCheck
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject      |
      |IncorrectLastName|

  @DVLADrivingLicence_test @build @staging @integration
  Scenario Outline: DVLA Driving Licence details page unhappy path with IncorrectIssueDate
    Given User enters DVLA data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci D02, validity score 0, strength score 3 and type IdentityCheck
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |IncorrectIssueDate|

  @DVLADrivingLicence_test @build @staging @integration
  Scenario Outline: DVLA Driving Licence details page unhappy path with IncorrectValidToDate
    Given User enters DVLA data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci D02, validity score 0, strength score 3 and type IdentityCheck
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |IncorrectValidToDate|

  @DVLADrivingLicence_test @build @staging @integration
  Scenario Outline: DVLA Driving Licence details page unhappy path with IncorrectIssueNumber
    Given User enters DVLA data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci D02, validity score 0, strength score 3 and type IdentityCheck
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |IncorrectIssueNumber|

  @DVLADrivingLicence_test @build @staging @integration @smoke
  Scenario Outline: DVLA Driving Licence Retry Test Happy Path
    Given User enters invalid Driving Licence DVLA details
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User Re-enters DVLA data as a <DrivingLicenceSubject>
    And User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain validity score 2, strength score 3 and type IdentityCheck
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject             |
      |DrivingLicenceSubjectHappyKenneth |

  @DVLADrivingLicence_test @build @staging @integration @smoke
  Scenario Outline: DVLA Driving Licence User failed second attempt
    Given User enters invalid Driving Licence DVLA details
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User Re-enters DVLA data as a <DrivingLicenceSubject>
    And User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci D02, validity score 0, strength score 3 and type IdentityCheck
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |IncorrectDrivingLicenceNumber |

  @DVLADrivingLicence_test @build @staging @integration @smoke
  Scenario: DVLA Driving Licence User cancels after failed first attempt
    Given User enters invalid Driving Licence DVLA details
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User click on ‘prove your identity another way' Link
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci D02, validity score 0, strength score 3 and type IdentityCheck
    And The test is complete and I close the driver

  @DVLADrivingLicence_test @smoke
  Scenario: DVLA Driving Licence User cancels before first attempt via prove your identity another way route
    Given User click on ‘prove your identity another way' Link
    Then I navigate to the Driving Licence verifiable issuer to check for a Invalid response
    And JSON response should contain error description Authorization permission denied and status code as 302
    And The test is complete and I close the driver

  @DVLADrivingLicence_test @smoke
  Scenario: DVLA Driving Licence User cancels before first attempt via I do not have a UK driving licence route
    Given User click on ‘Back' Link
    When User click on I do not have a UK driving licence radio button
    Then I navigate to the Driving Licence verifiable issuer to check for a Invalid response
    And JSON response should contain error description Authorization permission denied and status code as 302
    And The test is complete and I close the driver

    ###########  DVLA Field Validations ##########
  #not existing in front end repo
  @DVLADrivingLicence_test @build @staging @integration @dvlaDirect
  Scenario: DVLA Driving Licence privacy notice link to consent
    Then I see the consent section Allow DVLA to check your driving licence details
    And I see the sentence DVLA needs your consent to check your driving licence details before you can continue. They will make sure your licence has not been cancelled or reported as lost or stolen.
   And I see the second line To find out more about how your driving licence details will be used, you can read:
    And I see privacy notice link the GOV.UK One Login privacy notice (opens in a new tab)
    Then I see the DVLA privacy notice link the DVLA privacy notice (opens in a new tab)
    And The test is complete and I close the driver
