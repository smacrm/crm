/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.utils;

import gnext.bean.mente.MenteItem;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.faces.model.SelectItem;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author gnextadmin
 */
public class LabelValue implements Serializable{
    @Getter @Setter
    private String label;

    @Setter
    private String value;

    @Getter @Setter
    private String type;

    @Getter @Setter
    private List<SelectItem> dataList = new ArrayList<>();

    @Getter @Setter
    private List<MenteItem> itemMenteList = new ArrayList<>();

    @Getter @Setter
    private String viewLabel;

    @Getter @Setter
    private String viewValue;

    @Getter @Setter
    private String level;

    @Getter @Setter
    private String dynamicKey;

    @Getter @Setter
    private String valueTo;

    @Getter @Setter
    private Boolean isSearchPeriod;

    @Getter @Setter
    private Integer lampDateSearchPeriod;

    @Getter @Setter
    private Boolean lampDatePeriodBeforeAfter;

    public LabelValue(String value, String label) {
        this.label = label;
        this.value = value;
    }

    public LabelValue(String value, String label, String type) {
        this.label = label;
        this.value = value;
        this.type = type;
    }

    public String getValue() {
        if(this.value != null && this.value.endsWith(".0")) return this.value.replace(".0", "");
        return this.value;
    }
}
