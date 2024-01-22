Feature: Prove Your Identity Full Journey Common

  Background:
    Given I navigate to the Orchestrator Stub
    And I click on Full journey route and Continue
    And clicks continue on the signed into your GOV.UK One Login page

  @dlProveYourIdentityFullJourney
  Scenario: IPV Core Routing To Driving Licence CRI - 3 Options Available In 'Who was your UK driving licence issued by?' Page (STUB)
    When I select the radio option UK driving licence and click on Continue
    And I check the page title is Who was your UK driving licence issued by? – Prove your identity – GOV.UK
    And I can see a radio button titled “DVLA”
    Then I can see a radio button titled “DVA”
    And I can see a I do not have a UK driving licence radio button titled I do not have a UK driving licence
    Then I can see CTA "continue"
    And The test is complete and I close the driver

  @dlProveYourIdentityFullJourney
  Scenario: IPV Core Unsuccessful Routing To Driving Licence CRI (STUB)
    When I click on the Prove your identity another way radio button and click on Continue
#    Then I check the page title is Sorry, there is a problem – – GOV.UK
#    And I can see the heading  Sorry, there is a error
    And The test is complete and I close the driver

  @dlProveYourIdentityFullJourney
  Scenario: User Does Not Have UK Driving Licence - Unhappy Path (STUB)
    Given I select the radio option UK driving licence and click on Continue
    And I check the page title is Who was your UK driving licence issued by? – Prove your identity – GOV.UK
    When I click I do not have UK Driving License and continue
    Then I should be navigated to How do you want to continue? page
    And The test is complete and I close the driver