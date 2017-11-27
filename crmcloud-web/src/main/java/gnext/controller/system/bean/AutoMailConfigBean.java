package gnext.controller.system.bean;

import gnext.bean.Member;
import gnext.bean.automail.AutoMail;
import gnext.bean.automail.AutoMailMember;
import gnext.bean.mente.MenteItem;
import gnext.model.authority.UserModel;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.faces.model.SelectItem;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author hungpham
 * @since Aug 24, 2017
 */
public class AutoMailConfigBean implements Serializable{

    private static final long serialVersionUID = 5474605785311528108L;
    
    @Getter @Setter
    private UUID uid = UUID.randomUUID();
    
    @Setter
    private AutoMail bean;
    
    @Getter @Setter
    private MenteItem item;
    
    @Getter @Setter
    private List<MenteItem> proposalList = new ArrayList<>();
    
    @Getter @Setter
    private List<MenteItem> productList = new ArrayList<>();
    
    @Getter @Setter
    private List<MenteItem> productLv2List = new ArrayList<>();
    
    @Getter @Setter
    private Boolean autoSend = Boolean.TRUE; // Tu dong gui mail sau khi save issue
    
    @Getter @Setter
    private Integer day = 0;
    
    @Getter @Setter
    private Integer optionId = 0;
    
    @Getter @Setter
    private Integer hours = 6;
    
    @Getter @Setter
    private Integer mode = 0; // 0: 当日, 1: 毎日
    
    @Getter @Setter
    private List<SelectItem> toList =  new ArrayList<>();
    
    @Getter @Setter
    private List<SelectItem> ccList =  new ArrayList<>();
    
    private boolean processAtFirstTime = true;

    public AutoMailConfigBean(){}
    public AutoMailConfigBean(MenteItem item) {
        this.item = item;
    }

    public AutoMail getBean() {
        if(bean == null){
            bean = new AutoMail();
            bean.setCreatedDate(new Date());
            bean.setCreatorId(UserModel.getLogined().getUserId());
        }else{
            bean.setUpdatedDate(new Date());
            bean.setUpdatedId(UserModel.getLogined().getUserId());
        }
        bean.setOptionId(item.getIssueStatusStep());
        bean.setAutoSend(autoSend);
        bean.setDay(this.getDay());
        bean.setHour(this.getHours());
        bean.setMode(this.getMode());
        bean.setItemId(this.getItem());
        bean.setCompanyId(UserModel.getLogined().getCompanyId());
        
        List<MenteItem> memteList = new ArrayList<>();
        memteList.addAll(this.getProposalList());
        memteList.addAll(this.getProductList());
        memteList.addAll(this.getProductLv2List());
        bean.setMenteItemList(memteList);
        
        List<AutoMailMember> autoMailMemberList = new ArrayList<>();
        this.getToList().forEach((to) -> {
            if(to.getValue() instanceof Member){
                AutoMailMember am =  new AutoMailMember(bean.getAutoConfigId(), ((Member)to.getValue()).getMemberId(), "to");
                autoMailMemberList.add(am);
            }
        });
        this.getCcList().forEach((cc) -> {
            if(cc.getValue() instanceof Member){
                AutoMailMember am =  new AutoMailMember(bean.getAutoConfigId(), ((Member)cc.getValue()).getMemberId(), "cc");
                autoMailMemberList.add(am);
            }
        });
        bean.setAutoMailMemberList(autoMailMemberList);
        
        return bean;
    }
    
    public String getProposalNames(String locale){
        return getMainteNames(proposalList, locale);
    }
    
    public String getProductNames(String locale){
        if(!processAtFirstTime){
            productLv2List.clear();
        }
        if(processAtFirstTime) processAtFirstTime = false;
        return getMainteNames(productList, locale);
    }
    
    public String getProductLv2Names(String locale){
        return getMainteNames(productLv2List, locale);
    }
    
    private String getMainteNames(List<MenteItem> list, String locale){
        List<String> nameList = new ArrayList<>();
        for(MenteItem it : list){
            nameList.add(it.getItemViewData(locale));
            break;
        }
        if(list.size() > 1) {
            nameList.add(String.format("(%d)", list.size()-1));
        }
        return StringUtils.join(nameList, "、");
    }

    @Override
    public int hashCode() {
        int hash = 5;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AutoMailConfigBean other = (AutoMailConfigBean) obj;
        if (!Objects.equals(this.uid, other.uid)) {
            return false;
        }
        return true;
    }
}
