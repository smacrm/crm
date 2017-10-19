/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service;

import gnext.bean.Prefecture;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author daind
 */
@Local
public interface PrefectureService extends EntityService<Prefecture> {
    public List<Prefecture> findCities(String prefectureLocaleCode, String prefectureName);
    
    public List<Prefecture> findCities(String prefectureLocaleCode);
    
    public Prefecture findByPrefectureCode(String prefectureLocaleCode,String prefectureCode);
    
    public Prefecture findByPrefectureName(String prefectureLocaleCode,String prefectureName);
    
}
