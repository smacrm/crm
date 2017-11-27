/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service.mail;

import gnext.bean.Company;
import gnext.bean.mail.MailData;
import gnext.bean.Member;
import gnext.bean.mail.MailExplode;
import gnext.bean.issue.Issue;
import gnext.service.EntityService;
import java.util.Collection;
import java.util.List;
import javax.ejb.Local;
import javax.persistence.EntityManager;

/**
 *
 * @author daind
 */
@Local
public interface MailDataService extends EntityService<MailData> {

    /**
     * Cặp hàm xử lí phân trang.
     * @param companyId
     * @param first
     * @param pageSize
     * @param sort
     * @param where
     * @return 
     */
    public List<MailData> find(int companyId, Integer first, Integer pageSize, String sort, String where);
    public int total(int companyId, final String where);

    /**
     * Tìm kiếm mail trong folder.
     * @param folderCode
     * @param companyId
     * @param limit
     * @return 
     */
    public List<MailData> searchByCompanyId(String folderCode, int companyId, Integer limit);
    
    /**
     * Tìm kiếm danh sách mail từ account.
     * @param folderCode
     * @param account
     * @return 
     */
    public List<MailData> searchByAccountId(String folderCode, Integer account);
    
    /**
     * Tìm kiếm nội dung mail từ mail_id và company_id.
     * @param companyId
     * @param mailId
     * @return 
     */
    public MailData searchById(Integer companyId, Integer mailId);

    /***
     * Hàm xử lí xóa danh sách mail cho 1 company.
     * @param c
     * @param mailIds danh sách mail_id.
     */
    public void delete(Company c, List<Integer> mailIds) throws Exception;
    
    /***
     * Di chuyển danh sách mail tới folder khác.
     * @param c
     * @param toFolderCode mã folder đích.
     * @param mailIds danh sách mail_id
     * @param _em vì hàm này được sử dụng ở service khác nên cần có entitymanager để quản lí trasactions.
     */
    public int moveToFolder(Company c, String toFolderCode, Collection<Integer> mailIds, EntityManager _em) throws Exception;
    
    /**
     * Hàm xử lí đánh dấu mail là đã được đọc.
     * có xử lí transaction.
     * @param c
     * @param mailIds 
     */
    public int markIsRed(Company c, List<Integer> mailIds) throws Exception ;
    
    /**
     * Hàm xử lí lấy tất cả các explode cho việc phân cắt email.
     * @param md
     * @return 
     */
    public List<MailExplode> searchExplodes(final MailData md);
    
    /**
     * Thiết lập issue chính liên quan tới mail.
     * @param issueId 
     * @param mailData 
     * @param member 
     */
    public void checkoutIssue(Integer issueId, MailData mailData, Member member) throws Exception;
    
    /**
     * Tìm các issue liên quan tới mail.
     * @param mailData
     * @return 
     */
    public List<Issue> searchIssueRelated(final MailData mailData);
    
    /**
     * Cập nhật người phụ trách cho email sau đó chuyển vào folder người phụ trách
     * @param mailData
     * @param c
     * @throws Exception 
     */
    public void changeMailPerson(MailData mailData, Company c) throws Exception;
}
