Feature: Driving License Test Common

  Background:
    Given I navigate to the IPV Core Stub
    And I click the Driving Licence CRI for the testEnvironment
    Then I search for Driving Licence user number 5 in the Experian table
    Then I check the page title is Who was your UK driving licence issued by? – Prove your identity – GOV.UK
    And I see ‘Why we need to know this’ component is present
    When I click the drop-down on the component
    Then I see the message begins with We need to make sure is shown
    And I assert the url path contains licence-issuer

  @DrivingLicenceTest @build @staging @integration @smoke
  Scenario:3 options and Radio button available in Driving Licence page
    Given I can see a DVLA radio button titled DVLA
    Then I can see a DVA radio button titled DVA
    And I can see a I do not have a UK driving licence radio button titled I do not have a UK driving licence
    Then I can see CTA Continue
    And The test is complete and I close the driver

  @DrivingLicenceTest @build @staging @integration @smoke
  Scenario: Beta Banner Reject Analysis
    When I view the Beta banner
    When the beta banner reads This is a new service – your feedback (opens in new tab) will help us to improve it.
    And I select Reject Analysis cookie
    Then I see the Reject Analysis sentence You’ve rejected additional cookies. You can change your cookie settings at any time.
    And  I select the ‘change your cookie settings’ link
    Then I check the page to change cookie preferences opens
    Then The test is complete and I close the driver

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
    And An error heading copy You must choose an option to continue
    Then I can select a link which directs to the problem field
    And The field error copy Error:You must choose an option to continue
    And The test is complete and I close the driver

  @DrivingLicenceTest @build @staging @integration
  Scenario: Check the Unrecoverable error/ Unknown error in Driving Licence CRI
    Given I delete the service_session cookie to get the unexpected error
    When I check the page title is Sorry, there is a problem – Prove your identity – GOV.UK
    Then I can see the error heading Sorry, there is a problem
    And The test is complete and I close the driver