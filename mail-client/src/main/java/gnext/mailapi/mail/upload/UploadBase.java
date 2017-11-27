/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.mailapi.mail.upload;

import java.util.Map;

/**
 *
 * @author daind
 */
public interface UploadBase {
    public void upload(Map<String, Object> data) throws Exception;
}
