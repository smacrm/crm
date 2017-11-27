/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.log;

import org.apache.commons.exec.LogOutputStream;
import org.apache.log4j.Logger;

/**
 *
 * @author daind
 */
public class ExecLogHangler extends LogOutputStream {
    private final org.apache.log4j.Logger logger;

    public ExecLogHangler(Logger logger) {
        super();
        this.logger = logger;
    }
    
    @Override
    protected void processLine(String line, int logLevel) {
        if(logger == null) return;
        if(line.contains("[ERROR]"))
            logger.log(org.apache.log4j.Level.ERROR, line);
        else
            logger.log(org.apache.log4j.Level.INFO, line);
    }
}
