/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.quartz;

import org.quartz.StatefulJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Những instance sẽ đựoc cho vào queue để chờ instance trước đó thực hiện xong.
 * @author daind
 */
public abstract class DisableConcurrentExecutionJob extends CrmJob implements StatefulJob {
    private static final Logger MAINLOGGER = LoggerFactory.getLogger(ConcurrentExecutionJob.class);
    public DisableConcurrentExecutionJob() { super(); }
}
