/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service;

import gnext.bean.ZipCode;
import javax.ejb.Local;

/**
 *
 * @author daind
 */
@Local
public interface ZipCodeService extends EntityService<ZipCode> {
    public ZipCode findByZipCode(String zipCode, String locale);
}
