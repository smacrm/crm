package gnext.caching;

import gnext.caching.impl.EhCache;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hungpham
 * @since Jul 14, 2017
 */
@ApplicationScoped
public class PayloadFactory implements Serializable {
    private static final Logger LOGGER = LoggerFactory.getLogger(PayloadFactory.class);
    private static final long serialVersionUID = 483817563025541024L;

    final private static Map<Integer, Payload> MAPPER = new HashMap<>();

    final public static Integer DEFAULT_PIPE = 1; //Cong ty ID mac dinh neu khong nhan duoc du lieu tu cong ty hien tai

    @PostConstruct
    public void init() {
    }

    public Payload getInstance(Integer companyId) {
        if (MAPPER.containsKey(companyId)) {
            return MAPPER.get(companyId);
        } else {
            Payload payload = this.getProvider();
            MAPPER.put(companyId, payload);
            return payload;
        }
    }

    private Payload getProvider() {
        Payload payload = new EhCache();
        payload.init();

        return payload;
    }

    @PreDestroy
    public void close() {
        if(MAPPER == null) return;
        MAPPER.forEach((companyId, payload) -> {
            payload.close();
        });
        MAPPER.clear();
    }
}
