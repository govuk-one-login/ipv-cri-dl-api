package gov.di_ipv_drivingpermit.utilities;

import java.util.HashMap;
import java.util.Map;

public class TestDataCreator {

    public static Map<String, TestInput> dvaTestUsers = new HashMap<>();
    public static Map<String, TestInput> dvlaTestUsers = new HashMap<>();

    public static DVADrivingLicenceSubject billyBatsonHappyPath;
    public static DVADrivingLicenceSubject selinaUnhappyPath;
    public static DVADrivingLicenceSubject billyBatsonIncorrectLicenceNumber;
    public static DVADrivingLicenceSubject billyBatsonIncorrectDateOfBirth;
    public static DVADrivingLicenceSubject billyBatsonIncorrectLastName;
    public static DVADrivingLicenceSubject billyBatsonIncorrectFirstName;
    public static DVADrivingLicenceSubject billyBatsonIncorrectIssueDate;
    public static DVADrivingLicenceSubject billyBatsonIncorrectValidToDate;
    public static DVADrivingLicenceSubject billyBatsonIncorrectPostcode;
    public static DVADrivingLicenceSubject billyBatsonLastNameWithNumbers;
    public static DVADrivingLicenceSubject billyBatsonIncorrectLastNameWithSpecialChars;
    public static DVADrivingLicenceSubject billyBatsonIncorrectNoSecondName;
    public static DVADrivingLicenceSubject billyBatsonIncorrectFirstNameWithNumbers;
    public static DVADrivingLicenceSubject billyBatsonIncorrectFirstNameWithSpecialChars;
    public static DVADrivingLicenceSubject billyBatsonIncorrectNoFirstName;
    public static DVADrivingLicenceSubject billyBatsonIncorrectMiddleNameWithSpecialChars;
    public static DVADrivingLicenceSubject billyBatsonIncorrectMiddleNameWithNumbers;
    public static DVADrivingLicenceSubject billyBatsonIncorrectInvalidDateOfBirth;
    public static DVADrivingLicenceSubject billyBatsonIncorrectDoBWithSpecialChars;
    public static DVADrivingLicenceSubject billyBatsonIncorrectDoBInFuture;
    public static DVADrivingLicenceSubject billyBatsonIncorrectNoDoB;
    public static DVADrivingLicenceSubject billyBatsonIncorrectInvalidIssueDate;
    public static DVADrivingLicenceSubject billyBatsonIncorrectIssueDateWithSpecialChars;
    public static DVADrivingLicenceSubject billyBatsonIncorrectIssueDateInFuture;
    public static DVADrivingLicenceSubject billyBatsonIncorrectNoDvaIssueDate;
    public static DVADrivingLicenceSubject billyBatsonIncorrectInvalidToDate;
    public static DVADrivingLicenceSubject billyBatsonIncorrectValidToDateWithSpecialChars;
    public static DVADrivingLicenceSubject billyBatsonValidToDateInPast;
    public static DVADrivingLicenceSubject billyBatsonIncorrectNoValidToDate;
    public static DVADrivingLicenceSubject billyBatsonIncorrectLessThan8Chars;
    public static DVADrivingLicenceSubject billyBatsonIncorrectLicenceNumberWithSpecialChars;
    public static DVADrivingLicenceSubject billyBatsonIncorrectLicenceNumberWithAlphanumerics;
    public static DVADrivingLicenceSubject billyBatsonIncorrectLicenceNumberWithAlphaChars;
    public static DVADrivingLicenceSubject billyBatsonIncorrectNoLicenceNumber;
    public static DVADrivingLicenceSubject billyBatsonIncorrectPostcodeLessThan5Chars;
    public static DVADrivingLicenceSubject billyBatsonIncorrectPostcodeWithSpecialChars;
    public static DVADrivingLicenceSubject billyBatsonIncorrectPostcodeOnlyNumericChars;
    public static DVADrivingLicenceSubject billyBatsonIncorrectPostcodeOnlyAlphas;
    public static DVADrivingLicenceSubject billyBatsonIncorrectNoPostcode;
    public static DVADrivingLicenceSubject billyBatsonIncorrectInternationalPostcode;

