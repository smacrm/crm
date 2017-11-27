package gnext.rest.customize;

import com.google.gson.Gson;
import gnext.bean.mente.MenteItem;
import gnext.model.authority.UserModel;
import gnext.service.GroupService;
import gnext.service.MemberService;
import gnext.rest.customize.bean.KeyValueBean;
import gnext.service.mente.MenteService;
import gnext.utils.InterfaceUtil.COLS;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author hungpham
 * @since Nov 1, 2016
 */
@Path("/customize/dynamic")
@Produces(MediaType.TEXT_PLAIN)
public class DynamicContent {

    @EJB private GroupService groupServiceImpl;
    @EJB private MemberService memberServiceImpl;
    @EJB private MenteService menteServiceImpl;
    
    @GET
    @Path("/list/{type}")
    public String list(@PathParam("type") String type) {
        List<KeyValueBean> list = new ArrayList<>();
        int companyId = UserModel.getLogined().getCompanyId();
        List<gnext.bean.Group> groupList;
        if(COLS.GROUP.equals(type)) type = "1";
        if(COLS.USER.equals(type)) type = "2";
        if(StringUtils.isNumeric(type)){ //static
            int iType = Integer.parseInt(type);
            switch(iType){
                case 1: //group
                    groupList = groupServiceImpl.findByCompanyId(companyId);
                    groupList.forEach((item) -> {
                        list.add(new KeyValueBean(1, item.getGroupId(), item.getGroupName())); // type 1 -> group
                    });
                    break;
                case 2: //member
                    groupList = groupServiceImpl.findByCompanyId(companyId);
                    groupList.forEach((item) -> {
                        KeyValueBean group = new KeyValueBean(1, item.getGroupId(), item.getGroupName()); // type 1 -> group
                        List<gnext.bean.Member> memberList = memberServiceImpl.findByGroupId(item.getGroupId());
                        memberList.forEach((m) -> {
                            group.getItems().add(new KeyValueBean(2, m.getMemberId(), m.getMemberNameFull())); // type 2 -> group
                        });
                        if(!group.getItems().isEmpty()){
                            list.add(group);
                        }
                    });
                    break;
            }
        }else{ //mente
            List<MenteItem> root = menteServiceImpl.getRootLevels(type, companyId);
            list.addAll(this.loopThrough(root));
        }
        
        return new Gson().toJson(list);
    }
    
    private List<KeyValueBean> loopThrough(List<MenteItem> root){
        List<KeyValueBean> list = new ArrayList<>();
        root.forEach((item) -> {
            KeyValueBean kv = new KeyValueBean(3, item.getItemId(), item.getItemViewData("ja"));  // type 3 -> mente
            kv.getItems().addAll(loopThrough(item.getItemChilds()));
            list.add(kv);
        });
        return list;
    }
}
