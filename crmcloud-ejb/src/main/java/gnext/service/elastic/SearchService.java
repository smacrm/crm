/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service.elastic;

import gnext.bean.elastic.Document;
import gnext.interceptors.annotation.enums.Module;
import java.util.List;
import javax.ejb.Local;
import org.elasticsearch.search.SearchHits;

/**
 *
 * @author hungpham
 */
@Local
public interface SearchService extends java.io.Serializable {
    public String index(Document doc, Module module);
    public int bulkIndex(List<Document> documents, Module module);
    public SearchHits search(Module module, String query, String keywords, List<String> columns);
}
