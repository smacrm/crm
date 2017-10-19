/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.multitenancy;

import gnext.bean.Company;
import java.io.Serializable;
import javax.enterprise.context.SessionScoped;
import lombok.Getter;
import lombok.Setter;

/**
 * Được duy trì trong trạng thái của HTTPSession khi người dùng truy cập hệ thống.
 * sau khi người dùng login basic và login form thành công. tại chức năng grant quyền tới 
 * người dùng sẽ holder lại company người dùng đã login trước đó trong lớp LoginController.
 * @author daind
 */
@SessionScoped
public class TenantHolder implements Serializable {
    @Getter @Setter int companyId;
    
    public boolean isAdminCompany() {
        return Company.MASTER_COMPANY_ID == companyId;
    }
}
