/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.mailapi.util;

import com.mysql.jdbc.StringUtils;
import gnext.mailapi.mail.Email;
import java.util.ArrayList;
import java.util.List;
import javax.mail.Flags;

/**
 *
 * @author daind
 */
public class FlagTerm {

    public static <M extends Email> List<javax.mail.search.FlagTerm> getFlagTerm(M m) {
        if(StringUtils.isNullOrEmpty(m.getFlag())) return new ArrayList<>();
        
        List<javax.mail.search.FlagTerm> flagTerms = new ArrayList<>();
        switch (m.getFlag().toUpperCase()) {
            case InterfaceUtil.Flag.ALL:        break;
            case InterfaceUtil.Flag.RECENT:     flagTerms.add(new javax.mail.search.FlagTerm(new Flags(Flags.Flag.RECENT), true));  break;
            case InterfaceUtil.Flag.SEEN:       flagTerms.add(new javax.mail.search.FlagTerm(new Flags(Flags.Flag.SEEN), true));    break;
            case InterfaceUtil.Flag.ANSWERED:   flagTerms.add(new javax.mail.search.FlagTerm(new Flags(Flags.Flag.ANSWERED), true));break;
            case InterfaceUtil.Flag.DELETED:    flagTerms.add(new javax.mail.search.FlagTerm(new Flags(Flags.Flag.DELETED), true)); break;
            case InterfaceUtil.Flag.DRAFT:      flagTerms.add(new javax.mail.search.FlagTerm(new Flags(Flags.Flag.DRAFT), true));   break;
            case InterfaceUtil.Flag.UNREAD:     flagTerms.add(new javax.mail.search.FlagTerm(new Flags(Flags.Flag.SEEN), false));   break;
            default: break;
        }
        return flagTerms;
    }
}
