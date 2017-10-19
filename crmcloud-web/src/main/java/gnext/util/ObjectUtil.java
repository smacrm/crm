package gnext.util;

import com.google.gson.Gson;
import gnext.controller.issue.bean.PersitBean;
import java.io.IOException;
import java.util.Date;
import org.apache.commons.lang3.time.DateUtils;

/**
 *
 * @author hungpham
 * @since Mar 8, 2017
 */
public class ObjectUtil {

    public static String serializeObjectToString(Object object) throws IOException {
        String json = new Gson().toJson(object);
        return StringUtil.easeyEncrypt(json);
    }

    public static <T extends Object> T deserializeObjectFromString(String objectString, Class<T> c) throws Exception {
        String decrypt = StringUtil.easeyDecrypt(objectString);
        return new Gson().fromJson(decrypt, c);
    }
    
    public static void main(String[] args) throws Exception{
        PersitBean bean = new PersitBean(278, 1);
        bean.getMemberList().add(1);
        bean.setExpiredDate(DateUtils.addDays(new Date(), 7));
        
        System.out.println(serializeObjectToString(bean));
    }
}
