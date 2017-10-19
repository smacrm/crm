/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.quartz;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Những instance sẽ đựoc chạy đồng thời không chờ cho những instance trước đó hoàn thành xong.
 * @author daind
 */
public abstract class ConcurrentExecutionJob extends CrmJob {
    private static final Logger MAINLOGGER = LoggerFactory.getLogger(ConcurrentExecutionJob.class);
}
