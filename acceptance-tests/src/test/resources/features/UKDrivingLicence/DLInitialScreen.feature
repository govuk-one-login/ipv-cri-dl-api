Feature: Driving License Test

 @DrivingLicence @happy_path @build
  Scenario: ‘Why we need to know this’ present on' Was your UK driving licence issued by DVLA or DVA?’ FE screen
    Given I navigate to the IPV Core Stub
    And I click the Driving Licence CRI for the Build environment
    Then I search for Driving Licence user number 5 in the Experian table
    And I check the page title who was your UK driving license issued by?
    Then I see ‘Why we need to know this’ component is present
    When I click the drop-down on the component
    Then I see the message begins with We need to make sure is shown