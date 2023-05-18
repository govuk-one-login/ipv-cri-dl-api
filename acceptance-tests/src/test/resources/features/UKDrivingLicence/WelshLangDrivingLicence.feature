Feature: Driving License Language Test

  Background: @Language-regression
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
  Scenario:3 options and Radio button available in Driving Licence page
     Given I check the page title is Pwy wnaeth gyhoeddi eich trwydded yrru y DU? – Profi pwy ydych chi – GOV.UK
     When I assert the URL is valid in Welsh
     Then I can see a radio button titled “DVLA”
     Then I can see a radio button titled “DVA”
     And I can see a I do not have a UK driving licence radio button titled Nid oes gennyf drwydded yrru y DU
     And I can see OR options as neu
     Then I can see CTA as Parhau
     And I see the licence Selection sentence starts with Gallwch ddod o hyd i hwn yn adran 4c o'ch trwydded yrru. Bydd naill ai'n dweud DVLA (Asiantaeth Trwyddedu Gyrru a Cherbydau) neu DVA (Asiantaeth Gyrrwyr a Cherbydau).
     And The test is complete and I close the driver

  @Language-regression
  Scenario:User Selects DVLA and landed in DVLA page and Validate the title and sentences
    Given I click on DVLA radio button and Parhau
    Then I check the page title is Rhowch eich manylion yn union fel maent yn ymddangos ar eich trwydded yrru – Profi pwy ydych chi – GOV.UK
    And I see the heading Rhowch eich manylion yn union fel maent yn ymddangos ar eich trwydded yrru
    And I see We will check your details as Byddwn yn gwirio eich manylion gydar DVLA i sicrhau nad yw eich trwydded yrru wedi cael ei chanslo na'i hadrodd fel un sydd ar goll neu wedi ei dwyn.
    And I see sentence Os nad oes gennych drwydded yrru y DU neu os na allwch gofio'ch manylion, gallwch brofi pwy ydych chi mewn ffordd arall yn lle.
    And The test is complete and I close the driver

  @Language-regression
  Scenario Outline:Retry message
    Given I click on DVLA radio button and Parhau
    When User enters DVLA data as a <DrivingLicenceSubject>
    Then User clicks on continue
    And I can see Check your details as Gwiriwch bod eich manylion yn paru gyda beth sydd ar eich trwydded yrru y DU
    Then I see error word as Gwall
    And I see We could not find your details as Nid oeddem yn gallu dod o hyd i'ch manylion
    And I see you will not be able to change your details as Ni fyddwch yn gallu newid eich manylion eto os byddwch yn gwneud camgymeriad.
    And I see Check your details as Roedd yna broblem wrth i ni wirio eich manylion gyda'r DVLA.
    And The test is complete and I close the driver

    Examples:
      |DrivingLicenceSubject |
      |IncorrectIssueNumber|

  @Language-regression
  Scenario: DVLA Name fields
    Given I click on DVLA radio button and Parhau
    When I can see the lastname as Enw olaf
    And I can see the givenName as Enwau a roddwyd
    And I can see the firstName as Enw cyntaf
    And I can see the middleName as Enwau canol
    And I can see the first name sentence Mae hwn yn adran 2 o'ch trwydded. Nid oes angen i chi gynnwys eich teitl.
    And I can see the sentence Gadewch hyn yn wag os nad oes gennych unrhyw enwau canol
    And The test is complete and I close the driver

  @Language-regression
  Scenario: DVLA DoB Fields
    Given I click on DVLA radio button and Parhau
    When I can see the DoB fields titled Dyddiad geni
    When I can see example as Er enghraifft, 5 9 1973
    Then I can see date as Diwrnod
    And I can see month as Mis
    And I can see year as Blwyddyn
    And The test is complete and I close the driver

  @Language-regression
  Scenario: DVLA Issue date fields
    Given I click on DVLA radio button and Parhau
    When I can see the Issue date field titled Dyddiad cyhoeddi
    Then I can see date sentence as Dyma'r dyddiad yn adran 4a o'ch trwydded, er enghraifft 27 5 2019
    And I can see date as Diwrnod
    Then I can see month as Mis
    And I can see year as Blwyddyn
    And The test is complete and I close the driver

  @Language-regression
  Scenario: DVLA Valid to date field
    Given I click on DVLA radio button and Parhau
    When I can see the Valid to date field titled Yn ddilys tan
    And I can see Valid to date sentence as Dyma'r dyddiad yn adran 4b o'ch trwydded, er enghraifft 27 5 2019
    Then I can see date as Diwrnod
    And I can see month as Mis
    Then I can see year as Blwyddyn
    And The test is complete and I close the driver

  @Language-regression
  Scenario: DVLA Licence number
    Given I click on DVLA radio button and Parhau
    When I can see the licence number field titled Rhif trwydded
    Then I see the Licence number sentence Dyma'r rhif hir yn adran 5 ar eich trwydded er enghraifft HARRI559146MJ931
    And The test is complete and I close the driver

  @Language-regression
  Scenario: DVLA Issue number
    Given I click on DVLA radio button and Parhau
    When I can see the issue number field titled Rhif cyhoeddi
    And I can see issue sentence as Dyma'r rhif 2 ddigid ar ôl y gofod yn adran 5 o'ch trwydded
    And The test is complete and I close the driver

  @Language-regression
  Scenario: DVLA Postcode
    Given I click on DVLA radio button and Parhau
    When I can see the postcode field titled Cod post
    Then I can see postcode sentence as Rhowch y cod post yn y cyfeiriad yn adran 8 o'ch trwydded
    And The test is complete and I close the driver

  @Language-regression
  Scenario Outline:  DVLA Driving Licence details page happy path
    Given I click on DVLA radio button and Parhau
    Then User enters DVLA data as a <DrivingLicenceSubject>
    When User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain validity score 2 and strength score 3
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject             |
      |DrivingLicenceSubjectHappyPeter   |

  @Language-regression
  Scenario:User selects I do not have a UK driving licence - Nid oes gennyf drwydded yrru y DU and landed in IPV Core
    Given I click Nid oes gennyf drwydded yrru y DU and Parhau
    When I am directed to the IPV Core routing page
    And I validate the URL having access denied
    Then I navigate to the Driving Licence verifiable issuer to check for a Invalid response
    And JSON response should contain error description Authorization permission denied and status code as 302
    And The test is complete and I close the driver

  @Language-regression
   Scenario: DVLA Driving Licence details Name field error message in Welsh(fail)
     Given I click on DVLA radio button and Parhau
     When I enter the invalid last name and first name
     When User clicks on continue
     Then the validation text reads Mae problem
     And I see the Lastname error in the error summary as Rhowch eich enw olaf fel y mae'n ymddangos ar eich trwydded yrru
     And I see the firstname error summary as Rhowch eich enw cyntaf fel y mae'n ymddangos ar eich trwydded yrru
     And I see the middlenames error summary as Rhowch unrhyw enwau canol fel y maent yn ymddangos ar eich trwydded yrru
     And The test is complete and I close the driver

  @Language-regression
  Scenario Outline: DVLA Driving Licence details IncorrectDateOfBirth error message in Welsh
    Given I click on DVLA radio button and Parhau
    When User clicks on continue
    Then the validation text reads Mae problem
    And I see issue date error in summary as Rhowch y dyddiad fel y mae'n ymddangos ar eich trwydded yrru
    Then I clear the data and re enter the date of birth
    And  User clicks on continue
    And I see check date of birth sentence as Gwiriwch eich bod wedi rhoi eich dyddiad geni yn gywir
    When User Re-enters DVLA data as a <DrivingLicenceSubject>
    And  User clicks on continue
    Then I see check date of birth sentence as Rhaid i'ch dyddiad geni fod yn y gorffennol
    And The test is complete and I close the driver

    Examples:
      |DrivingLicenceSubject |
      |DateOfBirthInFuture |

  @Language-regression
  Scenario: DVLA Driving Licence Issue date field error message in Welsh
    Given I click on DVLA radio button and Parhau
    When I enter the invalid issue date
    And  User clicks on continue
    And I see issue date error in summary as Rhowch y dyddiad fel y mae'n ymddangos ar eich trwydded yrru
    Then I clear the data and re enter the invalid future year
    And  User clicks on continue
    And  I see invalid issue date field error as Gwall:Rhaid i ddyddiad cyhoeddi fod yn y gorffennol
    And The test is complete and I close the driver

  @Language-regression
  Scenario: DVLA Driving Licence Valid until date field error message in Welsh
    Given I click on DVLA radio button and Parhau
    When I enter the invalid Valid to date field
    And  User clicks on continue
    And I see issue date error in summary as Rhowch y dyddiad fel y mae'n ymddangos ar eich trwydded yrru
    Then I clear the data and re enter the valid to expired year
    And  User clicks on continue
    And I can see the valid to date error in the error summary as Ni allwch ddefnyddio trwydded yrru sydd wedi dod i ben
    And The test is complete and I close the driver

 @Language-regression
  Scenario: DVLA licence number field error message in Welsh
    Given I click on DVLA radio button and Parhau
    When I enter driving licence field empty
    Then User clicks on continue
    And I see the licence number error in the summary as Rhowch y rhif yn union fel mae’n ymddangos ar eich trwydded yrru
    And I clear the licence number enter the invalid Driving Licence for DVLA
    Then User clicks on continue
    And I see the licence number error in the summary as Dylai rhif eich trwydded fod yn 16 nod o hyd
    Then I clear the licence number and enter Driving Licence with Special Char for DVLA
    Then User clicks on continue
    And I see the licence number error in the summary as Ni ddylai rhif eich trwydded gynnwys unrhyw symbolau neu ofodau
    And The test is complete and I close the driver

  @Language-regression
  Scenario Outline: DVLA Issue number field error message in Welsh
    Given I click on DVLA radio button and Parhau
    When User enters DVLA data as a <DrivingLicenceSubject>
    Then User clicks on continue
    And I see Issue Number Error as Gwall:Dylai eich rhif cyhoeddi fod yn 2 rif o hyd
    And I clear Issue number to see the error Enter Issue number
    Then User clicks on continue
    And I see the issue number error in field as Gwall:Rhowch y rhif cyhoeddi fel y mae'n ymddangos ar eich trwydded yrru
    And I clear Issue number and enter the issue number with special character
    Then User clicks on continue
    And I see the issue number error in field as Gwall:Ni ddylai eich rhif cyhoeddi gynnwys unrhyw symbolau neu ofodau
    And The test is complete and I close the driver

    Examples:
      |DrivingLicenceSubject |
      |IssueNumberLessThan2Char |

  @Language-regression
  Scenario: DVLA Postcode field error message in Welsh
    Given I click on DVLA radio button and Parhau
    When I clear the postcode to see the Enter your postcode error
    Then User clicks on continue
    And I see the postcode error in the field as Gwall:Rhowch eich cod post
    When I enter the invalid Postcode
    Then User clicks on continue
    And I see the postcode error in the field as Gwall:Dylai eich rhowch eich cod post ond cynnwys rhifau a llythrennau yn unig
    Then I clear the postcode and enter the less character postcode
    And User clicks on continue
    And I see the postcode error in the field as Gwall:Dylai eich rhowch eich cod post fod rhwng 5 a 7 nod
    And The test is complete and I close the driver

  @Language-regression
  Scenario:User Selects DVA and landed in DVA page and Page title and sub-text
    Given I click on DVA radio button and Parhau
    Then I check the page title is Rhowch eich manylion yn union fel maent yn ymddangos ar eich trwydded yrru – Profi pwy ydych chi – GOV.UK
    Then I see the heading Rhowch eich manylion yn union fel maent yn ymddangos ar eich trwydded yrru
    And I see sentence Os nad oes gennych drwydded yrru y DU neu os na allwch gofio'ch manylion, gallwch brofi pwy ydych chi mewn ffordd arall yn lle.
    And The test is complete and I close the driver

  @Language-regression
  Scenario Outline:Retry message DVA
    Given I click on DVA radio button and Parhau
    When User enters DVA data as a <DVADrivingLicenceSubject>
    Then User clicks on continue
    And I can see Check your details as Gwiriwch bod eich manylion yn paru gyda beth sydd ar eich trwydded yrru y DU
    Then I see error word as Gwall
    And I see We could not find your details as Nid oeddem yn gallu dod o hyd i'ch manylion
    And I see check your details for DVA as Roedd yna broblem wrth i ni wirio eich manylion gyda'r DVA.
    And I see you will not be able to change your details as Ni fyddwch yn gallu newid eich manylion eto os byddwch yn gwneud camgymeriad.
    And The test is complete and I close the driver

    Examples:
      |DVADrivingLicenceSubject |
      |IncorrectDrivingLicenceNumber |

  @Language-regression
  Scenario: DVA Name fields
    Given I click on DVA radio button and Parhau
    When I can see the lastname as Enw olaf
    And I can see the givenName as Enwau a roddwyd
    And I can see the firstName as Enw cyntaf
    And I can see the middleName as Enwau canol
    And I can see the first name sentence Mae hwn yn adran 2 o'ch trwydded. Nid oes angen i chi gynnwys eich teitl.
    And I can see the sentence Gadewch hyn yn wag os nad oes gennych unrhyw enwau canol
    And The test is complete and I close the driver

  @Language-regression
  Scenario: DVA DoB Field
    Given I click on DVA radio button and Parhau
    When I can see the DoB fields for DVA titled Dyddiad geni
    Then I can see example  for DVA as Er enghraifft, 5 9 1973
    Then I can see date for DVA as Diwrnod
    And I can see month for DVA as Mis
    And I can see year for DVA as Blwyddyn
    And The test is complete and I close the driver

  @Language-regression
  Scenario: DVA Issue date field
    Given I click on DVA radio button and Parhau
    When I see the Issue date field titled Dyddiad cyhoeddi for DVA
    Then I see date section example as Dyma'r dyddiad yn adran 4a o'ch trwydded, er enghraifft 27 5 2019
    Then I can see date for DVA as Diwrnod
    And I can see month for DVA as Mis
    And I can see year for DVA as Blwyddyn
    And The test is complete and I close the driver

  @Language-regression
  Scenario: DVA Valid until field
    Given I click on DVA radio button and Parhau
    When I can see the Valid to date field titled Yn ddilys tan
    And I see valid until example for DVA as Dyma'r dyddiad yn adran 4b o'ch trwydded, er enghraifft 27 5 2019
    Then I can see date for DVA as Diwrnod
    And I can see month for DVA as Mis
    And I can see year for DVA as Blwyddyn
    And The test is complete and I close the driver

  @Language-regression
  Scenario: DVA Licence number
    Given I click on DVA radio button and Parhau
    When I can see the licence number field for DVA titled Rhif trwydded
    And I see the DVA licence sentence Dyma'r rhif hir yn adran 5 ar eich trwydded
    And The test is complete and I close the driver

  @Language-regression
  Scenario: DVA Postcode
    Given I click on DVA radio button and Parhau
    Then I can see the postcode field titled Cod post
    Then I can see postcode sentence as Rhowch y cod post yn y cyfeiriad yn adran 8 o'ch trwydded

  @DVADrivingLicence_test @build
  Scenario Outline:  DVA Driving Licence details page happy path
    Given I click on DVA radio button and Parhau
    When User enters DVA data as a <DVADrivingLicenceSubject>
    Then User clicks on continue
    Then I navigate to the Driving Licence verifiable issuer to check for a Valid response
    And JSON payload should contain validity score 2 and strength score 3
    And The test is complete and I close the driver
    Examples:
      |DVADrivingLicenceSubject             |
      |DVADrivingLicenceSubjectHappyBilly   |

  @Language-regression
  Scenario: DVA licence number field error message in Welsh
    Given I click on DVA radio button and Parhau
    When I enter invalid driving licence less than 8 char for DVA
    Then User clicks on continue
    And I see the licence number error in the summary as Dylai rhif eich trwydded fod yn 8 nod o hyd
    And I clear the licence number enter the invalid Driving Licence
    Then User clicks on continue
    And I see the licence number error in the summary as Ni ddylai rhif eich trwydded gynnwys unrhyw symbolau neu ofodau
    And I clear the licence number enter the invalid Driving Licence with special character
    Then User clicks on continue
    And I see the licence number error in the summary as Ni ddylai rhif eich trwydded gynnwys unrhyw symbolau neu ofodau
    And The test is complete and I close the driver

  @Language-regression
  Scenario: DVA Driving Licence details Name field error message in Welsh
    Given I click on DVA radio button and Parhau
    When I enter the invalid last name and first name
    When User clicks on continue
    Then the validation text reads Mae problem
    And I see the Lastname error in the error summary as Rhowch eich enw olaf fel y mae'n ymddangos ar eich trwydded yrru
    And I see the firstname error summary as Rhowch eich enw cyntaf fel y mae'n ymddangos ar eich trwydded yrru
    And I see the middlenames error summary as Rhowch unrhyw enwau canol fel y maent yn ymddangos ar eich trwydded yrru
    And The test is complete and I close the driver

  @Language-regression
  Scenario: DVA Driving Licence details IncorrectDateOfBirth error message in Welsh
    Given I click on DVA radio button and Parhau
    When User clicks on continue
    Then the validation text reads Mae problem
    And I see DVA enter the date as it appears above the field as Gwall:Rhowch eich dyddiad geni fel y mae'n ymddangos ar eich trwydded yrru
    And I clear the data and re enter the date of birth to enter pastDOB for DVA
    And  User clicks on continue
    Then I see check date of birth sentence as Rhaid i'ch dyddiad geni fod yn y gorffennol
    And The test is complete and I close the driver

  @Language-regression
  Scenario: DVA Driving Licence Valid to date field error message in Welsh
    Given I click on DVA radio button and Parhau
    When I enter the invalid Valid to date field for DVA
    And  User clicks on continue
    Then I can see the Valid to date field error as Gwall:Rhowch y dyddiad fel y mae'n ymddangos ar eich trwydded yrru
    And I clear the data and re enter the valid to expired year for DVA
    Then  User clicks on continue
    And I can see the valid to date error in the error summary as Ni allwch ddefnyddio trwydded yrru sydd wedi dod i ben
    And The test is complete and I close the driver

  @Language-regression
  Scenario: DVA Postcode field error message in Welsh
    Given I click on DVA radio button and Parhau
    And I clear the postcode to see the Enter your postcode error
    Then User clicks on continue
    And I see the postcode error in the field as Gwall:Rhowch eich cod post
    When I enter the invalid Postcode
    Then User clicks on continue
    And I see the postcode error in the field as Gwall:Dylai eich rhowch eich cod post ond cynnwys rhifau a llythrennau yn unig
    Then I clear the postcode and enter the less character postcode
    And User clicks on continue
    And I see the postcode error in the field as Gwall:Dylai eich rhowch eich cod post fod rhwng 5 a 7 nod
    And The test is complete and I close the driver

  @Language-regression
  Scenario: DVA Driving Licence Issue date field error message in Welsh
    Given I click on DVA radio button and Parhau
    When I enter the invalid issue date for DVA
    And  User clicks on continue
    And I see issue date error in summary for DVA as Gwall:Rhowch y dyddiad fel y mae'n ymddangos ar eich trwydded yrru
    Then I clear the data and re enter the invalid future year for DVA
    And  User clicks on continue
    And  As a DVA user I see invalid issue date field error as Gwall:Rhaid i ddyddiad cyhoeddi fod yn y gorffennol
    Then I check the page title is Gwall: Rhowch eich manylion yn union fel maent yn ymddangos ar eich trwydded yrru – Profi pwy ydych chi – GOV.UK
    And The test is complete and I close the driver

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

  @Language-regression
  Scenario: DVAError tab title validation
    Given I click on DVA radio button and Parhau
    Then I check the page title is Rhowch eich manylion yn union fel maent yn ymddangos ar eich trwydded yrru – Profi pwy ydych chi – GOV.UK
    When I enter the invalid Postcode
    And User clicks on continue
    Then I check the page title is Gwall: Rhowch eich manylion yn union fel maent yn ymddangos ar eich trwydded yrru – Profi pwy ydych chi – GOV.UK
    And The test is complete and I close the driver

  @Language-regression
  Scenario Outline:  DVLA Driving Licence error validation when DVLA consent checkbox is unselected
    Given I click on DVLA radio button and Parhau
    Then User enters DVLA data as a <DrivingLicenceSubject>
    And DVLA consent checkbox is unselected
    When User clicks on continue
    Then User can see the DVLA consent error in summary as Mae'n rhaid i chi roi eich caniatâd i barhau
    And User can see the DVLA consent error on the checkbox as Gwall:Mae'n rhaid i chi roi eich caniatâd i barhau
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject             |
      |DrivingLicenceSubjectHappyPeter   |

  @Language-regression
  Scenario Outline:  DVA Driving Licence error validation when DVA consent checkbox is unselected
    Given I click on DVA radio button and Parhau
    Then User enters DVA data as a <DrivingLicenceSubject>
    And DVA consent checkbox is unselected
    When User clicks on continue
    Then User can see the DVA consent error in summary as Mae'n rhaid i chi roi eich caniatâd i barhau
    And User can see the DVA consent error on the checkbox as Gwall:Mae'n rhaid i chi roi eich caniatâd i barhau
    And The test is complete and I close the driver
    Examples:
      |DrivingLicenceSubject             |
      |DVADrivingLicenceSubjectHappyBilly|