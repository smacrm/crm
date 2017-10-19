/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service.report;

import gnext.bean.mente.MenteOptionDataValue;
import gnext.bean.report.NestProduct;
import gnext.bean.report.ReportItem;
import gnext.service.EntityService;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author daind
 */
@Local
public interface ReportService extends EntityService<ReportItem> {
    
    public List<MenteOptionDataValue> getFullLabelMapping(final int type, String locale, int companyId);
    public List<ReportItem> getReportData(final int type, String dateFrom, String dateTo, int expectedYear, int expectedMonth, String locale, int companyId);
    public List<NestProduct> getNestProduct(int type, String locale, int companyId);
}
