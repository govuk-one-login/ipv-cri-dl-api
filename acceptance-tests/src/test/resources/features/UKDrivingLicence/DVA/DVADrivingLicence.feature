Feature: DVA Driving Licence Test

  Background:
    Given I navigate to the IPV Core Stub
    And I click the Driving Licence CRI for the testEnvironment
    And I search for Driving Licence user number 5 in the Experian table
    Then I check the page title is Who was your UK driving licence issued by? – Prove your identity – GOV.UK
    And I should see DVA as an option
    And I click on DVA radio button and Continue
    And I check the page title is Enter your details exactly as they appear on your UK driving licence – Prove your identity – GOV.UK
    And I see a form requesting DVA LicenceNumber

  @DVADrivingLicence_test @build @staging @integration @smoke
  Scenario Outline:  DVA Driving Licence details page happy path
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain validity score 2, strength score 3 and type IdentityCheck
    And JSON response should contain personal number 55667788 same as given Driving Licence
    And JSON response should contain JTI field
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject             |
      |DVADrivingLicenceSubjectHappyBilly   |

  @DVADrivingLicence_test
  Scenario Outline: DVA Driving Licence details page unhappy path with InvalidDVADrivingLicenceDetails
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Invalid response
    And JSON response should contain error description Authorization permission denied and status code as 302
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject      |
      |DVADrivingLicenceSubjectUnhappySelina |

  @DVADrivingLicence_test @build @staging @integration
  Scenario Outline: DVA Driving Licence details page unhappy path with IncorrectDVADrivingLicenceNumber
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci D02, validity score 0, strength score 3 and type IdentityCheck
    And JSON response should contain personal number 88776655 same as given Driving Licence
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject      |
      |IncorrectDrivingLicenceNumber |

  @DVADrivingLicence_test @build @staging @integration
  Scenario Outline: DVA Driving Licence details page unhappy path with IncorrectDVADateOfBirth
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci D02, validity score 0, strength score 3 and type IdentityCheck
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |IncorrectDateOfBirth |

  @DVADrivingLicence_test @build @staging @integration
  Scenario Outline: DVA Driving Licence details page unhappy path with IncorrectDVAFirstName
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci D02, validity score 0, strength score 3 and type IdentityCheck
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |IncorrectFirstName|

  @DVADrivingLicence_test @build @staging @integration
  Scenario Outline: DVA Driving Licence details page unhappy path with IncorrectDVALastName
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci D02, validity score 0, strength score 3 and type IdentityCheck
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |IncorrectLastName|

  @DVADrivingLicence_test @build @staging @integration
  Scenario Outline: DVA Driving Licence details page unhappy path with IncorrectDVAIssueDate
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci D02, validity score 0, strength score 3 and type IdentityCheck
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |IncorrectIssueDate|

  @DVADrivingLicence_test @build @staging @integration
  Scenario Outline: DVA Driving Licence details page unhappy path with IncorrectDVAValidToDate
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci D02, validity score 0, strength score 3 and type IdentityCheck
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |IncorrectValidToDate|

  @DVADrivingLicence_test @build @staging @integration
  Scenario Outline: DVA Driving Licence details page unhappy path with IncorrectDVAPostcode
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci D02, validity score 0, strength score 3 and type IdentityCheck
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |IncorrectPostcode|


  @DVADrivingLicence_test @build @staging @integration @smoke
  Scenario Outline: DVA Driving Licence Retry Test Happy Path
    Given User enters invalid Driving Licence DVA details
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User Re-enters DVA data as a <DVADrivingLicenceSubject>
    And User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain validity score 2, strength score 3 and type IdentityCheck
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |DVADrivingLicenceSubjectHappyBilly |

  @DVADrivingLicence_test @build @staging @integration @smoke
  Scenario Outline: DVA Driving Licence User failed second attempt
    Given User enters invalid Driving Licence DVA details
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User Re-enters DVA data as a <DVADrivingLicenceSubject>
    And User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci D02, validity score 0, strength score 3 and type IdentityCheck
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |IncorrectDrivingLicenceNumber |

  @DVADrivingLicence_test @build @staging @integration @smoke
  Scenario: DVA Driving Licence User cancels after failed first attempt
    Given User enters invalid Driving Licence DVA details
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User click on ‘prove your identity another way' Link
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci D02, validity score 0, strength score 3 and type IdentityCheck
    And The test is complete and I close the driver

  @DVADrivingLicence_test @smoke
  Scenario: DVA Driving Licence User cancels before first attempt via prove your identity another way route
    Given User click on ‘prove your identity another way' Link
    Then I navigate to the Driving Licence verifiable issuer to check for a Invalid response
    And JSON response should contain error description Authorization permission denied and status code as 302
    And The test is complete and I close the driver

  @DVADrivingLicence_test @smoke
  Scenario: DVA Driving Licence User cancels before first attempt via I do not have a UK driving licence route
    Given User click on ‘Back' Link
    When User click on I do not have a UK driving licence radio button
    Then I navigate to the Driving Licence verifiable issuer to check for a Invalid response
    And JSON response should contain error description Authorization permission denied and status code as 302
    And The test is complete and I close the driver

  @DVADrivingLicence_test @build
  Scenario Outline: DVAError tab title validation
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then I check the page title is Error: Enter your details exactly as they appear on your UK driving licence – Prove your identity – GOV.UK
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject             |
      |NoLastName   |
      |NoFirstName |
      |NoDateOfBirth   |
      |NoIssueDate   |
      |NoValidToDate  |
      |NoDrivingLicenceNumber |
      |NoPostcode|
      |InvalidFirstNameWithNumbers|
      |InvalidFirstNameWithSpecialCharacters|
      |DateOfBirthWithSpecialCharacters     |
      |InvalidDateOfBirth|
      |DateOfBirthInFuture            |
      |IssueDateWithSpecialCharacters|
      |ValidToDateWithSpecialCharacters|
      |ValidToDateInPast |
      |DrivingLicenceNumberWithSpecialChar|
      |PostcodeWithSpecialChar            |
      |InternationalPostcode              |