    public static DrivingLicenceSubject peterHappyPath;
    public static DrivingLicenceSubject peterIncorrectIssueNumber;
    public static DrivingLicenceSubject peterIncorrectDrivingLicenceNumber;
    public static DrivingLicenceSubject peterNoIssueNumber;
    public static DrivingLicenceSubject peterIssueNumberWithAlphaChar;
    public static DrivingLicenceSubject peterIssueNumberWithAlphaNumericChar;
    public static DrivingLicenceSubject peterIssueNumberWithSpecialChar;
    public static DrivingLicenceSubject peterIssueNumberLessThanTwo;
    public static DrivingLicenceSubject peterLicenceNumberLessThanSixteen;
    public static DrivingLicenceSubject peterIncorrectLicenceNumberWithAlphaChars;
    public static DrivingLicenceSubject peterIncorrectLicenceNumberWithNumericChars;
    public static DrivingLicenceSubject peterIncorrectLicenceNumberWithSpecialChar;
    public static DrivingLicenceSubject peterIncorrectIssueDate;
    public static DrivingLicenceSubject peterIncorrectLastName;
    public static DrivingLicenceSubject peterIncorrectFirstName;
    public static DrivingLicenceSubject peterIncorrectPostcode;
    public static DrivingLicenceSubject peterIncorrectValidToDate;

