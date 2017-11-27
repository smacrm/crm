/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service.issue;

import gnext.bean.issue.IssueLamp;
import gnext.service.EntityService;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author tungdt
 */
@Local
public interface IssueLampService extends EntityService<IssueLamp>{
    public List<IssueLamp> findIssueLamps(Integer companyId);
    public void insert(IssueLamp issueLamp) throws Exception;
    public IssueLamp checkSameLampColor(IssueLamp lamp);
    public void update(IssueLamp issueLamp) throws Exception;
//    public void removeIssueLamp(IssueLamp issueLamp) throws Exception;
}