###########  DVA Field Validations ##########
    #not existing in front end repo
  @DVADrivingLicence_test @build @staging @integration @smoke
  Scenario: DVA Driving Licence privacy notice link to consent
    Then I see the DVA consent section Allow DVA to check your driving licence details
    And I see the Consent sentence in DVA page DVA needs your consent to check your driving licence details before you can continue. They will make sure your licence has not been cancelled or reported as lost or stolen.
    And I see the Consent second line in DVA page To find out more about how your driving licence details will be used, you can read:
    And I see privacy DVA notice link the GOV.UK One Login privacy notice (opens in a new tab)
    Then I see the DVA privacy notice link the DVA privacy notice (opens in a new tab)
    And The test is complete and I close the driver

      #not existing in front end repo
  @DVADrivingLicence_test @build @staging @integration @smoke
  Scenario Outline:  DVA Driving Licence Generate VC with invalid DL number and prove in another way unhappy path
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    When User click on ‘prove your identity another way' Link
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON response should contain personal number 88776655 same as given Driving Licence
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject           |
      | IncorrectDrivingLicenceNumber     |

  @DVADrivingLicence_test @build @staging @integration @smoke
  Scenario Outline:  DVA Driving Licence error validation when DVA consent checkbox is unselected
    Given User enters DVA data as a <DrivingLicenceSubject>
    And DVA consent checkbox is unselected
    When User clicks on continue
    Then User can see the DVA consent error in summary as You must give your consent to continue
    And User can see the DVA consent error on the checkbox as Error:You must give your consent to continue
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject             |
      |DVADrivingLicenceSubjectHappyBilly|

  @DVADrivingLicence_test @build @staging @integration @smoke
  Scenario: DVA Driving Licence privacy notice link to consent
    Then I see the DVA consent section Allow DVA to check your driving licence details
    And I see the Consent sentence in DVA page DVA needs your consent to check your driving licence details before you can continue. They will make sure your licence has not been cancelled or reported as lost or stolen.
    And I see the Consent second line in DVA page To find out more about how your driving licence details will be used, you can read:
    And I see privacy DVA notice link the GOV.UK One Login privacy notice (opens in a new tab)
    Then I see the DVA privacy notice link the DVA privacy notice (opens in a new tab)
    And The test is complete and I close the driver
