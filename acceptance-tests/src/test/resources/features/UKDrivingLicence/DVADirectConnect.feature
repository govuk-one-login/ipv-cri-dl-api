Feature: DVA Driving Licence Test

  Background:
    Given I navigate to the IPV Core Stub
    And I click the Driving Licence CRI for the testEnvironment
    And I search for Driving Licence user number 5 in the Experian table
    And I should be on `Who was your UK driving licence issued by? - Prove your identity - GOV.UK` page
    And I click on DVA radio button and Continue
    And I should be on DVA `Enter your details exactly as they appear on your UK driving licence - Prove your identity - GOV.UK` page

  @DVADrivingLicence_test @build @staging @integration @smoke
  Scenario Outline:  DVA Driving Licence details page happy path
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    And DVA Direct is passed in request header
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain validity score 2 and strength score 3
    And JSON response should contain personal number 55667788 same as given Driving Licence
    And exp should not be present in the JSON payload
    Examples:
      |DVADrivingLicenceSubject             |
      |DVADrivingLicenceSubjectHappyBilly   |

  @DVADrivingLicence_test
  Scenario Outline: DVA Driving Licence details page unhappy path with InvalidDVADrivingLicenceDetails
    Given User enters DVA data as a <DVADrivingLicenceSubject>
    When User clicks on continue
    And DVA Direct is passed in request header
    Then I navigate to the Driving Licence verifiable issuer to check for a Invalid response
    And JSON response should contain error description Authorization permission denied and status code as 302
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject      |
      |DVADrivingLicenceSubjectUnhappySelina |