    public static void createDefaultResponses() {
        billyBatsonHappyPath =
                new DVADrivingLicenceSubject(
                        "55667788",
                        "BATSON",
                        "BILLY",
                        "",
                        "26",
                        "07",
                        "1981",
                        "01",
                        "10",
                        "2042",
                        "19",
                        "04",
                        "2001",
                        "NW3 5RG");
        selinaUnhappyPath =
                new DVADrivingLicenceSubject(
                        "88776655",
                        "KYLE",
                        "SELINA",
                        "",
                        "12",
                        "08",
                        "1985",
                        "04",
                        "08",
                        "2032",
                        "14",
                        "09",
                        "2009",
                        "E20 2AQ");

        peterHappyPath =
                new DrivingLicenceSubject(
                        "PARKE610112PBFGH",
                        "PARKER",
                        "PETER",
                        "BENJAMIN",
                        "11",
                        "10",
                        "1962",
                        "09",
                        "12",
                        "2062",
                        "23",
                        "05",
                        "1982",
                        "12",
                        "BS98 1TL");

        billyBatsonIncorrectLicenceNumber = new DVADrivingLicenceSubject(billyBatsonHappyPath);
        billyBatsonIncorrectLicenceNumber.setDvaLicenceNumber("88776655");

        billyBatsonIncorrectDateOfBirth = new DVADrivingLicenceSubject(billyBatsonHappyPath);
        billyBatsonIncorrectDateOfBirth.setBirthDay("12");
        billyBatsonIncorrectDateOfBirth.setBirthMonth("08");
        billyBatsonIncorrectDateOfBirth.setBirthYear("1985");

        billyBatsonIncorrectLastName = new DVADrivingLicenceSubject(billyBatsonHappyPath);
        billyBatsonIncorrectLastName.setLastName("KYLE");

        billyBatsonIncorrectFirstName = new DVADrivingLicenceSubject(billyBatsonHappyPath);
        billyBatsonIncorrectFirstName.setFirstName("SELINA");

        billyBatsonIncorrectIssueDate = new DVADrivingLicenceSubject(billyBatsonHappyPath);
        billyBatsonIncorrectIssueDate.setIssueDay("14");
        billyBatsonIncorrectIssueDate.setIssueMonth("09");
        billyBatsonIncorrectIssueDate.setIssueYear("2009");

        billyBatsonIncorrectValidToDate = new DVADrivingLicenceSubject(billyBatsonHappyPath);
        billyBatsonIncorrectValidToDate.setValidToDay("04");
        billyBatsonIncorrectValidToDate.setValidToMonth("08");
        billyBatsonIncorrectValidToDate.setValidToYear("2032");

        billyBatsonIncorrectPostcode = new DVADrivingLicenceSubject(billyBatsonHappyPath);
        billyBatsonIncorrectPostcode.setPostcode("E20 2AQ");

        billyBatsonLastNameWithNumbers = new DVADrivingLicenceSubject(billyBatsonHappyPath);
        billyBatsonLastNameWithNumbers.setLastName("KYLE123");

        billyBatsonIncorrectLastNameWithSpecialChars =
                new DVADrivingLicenceSubject(billyBatsonHappyPath);
        billyBatsonIncorrectLastNameWithSpecialChars.setLastName("KYLE^&(");

        billyBatsonIncorrectNoSecondName = new DVADrivingLicenceSubject(billyBatsonHappyPath);
        billyBatsonIncorrectNoSecondName.setLastName("");

        billyBatsonIncorrectFirstNameWithNumbers =
                new DVADrivingLicenceSubject(billyBatsonHappyPath);
        billyBatsonIncorrectFirstNameWithNumbers.setFirstName("SELINA987");

        billyBatsonIncorrectFirstNameWithSpecialChars =
                new DVADrivingLicenceSubject(billyBatsonHappyPath);
        billyBatsonIncorrectFirstNameWithSpecialChars.setFirstName("SELINA%$@");

        billyBatsonIncorrectMiddleNameWithSpecialChars =
                new DVADrivingLicenceSubject(billyBatsonHappyPath);
        billyBatsonIncorrectMiddleNameWithSpecialChars.setMiddleNames("SELINA%$@");

        billyBatsonIncorrectMiddleNameWithNumbers =
                new DVADrivingLicenceSubject(billyBatsonHappyPath);
        billyBatsonIncorrectMiddleNameWithNumbers.setMiddleNames("SELINA987");

        billyBatsonIncorrectNoFirstName = new DVADrivingLicenceSubject(billyBatsonHappyPath);
        billyBatsonIncorrectNoFirstName.setFirstName("");

        billyBatsonIncorrectInvalidDateOfBirth = new DVADrivingLicenceSubject(billyBatsonHappyPath);
        billyBatsonIncorrectInvalidDateOfBirth.setBirthDay("51");
        billyBatsonIncorrectInvalidDateOfBirth.setBirthMonth("71");
        billyBatsonIncorrectInvalidDateOfBirth.setBirthYear("198");

        billyBatsonIncorrectDoBWithSpecialChars =
                new DVADrivingLicenceSubject(billyBatsonHappyPath);
        billyBatsonIncorrectDoBWithSpecialChars.setBirthDay("@");
        billyBatsonIncorrectDoBWithSpecialChars.setBirthMonth("*&");
        billyBatsonIncorrectDoBWithSpecialChars.setBirthYear("19 7");

        billyBatsonIncorrectDoBInFuture = new DVADrivingLicenceSubject(billyBatsonHappyPath);
        billyBatsonIncorrectDoBInFuture.setBirthDay("10");
        billyBatsonIncorrectDoBInFuture.setBirthMonth("10");
        billyBatsonIncorrectDoBInFuture.setBirthYear("2042");

        billyBatsonIncorrectNoDoB = new DVADrivingLicenceSubject(billyBatsonHappyPath);
        billyBatsonIncorrectNoDoB.setBirthDay("");
        billyBatsonIncorrectNoDoB.setBirthMonth("");
        billyBatsonIncorrectNoDoB.setBirthYear("");

        billyBatsonIncorrectInvalidIssueDate = new DVADrivingLicenceSubject(billyBatsonHappyPath);
        billyBatsonIncorrectInvalidIssueDate.setIssueDay("AA");
        billyBatsonIncorrectInvalidIssueDate.setIssueMonth("BB");
        billyBatsonIncorrectInvalidIssueDate.setIssueYear("AABC");

        billyBatsonIncorrectIssueDateWithSpecialChars =
                new DVADrivingLicenceSubject(billyBatsonHappyPath);
        billyBatsonIncorrectIssueDateWithSpecialChars.setIssueDay("&");
        billyBatsonIncorrectIssueDateWithSpecialChars.setIssueMonth("^%");
        billyBatsonIncorrectIssueDateWithSpecialChars.setIssueYear("£$ ^");

        billyBatsonIncorrectIssueDateInFuture = new DVADrivingLicenceSubject(billyBatsonHappyPath);
        billyBatsonIncorrectIssueDateInFuture.setIssueDay("01");
        billyBatsonIncorrectIssueDateInFuture.setIssueMonth("10");
        billyBatsonIncorrectIssueDateInFuture.setIssueYear("2043");

        billyBatsonIncorrectNoDvaIssueDate = new DVADrivingLicenceSubject(billyBatsonHappyPath);
        billyBatsonIncorrectNoDvaIssueDate.setIssueDay("");
        billyBatsonIncorrectNoDvaIssueDate.setIssueMonth("");
        billyBatsonIncorrectNoDvaIssueDate.setIssueYear("");

        billyBatsonIncorrectInvalidToDate = new DVADrivingLicenceSubject(billyBatsonHappyPath);
        billyBatsonIncorrectInvalidToDate.setValidToDay("50");
        billyBatsonIncorrectInvalidToDate.setValidToMonth("10");
        billyBatsonIncorrectInvalidToDate.setValidToYear("2030");

        billyBatsonIncorrectValidToDateWithSpecialChars =
                new DVADrivingLicenceSubject(billyBatsonHappyPath);
        billyBatsonIncorrectValidToDateWithSpecialChars.setValidToDay("!@");
        billyBatsonIncorrectValidToDateWithSpecialChars.setValidToMonth("£$");
        billyBatsonIncorrectValidToDateWithSpecialChars.setValidToYear("%^ *");

        billyBatsonValidToDateInPast = new DVADrivingLicenceSubject(billyBatsonHappyPath);
        billyBatsonValidToDateInPast.setValidToDay("10");
        billyBatsonValidToDateInPast.setValidToMonth("01");
        billyBatsonValidToDateInPast.setValidToYear("2010");

        billyBatsonIncorrectNoValidToDate = new DVADrivingLicenceSubject(billyBatsonHappyPath);
        billyBatsonIncorrectNoValidToDate.setValidToDay("");
        billyBatsonIncorrectNoValidToDate.setValidToMonth("");
        billyBatsonIncorrectNoValidToDate.setValidToYear("");

        billyBatsonIncorrectLessThan8Chars = new DVADrivingLicenceSubject(billyBatsonHappyPath);
        billyBatsonIncorrectLessThan8Chars.setDvaLicenceNumber("5566778");

        billyBatsonIncorrectLicenceNumberWithSpecialChars =
                new DVADrivingLicenceSubject(billyBatsonHappyPath);
        billyBatsonIncorrectLicenceNumberWithSpecialChars.setDvaLicenceNumber("55667^&*");

        billyBatsonIncorrectLicenceNumberWithAlphanumerics =
                new DVADrivingLicenceSubject(billyBatsonHappyPath);
        billyBatsonIncorrectLicenceNumberWithAlphanumerics.setDvaLicenceNumber("55667ABC");

        billyBatsonIncorrectLicenceNumberWithAlphaChars =
                new DVADrivingLicenceSubject(billyBatsonHappyPath);
        billyBatsonIncorrectLicenceNumberWithAlphaChars.setDvaLicenceNumber("XYZabdAB");

        billyBatsonIncorrectNoLicenceNumber = new DVADrivingLicenceSubject(billyBatsonHappyPath);
        billyBatsonIncorrectNoLicenceNumber.setDvaLicenceNumber("");

        billyBatsonIncorrectPostcodeLessThan5Chars =
                new DVADrivingLicenceSubject(billyBatsonHappyPath);
        billyBatsonIncorrectPostcodeLessThan5Chars.setPostcode("E20A");

        billyBatsonIncorrectPostcodeWithSpecialChars =
                new DVADrivingLicenceSubject(billyBatsonHappyPath);
        billyBatsonIncorrectPostcodeWithSpecialChars.setPostcode("NW* ^%G");

        billyBatsonIncorrectPostcodeOnlyNumericChars =
                new DVADrivingLicenceSubject(billyBatsonHappyPath);
        billyBatsonIncorrectPostcodeOnlyNumericChars.setPostcode("123 456");

        billyBatsonIncorrectPostcodeOnlyAlphas = new DVADrivingLicenceSubject(billyBatsonHappyPath);
        billyBatsonIncorrectPostcodeOnlyAlphas.setPostcode("ABC XYZ");

        billyBatsonIncorrectNoPostcode = new DVADrivingLicenceSubject(billyBatsonHappyPath);
        billyBatsonIncorrectNoPostcode.setPostcode("");

        billyBatsonIncorrectInternationalPostcode =
                new DVADrivingLicenceSubject(billyBatsonHappyPath);
        billyBatsonIncorrectInternationalPostcode.setPostcode("CA 95128");

        peterIncorrectIssueNumber = new DrivingLicenceSubject(peterHappyPath);
        peterIncorrectIssueNumber.setIssueNumber("20");

        peterIncorrectDrivingLicenceNumber = new DrivingLicenceSubject(peterHappyPath);
        peterIncorrectDrivingLicenceNumber.setLicenceNumber("PARKE610112PBFGI");

        peterNoIssueNumber = new DrivingLicenceSubject(peterHappyPath);
        peterNoIssueNumber.setIssueNumber("");

        peterIssueNumberWithAlphaChar = new DrivingLicenceSubject(peterHappyPath);
        peterIssueNumberWithAlphaChar.setIssueNumber("AB");

        peterIssueNumberWithAlphaNumericChar = new DrivingLicenceSubject(peterHappyPath);
        peterIssueNumberWithAlphaNumericChar.setIssueNumber("A1");

        peterIssueNumberWithSpecialChar = new DrivingLicenceSubject(peterHappyPath);
        peterIssueNumberWithSpecialChar.setIssueNumber("A@");

        peterIssueNumberLessThanTwo = new DrivingLicenceSubject(peterHappyPath);
        peterIssueNumberLessThanTwo.setIssueNumber("1");

        peterLicenceNumberLessThanSixteen = new DrivingLicenceSubject(peterHappyPath);
        peterLicenceNumberLessThanSixteen.setLicenceNumber("PARKE610112PBF");

        peterIncorrectLicenceNumberWithAlphaChars = new DrivingLicenceSubject(peterHappyPath);
        peterIncorrectLicenceNumberWithAlphaChars.setLicenceNumber("abcdefghijklomnp");

        peterIncorrectLicenceNumberWithNumericChars = new DrivingLicenceSubject(peterHappyPath);
        peterIncorrectLicenceNumberWithNumericChars.setLicenceNumber("1234567890111213");

        peterIncorrectLicenceNumberWithSpecialChar = new DrivingLicenceSubject(peterHappyPath);
        peterIncorrectLicenceNumberWithSpecialChar.setLicenceNumber("12345678901112@@");

        peterIncorrectIssueDate = new DrivingLicenceSubject(peterHappyPath);
        peterIncorrectIssueDate.setIssueDay("14");
        peterIncorrectIssueDate.setIssueMonth("09");
        peterIncorrectIssueDate.setIssueYear("2009");

        peterIncorrectLastName = new DrivingLicenceSubject(peterHappyPath);
        peterIncorrectLastName.setLastName("KYLE");

        peterIncorrectFirstName = new DrivingLicenceSubject(peterHappyPath);
        peterIncorrectFirstName.setFirstName("KYLE");

        peterIncorrectPostcode = new DrivingLicenceSubject(peterHappyPath);
        peterIncorrectPostcode.setPostcode("E20 2AQ");

        peterIncorrectValidToDate = new DrivingLicenceSubject(peterHappyPath);
        peterIncorrectValidToDate.setValidToDay("04");
        peterIncorrectValidToDate.setValidToMonth("08");
        peterIncorrectValidToDate.setValidToYear("2032");

        dvaTestUsers.put("DVADrivingLicenceSubjectHappyBilly", billyBatsonHappyPath);
        dvaTestUsers.put("DVADrivingLicenceSubjectUnhappySelina", selinaUnhappyPath);
        dvaTestUsers.put("NoLastName", billyBatsonIncorrectNoSecondName);
        dvaTestUsers.put("NoFirstName", billyBatsonIncorrectNoFirstName);
        dvaTestUsers.put("NoDateOfBirth", billyBatsonIncorrectNoDoB);
        dvaTestUsers.put("NoIssueDate", billyBatsonIncorrectNoDvaIssueDate);
        dvaTestUsers.put("NoValidToDate", billyBatsonIncorrectNoValidToDate);
        dvaTestUsers.put("NoDrivingLicenceNumber", billyBatsonIncorrectNoLicenceNumber);
        dvaTestUsers.put("NoPostcode", billyBatsonIncorrectNoPostcode);
        dvaTestUsers.put("InvalidFirstNameWithNumbers", billyBatsonIncorrectFirstNameWithNumbers);
        dvaTestUsers.put(
                "InvalidFirstNameWithSpecialCharacters",
                billyBatsonIncorrectFirstNameWithSpecialChars);
        dvaTestUsers.put(
                "DateOfBirthWithSpecialCharacters", billyBatsonIncorrectDoBWithSpecialChars);
        dvaTestUsers.put("InvalidDateOfBirth", billyBatsonIncorrectInvalidDateOfBirth);
        dvaTestUsers.put("IncorrectDateOfBirth", billyBatsonIncorrectDateOfBirth);
        dvaTestUsers.put(
                "IssueDateWithSpecialCharacters", billyBatsonIncorrectIssueDateWithSpecialChars);
        dvaTestUsers.put(
                "ValidToDateWithSpecialCharacters",
                billyBatsonIncorrectValidToDateWithSpecialChars);
        dvaTestUsers.put("ValidToDateInPast", billyBatsonValidToDateInPast);
        dvaTestUsers.put(
                "DrivingLicenceNumberWithSpecialChar",
                billyBatsonIncorrectLicenceNumberWithSpecialChars);
        dvaTestUsers.put("PostcodeWithSpecialChar", billyBatsonIncorrectPostcodeWithSpecialChars);
        dvaTestUsers.put("InternationalPostcode", billyBatsonIncorrectInternationalPostcode);
        dvaTestUsers.put("IncorrectDVALastName", billyBatsonIncorrectLastName);
        dvaTestUsers.put("IncorrectDVAIssueDate", billyBatsonIncorrectInvalidIssueDate);
        dvaTestUsers.put("PostcodeWithAlphaChar", billyBatsonIncorrectPostcodeOnlyAlphas);
        dvaTestUsers.put("PostcodeWithNumericChar", billyBatsonIncorrectPostcodeOnlyNumericChars);
        dvaTestUsers.put("PostcodeLessThan5Char", billyBatsonIncorrectPostcodeLessThan5Chars);
        dvaTestUsers.put(
                "DrivingLicenceNumberWithAlphaChar",
                billyBatsonIncorrectLicenceNumberWithAlphaChars);
        dvaTestUsers.put(
                "DrivingLicenceNumberWithNumericChar",
                billyBatsonIncorrectLicenceNumberWithAlphanumerics);
        dvaTestUsers.put("DrivingLicenceNumLessThan8Char", billyBatsonIncorrectLessThan8Chars);
        dvaTestUsers.put("InvalidValidToDate", billyBatsonIncorrectValidToDateWithSpecialChars);
        dvaTestUsers.put("IncorrectValidToDate", billyBatsonIncorrectValidToDate);
        dvaTestUsers.put("IssueDateInFuture", billyBatsonIncorrectIssueDateInFuture);
        dvaTestUsers.put("DateOfBirthInFuture", billyBatsonIncorrectDoBInFuture);
        dvaTestUsers.put("InvalidLastNameWithNumbers", billyBatsonLastNameWithNumbers);
        dvaTestUsers.put(
                "InvalidLastNameWithSpecialCharacters",
                billyBatsonIncorrectLastNameWithSpecialChars);
        dvaTestUsers.put("IncorrectDrivingLicenceNumber", billyBatsonIncorrectLicenceNumber);
        dvaTestUsers.put("IncorrectPostcode", billyBatsonIncorrectPostcode);
        dvaTestUsers.put("IncorrectLastName", billyBatsonIncorrectLastName);
        dvaTestUsers.put("IncorrectFirstName", billyBatsonIncorrectFirstName);
        dvaTestUsers.put("InvalidIssueDate", billyBatsonIncorrectInvalidIssueDate);
        dvaTestUsers.put("IncorrectIssueDate", billyBatsonIncorrectIssueDate);

        dvlaTestUsers.put("DrivingLicenceSubjectHappyPeter", peterHappyPath);
        dvlaTestUsers.put("NoLastName", billyBatsonIncorrectNoSecondName);
        dvlaTestUsers.put("NoFirstName", billyBatsonIncorrectNoFirstName);
        dvlaTestUsers.put("NoDateOfBirth", billyBatsonIncorrectNoDoB);
        dvlaTestUsers.put("NoIssueDate", billyBatsonIncorrectNoDvaIssueDate);
        dvlaTestUsers.put("NoValidToDate", billyBatsonIncorrectNoValidToDate);
        dvlaTestUsers.put("NoDrivingLicenceNumber", billyBatsonIncorrectNoLicenceNumber);
        dvlaTestUsers.put("NoPostcode", billyBatsonIncorrectNoPostcode);
        dvlaTestUsers.put("InvalidFirstNameWithNumbers", billyBatsonIncorrectFirstNameWithNumbers);
        dvlaTestUsers.put(
                "InvalidFirstNameWithSpecialCharacters",
                billyBatsonIncorrectFirstNameWithSpecialChars);
        dvlaTestUsers.put("InvalidLastNameWithNumbers", billyBatsonLastNameWithNumbers);
        dvlaTestUsers.put(
                "InvalidLastNameWithSpecialCharacters",
                billyBatsonIncorrectLastNameWithSpecialChars);
        dvlaTestUsers.put(
                "DateOfBirthWithSpecialCharacters", billyBatsonIncorrectDoBWithSpecialChars);
        dvlaTestUsers.put("InvalidDateOfBirth", billyBatsonIncorrectInvalidDateOfBirth);
        dvlaTestUsers.put("IncorrectDateOfBirth", billyBatsonIncorrectDateOfBirth);
        dvlaTestUsers.put(
                "IssueDateWithSpecialCharacters", billyBatsonIncorrectIssueDateWithSpecialChars);
        dvlaTestUsers.put(
                "ValidToDateWithSpecialCharacters",
                billyBatsonIncorrectValidToDateWithSpecialChars);
        dvlaTestUsers.put("ValidToDateInPast", billyBatsonValidToDateInPast);
        dvlaTestUsers.put("InvalidValidToDate", billyBatsonIncorrectValidToDateWithSpecialChars);
        dvlaTestUsers.put("IncorrectValidToDate", peterIncorrectValidToDate);
        dvlaTestUsers.put(
                "DrivingLicenceNumberWithSpecialChar", peterIncorrectLicenceNumberWithSpecialChar);
        dvlaTestUsers.put("PostcodeWithSpecialChar", billyBatsonIncorrectPostcodeWithSpecialChars);
        dvlaTestUsers.put("InternationalPostcode", billyBatsonIncorrectInternationalPostcode);
        dvlaTestUsers.put("IncorrectIssueNumber", peterIncorrectIssueNumber);
        dvlaTestUsers.put("IncorrectDrivingLicenceNumber", peterIncorrectDrivingLicenceNumber);
        dvlaTestUsers.put("PostcodeWithAlphaChar", billyBatsonIncorrectPostcodeOnlyAlphas);
        dvlaTestUsers.put("PostcodeWithNumericChar", billyBatsonIncorrectPostcodeOnlyNumericChars);
        dvlaTestUsers.put("PostcodeLessThan5Char", billyBatsonIncorrectPostcodeLessThan5Chars);
        dvlaTestUsers.put("IncorrectPostcode", peterIncorrectPostcode);
        dvlaTestUsers.put("NoIssueNumber", peterNoIssueNumber);
        dvlaTestUsers.put("IssueNumberWithAlphaChar", peterIssueNumberWithAlphaChar);
        dvlaTestUsers.put("IssueNumberWithAlphanumericChar", peterIssueNumberWithAlphaNumericChar);
        dvlaTestUsers.put("IssueNumberWithSpecialChar", peterIssueNumberWithSpecialChar);
        dvlaTestUsers.put("IssueNumberLessThan2Char", peterIssueNumberLessThanTwo);
        dvlaTestUsers.put(
                "DrivingLicenceNumberWithAlphaChar", peterIncorrectLicenceNumberWithAlphaChars);
        dvlaTestUsers.put(
                "DrivingLicenceNumberWithNumericChar", peterIncorrectLicenceNumberWithNumericChars);
        dvlaTestUsers.put("DrivingLicenceNumLessThan16Char", peterLicenceNumberLessThanSixteen);
        dvlaTestUsers.put("IssueDateInFuture", billyBatsonIncorrectIssueDateInFuture);
        dvlaTestUsers.put("InvalidIssueDate", billyBatsonIncorrectInvalidIssueDate);
        dvlaTestUsers.put("DateOfBirthInFuture", billyBatsonIncorrectDoBInFuture);
        dvlaTestUsers.put(
                "InvalidMiddleNamesWithSpecialCharacters",
                billyBatsonIncorrectMiddleNameWithSpecialChars);
        dvlaTestUsers.put(
                "InvalidMiddleNamesWithNumbers", billyBatsonIncorrectMiddleNameWithNumbers);
        dvlaTestUsers.put("IncorrectLastName", peterIncorrectLastName);
        dvlaTestUsers.put("IncorrectFirstName", peterIncorrectFirstName);
        dvlaTestUsers.put("IncorrectIssueDate", peterIncorrectIssueDate);
    }

    public static TestInput getDVATestUserFromMap(String issuer, String scenario) {
        if (issuer.equals("DVLA")) {
            return dvlaTestUsers.get(scenario);
        } else {
            return dvaTestUsers.get(scenario);
        }
    }
}
