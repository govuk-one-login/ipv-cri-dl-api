@QualityGateRegressionTest @QualityGateIntegrationTest
Feature: Driving License Test Common - API

  @build-api @staging-api
  Scenario: GET request to well-known/jwks endpoint returns single public key
    Given User sends a GET request to the well-known jwks endpoint

  @build-api @staging-api
  Scenario Outline: Public API endpoints that are not well known cannot be accessed (issuer/token)
    Given User sends a basic POST request to public <endpoint_name> endpoint without apiKey they get a forbidden error

    Examples:
      | endpoint_name     |
      | /token            |
      | /credential/issue |

  @stub @uat
  Scenario Outline: Driving Licence Auth Source - Negative Scenario - No Shared Claims sent in request to Driving Licence CRI
    Given I navigate to the IPV Core Stub and select Driving Licence CRI for the testEnvironment
    And I enter the context value <contextValue> in the Input context value as a string
    And I click the Go to Driving Licence CRI button
    Then I navigate to the Driving Licence verifiable issuer to check for a Invalid response
    And JSON response should contain error description Unexpected server error and status code as 302

    Examples:
      | contextValue  |
      | check_details |
