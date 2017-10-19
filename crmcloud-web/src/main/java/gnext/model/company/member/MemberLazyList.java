/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.model.company.member;

import gnext.bean.Member;
import gnext.controller.company.MemberController;
import gnext.model.AbstractLazyList;
import gnext.service.MemberService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.primefaces.model.SortOrder;

/**
 *
 * @author havd
 */
public class MemberLazyList extends AbstractLazyList<MemberModel>{
    private static final long serialVersionUID = -6066649522218682526L;
    
    private List<MemberModel> memberModels;
    private final MemberController controller;
    private final MemberService memberService;
    
    public MemberLazyList(final MemberController controller) {
        this.controller = controller;
        this.memberService = controller.getMemberService();
    }

    @Override
    public Object getRowKey(MemberModel memberModel) {
        return memberModel.getMember().getMemberId();
    }

    @Override
    public MemberModel getRowData(String memberId) {
        Integer id = Integer.valueOf(memberId);
        for (MemberModel memberModel : memberModels) {
            if (id.equals(getRowKey(memberModel))) {
                return memberModel;
            }
        }
        return null;
    }
    
    @Override
    protected int getTotal() {
        return memberService.total(controller.getQuery());
    }

    @Override
    protected List<MemberModel> getResults(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
        List<Member> members = memberService.find(first, pageSize, sortField, sortField, controller.getQuery());
        memberModels = new ArrayList<>();
        for (int i = 0; i < members.size(); i++) {
            Member member = members.get(i);
            String memberLoginId = member.getMemberLoginId();
            if (memberLoginId.length() > 5) {
                String memberLoginIdSub = memberLoginId.substring(5, memberLoginId.length());
                member.setMemberLoginId(memberLoginIdSub);
            }
            MemberModel model = new MemberModel(member);
            model.setRowNum(first + i);
            model.updateExtraInfors(controller);
            model.displayRole(controller);
            model.initInfos(controller.getCompanyTargetInfoService());
            memberModels.add(model);
        }
        return memberModels;
    }
}
