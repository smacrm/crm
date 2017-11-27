package gnext.rest.iteply.bean;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author hungpham
 * @since Dec 26, 2016
 */
public class Itelpy implements Serializable{
    @Getter @Setter private String IssueCode;
    @Getter @Setter private String TelNumber;
    @Getter @Setter private String Code;
    @Getter @Setter private String PrefectureName;
    @Getter @Setter private String Address1;
    @Getter @Setter private String Address2;
    @Getter @Setter private String FirstNameKanji;
    @Getter @Setter private String LastNameKanji;
    @Getter @Setter private String LastNameKana;
    @Getter @Setter private String FirstNameKana;
    @Getter @Setter private String IssueReceiveLargeName;
    @Getter @Setter private String IssueReceiveMediumName;
    @Getter @Setter private String IssueReceiveSmallName;
    @Getter @Setter private String IssueReceiveDetailName;
    @Getter @Setter private String ClassName;
    @Getter @Setter private String Note;
    @Getter @Setter private String ProductLargeName;
    @Getter @Setter private String ProductMediumName;
    @Getter @Setter private String ProductSmallName;
    @Getter @Setter private String ProductDetailName;
    @Getter @Setter private String SpecialtyFlag;
    @Getter @Setter private String SpeciallyFirstKanji;
    @Getter @Setter private String SpeciallyLastKanji;
    @Getter @Setter private String SpeciallyFirstKana;
    @Getter @Setter private String SpeciallyLastKana;
    @Getter @Setter private String SpeciallyPrefectureName;
    @Getter @Setter private String SpecialtyContents;

    public Itelpy() {
    }
    
}
