package gnext.service.mente;

import gnext.bean.mente.Products;
import gnext.service.EntityService;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author hungpham
 * @since Jul 19, 2017
 */
@Local
public interface ProductService extends EntityService<Products> {

    public List<Products> getAllProducts(int companyId);

    public List<Products> getAllProducts(int companyId, int smallProductId);

    public int removeAllProductsExcept(int companyId, List<String> importedCodeList);

    public int removeProduct(int companyId, int productId);

    public Products getProductByCode(int companyId, String productsCode);

    public Products getProductByCode(int companyId, String productsCode, Boolean isSearchAll);

    public Products saveProduct(Products bean) throws Exception;
}
