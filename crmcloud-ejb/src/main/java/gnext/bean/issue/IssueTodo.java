/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.bean.issue;

import gnext.bean.Member;
import gnext.bean.mente.MenteItem;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author gnextadmin
 */
@Entity
@Table(name = "crm_issue_todo")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "IssueTodo.findByIssueId", query = " SELECT c FROM IssueTodo c WHERE c.todoIssueId.issueId = :todoIssueId AND c.todoDeleted = :todoDeleted ")
    , @NamedQuery(name = "IssueTodo.findByTodoId", query = " SELECT c FROM IssueTodo c WHERE c.todoId = :todoId AND c.todoDeleted = :todoDeleted ")
    , @NamedQuery(name = "IssueTodo.deleteAllByIssueId", query = " DELETE FROM IssueTodo c WHERE c.todoIssueId.issueId = :todoIssueId ")
    , @NamedQuery(name = "IssueTodo.updateStatusByTodoId", query = " UPDATE IssueTodo c SET c.todoStatus = :todoStatus WHERE c.todoId = :todoId ")
    , @NamedQuery(name = "IssueTodo.findTodoListByUserId"
            , query = " SELECT t FROM IssueTodo t "
                    + " WHERE t.todoIssueId.company.companyId = :companyId "
                    + " AND t.todoIssueId.issueClosedDate IS NULL "
                    + " AND t.todoIssueId.issueDeleted = 0 "
                    + " AND t.todoDeleted = 0 "
                    + " AND ("
                    + " (t.todoIntendedDatetime >= :todayStart AND t.todoIntendedDatetime < :todayEnd) "
                    + " OR "
                    + " (t.todoIntendedDatetime < :yesterday AND t.todoStatus = 0 ) "
                    + " )"
                    + " AND ("
                    + " (t.todoShareFlag = TRUE AND t.creator.group.groupId = :groupId) "
                    + " OR "
                    + " (t.todoShareFlag = FALSE AND t.creator.memberId = :memberId)"
                    + " ) ORDER BY t.todoIntendedDatetime DESC ") 
})
@SuppressWarnings("JPQLValidation")
public class IssueTodo implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Getter @Setter
    @Column(name = "todo_id")
    private Integer todoId;

    @Getter @Setter
    @Column(name = "todo_intended_datetime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date todoIntendedDatetime;

    @Getter @Setter
    @JoinColumn(name = "todo_important_level",
            referencedColumnName = "item_id",
            nullable = false, insertable = true, updatable = true)
    @ManyToOne(optional = false)
    private MenteItem todoImportantLevel;

    @Getter @Setter
    @Column(name = "todo_status")
    private Short todoStatus;

    @Size(max = 2000)
    @Getter @Setter
    @Column(name = "todo_content")
    private String todoContent;

    @Getter @Setter
    @Column(name = "todo_share_flag")
    private Boolean todoShareFlag;

//    @Size(max = 500)
//    @Getter @Setter
//    @Column(name = "todo_share_list")
//    private String todoShareList;

    @Getter @Setter
    @Column(name = "todo_deleted")
    private Short todoDeleted;

    @JoinColumn(name = "creator_id", referencedColumnName = "member_id", nullable = false)
    @ManyToOne(optional = false)
    @Setter @Getter private Member creator;

    @Getter @Setter
    @Column(name = "created_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdTime;

    @Getter @Setter
    @Column(name = "updated_id")
    private Integer updatedId;

    @Getter @Setter
    @Column(name = "updated_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedTime;

    @Getter @Setter
    @JoinColumn(name = "todo_issue_id", referencedColumnName = "issue_id")
    @ManyToOne(optional = false)
    private Issue todoIssueId;

    public IssueTodo() {
    }

    public IssueTodo(Integer todoId) {
        this.todoId = todoId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (todoId != null ? todoId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof IssueTodo)) {
            return false;
        }
        IssueTodo other = (IssueTodo) object;
        return !((this.todoId == null && other.todoId != null) || (this.todoId != null && !this.todoId.equals(other.todoId)));
    }
    
    public boolean isDone(){
        return todoStatus != null && todoStatus == 1;
    }
    
    public void setDone(boolean done){
        todoStatus = done ? (short)1 : (short)0;
    }

    @Override
    public String toString() {
        return "gnext.issue.CrmIssueTodo[ todoId=" + todoId + " ]";
    }

//    @Transient
//    @Setter
//    private String todoStatusName;
//    public String getTodoStatusName() {
//        return (this.todoStatus==0)?"完了":"未完了";
//    }

    @Transient
    @Getter @Setter
    private String todoImportantLevelName;
}
