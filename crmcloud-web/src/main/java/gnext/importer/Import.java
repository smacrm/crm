/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.importer;

import java.io.InputStream;

/**
 *
 * @author tungdt
 */
public interface Import {

    public void execute(InputStream is) throws Exception;
}
