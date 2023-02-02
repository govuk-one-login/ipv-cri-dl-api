Feature: Driving License Test

 Background:
    Given I navigate to the IPV Core Stub
    And I click the Driving Licence CRI for the testEnvironment
    Then I search for Driving Licence user number 5 in the Experian table
    Then I check the page title who was your UK driving license issued by?
    And I assert the URL is valid

 @DrivingLicenceTest @build @staging @integration @smoke
  Scenario:3 options and Radio button available in Driving Licence page
    Given I can see a radio button titled “DVLA”
    Then I can see a radio button titled “DVA”
    And I can see a radio button titled “I do not have a UK driving licence”
    Then I can see CTA "continue"
    And The test is complete and I close the driver

  @DrivingLicenceTest @build @staging @integration
  Scenario:User Selects DVLA and landed in DVLA page
    Given I click on DVLA radio button and Continue
    Then I should on the page DVLA Enter your details exactly as they appear on your UK driving licence
    And The test is complete and I close the driver

  @DrivingLicenceTest @build @staging @integration
  Scenario:User Selects DVA and landed in DVA page
    Given I click on DVA radio button and Continue
    Then I should be on the page DVA Enter your details exactly as they appear on your UK driving licence
    And The test is complete and I close the driver

  @DrivingLicenceTest
  Scenario:User selects no Driving Licence and landed in IPV Core
    Given I click I do not have UK Driving License and continue
    When I am directed to the IPV Core routing page
    And I validate the URL having access denied
    Then I navigate to the Driving Licence verifiable issuer to check for a Invalid response
    And JSON response should contain error description Authorization permission denied and status code as 302
    And The test is complete and I close the driver

  @DrivingLicenceTest @build @staging @integration
  Scenario: User continue with no selection and see the error displayed
    Given I have not selected anything and Continue
    When I can see an error box highlighted red
    And An error heading copy “You must choose an option to continue”
    Then I can select a link which directs to the problem field
    And The field error copy “You must choose an option to continue”
    And The test is complete and I close the driver

  @DVLADrivingLicence_test @build @staging @integration
  Scenario: Check the Unrecoverable error/ Unknown error in Driving Licence CRI
    Given I delete the cookie to get the unexpected error
    When I can see the relevant error page with correct title
    Then I can see the heading  Sorry, there is a error
    And The test is complete and I close the driver
