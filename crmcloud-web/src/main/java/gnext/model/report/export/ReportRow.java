package gnext.model.report.export;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author hungpham
 * @since Nov 30, 2016
 */
public class ReportRow{
    @Getter @Setter private String id = "";
    @Setter private Map<Integer, String> headers = new HashMap<>();
    @Setter private Map<Integer, Month> data = new HashMap<>();
    @Getter @Setter private List<ReportRow> childs = new ArrayList<>();
    private Integer current = null;
    private Integer last = null;
    @Getter private int reportType = 0;

    public ReportRow(int reportType) {
        this.reportType = reportType;
    }

    public ReportRow(String id, int reportType) {
        this.id = id;
        this.reportType = reportType;
    }
    
    public ReportRow(String id, int reportType, Map<Integer, Month> data) {
        this.id = id;
        this.reportType = reportType;
        this.data = data;
    }
    
    public boolean hasChild(){
        return this.childs.size() > 0;
    }
    
    public boolean hasChild(String id){
        for(ReportRow item : this.childs){
            if(item.getId().equals(id)) return true;
        }
        return false;
    }
    
    public void push(ReportRow item){
        List<String> arrPath = new ArrayList<>();
        
        Arrays.asList(StringUtils.split(item.getId(), "-")).forEach((id) -> {
            arrPath.add(id);
            String path = StringUtils.join(arrPath, "-");
            String parentId = path;
            if(path.indexOf("-") > 0){
                parentId = path.substring(0, path.lastIndexOf("-"));
            }
            ReportRow node = getNode(this, parentId);
            if(node != null){
                if(path.equals(item.getId())){
                    if(!node.hasChild(item.getId())) node.getChilds().add(item);
                }else{
                    if(!node.getId().equals(path) && !node.hasChild(path)) node.getChilds().add(new ReportRow(path, getReportType()));
                }
            }else{
                if(!this.hasChild(path)) this.getChilds().add(new ReportRow(path, getReportType()));
            }
        });
    }
    
    private ReportRow getNode(ReportRow tree, String nodeId){
        if(tree.getId().equals(nodeId)) return tree;
        if(tree.hasChild()){
            for(ReportRow child : tree.getChilds()){
                ReportRow node = this.getNode(child, nodeId);
                if(node != null){
                    return node;
                }
            }
        }
        return null;
    }
    
    public List<ReportRow> getRows(){
        return getRows(this);
    }
    
    public List<ReportRow> getRows(ReportRow parent){
        List<ReportRow> rows = new ArrayList<>();
    
        if(parent.hasChild()){
            for(ReportRow child : parent.getChilds()){
                rows.addAll(this.getRows(child));
            }
            if(!parent.getId().isEmpty()) rows.add(parent);
        }else{
            rows.add(parent);
        }
        return rows;
    }

    public Map<Integer, Month> getData() {
        if(data.isEmpty()){
            this.childs.forEach((child) -> {
                child.getData().forEach((k, v) -> {
                    if(data.containsKey(k) && data.get(k) != null){
                        data.put(k, data.get(k).clone().merge(v));
                    }else{
                        data.put(k, v);
                    }
                });
            });
            return data;
        }
        return data;
    }

    public Map<Integer, String> getHeaders() {
        if(headers.isEmpty()){
            String[] path = StringUtils.split(this.id, "-");
            for(int i = 0; i< path.length; i++){
                headers.put(i+1, "");
            }
        }
        return headers;
    }
    
    public Integer getCurrent(){
        if(this.current == null){
            this.current = 0;
            this.getData().forEach((k, v) -> {
                if(v != null){
                    this.current += v.getCurrent();
                }
            });
        }
        return this.current;
    }
    public Integer getLast(){
        if(this.last == null){
            this.last = 0;
            this.getData().forEach((k, v) -> {
                if(v != null){
                    this.last += v.getLast();
                }
            });
        }
        return this.last;
    }
    
    public String getPercent(){
        double dLast = this.getLast();
        double dCurrent = this.getCurrent();
        if(dLast == 0 && dCurrent == 0) return null;
        else if(dLast == 0) return "100";
        else if(dCurrent == 0) return null;
        return String.format( "%.2f", (dCurrent * 100 / dLast) * 100 / 100);
    }
    
    @Override
    public String toString() {
        return "ID = " + id + ", childs = " + this.childs.size();
    }
}
