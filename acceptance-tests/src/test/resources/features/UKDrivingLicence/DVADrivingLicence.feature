Feature: DVA Driving Licence Test

  Background:
    Given I navigate to the IPV Core Stub
    And I click the Driving Licence CRI for the testEnvironment
    And I search for Driving Licence user number 5 in the Experian table
    Then I check the page title is Who was your UK driving licence issued by? – Prove your identity – GOV.UK
    And I should see DVA as an option
    And I click on DVA radio button and Continue
    And I check the page title is Enter your details exactly as they appear on your UK driving licence – Prove your identity – GOV.UK
    And I set the document checking route
    And I see a form requesting DVA LicenceNumber

  @DVADrivingLicence_test @build @staging @integration @smoke @direct
  Scenario Outline:  DVA Driving Licence details page happy path
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain validity score 2 and strength score 3
    And JSON response should contain personal number 55667788 same as given Driving Licence
    And exp should not be present in the JSON payload
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject             |
      |DVADrivingLicenceSubjectHappyBilly   |

  @DVADrivingLicence_test @direct
  Scenario Outline: DVA Driving Licence details page unhappy path with InvalidDVADrivingLicenceDetails
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Invalid response
    And JSON response should contain error description Authorization permission denied and status code as 302
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject      |
      |DVADrivingLicenceSubjectUnhappySelina |

  @DVADrivingLicence_test @build @staging @integration @direct
  Scenario Outline: DVA Driving Licence details page unhappy path with IncorrectDVADrivingLicenceNumber
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci D02, validity score 0 and strength score 3
    And JSON response should contain personal number 88776655 same as given Driving Licence
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject      |
      |IncorrectDrivingLicenceNumber |

  @DVADrivingLicence_test @build @staging @integration @direct
  Scenario Outline: DVA Driving Licence details page unhappy path with IncorrectDVADateOfBirth
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci D02, validity score 0 and strength score 3
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |IncorrectDateOfBirth |

  @DVADrivingLicence_test @build @staging @integration @direct
  Scenario Outline: DVA Driving Licence details page unhappy path with IncorrectDVAFirstName
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci D02, validity score 0 and strength score 3
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |IncorrectFirstName|

  @DVADrivingLicence_test @build @staging @integration @direct
  Scenario Outline: DVA Driving Licence details page unhappy path with IncorrectDVALastName
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci D02, validity score 0 and strength score 3
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |IncorrectLastName|

  @DVADrivingLicence_test @build @staging @integration @direct
  Scenario Outline: DVA Driving Licence details page unhappy path with IncorrectDVAIssueDate
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci D02, validity score 0 and strength score 3
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |IncorrectIssueDate|

  @DVADrivingLicence_test @build @staging @integration @direct
  Scenario Outline: DVA Driving Licence details page unhappy path with IncorrectDVAValidToDate
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci D02, validity score 0 and strength score 3
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |IncorrectValidToDate|

  @DVADrivingLicence_test @build @staging @integration @direct
  Scenario Outline: DVA Driving Licence details page unhappy path with IncorrectDVAPostcode
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci D02, validity score 0 and strength score 3
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |IncorrectPostcode|


  @DVADrivingLicence_test @build @staging @integration @smoke @direct
  Scenario Outline: DVA Driving Licence Retry Test Happy Path
    Given User enters invalid Driving Licence DVA details
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User Re-enters DVA data as a <DVADrivingLicenceSubject>
    And User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain validity score 2 and strength score 3
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |DVADrivingLicenceSubjectHappyBilly |

  @DVADrivingLicence_test @build @staging @integration @smoke @direct
  Scenario Outline: DVA Driving Licence User failed second attempt
    Given User enters invalid Driving Licence DVA details
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User Re-enters DVA data as a <DVADrivingLicenceSubject>
    And User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci D02, validity score 0 and strength score 3
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |IncorrectDrivingLicenceNumber |

  @DVADrivingLicence_test @build @staging @integration @smoke @direct
  Scenario: DVA Driving Licence User cancels after failed first attempt
    Given User enters invalid Driving Licence DVA details
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User click on ‘prove your identity another way' Link
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci D02, validity score 0 and strength score 3
    And The test is complete and I close the driver

  @DVADrivingLicence_test @smoke @direct
  Scenario: DVA Driving Licence User cancels before first attempt via prove your identity another way route
    Given User click on ‘prove your identity another way' Link
    Then I navigate to the Driving Licence verifiable issuer to check for a Invalid response
    And JSON response should contain error description Authorization permission denied and status code as 302
    And The test is complete and I close the driver

  @DVADrivingLicence_test @smoke @direct
  Scenario: DVA Driving Licence User cancels before first attempt via I do not have a UK driving licence route
    Given User click on ‘Back' Link
    When User click on I do not have a UK driving licence radio button
    Then I navigate to the Driving Licence verifiable issuer to check for a Invalid response
    And JSON response should contain error description Authorization permission denied and status code as 302
    And The test is complete and I close the driver

