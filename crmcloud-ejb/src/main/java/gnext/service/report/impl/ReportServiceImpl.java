package gnext.service.report.impl;

import gnext.bean.mente.MenteOptionDataValue;
import gnext.bean.report.NestProduct;
import gnext.service.impl.*;
import gnext.bean.report.ReportItem;
import gnext.service.report.ReportService;
import gnext.utils.InterfaceUtil.COLS;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import gnext.multitenancy.TenantHolder;
import gnext.utils.JPAUtils;

/**
 * 
 * @author hungpham
 * @since 2016/11
 */
@Stateless
public class ReportServiceImpl extends AbstractService<ReportItem> implements ReportService {
    private final Logger logger = LoggerFactory.getLogger(ReportServiceImpl.class);

    public ReportServiceImpl() { super(ReportItem.class); }

    @Inject TenantHolder tenantHolder;
    @Override protected EntityManager getEntityManager() {
        return JPAUtils.getSlaveEntityManager(tenantHolder);
    }
    
    @Override
    public List<ReportItem> getReportData(int type, String dateFrom, String dateTo, int expectedYear, int expectedMonth, String locale, int companyId) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(companyId);
            
            List<ReportItem> fullReportItem = null;
            if(type == 2){
                StringBuilder str = new StringBuilder();
                str.append("select `year`, `month`, `id`, sum(`current`) `current`, `last`").
                append("from( ");
                    str.append("select YEAR(d.issue_receive_date) as `year`, MONTH(d.issue_receive_date) as `month`,").
                        append("REPLACE(get_lineage(b.item_id, (select max(a1.item_level) from crm_mente_item a1 where a1.item_name = b.item_name and a1.company_id = a.company_id)), ',', '-') as `id`,"). 
                        append("count(d.issue_id) `current`, ").
                        append("count(d1.issue_id) `last`").
                    append("from crm_mente_item_value a ").
                        append("inner join crm_mente_item b on a.mente_issue_field_value = b.item_id and b.item_deleted = 0 ").
                        append("inner join crm_mente_option_data_value c on b.item_id = c.item_id ").
                        append("inner join crm_issue d on a.issue_id = d.issue_id and d.issue_deleted = 0 ").
                        append("left join crm_mente_item b1 on a.mente_issue_field_value = b1.item_id and b1.item_deleted = 0 ").
                        append("left join crm_issue d1 on a.issue_id = d1.issue_id and d1.issue_deleted = 0 ").
                            append("and DATE_FORMAT(d1.issue_receive_date, '%Y%C') = CONCAT(YEAR(d.issue_receive_date)-1, MONTH(d.issue_receive_date)) ").
                    append("where b.item_name = '").append(COLS.PROPOSAL).append("' AND c.item_language = ? ").
                        append("and d.issue_receive_date >= ? and d.issue_receive_date < ? and a.company_id = ? ").
                    append("group by `year`, `month`, a.`issue_id` ").
                    append("order by d.issue_receive_date ASC");
                str.append(") a ").
                append("group by `year`, `month`, `id`; ");

                fullReportItem = em_slave.createNativeQuery(str.toString(), ReportItem.class)
                    .setParameter(1, locale)
                    .setParameter(2, dateFrom)
                    .setParameter(3, dateTo)
                    .setParameter(4, companyId)
                    .getResultList();
            }else if(type == 1){
                StringBuilder str = new StringBuilder();
                str.append("select REPLACE( ").
                    append("    CASE ROUND ( ").
                    append("        ( ").
                    append("            LENGTH(a.item_path) ").
                    append("            - LENGTH( REPLACE ( a.item_path, ',', '') ) ").
                    append("        ) / LENGTH(',')        ").
                    append("        ) ").
                    append("    WHEN 3 THEN REPLACE ( a.item_path, ',0', '') ").
                    append("    WHEN 2 THEN a.item_path ").
                    append("    WHEN 1 THEN CONCAT(a.item_path, '-0') ").
                    append("    WHEN 0 THEN CONCAT(a.item_path, '-0-0') END, ',', '-' ").
                    append("    ) as id, ").
                    append("    YEAR(a.issue_receive_date) as `year`, MONTH(a.issue_receive_date) as `month`, ").
                    append("    count(a.issue_id) `current`, ").
                    append("    count(b.issue_id) `last` ").
                    append("from").
                    append(" 	( ").
                    append(" 	select ").
                    append(" 		dedup_item_path(group_concat( ").
                    append(" 			if( (b.item_name = '" + COLS.PRODUCT +"') or  ").
                    append(" 				(b.item_name = '" + COLS.PROPOSAL + "' and b.item_level = 2) ").
                    append(" 			, if(b.item_name = '" + COLS.PRODUCT + "',  ").
                    append(" 				concat('0,',get_lineage(a.mente_issue_field_value, 0)),  ").
                    append(" 				get_lineage(a.mente_issue_field_value, 0)),  ").
                    append(" 			a.mente_issue_field_value ").
                    append(" 			)order by b.item_name desc) ").
                    append(" 		) as item_path,  ").
                    append(" 		a.issue_id, ").
                    append("            d.issue_receive_date ").
                    append(" 	from crm_mente_item_value a ").
                    append(" 		inner join crm_mente_item b on a.mente_issue_field_value = b.item_id ").
                    append(" 		inner join crm_mente_option_data_value c on b.item_id = c.item_id ").
                    append(" 		inner join crm_issue d on a.issue_id = d.issue_id ").
                    append(" 	where ((b.item_name = '" + COLS.PROPOSAL + "' and b.item_level <= 2) ").
                    append(" 		 or (b.item_name = '" + COLS.PRODUCT + "' and b.item_level = 1)) ").
                    append(" 		 and b.item_deleted = 0 and d.issue_deleted = 0 and d.issue_receive_date >= ? and d.issue_receive_date < ? ").
                    append(" 		 and a.company_id = ? ").
                    append(" 	group by a.issue_id ").
                    append(" 	order by a.issue_id asc  ").
                    append("    ) a ").
                    append("    left join crm_issue b on a.issue_id = b.issue_id and b.issue_deleted = 0 ").
                    append(" 		and DATE_FORMAT(b.issue_receive_date, '%Y%C') = CONCAT(YEAR(a.issue_receive_date)-1, MONTH(a.issue_receive_date))").
                    append("group by `year`, `month`, a.`item_path` ").
                    append("having `id` IS NOT NULL ").
                    append("order by a.issue_receive_date ASC;");

                fullReportItem = em_slave.createNativeQuery(str.toString(), ReportItem.class)
                    .setParameter(1, dateFrom)
                    .setParameter(2, dateTo)
                    .setParameter(3, companyId)
                    .getResultList();
            }

