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
    And JSON payload should contain validity score 2 and strength score 3
    And JSON response should contain personal number PARKE610112PBFGH same as given Driving Licence
    And exp should not be present in the JSON payload
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject             |
      |DrivingLicenceSubjectHappyPeter   |

  @DVLADrivingLicence_test @build @staging @integration
  Scenario Outline: DVLA Driving Licence details page unhappy path with IncorrectDrivingLicenceNumber
    Given User enters DVLA data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci D02, validity score 0 and strength score 3
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
  Scenario Outline: DVLA Driving Licence details page unhappy path with IncorrectFirstName
    Given User enters DVLA data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci D02, validity score 0 and strength score 3
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |IncorrectFirstName|

  @DVLADrivingLicence_test @build @staging @integration
  Scenario Outline: DVLA Driving Licence details page unhappy path with IncorrectLastName
    Given User enters DVLA data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci D02, validity score 0 and strength score 3
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
    And JSON payload should contain ci D02, validity score 0 and strength score 3
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
    And JSON payload should contain ci D02, validity score 0 and strength score 3
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
    And JSON payload should contain ci D02, validity score 0 and strength score 3
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |IncorrectIssueNumber|

  @DVLADrivingLicence_test @build @staging @integration
  Scenario Outline: DVLA Driving Licence details page unhappy path with IncorrectPostcode
    Given User enters DVLA data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci D02, validity score 0 and strength score 3
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |IncorrectPostcode|

  @DVLADrivingLicence_test @build @staging @integration @smoke
  Scenario Outline: DVLA Driving Licence Retry Test Happy Path
    Given User enters invalid Driving Licence DVLA details
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User Re-enters DVLA data as a <DrivingLicenceSubject>
    And User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain validity score 2 and strength score 3
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject             |
      |DrivingLicenceSubjectHappyPeter |

  @DVLADrivingLicence_test @build @staging @integration @smoke
  Scenario Outline: DVLA Driving Licence User failed second attempt
    Given User enters invalid Driving Licence DVLA details
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User Re-enters DVLA data as a <DrivingLicenceSubject>
    And User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain ci D02, validity score 0 and strength score 3
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
    And JSON payload should contain ci D02, validity score 0 and strength score 3
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
  @DVLADrivingLicence_test @build @staging @integration
  Scenario Outline: DVLA Driving Licence Last name with numbers error validation
    Given User enters DVLA data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then I see the Lastname error in the error summary as Enter your last name as it appears on your driving licence
    And I see the Lastname error in the error field as Error:Enter your last name as it appears on your driving licence
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject      |
      |InvalidLastNameWithNumbers |

  @DVLADrivingLicence_test @build @staging @integration
  Scenario Outline: DVLA Driving Licence Last name with special characters error validation
    Given User enters DVLA data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then I see the Lastname error in the error summary as Enter your last name as it appears on your driving licence
    And I see the Lastname error in the error field as Error:Enter your last name as it appears on your driving licence
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |InvalidLastNameWithSpecialCharacters |

  @DVLADrivingLicence_test @build @staging @integration
  Scenario Outline: DVLA Driving Licence - No Last name in the Last name field error validation
    Given User enters DVLA data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then I see the Lastname error in the error summary as Enter your last name as it appears on your driving licence
    And I see the Lastname error in the error field as Error:Enter your last name as it appears on your driving licence
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |NoLastName |

  @DVLADrivingLicence_test @build @staging @integration
  Scenario Outline: DVLA Driving Licence First name with numbers error validation
    Given User enters DVLA data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then I see the firstname error summary as Enter your first name as it appears on your driving licence
    And I see the firstname error in the error field as Error:Enter your first name as it appears on your driving licence
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject      |
      |InvalidFirstNameWithNumbers |

  @DVLADrivingLicence_test @build @staging @integration
  Scenario Outline: DVLA Driving Licence First name with special characters error validation
    Given User enters DVLA data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then I see the firstname error summary as Enter your first name as it appears on your driving licence
    And I see the firstname error in the error field as Error:Enter your first name as it appears on your driving licence
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |InvalidFirstNameWithSpecialCharacters |

  @DVLADrivingLicence_test @build @staging @integration
  Scenario Outline: DVLA Driving Licence - No First name in the First name field error validation
    Given User enters DVLA data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then I see the firstname error summary as Enter your first name as it appears on your driving licence
    And I see the firstname error in the error field as Error:Enter your first name as it appears on your driving licence
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |NoFirstName |

  @DVLADrivingLicence_test @build @staging @integration
  Scenario Outline: DVLA Driving Licence Middle names with numbers error validation
    Given User enters DVLA data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then I see the middlenames error summary as Enter any middle names as they appear on your driving licence
    And I see the middlenames error in the error field as Error:Enter any middle names as they appear on your driving licence
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject      |
      |InvalidMiddleNamesWithNumbers |

  @DVLADrivingLicence_test @build @staging @integration
  Scenario Outline: DVLA Driving Licence Middle names with special characters error validation
    Given User enters DVLA data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then I see the middlenames error summary as Enter any middle names as they appear on your driving licence
    And I see the middlenames error in the error field as Error:Enter any middle names as they appear on your driving licence
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject      |
      |InvalidMiddleNamesWithSpecialCharacters |

  @DVLADrivingLicence_test @build @staging @integration
  Scenario Outline: DVLA Driving Licence Date of birth that are not real error validation
    Given User enters DVLA data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then I see check date of birth sentence as Check you have entered your date of birth correctly
    And I see enter the date as it appears above the field as Error:Check you have entered your date of birth correctly
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |IncorrectDateOfBirth |

  @DVLADrivingLicence_test @build @staging @integration
  Scenario Outline: DVLA Driving Licence Date of birth with special characters error validation
    Given User enters DVLA data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then I see check date of birth sentence as Enter your date of birth as it appears on your driving licence
    And I see enter the date as it appears above the field as Error:Enter your date of birth as it appears on your driving licence
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |DateOfBirthWithSpecialCharacters |

  @DVLADrivingLicence_test @build @staging @integration
  Scenario Outline: DVLA Driving Licence Date of birth in the future error validation
    Given User enters DVLA data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then I see check date of birth sentence as Your date of birth must be in the past
    And I see enter the date as it appears above the field as Error:Your date of birth must be in the past
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |DateOfBirthInFuture |

  @DVLADrivingLicence_test @build @staging @integration
  Scenario Outline: DVLA Driving Licence - No Date in the Date of birth field error validation
    Given User enters DVLA data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then I see check date of birth sentence as Enter your date of birth as it appears on your driving licence
    And I see enter the date as it appears above the field as Error:Enter your date of birth as it appears on your driving licence
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |NoDateOfBirth |

  @DVLADrivingLicence_test @build @staging @integration
  Scenario Outline: DVLA Driving Licence Issue date that are not real error validation
    Given User enters DVLA data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then I see issue date error in summary as Enter the date as it appears on your driving licence
    And I see invalid issue date field error as Error:Enter the date as it appears on your driving licence
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |InvalidIssueDate |

  @DVLADrivingLicence_test @build @staging @integration
  Scenario Outline: DVLA Driving Licence Issue date with special characters error validation
    Given User enters DVLA data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then I see issue date error in summary as Enter the date as it appears on your driving licence
    And I see invalid issue date field error as Error:Enter the date as it appears on your driving licence
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |IssueDateWithSpecialCharacters |

  @DVLADrivingLicence_test @build @staging @integration
  Scenario Outline: DVLA Driving Licence Issue date in the future error validation
    Given User enters DVLA data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then I see issue date error in summary as The issue date must be in the past
    And I see invalid issue date field error as Error:The issue date must be in the past
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |IssueDateInFuture |

  @DVLADrivingLicence_test @build @staging @integration
  Scenario Outline: DVLA Driving Licence - No date in the Issue date field error validation
    Given User enters DVLA data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then I see issue date error in summary as Enter the date as it appears on your driving licence
    And I see invalid issue date field error as Error:Enter the date as it appears on your driving licence
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |NoIssueDate |

  @DVLADrivingLicence_test @build @staging @integration
  Scenario Outline: DVLA Driving Licence Valid to date that are not real error validation
    Given User enters DVLA data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then I can see the valid to date error in the error summary as Enter the date as it appears on your driving licence
    And I can see the Valid to date field error as Error:Enter the date as it appears on your driving licence
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |InvalidValidToDate |

  @DVLADrivingLicence_test @build @staging @integration
  Scenario Outline: DVLA Driving Licence Valid to date with special characters error validation
    Given User enters DVLA data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then I can see the valid to date error in the error summary as Enter the date as it appears on your driving licence
    And I can see the Valid to date field error as Error:Enter the date as it appears on your driving licence
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |ValidToDateWithSpecialCharacters |

  @DVLADrivingLicence_test @build @staging @integration
  Scenario Outline: DVLA Driving Licence Valid to date in the past error validation
    Given User enters DVLA data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then I can see the valid to date error in the error summary as You cannot use an expired driving licence
    And  I can see the Valid to date field error as Error:You cannot use an expired driving licence
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |ValidToDateInPast |

  @DVLADrivingLicence_test @build @staging @integration
  Scenario Outline: DVLA Driving Licence - No date in the Valid to date field error validation
    Given User enters DVLA data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then I can see the valid to date error in the error summary as Enter the date as it appears on your driving licence
    And I can see the Valid to date field error as Error:Enter the date as it appears on your driving licence
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |NoValidToDate |

  @DVLADrivingLicence_test @build @staging @integration
  Scenario Outline: DVLA Driving Licence number less than 16 characters error validation
    Given User enters DVLA data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then I see the licence number error in the summary as Your licence number should be 16 characters long
    And I can see the licence number error in the field as Error:Your licence number should be 16 characters long
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject      |
      |DrivingLicenceNumLessThan16Char |

  @DVLADrivingLicence_test @build @staging @integration
  Scenario Outline: DVLA Driving Licence number with special characters and spaces error validation
    Given User enters DVLA data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then I see the licence number error in the summary as Your licence number should not include any symbols or spaces
    And I can see the licence number error in the field as Error:Your licence number should not include any symbols or spaces
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |DrivingLicenceNumberWithSpecialChar |

  @DVLADrivingLicence_test @build @staging @integration
  Scenario Outline: DVLA Driving Licence number with numeric characters error validation
    Given User enters DVLA data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then I see the licence number error in the summary as Enter the number exactly as it appears on your driving licence
    And I can see the licence number error in the field as Error:Enter the number exactly as it appears on your driving licence
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |DrivingLicenceNumberWithNumericChar |

  @DVLADrivingLicence_test @build @staging @integration
  Scenario Outline: DVLA Driving Licence number with alpha characters error validation
    Given User enters DVLA data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then I see the licence number error in the summary as Enter the number exactly as it appears on your driving licence
    And I can see the licence number error in the field as Error:Enter the number exactly as it appears on your driving licence
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |DrivingLicenceNumberWithAlphaChar |

  @DVLADrivingLicence_test @build @staging @integration
  Scenario Outline: DVLA Driving Licence - No Licence number in the Licence number field error validation
    Given User enters DVLA data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then I see the licence number error in the summary as Enter the number exactly as it appears on your driving licence
    And I can see the licence number error in the field as Error:Enter the number exactly as it appears on your driving licence
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |NoDrivingLicenceNumber |

  @DVLADrivingLicence_test @build @staging @integration
  Scenario Outline: DVLA Driving Licence Issue number less than 2 characters error validation
    Given User enters DVLA data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then I see the issue number error in summary as Your issue number should be 2 numbers long
    And I see the issue number error in field as Error:Your issue number should be 2 numbers long
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |IssueNumberLessThan2Char |

  @DVLADrivingLicence_test @build @staging @integration
  Scenario Outline: DVLA Driving Licence Issue number with special characters error validation
    Given User enters DVLA data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then I see the issue number error in summary as Your issue number should not include any symbols or spaces
    And I see the issue number error in field as Error:Your issue number should not include any symbols or spaces
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |IssueNumberWithSpecialChar |

  @DVLADrivingLicence_test @build @staging @integration
  Scenario Outline: DVLA Driving Licence Issue number with alphanumeric characters error validation
    Given User enters DVLA data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then I see the issue number error in summary as Enter the issue number as it appears on your driving licence
    And I see the issue number error in field as Error:Enter the issue number as it appears on your driving licence
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |IssueNumberWithAlphanumericChar |

  @DVLADrivingLicence_test @build @staging @integration
  Scenario Outline: DVLA Driving Licence Issue number with alpha characters error validation
    Given User enters DVLA data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then I see the issue number error in summary as Enter the issue number as it appears on your driving licence
    And I see the issue number error in field as Error:Enter the issue number as it appears on your driving licence
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |IssueNumberWithAlphaChar |

  @DVLADrivingLicence_test @build @staging @integration
  Scenario Outline: DVLA Driving Licence - No Issue number in the Issue number field error validation
    Given User enters DVLA data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then I see the issue number error in summary as Enter the issue number as it appears on your driving licence
    And I see the issue number error in field as Error:Enter the issue number as it appears on your driving licence
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |NoIssueNumber |

  @DVLADrivingLicence_test @build @staging @integration
  Scenario Outline: DVLA Driving Licence Postcode less than 5 characters error validation
    Given User enters DVLA data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then I see the postcode error in summary as Your postcode should be between 5 and 7 characters
    And I see the postcode error in the field as Error:Your postcode should be between 5 and 7 characters
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |PostcodeLessThan5Char |

  @DVLADrivingLicence_test @build @staging @integration
  Scenario Outline: DVLA Driving Licence Postcode with special characters error validation
    Given User enters DVLA data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then I see the postcode error in summary as Your postcode should only include numbers and letters
    And I see the postcode error in the field as Error:Your postcode should only include numbers and letters
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |PostcodeWithSpecialChar |

  @DVLADrivingLicence_test @build @staging @integration
  Scenario Outline: DVLA Driving Licence Postcode with numeric characters error validation
    Given User enters DVLA data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then I see the postcode error in summary as Your postcode should include numbers and letters
    And I see the postcode error in the field as Error:Your postcode should include numbers and letters
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |PostcodeWithNumericChar |

  @DVLADrivingLicence_test @build @staging @integration
  Scenario Outline: DVLA Driving Licence Postcode with alpha characters error validation
    Given User enters DVLA data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then I see the postcode error in summary as Your postcode should include numbers and letters
    And I see the postcode error in the field as Error:Your postcode should include numbers and letters
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |PostcodeWithAlphaChar |

  @DVLADrivingLicence_test @build @staging @integration
  Scenario Outline: DVLA Driving Licence - No Postcode in the Postcode field error validation
    Given User enters DVLA data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then I see the postcode error in summary as Enter your postcode
    And I see the postcode error in the field as Error:Enter your postcode
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |NoPostcode |

  @DVLADrivingLicence_test @build @staging @integration
  Scenario Outline: DVLA Driving Licence International Postcode error validation
    Given User enters DVLA data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then I see the postcode error in summary as Enter a UK postcode
    And I see the postcode error in the field as Error:Enter a UK postcode
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |InternationalPostcode |

  @DVLADrivingLicence_test @build @staging @integration @smoke
  Scenario Outline:  DVLA Driving Licence Generate VC with invalid DL number and prove in another way unhappy path
    Given User enters DVLA data as a <DrivingLicenceSubject>
    When User clicks on continue
    When User click on ‘prove your identity another way' Link
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON response should contain personal number PARKE610112PBFGI same as given Driving Licence
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject             |
      | IncorrectDrivingLicenceNumber    |

  @DVLADrivingLicence_test @build @staging @integration @smoke
  Scenario Outline:  DVLA Driving Licence error validation when DVLA consent checkbox is unselected
    Given User enters DVLA data as a <DrivingLicenceSubject>
    And DVLA consent checkbox is unselected
    When User clicks on continue
    Then User can see the DVLA consent error in summary as You must give your consent to continue
    And User can see the DVLA consent error on the checkbox as Error:You must give your consent to continue
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject             |
      |DrivingLicenceSubjectHappyPeter   |

  @DVLADrivingLicence_test @build @staging @integration @smoke
  Scenario Outline:  DVLA Driving Licence number validation test - Correct licence number structure
    Given User enters DVLA data as a <DrivingLicenceSubject>
    Then User clears the driving licence number and enters the new value as DECER657085K99LN
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    And The test is complete and I close the driver
    Examples:
      | DrivingLicenceSubject |
      | DrivingLicenceSubjectHappyKenneth         |

  @DVLADrivingLicence_test @build @staging @integration @smoke
  Scenario Outline:  DVLA Driving Licence number validation test - (VALID, surname > 5)
    Given User enters DVLA data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And The test is complete and I close the driver
    Examples:
      | DrivingLicenceSubject             |
      | DrivingLicenceSubjectHappyKenneth |

  @DVLADrivingLicence_test @build @staging @integration @smoke
  Scenario Outline:  DVLA Driving Licence number validation test - (VALID, female licenceNumber DOB Jan)
    Given User enters DVLA data as a <DrivingLicenceSubject>
    Then User clears the driving licence number and enters the new value as DECER651085K99LN
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    And The test is complete and I close the driver
    Examples:
      | DrivingLicenceSubject |
      | KennethDOBJan         |

  @DVLADrivingLicence_test @build @staging @integration @smoke
  Scenario Outline:  DVLA Driving Licence number validation test - (VALID, female licenceNumber DOB Dec)
    Given User enters DVLA data as a <DrivingLicenceSubject>
    Then User clears the driving licence number and enters the new value as DECER662085K99LN
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    And The test is complete and I close the driver
    Examples:
      | DrivingLicenceSubject |
      | KennethDOBDec         |

  @DVLADrivingLicence_test @build @staging @integration @smoke
  Scenario Outline:  DVLA Driving Licence number validation test - (VALID, licenceNumber DOB Dec)
    Given User enters DVLA data as a <DrivingLicenceSubject>
    Then User clears the driving licence number and enters the new value as DECER612085KE9LN
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    And The test is complete and I close the driver
    Examples:
      | DrivingLicenceSubject |
      | KennethDOBDec         |

  @DVLADrivingLicence_test @build @staging @integration @smoke
  Scenario Outline:  DVLA Driving Licence number validation test - (VALID, licenceNumber DOB Jan)
    Given User enters DVLA data as a <DrivingLicenceSubject>
    Then User clears the driving licence number and enters the new value as DECER601085KE9LN
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    And The test is complete and I close the driver
    Examples:
      | DrivingLicenceSubject |
      | KennethDOBJan         |

  @DVLADrivingLicence_test @build @staging @integration @smoke
  Scenario Outline:  DVLA Driving Licence number validation test - (VALID, 1 forename)
    Given User enters DVLA data as a <DrivingLicenceSubject>
    Then User clears the driving licence number and enters the new value as AB999607085J9AAA
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    And The test is complete and I close the driver
    Examples:
      | DrivingLicenceSubject |
      | JohnSmithHappy        |

  @DVLADrivingLicence_test @build @staging @integration @smoke
  Scenario Outline:  DVLA Driving Licence number validation test - (VALID, surname < 5)
    Given User enters DVLA data as a <DrivingLicenceSubject>
    Then User clears the driving licence number and enters the new value as AB999607085J9AAA
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    And The test is complete and I close the driver
    Examples:
      | DrivingLicenceSubject |
      | JohnShortSurname      |

  @DVLADrivingLicence_test @build @staging @integration @smoke
  Scenario Outline:  DVLA Driving Licence number validation test - (VALID, 2 forenames)
    Given User enters DVLA data as a <DrivingLicenceSubject>
    Then User clears the driving licence number and enters the new value as AB999607085JAAAA
    When User clicks on continue
    Then Proper error message for Could not find your details is displayed
    And The test is complete and I close the driver
    Examples:
      | DrivingLicenceSubject |
      | JohnTwoForename       |

  @DVLADrivingLicence_test @build @staging @integration @smoke
  Scenario: DVLA Driving Licence privacy notice link to consent
    Then I see the consent section Allow DVLA to check your driving licence details
    And I see the sentence DVLA needs your consent to check your driving licence details before you can continue. They will make sure your licence has not been cancelled or reported as lost or stolen.
   And I see the second line To find out more about how your driving licence details will be used, you can read:
    And I see privacy notice link the GOV.UK One Login privacy notice (opens in a new tab)
    Then I see the DVLA privacy notice link the DVLA privacy notice (opens in a new tab)
    And The test is complete and I close the driver
