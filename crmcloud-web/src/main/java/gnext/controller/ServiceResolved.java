/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.controller;

import gnext.service.CompanyService;
import gnext.service.DatabaseServerService;
import gnext.service.GroupService;
import gnext.service.MemberService;
import gnext.service.config.ConfigService;
import gnext.service.customize.AutoFormItemService;
import gnext.service.issue.IssueCustomerService;
import gnext.service.issue.IssueService;
import gnext.service.mente.MenteItemService;
import gnext.service.project.ProjectService;
import java.io.Serializable;
import javax.ejb.EJB;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import lombok.Getter;

/**
 * Dựa vào tính chất của PrimeFaces hệ thống sẽ load các services EJB để sử dụng
 * trong các trường hợp cần sử dung bên ngoài Containners.
 * @author daind
 */
@ManagedBean(name = "serviceResolved")
@ApplicationScoped
public class ServiceResolved implements Serializable {
    private static final long serialVersionUID = 5002377458390630109L;
    @Getter @EJB private ConfigService configService;
    @Getter @EJB private CompanyService companyService;
    @Getter @EJB private MemberService memberService;
    @Getter @EJB private GroupService groupService;
    @Getter @EJB private MenteItemService menteItemService;
    @Getter @EJB private IssueService issueService;
    @Getter @EJB private IssueCustomerService issueCustomerService;
    @Getter @EJB private ProjectService projectService;
    @Getter @EJB private DatabaseServerService databaseServerService;
    @Getter @EJB private AutoFormItemService autoFormItemService;
}
