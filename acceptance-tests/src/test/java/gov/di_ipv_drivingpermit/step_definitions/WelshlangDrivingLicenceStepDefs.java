package gov.di_ipv_drivingpermit.step_definitions;

import gov.di_ipv_drivingpermit.pages.DrivingLicencePageObject;
import gov.di_ipv_drivingpermit.pages.WelshLangDrivingLicencePageObject;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class WelshlangDrivingLicenceStepDefs extends WelshLangDrivingLicencePageObject {

    @And("I add a cookie to change the language to Welsh")
    public void iAddACookieToChangeTheLanguageToWelsh() {

        changeLanguageToWelsh();
    }

    @Given("I view the Beta banner")
    public void iViewTheBetaBanner() {
        betaBanner();
    }

    @Then(
            "the text reads “BETA Mae hwn yn wasanaeth newydd – bydd eich adborth \\(agor mewn tab newydd) yn ein helpu i'w wella.”")
    public void
            theTextReadsBETAMaeHwnYnWasanaethNewyddByddEichAdborthAgorMewnTabNewyddYnEinHelpuIWWella() {
        betaBannerSentenceWelsh();
    }

    @Then("I check the page title Pwy wnaeth gyhoeddi eich trwydded yrru y DU?")
    public void i_check_the_page_title_who_was_your_uk_driving_license_issued_by() {
        validateDLPageTitleWelsh();
    }

    @And("I assert the URL is valid in Welsh")
    public void iAssertTheURLIsValidInWelsh() {
        drivingLicencePageURLValidationWelsh();
    }

    @And("I can see a radio button titled “Nid oes gennyf drwydded yrru y DU”")
    public void iCanSeeARadioButtonTitledNidOesGennyfDrwyddedYrruYDU() {
        noDrivingLicenceBtnWelsh();
    }

    @And("I can see “Or”")
    public void iCanSeeOr() {
        orDisplayWelsh();
    }

    @Then("I can see CTA as Parhau")
    public void iCanSeeCTAAsParhau() {
        continueButtonWelsh();
    }

    @And("I see the sentence starts with “Gallwch ddod o hyd i hwn yn adran\"")
    public void iSeeTheSentenceISeeTheSentenceStartsWithGallwchDdodOHydIHwnYnAdran()
            throws Throwable {
        licenceSelectionSentence();
    }

    @Then("I should on the page DVLA and validate title")
    public void iShouldOnThePageDVLAAndValidateTitle() {
        pageTitleDVLAValidationWelsh();
    }

    @Then("I should on the page DVA and validate title")
    public void iShouldOnThePageDVAAndValidateTitle() {
        pageTitleDVLAValidationWelsh();
    }

    @And(
            "I see the heading Rhowch eich manylion yn union fel maent yn ymddangos ar eich trwydded yrru")
    public void iSeeTheHeadingRhowchEichManylionYnUnionFelMaentYnYmddangosArEichTrwyddedYrru() {
        dvlaPageHeading();
    }

    @Given("I can see the lastname as Enw olaf")
    public void iCanSeeTheLastnameAsEnwOlaf() {
        lastNameWelsh();
    }

    @And("I can see Given name as Enwau a roddwyd")
    public void iCanSeeGivenNameAsEnwauARoddwyd() {
        givenNameWelsh();
    }

    @And("I can see First name as Enw cyntaf")
    public void iCanSeeFirstNameAsEnwCyntaf() {
        firstNameWelsh();
    }

    @And("I can see the middle name as Enwau canol")
    public void iCanSeeTheMiddleNameAsEnwauCanol() {
        middleNameWelsh();
    }

    @And(
            "I can see the sentence “Mae hwn yn adran 2 och trwydded. Nid oes angen i chi gynnwys eich teitl.”")
    public void iCanSeeTheSentenceMaeHwnYnAdranOChTrwyddedNidOesAngenIChiGynnwysEichTeitl() {
        firstNameSentence();
    }

    @And(
            "I see the sentence below Os nad oes gennych drwydded yrru y DU neu os na allwch gofio'ch manylion, gallwch brofi pwy ydych chi mewn ffordd arall yn lle.")
    public void
            iSeeTheSentenceBelowOsNadOesGennychDrwyddedYrruYDUNeuOsNaAllwchGofioChManylionGallwchBrofiPwyYdychChiMewnFforddArallYnLle() {
        dvlaProveYourIdentitySentence();
    }

    @And("I can see the sentence “Gadewch hyn yn wag os nad oes gennych unrhyw enwau canol”")
    public void iCanSeeTheSentenceGadewchHynYnWagOsNadOesGennychUnrhywEnwauCanol() {
        middleNameSentence();
    }

    @Given("I can see the DoB fields titled “Dyddiad geni”")
    public void iCanSeeTheDoBFieldsTitledDyddiadGeni() {
        DateOfBirthField();
    }

    @And("I can see example as Er enghraifft")
    public void iCanSeeExampleAsErEnghraifft() {
        DateOfBirthFieldhint();
    }

    @And("I can see date as “Diwrnod”")
    public void iCanSeeDateAsDiwrnod() {
        dateField();
    }

    @And("I can see date for DVA as “Diwrnod”")
    public void iCanSeeDateForDVAAsDiwrnod() {
        dateFieldDVA();
    }

    @And("I can see month as “Mis”")
    public void iCanSeeMonthAsMis() {
        monthField();
    }

    @And("I can see month for DVA as “Mis”")
    public void iCanSeeMonthForDVAAsMis() {
        monthFieldDVA();
    }

    @And("I can see year as “Blwyddyn”")
    public void iCanSeeYearAsBlwyddyn() {
        yearField();
    }

    @Given("I can see the Issue date field titled “Dyddiad cyhoeddi”")
    public void iCanSeeTheIssueDateFieldTitledDyddiadCyhoeddi() {
        issueDateField();
    }

    @Then("Dyma r dyddiad yn adran 4a o ch trwydded, er enghraifft 27 5 2019")
    public void dymaRDyddiadYnAdranAOChTrwyddedErEnghraifft() {
        issueDateSentence();
    }

    @Then("Dyma r dyddiad yn adran 4b o ch trwydded, er enghraifft 27 5 2019")
    public void dymaRDyddiadYnAdranAOChTrwyddedErEnghraifftValidityDate() {
        validityDateSentence();
    }

    @Then("I can see the Valid to date field titled “Yn ddilys tan”")
    public void iCanSeeTheValidToDateFieldTitledYnDdilysTan() {
        validToDateFieldTitle();
    }

    @Given("I can see the licence number field titled “Rhif trwydded”")
    public void iSelectedDVLAOnThePreviousPage() {
        licenceNumberWelsh();
    }

    @Given("I can see the licence number field for DVA titled “Rhif trwydded”")
    public void iSelectedDVLAOnThePreviousPageForDVA() {
        licenceNumberWelshDVA();
    }

    @And("I see the sentence “Dyma'r rhif hir yn adran  ar eich trwydded”")
    public void iSeeTheSentenceDymaRRhifHirYnAdranArEichTrwydded() {
        licenceSentence();
    }

    @Then("I can see the issue number field titled “Rhif cyhoeddi”")
    public void iCanSeeTheIssueNumberFieldTitledDyddiadCyhoeddi() {
        issueNumberWelsh();
    }

    @And("I  can see “Dyma r rhif  ddigid ar ôl y gofod yn adran  o'ch trwydded”")
    public void iCanSeeDymaRRhifDdigidArÔlYGofodYnAdranOChTrwydded() {
        issueNumberSentence();
    }

    @Then("I can see the postcode field titled “Cod post”")
    public void iCanSeeThePostcodeFieldTitledCodPost() {
        postcodeWelsh();
    }

    @And("I can see “Rhowch y cod post yn y cyfeiriad yn adran  o ch trwydded”")
    public void rhowchYCodPostYnYCyfeiriadYnAdranOChTrwydded() {
        postcodeSentence();
    }

    @When("User clicks on Parhau")
    public void userClicksOnParhau() {
        new DrivingLicencePageObject().Continue.click();
    }

    @Given("I click on DVLA radio button and Parhau")
    public void i_click_on_DVLA_radio_button_and_Continue() {
        clickOnDVLARadioButtonWelsh();
    }

    @Given("I click on DVA radio button and Parhau")
    public void i_select_dva_radio_button_and_Parhau() {
        clickOnDVARadioButtonWelsh();
    }

    @Given("I click Nid oes gennyf drwydded yrru y DU and Parhau")
    public void i_select_i_do_not_have_uk_driving_license() {
        noDrivingLicenceOptionWelsh();
    }

    @And(
            "Proper error message for invalid Driving Licence \"Rhaid i'ch dyddiad geni fod yn y gorffennol”")
    public void properErrorMessageForInvalidDrivingLicenceRhaidIChDyddiadGeniFodYnYGorffennol()
            throws Throwable { // Write code here that turns the phrase above into concrete actions
        //    throw new cucumber.api.PendingException();}
        inValidDLText();
    }

    @Then(
            "Proper error message for invalid IssueDate “Rhowch y dyddiad fel y mae'n ymddangos ar eich trwydded yrru”")
    public void
            properErrorMessageForInvalidIssueDateRhowchYDyddiadFelYMaeNYmddangosArEichTrwyddedYrru() {
        inValidIssueDateText();
    }

    @Then(
            "I can see the Valid to date field error “Rhowch y dyddiad fel y mae'n ymddangos ar eich trwydded yrru” for DVA")
    public void
            ICanSeeTheValidtoDateFieldErrorRhowchYDyddiadFelYMaenymddangosareichtrwyddedyrruforDVA() {
        inValidIssueDateTextDVA();
    }

    @When("I enter the invalid last name and first name")
    public void iEnterTheInvalidLastNameAndFirstName() {
        invalidlastAndFirstNameWelsh();
    }

    @Then("the validation text reads “Mae problem”")
    public void theValidationTextReadsMaeProblem() {
        thereIsaProblemText();
    }

    @And("I see “Rhowch eich enw olaf fel y mae'n ymddangos ar eich trwydded yrru”")
    public void rhowchEichEnwOlafFelYMaeNYmddangosArEichTrwyddedYrru() {
        lastNameErrorSentenceWelsh();
    }

    @And("I see “Rhowch eich enw cyntaf fel y mae'n ymddangos ar eich trwydded yrru”")
    public void iSeeRhowchEichEnwCyntafFelYMaeNYmddangosArEichTrwyddedYrru() {
        firstNameErrorSentenceWelsh();
    }

    @And("I see “Rhowch unrhyw enwau canol fel y maent yn ymddangos ar eich trwydded yrru\"")
    public void iSeeRhowchUnrhywEnwauCanolFelYMaentYnYmddangosArEichTrwyddedYrru()
            throws Throwable { // Write code here that turns the phrase above into concrete actions
        //    throw new cucumber.api.PendingException();}
        middleNameErrorSentence();
    }

    @When("I click Parhau without entering any details")
    public void iClickParhauWithoutEnteringAnyDetails() {
        //  clearDOBandReEnterWelshForDVA();
        new DrivingLicencePageObject().Continue.click();
    }

    @And("I see “Rhowch y dyddiad fel y mae'n ymddangos ar eich trwydded yrru”")
    public void iSeeRhowchYDyddiadFelYMaeNYmddangosArEichTrwyddedYrru() {
        enterDOBErrorTextWelsh();
    }

    @And("I see “Rhowch y dyddiad fel y mae'n ymddangos ar eich trwydded yrru” for DVA")
    public void iSeeRhowchYDyddiadFelYMaeNYmddangosArEichTrwyddedYrruForDVA() {
        enterDOBErrorTextWelshDVA();
    }

    @And("I see “Gwiriwch eich bod wedi rhoi eich dyddiad geni yn gywir”")
    public void iSeeGwiriwchEichBodWediRhoiEichDyddiadGeniYnGywir() {
        enterValidDOBErrorTextWelsh();
    }

    @Then("I clear the data and re enter the date of birth")
    public void iClearTheDataAndReEnterTheDateOfBirth() {
        clearDOBandReEnterWelsh();
    }

    @Then("I clear the data and re enter the date of birth to enter futureDOB")
    public void iClearTheDataAndReEnterTheDateOfBirthTo() {
        clearDOBandReEnterWelshtofuture();
    }

    @Then("I see “Rhaid i'ch dyddiad geni fod yn y gorffennol”")
    public void iSeeRhaidIChDyddiadGeniFodYnYGorffennol() {
        errorMessageFutureDOBWelsh();
    }

    @Then("I see “Rhaid i'ch dyddiad geni fod yn y gorffennol” For DVA")
    public void iSeeRhaidIChDyddiadGeniFodYnYGorffennolForDVA() {
        errorMessageFutureDOBWelshDVA();
    }

    @When("I enter the invalid issue date")
    public void iEnterTheInvalidIssueDate() {
        enterInValidIssueDate();
    }

    @And("I see “Rhaid i ddyddiad cyhoeddi fod yn y gorffennol”")
    public void iSeeRhaidIDdyddiadCyhoeddiFodYnYGorffennol() {
        issueDateErrorWelsh();
    }

    @And("I see “Rhaid i ddyddiad cyhoeddi fod yn y gorffennol” for DVA")
    public void iSeeRhaidIDdyddiadCyhoeddiFodYnYGorffennolForDVA() {
        issueDateErrorDVAWelshh();
    }

    @Then("I clear the data and re enter the invalid future year")
    public void iClearTheDataAndReEnterTheInvalidFutureYear() {
        enterInValidIssueDateWithFutureYear();
    }

    @When("I enter the invalid Valid to date field")
    public void iEnterTheInvalidValidToDateField() {
        enterInValidUntilDate();
    }

    /*@Then("I can see the Valid to date field error “Rhowch y dyddiad fel y mae'n ymddangos ar eich trwydded yrru”")
    public void iCanSeeTheValidToDateFieldErrorRhowchYDyddiadFelYMaeNYmddangosArEichTrwyddedYrru() {
        inValidIssueDateText();
    }*/

    @Then("I clear the data and re enter the valid to expired year")
    public void iClearTheDataAndReEnterTheValidToExpiredYear() {
        enterTheValidToExpiredYear();
    }

    @And("I see Ni allwch ddefnyddio trwydded yrru sydd wedi dod i ben")
    public void iSeeNiAllwchDdefnyddioTrwyddedYrruSyddWediDodIBen() {
        validToErrorWelsh();
    }

    @When("I enter driving licence field empty")
    public void iEnterInvalidDrivingLicenceFieldEmpty() {
        invalidDrivingLicenceempty();
    }

    @Then("I clear the licence number and enter Driving Licence with Special Char for DVLA")
    public void iClearTheLicenceNumberAndEnterDrivingLicenceWithSpecialCharForDVLA() {
        invalidDrivingLicenceWithSplCharDVLA();
    }

    @And("I clear the licence number enter the invalid Driving Licence")
    public void iClearTheLicenceNumberEnterTheInvalidDrivingLicence() {
        inValidDrivingLicenceDVA();
    }

    @And("I clear the licence number enter the invalid Driving Licence for DVLA")
    public void iClearTheLicenceNumberEnterTheInvalidDrivingLicenceForDVLA() {
        invalidDrivingLicenceDVLA();
    }

    @And("I see “Ni ddylai rhif eich trwydded gynnwys unrhyw symbolau neu ofodau”")
    public void Niddylairhifeichtrwyddedgynnwysunrhywsymbolauneuofodau() {
        licenceErrorWelshforSplChar();
    }

    @And("I see “Rhowch y rhif yn union fel mae'n ymddangos ar eich trwydded yrru”")
    public void iSeeRhowchYRhifYnUnionFelMaeNYmddangosArEichTrwyddedYrru() {
        licenceErrorWelshforExactonDL();
    }

    @When("I enter inValid issue number")
    public void iEnterInValidIssueNumber() {
        invalidIssueNumber();
    }

    @And("I see “Dylai eich rhif cyhoeddi fod yn  rif o hyd”")
    public void iSeeDylaiEichRhifCyhoeddiFodYnRifOHyd() {
        IssueNumberErrorWelsh();
    }

    @And("I clear Issue number to see the error Enter Issue number")
    public void iClearIssueNumberToSeeTheErrorEnterIssueNumber() {
        clearIssueNumber();
    }

    @And("I see “Rhowch y rhif cyhoeddi fel y mae'n ymddangos ar eich trwydded yrru”")
    public void iSeeRhowchYRhifCyhoeddiFelYMaeNYmddangosArEichTrwyddedYrru() {
        enterIssueNumberErrorWelsh();
    }

    @When("I enter the invalid Postcode")
    public void iEnterTheInvalidPostcode() {
        enterInValidPostCode();
    }

    @And("I see “Dylai eich rhowch eich cod post ond cynnwys rhifau a llythrennau yn unig”")
    public void iSeeDylaiEichRhowchEichCodPostOndCynnwysRhifauALlythrennauYnUnig() {
        postCodeErrorInvalidWelsh();
    }

    @And("I clear the postcode to see the Enter your postcode error")
    public void iClearThePostcodeToSeeTheEnterYourPostcodeError() {
        new DrivingLicencePageObject().Postcode.clear();
    }

    @And("I see “Rhowch eich cod post”")
    public void iSeeRhowchEichCodPost() {
        enterYourPostCodeErrorWelsh();
    }

    @Then("I clear the postcode and enter the less character postcode")
    public void iClearThePostcodeAndEnterTheLessCharacterPostcode() {
        invalidPostCode();
    }

    @And("I see “Dylai eich rhowch eich cod post fod rhwng {int} a {int} nod”")
    public void iSeeDylaiEichRhowchEichCodPostFodRhwngANod(int arg0, int arg1) {
        postCodeErrorWelsh();
    }

    @When("I enter the invalid date of birth for DVA")
    public void iEnterTheInvalidDateOfBirthForDVA() {
        invalidDobForDVAWelsh();
    }

    @Then("I clear the data and re enter the date of birth to enter futureDOB for DVA")
    public void iClearTheDataAndReEnterTheDateOfBirthToEnterFutureDOBForDVA() {
        dvaclearDOBandReEnterWelshtofuture();
    }

    @Then("I clear the data and re enter the date of birth to enter pastDOB for DVA")
    public void iClearTheDataAndReEnterTheDateOfBirthToEnterPastDOBForDVA() {
        dvaPastErrorrWelsh();
    }

    @When("I enter the invalid issue date for DVA")
    public void iEnterTheInvalidIssueDateForDVA() {
        invalidIssueDayForDVA();
    }

    @Then("I clear the data and re enter the invalid future year for DVA")
    public void iClearTheDataAndReEnterTheInvalidFutureYearForDVA() {
        enterInValidIssueDateWithFutureYearDVA();
    }

    @When("I enter invalid driving licence less than 8 char for DVA")
    public void iEnterInvalidDrivingLicenceLessThanCharForDVA() {
        invalidDrivingLicenceWithlessCharDVA();
    }

    @And("I see “Rhowch y rhif yn union fel mae'n ymddangos ar eich trwydded yrru”x")
    public void iSeeRhowchYRhifYnUnionFelMaeNYmddangosArEichTrwyddedYrruX() {
        licenceErrorWelshforExactonDL();
    }

    @When("I enter the invalid Valid to date field for DVA")
    public void iEnterTheInvalidValidToDateFieldForDVA() {
        invalidValidUntilForDVA();
    }

    @Then("I clear the data and re enter the valid to expired year for DVA")
    public void iClearTheDataAndReEnterTheValidToExpiredYearForDVA() {
        enterTheValidToExpiredYearForDVA();
    }

    @And("I see “Dylai rhif eich trwydded fod yn [X] nod o hyd” for DVLA")
    public void iSeeDylaiRhifEichTrwyddedFodYnXNodOHydForDVLA() {
        licenceNumberErrorWelshForDVLA();
    }

    @And("I see “Dylai rhif eich trwydded fod yn [X] nod o hyd” for DVA")
    public void iSeeDylaiRhifEichTrwyddedFodYnXNodOHydForDVA() {
        licenceNumberErrorWelshForDVA();
    }

    @And("I see the sentence “Dyma'r rhif hir yn adran  ar eich trwydded” for DVA")
    public void iSeeTheSentenceDymaRRhifHirYnAdranArEichTrwyddedForDVA() {
        licenceNumberErrorWelshforDVA();
    }

    @Then("I validate the page error page title")
    public void iValidateThePageErrorPageTitle() {
        ErrorPageTitleDVLA();
    }

    @Then(
            "I see the error box “Gwiriwch bod eich manylion yn paru gyda beth sydd ar eich trwydded yrru y DU”")
    public void iSeeTheErrorBoxGwiriwchBodEichManylionYnParuGydaBethSyddArEichTrwyddedYrruYDU() {
        checkYourDetailsSentence();
    }

    @And("I see “Gwall”")
    public void iSeeGwall() {
        erroeWord();
    }

    @And("I see “Nid oeddem yn gallu dod o hyd i'ch manylion”")
    public void iSeeNidOeddemYnGalluDodOHydIChManylion() {
        weCouldNotFindDetailsSentence();
    }

    @And("I see “Ni fyddwch yn gallu newid eich manylion eto os byddwch yn gwneud camgymeriad.”")
    public void iSeeNiFyddwchYnGalluNewidEichManylionEtoOsByddwchYnGwneudCamgymeriad() {
        thereIsaProblemSentence();
    }

    @And("I see Roedd yna broblem wrth i ni wirio eich manylion gyda'r [DVLA]")
    public void iSeeRoeddYnaBroblemWrthINiWirioEichManylionGydaRDVLADVA() {
        youWillBeAbleToFindSentence();
    }

    @And("I see Roedd yna broblem wrth i ni wirio eich manylion gyda'r [DVA]")
    public void iSeeRoeddYnaBroblemWrthINiWirioEichManylionGydaRDVA() {
        youWillBeAbleToFindSentenceDVA();
    }

    @When("I can see the DoB fields for DVA titled “Dyddiad geni”")
    public void iCanSeeTheDoBFieldsForDVATitledDyddiadGeni() {
        DateOfBirthFieldDVA();
    }

    @Then("I can see example  for DVA as Er enghraifft")
    public void iCanSeeExampleForDVAAsErEnghraifft() {
        DateOfBirthFieldHintDVA();
    }

    @And("I can see year for DVA as “Blwyddyn”")
    public void iCanSeeYearForDVAAsBlwyddyn() {
        yearFieldDVA();
    }

    @When("I can see the Issue date field titled for DVA “Dyddiad cyhoeddi”")
    public void iCanSeeTheIssueDateFieldTitledForDVADyddiadCyhoeddi() {
        issueDateFieldDVA();
    }

    @Then("Dyma r dyddiad yn adran 4a o ch trwydded, er enghraifft 27 5 2019 for DVA")
    public void dymaRDyddiadYnAdranAOChTrwyddedErEnghraifftForDVA() {
        issueDateSentenceDVA();
    }

    @And("I see “Ni ddylai rhif eich trwydded gynnwys unrhyw symbolau neu ofodau” for DVA")
    public void iSeeNiDdylaiRhifEichTrwyddedGynnwysUnrhywSymbolauNeuOfodauForDVA() {
        licenceErrorWelshforSplCharForDVA();
    }

    @And(
            "I see the sentence Byddwn yn gwirio eich manylion gydar DVLA i sicrhau nad yw eich trwydded yrru wedi cael ei chanslo na'i hadrodd fel un sydd ar goll neu wedi ei dwyn.")
    public void
            iSeeTheSentenceByddwnYnGwirioEichManylionGydarDVLAISicrhauNadYwEichTrwyddedYrruWediCaelEiChansloNaIHadroddFelUnSyddArGollNeuWediEiDwyn() {
        weWillCheckYourDetails();
    }
}
