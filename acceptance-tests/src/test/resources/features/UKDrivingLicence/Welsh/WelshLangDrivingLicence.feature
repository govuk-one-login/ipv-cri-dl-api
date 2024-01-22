Feature: Driving License Language Test

  Background:
    Given I navigate to the IPV Core Stub
    And I click the Driving Licence CRI for the testEnvironment
    Then I search for Driving Licence user number 5 in the Experian table
    And I add a cookie to change the language to Welsh
    And I assert the URL is valid in Welsh

  @Language-regression
  Scenario: Beta Banner
    Given I view the Beta banner
    When the beta banner reads Mae hwn yn wasanaeth newydd – bydd eich adborth (agor mewn tab newydd) yn ein helpu i'w wella.
    Then The test is complete and I close the driver

  @Language-regression
  Scenario Outline: DVLA Error tab title validation
    Given I click on DVLA radio button and Parhau
    Then I check the page title is Rhowch eich manylion yn union fel maent yn ymddangos ar eich trwydded yrru – Profi pwy ydych chi – GOV.UK
    Then User enters DVLA data as a <DrivingLicenceSubject>
    And User clicks on continue
    Then I check the page title is Gwall: Rhowch eich manylion yn union fel maent yn ymddangos ar eich trwydded yrru – Profi pwy ydych chi – GOV.UK
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |InvalidDateOfBirth |

#To be added to front end repo
  @Language-regression
  Scenario: DVAError tab title validation
    Given I click on DVA radio button and Parhau
    Then I check the page title is Rhowch eich manylion yn union fel maent yn ymddangos ar eich trwydded yrru – Profi pwy ydych chi – GOV.UK
    When I enter the invalid Postcode
    And User clicks on continue
    Then I check the page title is Gwall: Rhowch eich manylion yn union fel maent yn ymddangos ar eich trwydded yrru – Profi pwy ydych chi – GOV.UK
    And The test is complete and I close the driver

  #not existing in front end repo
  @Language-regression
  Scenario: DVLA Driving Licence privacy notice link to consent
    Given I click on DVLA radio button and Parhau
    Then I see the consent section Caniatau DVLA i wirio eich manylion trwydded yrru
    And I see the sentence Mae DVLA angen eich caniatâd i wirio eich manylion trwydded yrru cyn y gallwch barhau. Byddant yn sicrhau nad yw eich trwydded wedi cael ei chanslo na'i hadrodd fel un sydd ar goll neu wedi'i dwyn.
    And I see the second line I ddarganfod mwy am sut bydd eich manylion trwydded yrru yn cael eu defnyddio, gallwch ddarllen:
    And I see privacy notice link hysbysiad preifatrwydd GOV.UK One Login (agor mewn tab newydd)
    Then I see the DVLA privacy notice link hysbysiad preifatrwydd DVLA (agor mewn tab newydd)
    And The test is complete and I close the driver

  #not existing in front end repo
  @Language-regression
  Scenario: DVA Driving Licence privacy notice link to consent
    Given I click on DVA radio button and Parhau
    Then I see the DVA consent section Caniatau DVA i wirio eich manylion trwydded yrru
    And I see the Consent sentence in DVA page Mae DVA angen eich caniatâd i wirio eich manylion trwydded yrru cyn y gallwch barhau. Byddant yn sicrhau nad yw eich trwydded wedi cael ei chanslo na'i hadrodd fel un sydd ar goll neu wedi'i dwyn.
    And I see the Consent second line in DVA page I ddarganfod mwy am sut bydd eich manylion trwydded yrru yn cael eu defnyddio, gallwch ddarllen:
    And I see privacy DVA notice link hysbysiad preifatrwydd GOV.UK One Login (agor mewn tab newydd)
    Then I see the DVA privacy notice link hysbysiad preifatrwydd DVA (agor mewn tab newydd)
    And The test is complete and I close the driver