/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service.customize.impl;

import gnext.service.impl.*;
import gnext.bean.customize.AutoFormTab;
import gnext.service.customize.AutoFormTabService;
import javax.ejb.Stateless;
import javax.inject.Inject;
import gnext.multitenancy.TenantHolder;
import gnext.utils.JPAUtils;
import javax.persistence.EntityManager;

/**
 *
 * @author hungpd
 */
@Stateless
public class AutoFormTabServiceImpl extends AbstractService<AutoFormTab> implements AutoFormTabService {
    @Inject TenantHolder tenantHolder;
    @Override protected EntityManager getEntityManager() {
        return JPAUtils.getSlaveEntityManager(tenantHolder);
    }
    public AutoFormTabServiceImpl() { super(AutoFormTab.class); }
}
