/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service.issue;

import gnext.bean.Member;
import gnext.bean.issue.IssueTodo;
import gnext.service.EntityService;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author daind
 */
@Local
public interface IssueTodoService extends EntityService<IssueTodo> {
    public List<IssueTodo> getTodoListByUser(final Member user);
    public List<IssueTodo> getTodoByIssueId(final int issueId);
    public IssueTodo getTodoIdAndIssueId(final int todoId, final int issueId);
    public int updateStatusByTodoId(final int todoId, final short status);
}
