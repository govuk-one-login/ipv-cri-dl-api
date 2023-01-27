Feature: Driving License Test

  Background: @happy_path @build
    Given I navigate to the IPV Core Stub
    And I click the Driving Licence CRI for the testEnvironment
    Then I search for Driving Licence user number 5 in the Experian table
    And I add a cookie to change the language to Welsh
    Then I check the page title Pwy wnaeth gyhoeddi eich trwydded yrru y DU?
    And I assert the URL is valid in Welsh

  @DrivingLicence_test @build
  Scenario: Beta Banner
    Given I view the Beta banner
    When the text reads “BETA Mae hwn yn wasanaeth newydd – bydd eich adborth (agor mewn tab newydd) yn ein helpu i'w wella.”
    Then The test is complete and I close the driver

  @DrivingLicenceTest
  Scenario:3 options and Radio button available in Driving Licence page
     Given I check the page title Pwy wnaeth gyhoeddi eich trwydded yrru y DU?
     When I assert the URL is valid in Welsh
     Then I can see a radio button titled “DVLA”
     Then I can see a radio button titled “DVA”
     And I can see a radio button titled “Nid oes gennyf drwydded yrru y DU”
     And I can see “Or”
     Then I can see CTA as Parhau
     And I see the sentence starts with “Gallwch ddod o hyd i hwn yn adran"
     And The test is complete and I close the driver

  @DVLADrivingLicenceTest
  Scenario:User Selects DVLA and landed in DVLA page and Validate the title and sentences
    Given I click on DVLA radio button and Parhau
    Then I should on the page DVLA and validate title
    And I see the heading Rhowch eich manylion yn union fel maent yn ymddangos ar eich trwydded yrru
    And I see the sentence Byddwn yn gwirio eich manylion gydar DVLA i sicrhau nad yw eich trwydded yrru wedi cael ei chanslo na'i hadrodd fel un sydd ar goll neu wedi ei dwyn.
    And I see the sentence below Os nad oes gennych drwydded yrru y DU neu os na allwch gofio'ch manylion, gallwch brofi pwy ydych chi mewn ffordd arall yn lle.
    And The test is complete and I close the driver

  @DVLADrivingLicenceTest
  Scenario Outline:Retry message
    Given I click on DVLA radio button and Parhau
    When User enters data as a <DrivingLicenceSubject>
    Then User clicks on Parhau
    And I see the error box “Gwiriwch bod eich manylion yn paru gyda beth sydd ar eich trwydded yrru y DU”
    Then I see “Gwall”
    And I see “Nid oeddem yn gallu dod o hyd i'ch manylion”
    And I see Roedd yna broblem wrth i ni wirio eich manylion gyda'r [DVLA]
    Then I see “Ni fyddwch yn gallu newid eich manylion eto os byddwch yn gwneud camgymeriad.”
    And The test is complete and I close the driver

    Examples:
      |DrivingLicenceSubject |
      |IncorrectIssueNumber|

  @DVLADrivingLicenceTest
  Scenario: DVLA Name fields
    Given I click on DVLA radio button and Parhau
    When I can see the lastname as Enw olaf
    And I can see Given name as Enwau a roddwyd
    And I can see First name as Enw cyntaf
    And I can see the middle name as Enwau canol
    And I can see the sentence “Mae hwn yn adran 2 och trwydded. Nid oes angen i chi gynnwys eich teitl.”
    And I can see the sentence “Gadewch hyn yn wag os nad oes gennych unrhyw enwau canol”
    And The test is complete and I close the driver

  @DVLADrivingLicenceTest
  Scenario: DVLA DoB Fields
    Given I click on DVLA radio button and Parhau
    When I can see the DoB fields titled “Dyddiad geni”
    When I can see example as Er enghraifft
    Then I can see date as “Diwrnod”
    And I can see month as “Mis”
    And I can see year as “Blwyddyn”
    And The test is complete and I close the driver

  @DVLADrivingLicenceTest
  Scenario: DVLA Issue date fields
    Given I click on DVLA radio button and Parhau
    When I can see the Issue date field titled “Dyddiad cyhoeddi”
    Then Dyma r dyddiad yn adran 4a o ch trwydded, er enghraifft 27 5 2019
    And I can see date as “Diwrnod”
    Then I can see month as “Mis”
    And I can see year as “Blwyddyn”
    And The test is complete and I close the driver

  @DVLADrivingLicenceTest
  Scenario: DVLA Valid to date field
    Given I click on DVLA radio button and Parhau
    When I can see the Valid to date field titled “Yn ddilys tan”
    And Dyma r dyddiad yn adran 4b o ch trwydded, er enghraifft 27 5 2019
    Then I can see date as “Diwrnod”
    And I can see month as “Mis”
    Then I can see year as “Blwyddyn”
    And The test is complete and I close the driver

  @DVLADrivingLicenceTest
  Scenario: DVLA Licence number
    Given I click on DVLA radio button and Parhau
    When I can see the licence number field titled “Rhif trwydded”
    Then I see the sentence “Dyma'r rhif hir yn adran  ar eich trwydded”
    And The test is complete and I close the driver

  @DVLADrivingLicenceTest
  Scenario: DVLA Issue number
    Given I click on DVLA radio button and Parhau
    When I can see the issue number field titled “Rhif cyhoeddi”
    And I  can see “Dyma r rhif  ddigid ar ôl y gofod yn adran  o'ch trwydded”
    And The test is complete and I close the driver

  @DVLADrivingLicenceTest
  Scenario: DVLA Postcode
    Given I click on DVLA radio button and Parhau
    When I can see the postcode field titled “Cod post”
    Then I can see “Rhowch y cod post yn y cyfeiriad yn adran  o ch trwydded”
    And The test is complete and I close the driver

  @DVLADrivingLicence_test @build
  Scenario Outline:  DVLA Driving Licence details page happy path
    Given I click on DVLA radio button and Parhau
    Then User enters data as a <DrivingLicenceSubject>
    When User clicks on Parhau
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain validity score 2 and strength score 3
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject             |
      |DrivingLicenceSubjectHappyPeter   |

  @DVADrivingLicence_test @build
  Scenario:User selects I do not have a UK driving licence - Nid oes gennyf drwydded yrru y DU and landed in IPV Core
    Given I click Nid oes gennyf drwydded yrru y DU and Parhau
    When I am directed to the IPV Core routing page
    And I validate the URL having access denied
    Then I navigate to the Driving Licence verifiable issuer to check for a Invalid response
    And JSON response should contain error description Authorization permission denied and status code as 302
    And The test is complete and I close the driver

  @DVLADrivingLicence_test @build
   Scenario: DVLA Driving Licence details Name field error message in Welsh
     Given I click on DVLA radio button and Parhau
     When I enter the invalid last name and first name
     When User clicks on Parhau
     Then the validation text reads “Mae problem”
     And I see “Rhowch eich enw olaf fel y mae'n ymddangos ar eich trwydded yrru”
     And I see “Rhowch eich enw cyntaf fel y mae'n ymddangos ar eich trwydded yrru”
     And I see “Rhowch unrhyw enwau canol fel y maent yn ymddangos ar eich trwydded yrru"
     And The test is complete and I close the driver

  @DVLADrivingLicence_test @build
  Scenario: DVLA Driving Licence details IncorrectDateOfBirth error message in Welsh
    Given I click on DVLA radio button and Parhau
    When I click Parhau without entering any details
    Then the validation text reads “Mae problem”
   # And I see “Rhowch y dyddiad fel y mae'n ymddangos ar eich trwydded yrru”
    Then I clear the data and re enter the date of birth
    And  User clicks on Parhau
    And I see “Gwiriwch eich bod wedi rhoi eich dyddiad geni yn gywir”
    Then I clear the data and re enter the date of birth to enter futureDOB
    And  User clicks on Parhau
    Then I see “Rhaid i'ch dyddiad geni fod yn y gorffennol”
    And The test is complete and I close the driver

  @DVLADrivingLicence_test @build
  Scenario: DVLA Driving Licence Issue date field error message in Welsh
    Given I click on DVLA radio button and Parhau
    When I enter the invalid issue date
    And  User clicks on Parhau
    And I see “Rhowch y dyddiad fel y mae'n ymddangos ar eich trwydded yrru”
    Then I clear the data and re enter the invalid future year
    And  User clicks on Parhau
    And  I see “Rhaid i ddyddiad cyhoeddi fod yn y gorffennol”
    And The test is complete and I close the driver

  @DVLADrivingLicence_test @build
  Scenario: DVLA Driving Licence Valid until date field error message in Welsh
    Given I click on DVLA radio button and Parhau
    When I enter the invalid Valid to date field
    And  User clicks on Parhau
    And I see “Rhowch y dyddiad fel y mae'n ymddangos ar eich trwydded yrru”
    Then I clear the data and re enter the valid to expired year
    And  User clicks on Parhau
    And I see Ni allwch ddefnyddio trwydded yrru sydd wedi dod i ben
    And The test is complete and I close the driver

  @DVLADrivingLicence_test @build
  Scenario: DVLA licence number field error message in Welsh
    Given I click on DVLA radio button and Parhau
    When I enter driving licence field empty
    Then User clicks on Parhau
    And I see “Rhowch y rhif yn union fel mae'n ymddangos ar eich trwydded yrru”x
    And I clear the licence number enter the invalid Driving Licence for DVLA
    Then User clicks on Parhau
    And I see “Dylai rhif eich trwydded fod yn [X] nod o hyd” for DVLA
    Then I clear the licence number and enter Driving Licence with Special Char for DVLA
    Then User clicks on Parhau
    And I see “Ni ddylai rhif eich trwydded gynnwys unrhyw symbolau neu ofodau”
    And The test is complete and I close the driver

  @DVLADrivingLicence_test @build
  Scenario: DVLA Issue number field error message in Welsh
    Given I click on DVLA radio button and Parhau
    When I enter inValid issue number
    Then User clicks on Parhau
    And I see “Dylai eich rhif cyhoeddi fod yn  rif o hyd”
    And I clear Issue number to see the error Enter Issue number
    Then User clicks on Parhau
    And I see “Rhowch y rhif cyhoeddi fel y mae'n ymddangos ar eich trwydded yrru”
    And The test is complete and I close the driver

  @DVLADrivingLicence_test @build
  Scenario: DVLA Postcode field error message in Welsh
    Given I click on DVLA radio button and Parhau
    When I clear the postcode to see the Enter your postcode error
    Then User clicks on Parhau
    And I see “Rhowch eich cod post”
    When I enter the invalid Postcode
    Then User clicks on Parhau
    And I see “Dylai eich rhowch eich cod post ond cynnwys rhifau a llythrennau yn unig”
    Then I clear the postcode and enter the less character postcode
    And User clicks on Parhau
    And I see “Dylai eich rhowch eich cod post fod rhwng 5 a 7 nod”
    And The test is complete and I close the driver

  @DVADrivingLicence_test @build
  Scenario:User Selects DVA and landed in DVA page and Page title and sub-text
    Given I click on DVA radio button and Parhau
    When I should on the page DVA and validate title
    Then I see the heading Rhowch eich manylion yn union fel maent yn ymddangos ar eich trwydded yrru
    And I see the sentence below Os nad oes gennych drwydded yrru y DU neu os na allwch gofio'ch manylion, gallwch brofi pwy ydych chi mewn ffordd arall yn lle.
    And The test is complete and I close the driver

  @DVADrivingLicenceTest
  Scenario Outline:Retry message DVA
    Given I click on DVA radio button and Parhau
    When User enters DVA data as a <DVADrivingLicenceSubject>
    Then User clicks on Parhau
    And I see the error box “Gwiriwch bod eich manylion yn paru gyda beth sydd ar eich trwydded yrru y DU”
    And I see “Gwall”
    Then I see “Nid oeddem yn gallu dod o hyd i'ch manylion”
    And I see Roedd yna broblem wrth i ni wirio eich manylion gyda'r [DVA]
    Then I see “Ni fyddwch yn gallu newid eich manylion eto os byddwch yn gwneud camgymeriad.”
    And The test is complete and I close the driver

    Examples:
      |DVADrivingLicenceSubject |
      |IncorrectDVADrivingLicenceNumber |

  @DVADrivingLicence_test @build
  Scenario: DVA Name fields
    Given I click on DVA radio button and Parhau
    When I can see the lastname as Enw olaf
    Then I can see Given name as Enwau a roddwyd
    And I can see First name as Enw cyntaf
    And I can see the middle name as Enwau canol
    Then I can see the sentence “Mae hwn yn adran 2 och trwydded. Nid oes angen i chi gynnwys eich teitl.”
    And I can see the sentence “Gadewch hyn yn wag os nad oes gennych unrhyw enwau canol”
    And The test is complete and I close the driver

  @DVADrivingLicence_test @build
  Scenario: DVA DoB Field
    Given I click on DVA radio button and Parhau
    When I can see the DoB fields for DVA titled “Dyddiad geni”
    Then I can see example  for DVA as Er enghraifft
    Then I can see date for DVA as “Diwrnod”
    And I can see month for DVA as “Mis”
    And I can see year for DVA as “Blwyddyn”
    And The test is complete and I close the driver

  @DVADrivingLicence_test @build
  Scenario: DVA Issue date field
    Given I click on DVA radio button and Parhau
    When I can see the Issue date field titled for DVA “Dyddiad cyhoeddi”
    Then Dyma r dyddiad yn adran 4a o ch trwydded, er enghraifft 27 5 2019 for DVA
    Then I can see date for DVA as “Diwrnod”
    And I can see month for DVA as “Mis”
    And I can see year for DVA as “Blwyddyn”
    And The test is complete and I close the driver

  @DVADrivingLicence_test @build
  Scenario: DVA Valid until field
    Given I click on DVA radio button and Parhau
    When I can see the Valid to date field titled “Yn ddilys tan”
    And Dyma r dyddiad yn adran 4b o ch trwydded, er enghraifft 27 5 2019
    Then I can see date for DVA as “Diwrnod”
    And I can see month for DVA as “Mis”
    And I can see year for DVA as “Blwyddyn”
    And The test is complete and I close the driver

  @DVADrivingLicence_test @build
  Scenario: DVA Licence number
    Given I click on DVA radio button and Parhau
    When I can see the licence number field for DVA titled “Rhif trwydded”
    And I see the sentence “Dyma'r rhif hir yn adran  ar eich trwydded” for DVA
    And The test is complete and I close the driver

  @DVADrivingLicence_test @build
  Scenario: DVA Postcode
    Given I click on DVA radio button and Parhau
    Then I can see the postcode field titled “Cod post”
    And I can see “Rhowch y cod post yn y cyfeiriad yn adran  o ch trwydded”


  @DVADrivingLicence_test @build
  Scenario Outline:  DVA Driving Licence details page happy path
    Given I click on DVA radio button and Parhau
    When User enters DVA data as a <DVADrivingLicenceSubject>
    Then User clicks on Parhau
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain validity score 2 and strength score 3
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject             |
      |DVADrivingLicenceSubjectHappyBilly   |

  @DVADrivingLicence_test @build
  Scenario: DVA licence number field error message in Welsh
    Given I click on DVA radio button and Parhau
    When I enter invalid driving licence less than 8 char for DVA
    Then User clicks on Parhau
    And I see “Dylai rhif eich trwydded fod yn [X] nod o hyd” for DVA
    And I clear the licence number enter the invalid Driving Licence
    Then User clicks on Parhau
    And I see “Ni ddylai rhif eich trwydded gynnwys unrhyw symbolau neu ofodau” for DVA
    And The test is complete and I close the driver

  @DVADrivingLicence_test @build
  Scenario: DVA Driving Licence details Name field error message in Welsh
    Given I click on DVA radio button and Parhau
    When I enter the invalid last name and first name
    When User clicks on Parhau
    Then the validation text reads “Mae problem”
    And I see “Rhowch eich enw olaf fel y mae'n ymddangos ar eich trwydded yrru”
    And I see “Rhowch eich enw cyntaf fel y mae'n ymddangos ar eich trwydded yrru”
    And I see “Rhowch unrhyw enwau canol fel y maent yn ymddangos ar eich trwydded yrru"
    And The test is complete and I close the driver

  @DVADrivingLicence_test @build
  Scenario: DVA Driving Licence details IncorrectDateOfBirth error message in Welsh
    Given I click on DVA radio button and Parhau
    When I click Parhau without entering any details
    Then the validation text reads “Mae problem”
    And I see “Rhowch y dyddiad fel y mae'n ymddangos ar eich trwydded yrru” for DVA
    And I clear the data and re enter the date of birth to enter pastDOB for DVA
    And  User clicks on Parhau
    Then I see “Rhaid i'ch dyddiad geni fod yn y gorffennol” For DVA
    And The test is complete and I close the driver

  @DVADrivingLicence_test @build
  Scenario: DVA Driving Licence Valid to date field error message in Welsh
    Given I click on DVA radio button and Parhau
    When I enter the invalid Valid to date field for DVA
    And  User clicks on Parhau
    Then I can see the Valid to date field error “Rhowch y dyddiad fel y mae'n ymddangos ar eich trwydded yrru” for DVA
    And I clear the data and re enter the valid to expired year for DVA
    Then  User clicks on Parhau
    And I see Ni allwch ddefnyddio trwydded yrru sydd wedi dod i ben
    And The test is complete and I close the driver

  @DVADrivingLicence_test @build
  Scenario: DVA Postcode field error message in Welsh
    Given I click on DVA radio button and Parhau
    And I clear the postcode to see the Enter your postcode error
    Then User clicks on Parhau
    And I see “Rhowch eich cod post”
    When I enter the invalid Postcode
    Then User clicks on Parhau
    And I see “Dylai eich rhowch eich cod post ond cynnwys rhifau a llythrennau yn unig”
    Then I clear the postcode and enter the less character postcode
    And User clicks on Parhau
    And I see “Dylai eich rhowch eich cod post fod rhwng 5 a 7 nod”
    And The test is complete and I close the driver

  @DVADrivingLicence_test @build
  Scenario: DVA Driving Licence Issue date field error message in Welsh
    Given I click on DVA radio button and Parhau
    When I enter the invalid issue date for DVA
    And  User clicks on Parhau
   # And I see “Rhowch y dyddiad fel y mae'n ymddangos ar eich trwydded yrru”
    Then I clear the data and re enter the invalid future year for DVA
    And  User clicks on Parhau
    And  I see “Rhaid i ddyddiad cyhoeddi fod yn y gorffennol” for DVA
    Then I validate the page error page title
    And The test is complete and I close the driver

  @DVLADrivingLicence_test@build
  Scenario Outline: DVLA Error tab title validation
    Given I click on DVLA radio button and Parhau
    When I should on the page DVLA and validate title
    Then User enters data as a <DrivingLicenceSubject>
    And User clicks on Parhau
    Then I validate the page error page title
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject |
      |IncorrectDateOfBirth |

  @DVADrivingLicence_test@build
  Scenario: DVAError tab title validation
    Given I click on DVA radio button and Parhau
    When I should on the page DVA and validate title
    When I enter the invalid Postcode
    And User clicks on Parhau
    Then I validate the page error page title
    And The test is complete and I close the driver