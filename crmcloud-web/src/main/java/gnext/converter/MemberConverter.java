/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.converter;

import gnext.bean.Member;
import java.util.List;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;
import org.primefaces.component.picklist.PickList;
import org.primefaces.model.DualListModel;

/**
 *
 * @author daind
 */
@FacesConverter(value = "gnext.picklist.converter.member")
public class MemberConverter extends AbstractConverter<Member>{

    @Override
    protected String convert2Text(FacesContext fc, UIComponent ui, Member e) {
        return String.valueOf(e.getMemberId());
    }

    @Override
    protected Member convert2Bean(FacesContext fc, UIComponent ui, String text) {
        if(!(ui instanceof PickList)) return null;
        Object dualList = ((PickList) ui).getValue();
        DualListModel dl = (DualListModel) dualList;

        List<Member> membersources = (List<Member>) dl.getSource();
        for (Member m : membersources) {
            String id = String.valueOf(m.getMemberId());
            if (text.equals(id)) { return m; }
        }

        List<Member> membertargets = (List<Member>) dl.getTarget();
        for (Member m : membertargets) {
            String id = String.valueOf(m.getMemberId());
            if (text.equals(id)) { return m; }
        }
        return null;
    }
}
