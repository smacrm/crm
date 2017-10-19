/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service;

import gnext.bean.CompanyTargetInfo;
import gnext.bean.Group;
import gnext.bean.Member;
import java.util.List;
import java.util.Map;
import javax.ejb.Local;

/**
 *
 * @author tungdt
 */
@Local
public interface MemberImportService {
    public void batchUpdate(Map<Member, List<CompanyTargetInfo>> importData, Member userModel) throws Exception;
    public Group getMemberGroup(List<Group> groups, String strGroupName);
}
