Feature: Prove Your Identity Full Journey

  Background:
    Given I navigate to the Orchestrator Stub
    And The user chooses the environment STAGING from dropdown
    And I click on Full journey route and Continue
    And I select the radio option UK driving licence and click on Continue
    And clicks continue on the signed into your GOV.UK One Login page

  @dlProveYourIdentityFullJourney
  Scenario Outline: DVA Driving Licence Prove Your Identity Full Journey Route Happy Path (STUB)
    And I select the radio option UK driving licence and click on Continue
    And I check the page title is Who was your UK driving licence issued by? – GOV.UK One Login
    And I click on DVA radio button and Continue
    And I check the page title is Enter your details exactly as they appear on your UK driving licence – GOV.UK One Login
    When User enters DVA data as a <DVADrivingLicenceSubject>
    And User clicks on continue
    And I enter BA2 5AA in the Postcode field and find address
    And the user chooses their address 8 HADLEY ROAD, BATH, BA2 5AA from dropdown and click `Choose address`
    And the user enters the date 2014 they moved into their current address
    And the user clicks `I confirm my details are correct`
    Then I navigate to the page We need to check your details – GOV.UK One Login
    And User clicks on continue
    And the user clicks `Answer security questions`
    And kenneth answers the first question correctly
    And kenneth answers the second question correctly
    And kenneth answers the third question correctly
    When the user clicks `I confirm my details are correct`
    And driving licence VC should contain validity score 2 and strength score 3
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject             |
      |DVADrivingLicenceSubjectHappyKenneth   |

  @dlProveYourIdentityFullJourney
  Scenario Outline: DVA Prove Your Identity Full Journey Route - Retry Test Happy Path
    Given I select the radio option UK driving licence and click on Continue
    And I check the page title is Who was your UK driving licence issued by? – GOV.UK One Login
    And I click on DVA radio button and Continue
    And I check the page title is Enter your details exactly as they appear on your UK driving licence – GOV.UK One Login
    When User enters invalid Driving Licence DVA details
    And User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User Re-enters DVA data as a <DVADrivingLicenceSubject>
    And User clicks on continue
    And I enter BA2 5AA in the Postcode field and find address
    And the user chooses their address 8 HADLEY ROAD, BATH, BA2 5AA from dropdown and click `Choose address`
    And the user enters the date 2014 they moved into their current address
    And the user clicks `I confirm my details are correct`
    Then I navigate to the page We need to check your details – GOV.UK One Login
    And User clicks on continue
    And the user clicks `Answer security questions`
    And kenneth answers the first question correctly
    And kenneth answers the second question correctly
    And kenneth answers the third question correctly
    When the user clicks `I confirm my details are correct`
    And driving licence VC should contain validity score 2 and strength score 3
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |DVADrivingLicenceSubjectHappyKenneth |

  @dlProveYourIdentityFullJourney
  Scenario Outline: DVA Prove Your Identity Full Journey Route Unhappy Path - User Failed Second Attempt
    Given I select the radio option UK driving licence and click on Continue
    And I check the page title is Who was your UK driving licence issued by? – GOV.UK One Login
    And I click on DVA radio button and Continue
    And I check the page title is Enter your details exactly as they appear on your UK driving licence – GOV.UK One Login
    When User enters invalid Driving Licence DVA details
    And User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User Re-enters DVA data as a <DVADrivingLicenceSubject>
    And User clicks on continue
    Then I check the page title is Sorry, you’ll need to prove your identity another way – GOV.UK One Login
    And I can see the error heading Sorry, you’ll need to prove your identity another way
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |IncorrectDrivingLicenceNumber |

  @dlProveYourIdentityFullJourney
  Scenario: DVA Prove Your Identity Full Journey Route - Back Button From DVA Details Entry Screen To Licence Issuer Page
    Given I select the radio option UK driving licence and click on Continue
    And I check the page title is Who was your UK driving licence issued by? – GOV.UK One Login
    And I click on DVA radio button and Continue
    And I check the page title is Enter your details exactly as they appear on your UK driving licence – GOV.UK One Login
    When User click on ‘Back' Link
    And I check the page title is Who was your UK driving licence issued by? – GOV.UK One Login
    And The test is complete and I close the driver
