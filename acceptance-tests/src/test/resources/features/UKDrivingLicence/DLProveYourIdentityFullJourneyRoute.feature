Feature: Prove Your Identity Full Journey

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

############  DVA  ############

  @dlProveYourIdentityFullJourney
  Scenario Outline: DVA Driving Licence Prove Your Identity Full Journey Route Happy Path (STUB)
    And I select the radio option UK driving licence and click on Continue
    And I check the page title is Who was your UK driving licence issued by? – Prove your identity – GOV.UK
    And I click on DVA radio button and Continue
    And I check the page title is Enter your details exactly as they appear on your UK driving licence – Prove your identity – GOV.UK
    And I set the document checking route
    When User enters DVA data as a <DVADrivingLicenceSubject>
    And User clicks on continue
    And I enter BA2 5AA in the Postcode field and find address
    And the user chooses their address 8 HADLEY ROAD, BATH, BA2 5AA from dropdown and click `Choose address`
    And the user enters the date 2014 they moved into their current address
    And the user clicks `I confirm my details are correct`
    Then I navigate to the page We need to check your details – Prove your identity – GOV.UK
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
    And I check the page title is Who was your UK driving licence issued by? – Prove your identity – GOV.UK
    And I click on DVA radio button and Continue
    And I check the page title is Enter your details exactly as they appear on your UK driving licence – Prove your identity – GOV.UK
    When User enters invalid Driving Licence DVA details
    And User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User Re-enters DVA data as a <DVADrivingLicenceSubject>
    And User clicks on continue
    And I enter BA2 5AA in the Postcode field and find address
    And the user chooses their address 8 HADLEY ROAD, BATH, BA2 5AA from dropdown and click `Choose address`
    And the user enters the date 2014 they moved into their current address
    And the user clicks `I confirm my details are correct`
    Then I navigate to the page We need to check your details – Prove your identity – GOV.UK
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
    And I check the page title is Who was your UK driving licence issued by? – Prove your identity – GOV.UK
    And I click on DVA radio button and Continue
    And I check the page title is Enter your details exactly as they appear on your UK driving licence – Prove your identity – GOV.UK
    When User enters invalid Driving Licence DVA details
    And User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User Re-enters DVA data as a <DVADrivingLicenceSubject>
    And User clicks on continue
    Then I check the page title is Sorry, we cannot prove your identity – GOV.UK
    And I can see the error heading Sorry, we cannot prove your identity
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject |
      |IncorrectDrivingLicenceNumber |

  @dlProveYourIdentityFullJourney
  Scenario: DVA Prove Your Identity Full Journey Route - Back Button From DVA Details Entry Screen To Licence Issuer Page
    Given I select the radio option UK driving licence and click on Continue
    And I check the page title is Who was your UK driving licence issued by? – Prove your identity – GOV.UK
    And I click on DVA radio button and Continue
    And I check the page title is Enter your details exactly as they appear on your UK driving licence – Prove your identity – GOV.UK
    When User click on ‘Back' Link
    And I check the page title is Who was your UK driving licence issued by? – Prove your identity – GOV.UK
    And The test is complete and I close the driver

############  DVLA  ############

  @dlProveYourIdentityFullJourney
  Scenario Outline: DVLA Driving Licence Prove Your Identity Full Journey Route Happy Path (STUB)
    Given I select the radio option UK driving licence and click on Continue
    And I check the page title is Who was your UK driving licence issued by? – Prove your identity – GOV.UK
    And I click on DVLA radio button and Continue
    And I check the page title is Enter your details exactly as they appear on your UK driving licence – Prove your identity – GOV.UK
    When User enters DVLA data as a <DrivingLicenceSubject>
    And User clicks on continue
    And I enter BA2 5AA in the Postcode field and find address
    And the user chooses their address 8 HADLEY ROAD, BATH, BA2 5AA from dropdown and click `Choose address`
    And the user enters the date 2014 they moved into their current address
    And the user clicks `I confirm my details are correct`
    Then I navigate to the page We need to check your details – Prove your identity – GOV.UK
    And User clicks on continue
    And the user clicks `Answer security questions`
    And kenneth answers the first question correctly
    And kenneth answers the second question correctly
    And kenneth answers the third question correctly
    When the user clicks `I confirm my details are correct`
    And driving licence VC should contain validity score 2 and strength score 3
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject             |
      |DrivingLicenceSubjectHappyKenneth |

  @dlProveYourIdentityFullJourney
  Scenario Outline: DVLA Prove Your Identity Full Journey Route - Retry Test Happy Path
    Given I select the radio option UK driving licence and click on Continue
    And I check the page title is Who was your UK driving licence issued by? – Prove your identity – GOV.UK
    And I click on DVLA radio button and Continue
    And I check the page title is Enter your details exactly as they appear on your UK driving licence – Prove your identity – GOV.UK
    When User enters invalid Driving Licence DVLA details
    And User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User Re-enters DVLA data as a <DrivingLicenceSubject>
    And User clicks on continue
    And I enter BA2 5AA in the Postcode field and find address
    And the user chooses their address 8 HADLEY ROAD, BATH, BA2 5AA from dropdown and click `Choose address`
    And the user enters the date 2014 they moved into their current address
    And the user clicks `I confirm my details are correct`
    Then I navigate to the page We need to check your details – Prove your identity – GOV.UK
    And User clicks on continue
    And the user clicks `Answer security questions`
    And kenneth answers the first question correctly
    And kenneth answers the second question correctly
    And kenneth answers the third question correctly
    When the user clicks `I confirm my details are correct`
    And driving licence VC should contain validity score 2 and strength score 3
    And The test is complete and I close the driver
    Examples:
      | DrivingLicenceSubject |
      | DrivingLicenceSubjectHappyKenneth |

  @dlProveYourIdentityFullJourney
  Scenario Outline: DVLA Prove Your Identity Full Journey Route Unhappy Path - User failed Second Attempt
    Given I select the radio option UK driving licence and click on Continue
    And I check the page title is Who was your UK driving licence issued by? – Prove your identity – GOV.UK
    And I click on DVLA radio button and Continue
    And I check the page title is Enter your details exactly as they appear on your UK driving licence – Prove your identity – GOV.UK
    When User enters invalid Driving Licence DVLA details
    And User clicks on continue
    Then Proper error message for Could not find your details is displayed
    When User Re-enters DVLA data as a <DrivingLicenceSubject>
    And User clicks on continue
    Then I check the page title is Sorry, we cannot prove your identity – GOV.UK
    And I can see the error heading Sorry, we cannot prove your identity
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |IncorrectDrivingLicenceNumber |

  @dlProveYourIdentityFullJourney
  Scenario: DVLA Prove Your Identity Full Journey Route - Back Button From DVLA Details Entry Screen To Licence Issuer Page
    Given I select the radio option UK driving licence and click on Continue
    And I check the page title is Who was your UK driving licence issued by? – Prove your identity – GOV.UK
    And I click on DVLA radio button and Continue
    And I check the page title is Enter your details exactly as they appear on your UK driving licence – Prove your identity – GOV.UK
    When User click on ‘Back' Link
    And I check the page title is Who was your UK driving licence issued by? – Prove your identity – GOV.UK
    And The test is complete and I close the driver