###########  DVA Field Validations ##########
  @DVADrivingLicence_test @build @staging @integration
  Scenario Outline: DVA Driving Licence Last name with numbers error validation
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then I see the Lastname error in the error summary as Enter your last name as it appears on your driving licence
    And I see the Lastname error in the error field as Error:Enter your last name as it appears on your driving licence
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject      |
      |InvalidLastNameWithNumbers |

  @DVADrivingLicence_test @build @staging @integration
  Scenario Outline: DVA Driving Licence Last name with special characters error validation
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then I see the Lastname error in the error summary as Enter your last name as it appears on your driving licence
    And I see the Lastname error in the error field as Error:Enter your last name as it appears on your driving licence
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |InvalidLastNameWithSpecialCharacters |

  @DVADrivingLicence_test @build @staging @integration
  Scenario Outline: DVA Driving Licence No Last name in the Last name field error validation
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then I see the Lastname error in the error summary as Enter your last name as it appears on your driving licence
    And I see the Lastname error in the error field as Error:Enter your last name as it appears on your driving licence
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |NoLastName |

  @DVADrivingLicence_test @build @staging @integration
  Scenario Outline: DVA Driving Licence First name with numbers error validation
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then I see the firstname error summary as Enter your first name as it appears on your driving licence
    And I see the firstname error in the error field as Error:Enter your first name as it appears on your driving licence
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject      |
      |InvalidFirstNameWithNumbers |

  @DVADrivingLicence_test @build @staging @integration
  Scenario Outline: DVA Driving Licence First name with special characters error validation
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then I see the firstname error summary as Enter your first name as it appears on your driving licence
    And I see the firstname error in the error field as Error:Enter your first name as it appears on your driving licence
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |InvalidFirstNameWithSpecialCharacters |

  @DVADrivingLicence_test @build @staging @integration
  Scenario Outline: DVA Driving Licence No First name in the First name field error validation
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then I see the firstname error summary as Enter your first name as it appears on your driving licence
    And I see the firstname error in the error field as Error:Enter your first name as it appears on your driving licence
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |NoFirstName |

  @DVADrivingLicence_test @build @staging @integration
  Scenario Outline: DVA Driving Licence Date of birth that are not real error validation
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then I see check date of birth sentence as Enter your date of birth as it appears on your driving licence
    And As a DVA user I see enter the date as it appears above the field as Error:Enter your date of birth as it appears on your driving licence
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |InvalidDateOfBirth |

  @DVADrivingLicence_test @build @staging @integration
  Scenario Outline: DVA Driving Licence Date of birth with special characters error validation
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then I see check date of birth sentence as Enter your date of birth as it appears on your driving licence
    And As a DVA user I see enter the date as it appears above the field as Error:Enter your date of birth as it appears on your driving licence
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |DateOfBirthWithSpecialCharacters |

  @DVADrivingLicence_test @build @staging @integration
  Scenario Outline: DVA Driving Licence Date of birth in the future error validation
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then I see check date of birth sentence as Your date of birth must be in the past
    And As a DVA user I see enter the date as it appears above the field as Error:Your date of birth must be in the past
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |DateOfBirthInFuture |

  @DVADrivingLicence_test @build @staging @integration
  Scenario Outline: DVA Driving Licence - No Date in the Date of birth field error validation
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then I see check date of birth sentence as Enter your date of birth as it appears on your driving licence
    And As a DVA user I see enter the date as it appears above the field as Error:Enter your date of birth as it appears on your driving licence
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |NoDateOfBirth |

  @DVADrivingLicence_test @build @staging @integration
  Scenario Outline: DVA Driving Licence Issue date that are not real error validation
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then As a DVA user I see issue date error in summary as Enter the date as it appears on your driving licence
    And As a DVA user I see invalid issue date field error as Error:Enter the date as it appears on your driving licence
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |IncorrectDVAIssueDate |

  @DVADrivingLicence_test @build @staging @integration
  Scenario Outline: DVA Driving Licence Issue date with special characters error validation
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then As a DVA user I see issue date error in summary as Enter the date as it appears on your driving licence
    And As a DVA user I see invalid issue date field error as Error:Enter the date as it appears on your driving licence
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |IssueDateWithSpecialCharacters |

  @DVADrivingLicence_test @build @staging @integration
  Scenario Outline: DVA Driving Licence Issue date in the future error validation
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then As a DVA user I see issue date error in summary as The issue date must be in the past
    And As a DVA user I see invalid issue date field error as Error:The issue date must be in the past
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |IssueDateInFuture |

  @DVADrivingLicence_test @build @staging @integration
  Scenario Outline: DVA Driving Licence - No date in the Issue date field error validation
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then As a DVA user I see issue date error in summary as Enter the date as it appears on your driving licence
    And As a DVA user I see invalid issue date field error as Error:Enter the date as it appears on your driving licence
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |NoIssueDate |

  @DVADrivingLicence_test @build @staging @integration
  Scenario Outline: DVA Driving Licence Valid to date that are not real error validation
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then I can see the valid to date error in the error summary as Enter the date as it appears on your driving licence
    And I can see the Valid to date field error as Error:Enter the date as it appears on your driving licence
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |InvalidValidToDate |

  @DVADrivingLicence_test @build @staging @integration
  Scenario Outline: DVA Driving Licence Valid to date with special characters error validation
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then I can see the valid to date error in the error summary as Enter the date as it appears on your driving licence
    And I can see the Valid to date field error as Error:Enter the date as it appears on your driving licence
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |ValidToDateWithSpecialCharacters |

  @DVADrivingLicence_test @build @staging @integration
  Scenario Outline: DVA Driving Licence Valid to date in the past error validation
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then I can see the valid to date error in the error summary as You cannot use an expired driving licence
    And I can see the Valid to date field error as Error:You cannot use an expired driving licence
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |ValidToDateInPast |

  @DVADrivingLicence_test @build @staging @integration
  Scenario Outline: DVA Driving Licence - No date in the Valid to date field error validation
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then I can see the valid to date error in the error summary as Enter the date as it appears on your driving licence
    And I can see the Valid to date field error as Error:Enter the date as it appears on your driving licence
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |NoValidToDate |

  @DVADrivingLicence_test @build @staging @integration
  Scenario Outline: DVA Driving Licence number less than 8 characters error validation
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then As a DVA user I see the licence number error in the summary as Your licence number should be 8 characters long
    And As a DVA user I can see the licence number error in the field as Error:Your licence number should be 8 characters long
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |DrivingLicenceNumLessThan8Char |

  @DVADrivingLicence_test @build @staging @integration
  Scenario Outline: DVA Driving Licence number with special characters and spaces error validation
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then As a DVA user I see the licence number error in the summary as Your licence number should not include any symbols or spaces
    And As a DVA user I can see the licence number error in the field as Error:Your licence number should not include any symbols or spaces
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |DrivingLicenceNumberWithSpecialChar |

  @DVADrivingLicence_test @build @staging @integration
  Scenario Outline: DVA Driving Licence number with alpha numeric characters error validation
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then As a DVA user I see the licence number error in the summary as Enter the number exactly as it appears on your driving licence
    And As a DVA user I can see the licence number error in the field as Error:Enter the number exactly as it appears on your driving licence
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |DrivingLicenceNumberWithNumericChar |

  @DVADrivingLicence_test @build @staging @integration
  Scenario Outline: DVA Driving Licence number with alpha characters error validation
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then As a DVA user I see the licence number error in the summary as Enter the number exactly as it appears on your driving licence
    And As a DVA user I can see the licence number error in the field as Error:Enter the number exactly as it appears on your driving licence
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |DrivingLicenceNumberWithAlphaChar |

  @DVADrivingLicence_test @build @staging @integration
  Scenario Outline: DVA Driving Licence - No Licence number in the Licence number field error validation
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then As a DVA user I see the licence number error in the summary as Enter the number exactly as it appears on your driving licence
    And As a DVA user I can see the licence number error in the field as Error:Enter the number exactly as it appears on your driving licence
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |NoDrivingLicenceNumber |

  @DVADrivingLicence_test @build @staging @integration
  Scenario Outline: DVA Driving Licence Postcode less than 5 characters error validation
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then I see the postcode error in summary as Your postcode should be between 5 and 7 characters
    And I see the postcode error in the field as Error:Your postcode should be between 5 and 7 characters
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |PostcodeLessThan5Char |

  @DVADrivingLicence_test @build @staging @integration
  Scenario Outline: DVA Driving Licence Postcode with special characters error validation
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then I see the postcode error in summary as Your postcode should only include numbers and letters
    And I see the postcode error in the field as Error:Your postcode should only include numbers and letters
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |PostcodeWithSpecialChar |

  @DVADrivingLicence_test @build @staging @integration
  Scenario Outline: DVA Driving Licence Postcode with numeric characters error validation
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then I see the postcode error in summary as Your postcode should include numbers and letters
    And I see the postcode error in the field as Error:Your postcode should include numbers and letters
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |PostcodeWithNumericChar |

  @DVADrivingLicence_test @build @staging @integration
  Scenario Outline: DVA Driving Licence Postcode with alpha characters error validation
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then I see the postcode error in summary as Your postcode should include numbers and letters
    And I see the postcode error in the field as Error:Your postcode should include numbers and letters
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |PostcodeWithAlphaChar |

  @DVADrivingLicence_test @build @staging @integration
  Scenario Outline: DVA Driving Licence - No Postcode in the Postcode field error validation
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then I see the postcode error in summary as Enter your postcode
    And I see the postcode error in the field as Error:Enter your postcode
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |NoPostcode |

  @DVADrivingLicence_test @build @staging @integration
  Scenario Outline: DVA Driving Licence International Postcode error validation
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    Then I see the postcode error in summary as Enter a UK postcode
    And I see the postcode error in the field as Error:Enter a UK postcode
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject      |
      |InternationalPostcode |

  @DVADrivingLicence_test @build @staging @integration @smoke @direct
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

  @DVADrivingLicence_test @build @staging @integration @smoke @direct
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