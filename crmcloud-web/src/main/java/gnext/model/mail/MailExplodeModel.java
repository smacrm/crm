/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.model.mail;

import com.google.gson.Gson;
import gnext.bean.mail.MailExplode;
import gnext.model.BaseModel;
import gnext.model.mail.items.MailExplodeItem;
import gnext.util.InterfaceUtil;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author daind
 */
public class MailExplodeModel extends BaseModel<MailExplode> {

    @Getter @Setter private MailExplode mailExplode;
    @Getter @Setter private List<MailExplodeItem> explodeItems = new ArrayList<>();

    public MailExplodeModel(MailExplode mailExplode) {
        this.mailExplode = mailExplode;
    }

    public void addItems(List<MailExplodeItem> explodeItems) {
        this.explodeItems.clear();
        this.explodeItems.addAll(explodeItems);
    }

    public void addItem(MailExplodeItem explodeItem) {
        explodeItem.setSecondChar(InterfaceUtil.EXPLODE_RULE.EOL);
        this.explodeItems.add(explodeItem);
    }

    public String getCondition() {
        if (!explodeItems.isEmpty()) {
            String condition = new Gson().toJson(explodeItems);
            return condition;
        }

        return null;
    }

    public boolean updateItemsFromJson() {
        String conditions = mailExplode.getMailExplodeConditions();
        if (!StringUtils.isEmpty(conditions)) {
            MailExplodeItem[] items = new Gson().fromJson(conditions,
                    MailExplodeItem[].class);
            List<MailExplodeItem> ei = new ArrayList<>();
            for (int i = 0; i < items.length; i++) {
                MailExplodeItem item = items[i];
                item.setId(i);
                ei.add(item);
            }

            this.addItems(ei);

            return true;
        }

        return false;
    }

    public void checkEmptyItems() {
        if (this.explodeItems.isEmpty()) this.addItem(new MailExplodeItem(explodeItems.size()));
    }
}
