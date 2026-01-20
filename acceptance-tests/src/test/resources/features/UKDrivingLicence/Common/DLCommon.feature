@QualityGateRegressionTest @QualityGateIntegrationTest
Feature: Driving License Test Common

  Background:
    Given I navigate to the IPV Core Stub
    And I click the Driving Licence CRI for the testEnvironment
    Then I search for Driving Licence user number 5 in the Experian table
    And I assert the url path contains licence-issuer
    Then I check the page title is Was your UK photocard driving licence issued by DVLA or DVA? – GOV.UK One Login
    And I see ‘Why we need to know this’ component is present
    When I click the drop-down on the component
    Then I see the message begins with We need to make sure is shown
    And I assert the url path contains licence-issuer

  @DrivingLicenceTest @build @staging @integration @stub
  Scenario: Three selection options and Radio button available on the Driving Licence page
    Given I can see a DVLA radio button titled DVLA
    Then I can see a DVA radio button titled DVA
    And I can see a I do not have a UK driving licence radio button titled I do not have a UK photocard driving licence
    Then I can see CTA Continue
    And The test is complete and I close the driver

  @DrivingLicenceTest
  Scenario: User selects no Driving Licence and landed in IPV Core
    Given I click I do not have UK Driving License and continue
    When I am directed to the IPV Core routing page
    And I validate the URL having access denied
    Then I navigate to the Driving Licence verifiable issuer to check for a Invalid response
    And JSON response should contain error description Authorization permission denied and status code as 302
    And The test is complete and I close the driver

  @DrivingLicenceTest @build @staging @integration @stub
  Scenario: User continue with no selection and see the error displayed
    Given I have not selected anything and continue
    When I can see an error box highlighted red
    And An error heading copy You must choose an option to continue
    Then I can select a link which directs to the problem field
    And The field error copy Error:You must choose an option to continue
    And The test is complete and I close the driver

  @DrivingLicenceTest @build @staging @integration @stub
  Scenario: Check the Unrecoverable error/ Unknown error in Driving Licence CRI
    Given I delete the service_session cookie to get the unexpected error
    When I check the page title is Sorry, there is a problem – GOV.UK One Login
    Then I can see the error heading Sorry, there is a problem
    And The test is complete and I close the driver
