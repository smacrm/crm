/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service.job;

import gnext.bean.job.Batch;
import gnext.service.EntityService;
import javax.ejb.Local;

/**
 *
 * @author daind
 */
@Local
public interface BatchService extends EntityService<Batch> {
    public Batch search(Integer companyId, Integer batchId, boolean batchDeleted);
}
