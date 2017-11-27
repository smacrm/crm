package gnext.model.search;

import gnext.security.annotation.SecurePage;
import gnext.util.BigSmallString;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 * Process Smart Query String pass to backend from UI (search-filer.js)
 * returned value can be plain (JPA) query or ElasticStyle query
 * 
 * @author hungpham
 * @since 2016/10
 */
public class SearchFilter {
    
    @Getter private List<SearchGroup> filters;
    @Getter @Setter private SecurePage.Module module;
    
    public SearchFilter(List<SearchGroup> filters){
        this.filters = filters;
    }
    
    /**
     * Get JPA Query String
     * 
     * @return String
     */
    public String getQuery(){
        return getQuery(false);
    }
    
    /**
     * Get Query String with elastic-style as parameter
     * 
     * @param elasticStyle
     * @return String
     */
    public String getQuery(boolean elasticStyle){
        if(filters == null || filters.isEmpty()) return null;
        
        final StringBuilder query = new StringBuilder();
        String prevGroupCond = null;
        for(SearchGroup group : filters){
            StringBuilder q = new StringBuilder();
            String prevFieldCond = null;
            for(SearchField field : group.getFilters()){
                field.setModule(getModule());
                if( !StringUtils.isEmpty(this.escape(field.getOperator(),field.getValue(),elasticStyle))
                        || "bl".equals(field.getOperator())
                        || "nbl".equals(field.getOperator())){
                    if( prevFieldCond != null && !StringUtils.isEmpty(prevFieldCond) ){
                        q.append(prevFieldCond);
                    }
                    
                    if(!StringUtils.isEmpty(field.getSqlCustomize())) {
                        q.append(term(field.getSqlCustomize()));
                    } else if("%".equals(field.getOperator())){ //like operator
                        if(elasticStyle){
                            q.append(term(field.getName(), false, true));
                            q.append(":");
                            q.append(term("*", this.escape(field.getOperator(),field.getValue(),elasticStyle), "*", false));
                        }else{
                            q.append(term(field.getName(true)));
                            q.append(" LIKE ");
                            q.append(term("%", this.escape(field.getOperator(),field.getValue(),elasticStyle), "%"));
                        }
                    } else if("%-".equals(field.getOperator())){ //like end operator
                        if(elasticStyle){
                            q.append(term(field.getName(), false, true));
                            q.append(":");
                            q.append(term("*", this.escape(field.getOperator(),field.getValue(),elasticStyle), "", false));
                        }else{
                            q.append(term(field.getName(true)));
                            q.append(" LIKE ");
                            q.append(term("%", this.escape(field.getOperator(),field.getValue(),elasticStyle), ""));
                        }
                    } else if("-%".equals(field.getOperator())){ //like start operator
                        if(elasticStyle){
                            q.append(term(field.getName(), false, true));
                            q.append(":");
                            q.append(term("", this.escape(field.getOperator(),field.getValue(),elasticStyle), "*", false));
                        }else{
                            q.append(term(field.getName(true)));
                            q.append(" LIKE ");
                            q.append(term("", this.escape(field.getOperator(),field.getValue(),elasticStyle), "%"));
                        }
                    } else if("!%".equals(field.getOperator())){ //not like operator
                        if(elasticStyle){
                            q.append(term("-"+field.getName(), false, true));
                            q.append(":");
                            q.append(term("*", this.escape(field.getOperator(),field.getValue(),elasticStyle), "*", false));
                        }else{
                            q.append(term(field.getName(true)));
                            q.append(" NOT LIKE ");
                            q.append(term("%", this.escape(field.getOperator(),field.getValue(),elasticStyle), "%"));
                        }
                    }else if("bl".equals(field.getOperator())){ //blank operator
                        if(elasticStyle){
                            q.append(term("-"+field.getName(), false, true));
                            q.append(":*");
                        }else{
                            q.append("(");
                            q.append(term(field.getName(true)));
                            q.append(" IS NULL OR ");
                            q.append(term(field.getName()));
                            q.append(" = '' ");
                            q.append(")");
                        }
                    }else if("nbl".equals(field.getOperator())){ //blank operator
                        if(elasticStyle){
                            q.append(term(field.getName(), false, true));
                            q.append(":*");
                        }else{
                            q.append("(");
                            q.append(term(field.getName(true)));
                            q.append(" IS NOT NULL AND ");
                            q.append(String.format("TRIM(%s)", term(field.getName())));
                            q.append(" != '' ");
                            q.append(")");
                        }
                    }else{
                        if(elasticStyle){
                            q.append(term(field.getName(), false, true));
                            q.append(":");
                            if( !"=".equals(field.getOperator()) ){
                                q.append(term(field.getOperator(), false, true));
                            }
                            q.append(term(this.escape(field.getOperator(),field.getValue(),elasticStyle), false, true));
                        }else{
                            if(SecurePage.Module.ISSUE == getModule() && "=".equals(field.getOperator()) && !"date".equals(field.getType())){ //equals operator or issue module
                                String fieldName = term(field.getName(true));
                                String fieldValue = StringUtils.trim(term(this.escape(field.getOperator(),field.getValue(),elasticStyle), true));
                                q.append(term(String.format("FIND_IN_SET(REPLACE(%s, ' ', ''), REPLACE(%s, ' ', ''))", fieldValue, fieldName)));
                            }else{
                                q.append(term(field.getName(true)));
                                q.append(term(field.getOperator()));
                                q.append(term(this.escape(field.getOperator(),field.getValue(),elasticStyle), true));
                            }
                        }
                    }
                    prevFieldCond = term(field.getCondition());
                }
            }
            if( !StringUtils.isEmpty(q.toString()) ){
                if( prevGroupCond != null && !StringUtils.isEmpty(prevGroupCond) ){
                    query.append(prevGroupCond);
                }
                query.append("(").append(q).append(")");
                String groupOperator = group.getOperator() == null ? "": group.getOperator();
                prevGroupCond = elasticStyle?term(groupOperator, false, true):term(groupOperator);
            }else{
                prevGroupCond = "";
            }
        }
        return query.toString();
    }
    
