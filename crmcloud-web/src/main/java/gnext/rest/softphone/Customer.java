package gnext.rest.softphone;

import gnext.rest.softphone.bean.CustomerBean;
import com.google.gson.Gson;
import gnext.bean.Member;
import gnext.bean.issue.Issue;
import gnext.security.notification.DeviceSessionHandler;
import gnext.service.MemberService;
import gnext.service.issue.IssueCustomerService;
import gnext.rest.softphone.bean.ForwardMemberBean;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author hungpham
 * @since 2017/02
 */
@Path("/softphone/customer")
@RequestScoped
@Produces(MediaType.TEXT_PLAIN)
public class Customer {
    
    @EJB private IssueCustomerService customerservice;
    @EJB private MemberService memberServiceImpl;
    
    @Inject private DeviceSessionHandler sessionHandler;
    
    /**
     * Search customer list by phone number
     * @param companyId
     * @param phoneNumber
     * @return 
     */
    @GET
    @Path("/search")
    public String list(@QueryParam("cid") Integer companyId, @QueryParam("pn") String phoneNumber) {
        final List<CustomerBean> list = new ArrayList<>();
        List<gnext.bean.issue.Customer> data = customerservice.searchByPhoneNumber(companyId, phoneNumber);
        data.forEach((cus) -> {
            CustomerBean item = new CustomerBean();
            final String[] validPhoneNumber = {""};
            cus.getCustTargetInfoList().forEach((info) -> {
                if(info.getCustFlagType() == 2 || info.getCustFlagType() == 3){
                    if(isValidCondition(phoneNumber, info.getCustTargetData())){
                        validPhoneNumber[0] = info.getCustTargetData();
                        return;
                    }
                }
            });
            item.setName(cus.getCustFirstHira() + cus.getCustLastHira());
            item.setPhone(validPhoneNumber[0]);
            item.setLastCall(new Date());
            list.add(item);
        });
        return new Gson().toJson(list);
    }
    
    
    /**
     * Get list user for forward action
     * @param cid CompanyId
     * @return 
     */
    @GET
    @Path("/forward")
    public String forwardUsers(@QueryParam("cid") String cid) {
        final List<ForwardMemberBean> list = new ArrayList();
        if(StringUtils.isNumeric(cid)){
            int companyId = Integer.parseInt(cid);
            List<Member> members = memberServiceImpl.findByCompanyId(companyId);
            members.forEach((m) -> {
                ForwardMemberBean fmb = new ForwardMemberBean();
                fmb.setName(m.getMemberNameFirst());
                fmb.setAccount(m.getMemberLoginId());
                fmb.setOnline(sessionHandler.isExists(companyId, m.getMemberLoginId()) != null);
                
                list.add(fmb);
            });
            return new Gson().toJson(list);
        }
        return StringUtils.EMPTY;
    }
    
    private boolean isValidCondition(String phoneNumber, String data){
        return StringUtils.contains(removeNonDigits(data), removeNonDigits(phoneNumber));
    }
    
    private static String removeNonDigits(final String str) {
        if (str == null || str.length() == 0) {
            return "";
        }
        return str.replaceAll("\\D+", "");
    }
    
//    private ExecutorService executorService = java.util.concurrent.Executors.newCachedThreadPool();
//
//    @GET
//    @Path(value = "/issue")
//    public void issue(@Suspended final AsyncResponse asyncResponse, @QueryParam(value = "cid") final Integer companyId, @QueryParam(value = "pn") final String phoneNumber) {
//        executorService.submit(new Runnable() {
//            public void run() {
//                asyncResponse.resume(doIssue(companyId, phoneNumber));
//            }
//        });
//    }
    
    @GET
    @Path(value = "/issue")
    public String issue(@QueryParam(value = "cid") Integer companyId, @QueryParam(value = "pn") String phoneNumber) {
        final List<CustomerBean> list = new ArrayList<>();
        Issue issue = customerservice.getIssueRelate(companyId, phoneNumber);
        if(issue != null){
            return String.valueOf(issue.getIssueId());
        }
        return StringUtils.EMPTY;
    }

    private String doIssue(@QueryParam("cid") Integer companyId, @QueryParam("pn") String phoneNumber) {
        final List<CustomerBean> list = new ArrayList<>();
        Issue issue = customerservice.getIssueRelate(companyId, phoneNumber);
        if(issue != null){
            return String.valueOf(issue.getIssueId());
        }
        return StringUtils.EMPTY;
    }
}
