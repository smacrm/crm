package gnext.rest.common;

import com.google.gson.Gson;
import gnext.bean.issue.Issue;
import gnext.model.authority.UserModel;
import gnext.service.issue.IssueService;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author hungpham
 * @since Mar 17, 2017
 */
@Path("/issue/")
@Produces(MediaType.TEXT_PLAIN)
public class IssueRestService {

    @EJB private IssueService issueServiceImpl;
    
    @GET
    @Path("/search/{code}")
    public String list(@PathParam("code") String code) {
        int companyId = UserModel.getLogined().getCompanyId();
        List<Issue> issueList = issueServiceImpl.findByIssueViewCodeLike(companyId, code);
        if(issueList != null){
            final List<gnext.rest.common.bean.Issue> responseList = new ArrayList();
            issueList.forEach((item) -> {
                responseList.add(new gnext.rest.common.bean.Issue(item));
            });
            return new Gson().toJson(responseList);
        }
        return new Gson().toJson(new ArrayList());
    }
}
