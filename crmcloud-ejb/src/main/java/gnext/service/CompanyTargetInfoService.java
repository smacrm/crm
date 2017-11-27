/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service;

import gnext.bean.CompanyTargetInfo;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author daind
 */
@Local
public interface CompanyTargetInfoService extends EntityService<CompanyTargetInfo> {
    public List<CompanyTargetInfo> find(int companyTarget, int companyTargetId, int companyFlagType, short companyTargetDeleted);
    
    public List<CompanyTargetInfo> find(int companyTarget, int companyTargetId, short companyTargetDeleted);
}
