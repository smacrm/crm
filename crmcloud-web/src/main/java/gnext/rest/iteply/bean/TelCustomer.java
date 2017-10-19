package gnext.rest.iteply.bean;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author hungpham
 * @since Dec 26, 2016
 */
public class TelCustomer implements Serializable{
    @Getter @Setter private String Code;
    @Getter @Setter private String FirstNameKana;
    @Getter @Setter private String LastNameKana;
    @Getter @Setter private String FirstNameKanji;
    @Getter @Setter private String LastNameKanji;
    @Getter @Setter private String PrefectureCode;
    @Getter @Setter private String PrefectureName;
    @Getter @Setter private String Address1;
    @Getter @Setter private String Address2;
    @Getter @Setter private String ClassificationCode;
    @Getter @Setter private String ClassificationName;
    @Getter @Setter private String IssueReceiveLargeCode;
    @Getter @Setter private String IssueReceiveLargeName;
    @Getter @Setter private String IssueReceiveMediumCode;
    @Getter @Setter private String IssueReceiveMediumName;
    @Getter @Setter private String IssueReceiveSmallCode;
    @Getter @Setter private String IssueReceiveSmallName;
    @Getter @Setter private String IssueReceiveDetailCode;
    @Getter @Setter private String IssueReceiveDetailName;
    @Getter @Setter private String ProductLargeCode;
    @Getter @Setter private String ProductLargeName;
    @Getter @Setter private String ProductMediumCode;
    @Getter @Setter private String ProductMediumName;
    @Getter @Setter private String ProductSmallCode;
    @Getter @Setter private String ProductSmallName	;
    @Getter @Setter private String ProductDetailCode;
    @Getter @Setter private String ProductDetailName;
    @Getter @Setter private String Note;
    @Getter @Setter private String SpecialtyFlag;
    @Getter @Setter private String SpecialtyPrefectureCode;
    @Getter @Setter private String SpecialtyPrefectureName;
    @Getter @Setter private String SpecialtyAddress1;
    @Getter @Setter private String SpecialtyAddress2;
    @Getter @Setter private String SpecialtyFirstNameKanji;
    @Getter @Setter private String SpecialtyLastNameKanji;
    @Getter @Setter private String SpecialtyFirstNameKana;
    @Getter @Setter private String SpecialtyLastNameKana;
    @Getter @Setter private String SpecialtyContents;
    @Getter @Setter private String telNumber;

    public TelCustomer() {
    }
}