            if(fullReportItem != null){
                Map<String, ReportItem> reportItemList = new HashMap<>();
                Map<String, Integer> prevYearData = new HashMap<>();
                fullReportItem.forEach((item) -> {
                    if(item.getKey().getYear() < expectedYear && item.getKey().getMonth() <= expectedMonth+1){
                        String mapKey = String.format("%s|%s|%s", item.getKey().getYear() + 1, item.getKey().getMonth(), item.getKey().getId());
                        prevYearData.put(mapKey, item.getCurrent());
                    }else{
                        String mapKey = String.format("%s|%s|%s", item.getKey().getYear(), item.getKey().getMonth(), item.getKey().getId());
                        reportItemList.put(mapKey, item);
                    }
                });

                prevYearData.forEach((mapKey, last) -> {
                    ReportItem item = null;
                    if(reportItemList.containsKey(mapKey)){
                        item = reportItemList.get(mapKey);
                    }else{
                        String[] arrKey = StringUtils.split(mapKey, "|");
                        item = new ReportItem(NumberUtils.toInt(arrKey[0]), NumberUtils.toInt(arrKey[1]), arrKey[2], 0, 0);
                    }
                    item.setLast(last);
                    reportItemList.put(mapKey, item);
                });
                return new ArrayList<>(reportItemList.values());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return new ArrayList<>();
    }

    @Override
    public List<NestProduct> getNestProduct(int type, String locale, int companyId){
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(companyId);
            
            List<NestProduct> dataParsed =  new ArrayList<>();
            if(type == 2){
                StringBuilder str = new StringBuilder();
                str.append("select REPLACE(get_lineage(a.item_id, (SELECT max(a1.item_level) from crm_mente_item a1 where a1.item_name = a.item_name and a1.company_id = a.company_id)), ',', '-') as item_id ");
                str.append("from crm_mente_item a ");
                str.append("where a.item_name = '" + COLS.PROPOSAL + "' and a.item_deleted = 0 and a.company_id = ? ");
                str.append("order by item_id DESC;");

                List<String> list = em_slave.createNativeQuery(str.toString())
                    .setParameter(1, companyId)
                    .getResultList();
                list.forEach((pId) -> {
                    dataParsed.add(
                        new NestProduct(pId)
                    );
                });
            }else if(type == 1){
                StringBuilder str = new StringBuilder();
                str.append("select REPLACE(get_lineage(a.item_id, 2), ',', '-') as item_id ");
                str.append("from crm_mente_item a ");
                str.append("where a.item_name = '" + COLS.PROPOSAL + "' and a.item_deleted = 0 AND a.item_level <= 2 and a.company_id = ? ");
                str.append("order by item_id DESC;");

                List<String> listHeader = em_slave.createNativeQuery(str.toString())
                    .setParameter(1, companyId)
                    .getResultList();

                str.delete(0, str.length());
                str.append("select a.item_id ");
                str.append("from crm_mente_item a ");
                str.append("where a.item_name = '" + COLS.PRODUCT + "' and a.item_deleted = 0 AND a.item_level = 1 and a.company_id = ? ");
                str.append("order by item_id DESC;");

                List<Integer> listItem = em_slave.createNativeQuery(str.toString())
                    .setParameter(1, companyId)
                    .getResultList();

                listHeader.forEach((pId) -> {
                    listItem.forEach((cId) -> {
                        dataParsed.add(
                            new NestProduct(
                                String.format("%s-%s", pId, cId)
                            )
                        );
                    });
                    dataParsed.add(
                        new NestProduct(
                            String.format("%s-0", pId)
                        )
                    );
                });
            }
            
            return dataParsed;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return new ArrayList<>();
    }

    @Override
    public List<MenteOptionDataValue> getFullLabelMapping(int type, String locale, int companyId) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(companyId);
            
            if(type == 2){
                Query q = em_slave.createQuery("select o from MenteOptionDataValue o where o.company.companyId = :companyId and o.menteOptionDataValuePK.itemLanguage = :locale and o.menteItem.itemName = :key AND o.menteItem.itemDeleted = 0");
                q.setParameter("companyId", companyId);
                q.setParameter("locale", locale);
                q.setParameter("key", COLS.PROPOSAL);
                return q.getResultList();
            }else if(type == 1){
                Query q = em_slave.createQuery("select o from MenteOptionDataValue o where o.company.companyId = :companyId and o.menteOptionDataValuePK.itemLanguage = :locale and o.menteItem.itemName IN :keys AND o.menteItem.itemDeleted = 0");
                q.setParameter("companyId", companyId);
                q.setParameter("keys", Arrays.asList(COLS.PROPOSAL, COLS.PRODUCT));
                q.setParameter("locale", locale);
                return q.getResultList();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
         return new ArrayList<>();
    }
}
