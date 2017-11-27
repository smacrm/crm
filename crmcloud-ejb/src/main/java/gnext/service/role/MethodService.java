/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service.role;

import gnext.bean.role.Method;
import gnext.service.EntityService;
import javax.ejb.Local;


/**
 *
 * @author hungpham
 */
@Local
public interface MethodService extends EntityService<Method>{
    public Method find(String methodName);
}
