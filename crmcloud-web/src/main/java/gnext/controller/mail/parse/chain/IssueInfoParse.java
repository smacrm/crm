package gnext.controller.mail.parse.chain;

import gnext.controller.mail.parse.MailParse;
import gnext.bean.issue.Issue;
import gnext.bean.issue.IssueInfo;
import gnext.util.ClassUtil;
import java.lang.reflect.Field;
import java.util.Map;
import javax.persistence.Column;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author daind
 */
public class IssueInfoParse implements MailParse {
    
    @Override
    public void parse(Issue issue, Map<String, String> mappings, Map<String, String> params) throws Exception {
        if(issue == null) return;
        if(mappings == null || mappings.isEmpty()) return;

        parseIdFields(issue, mappings);
    }

    private void parseIdFields(final Issue issue, Map<String, String> mappings){
        if(issue.getIssueInfoList().isEmpty()) return;
        IssueInfo info = issue.getIssueInfoList().get(0);
        try{
            Class<?> clazz = info.getClass();
            Field[] fields = clazz.getDeclaredFields();
            for(Field f : fields) {
                if(f.getAnnotation(Column.class) == null) continue;
                String key = f.getAnnotation(Column.class).name();
                String val = mappings.get(key);
                if(f == null || "info_id".equals(key)
                    || StringUtils.isEmpty(key) || StringUtils.isEmpty(val)) continue;
                ClassUtil.amazingSetField(info, f, val, null);
            }
        } catch(NullPointerException e){ }
    }
}