    /**
     * Get All condition fields
     * 
     * @return String
     */
    public List<String> getConditionFields(){
        if(filters == null || filters.isEmpty()) return null;
        List<String> fields = new ArrayList<>();
        for(SearchGroup group : filters){
            for(SearchField field : group.getFilters()){
                if(!fields.contains(field.getName())) fields.add(field.getName());
            }
        }
        return fields;
    }
    
    /**
     * Hàm xử lí trả về mệnh đề SQL trong câu lệnh WHERE.
     * ví dụ: field_name='b', field_name like '%b%',...
     * @param name
     * @param operator
     * @param value
     * @return 
     */
    public String parseOperator(String name, String operator, String value) {
        StringBuilder q = new StringBuilder();
        if("%".equals(operator)){ //like operator
            q.append(term(name));
            q.append(" LIKE ");
            q.append(this.term("%", this.escape(operator, value, false), "%"));
        } else if("%-".equals(operator)){ //like end operator
            q.append(term(name));
            q.append(" LIKE ");
            q.append(this.term("%", this.escape(operator ,value, false), ""));
        } else if("-%".equals(operator)){ //like start operator
            q.append(term(name));
            q.append(" LIKE ");
            q.append(this.term("", this.escape(operator ,value, false), "%"));
        } else if("!%".equals(operator)){ //not like operator
            q.append(term(name));
            q.append(" NOT LIKE ");
            q.append(this.term("%", this.escape(operator ,value, false), "%"));
        } else if("bl".equals(operator)){ //blank operator
            q.append("(").append(term(name));
            q.append(" IS NULL OR ");
            q.append(term(name));
            q.append(" = '' ");
            q.append(")");
        } else if("nbl".equals(operator)){ //blank operator 
            q.append("(").append(term(name));
            q.append(" IS NOT NULL OR ");
            q.append(term(name));
            q.append(" != '' ");
            q.append(")");
        } else {
            q.append(term(name));
            q.append(this.term(operator));
            q.append(this.term(this.escape(operator ,value, false), true));
        }
        return q.toString();
    }
    
    /**
     * Escape special character for JPA query or Elastic Query
     * 
     * @param operator
     * @param value
     * @param elasticStyle
     * @return 
     */
    private String escape(String operator, String value, boolean elasticStyle){
        if(elasticStyle){
            
        }else{
            value = value.replace("'", "''");
            if(operator.contains("%")){
                value = value.replace("%", "\\%");
            }
        }
        return BigSmallString.toSmall(value);
    }
    
    /**
     * Get Search term
     * 
     * @param term
     * @return String
     */
    private String term(String term){
        return term(term, false);
    }
    
    /**
     * Get Search term with quote as parameter
     * 
     * @param term
     * @param quote
     * @return String
     */
    private String term(String term, boolean quote){
        return term(term, quote, false);
    }
    
    /**
     * Get Search term with quote and elastic-style as parameter
     * 
     * @param term
     * @param quote
     * @param elasticStyle
     * @return String
     */
    private String term(String term, boolean quote, boolean elasticStyle){
        return StringUtils.isEmpty(term) ? "" : quote ? String.format(elasticStyle?"'%s'":" '%s' ", term) : String.format(elasticStyle?"%s":" %s ", term);
    }
    
    /**
     * Get Search term with prefix and suffix
     * 
     * @param prefix
     * @param term
     * @param suffix
     * @return String
     */
    private String term(String prefix, String term, String suffix){
        return term(prefix, term, suffix, true);
    }
    
    /**
     * Get Search term with prefix and suffix
     * 
     * @param prefix
     * @param term
     * @param suffix
     * @param quote
     * @return String
     */
    private String term(String prefix, String term, String suffix, boolean quote){
        return StringUtils.isEmpty(term) ? "" : (quote?" '":" ") + prefix + term + suffix + (quote?"' ":" ");
    }
    
    /**
     * Merge 2 search-filter with "AND" operator
     * @param other 
     */
    public void merge(SearchFilter other){
        this.merge(other, SearchGroup.OPERATOR.AND);
    }
    
    /**
     * Merge 2 search-filter with specified operator
     * @param other 
     * @param operator 
     */
    public void merge(SearchFilter other, SearchGroup.OPERATOR operator){
        if( !StringUtils.isEmpty(other.getQuery()) ){
            List<SearchGroup> listOtherGroups = other.getFilters();
            if( !listOtherGroups.isEmpty() ){
                listOtherGroups.get(0).setOperator(operator.toString());
            }
            List<SearchGroup> mergeList = new ArrayList<>();
            mergeList.addAll(listOtherGroups);
            mergeList.addAll(getFilters());
            this.filters = mergeList;
        }
    }
}
