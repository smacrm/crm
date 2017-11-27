/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.util;

import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author HUONG
 */
public class InterfaceUtil {

    public interface HTML {
        String BR = "<br />";
        String CIRCLE = "●";
        String CONCAT_BR = "<-GN-BR-GN->";
    }

    public interface LIST_MODE {
        int PROJECT_SEARCH = 0;
        int PROJECT = 1;
        int MAIL = 2;
    }

    public interface TARGET_TYPE {
        int USER = 1;
        int COMPANY = 2;
    }

    public interface ISSUE_TYPE_NAME {
        String SUPPORT = "support";
        String REQUEST = "request";
        String COMMENT = "comment";
        String CUSTOMER = "customer";
    }

    public interface CUST_FLAG {

        boolean NORMAL = false;
        boolean SPECIAL = true;
    }

    public interface TABLE_TEMP_INT {

        int PRE_NEX = 0;
        int FIR_PRE_NEX_LAS = 1;
        int CUR_FIR_PRE_NEX_LAS = 3;
        int CUR_FIR_PRE_LIN_NEX_LAS = 4;
        int CUR_FIR_PRE_LIN_NEX_LAS_DOW = 5;
        int PRE_LIN_NEX_DOW = 6;
        int FIR_PRE_NEX_LAS_DOW = 7;
        int CUR_FIR_PRE_NEX_LAS_DOW = 8;
    }

    public interface STATUS {

        short COMPLETE = 1;
        short INCOMPLETE = 0;
    }

    public interface TABLE_TEMP_STR {

        String PREVISION = " {PreviousPageLink} ";
        String NEXT = " {NextPageLink} ";
        String FIRST = " {FirstPageLink} ";
        String LAST = " {LastPageLink} ";
        String CURRENT = " {CurrentPageReport} ";
        String PAGELINKS = " {PageLinks} ";
        String DROPDOWN = " {RowsPerPageDropdown} ";
    }

    public interface MENU_ICON {

        String COMPANY = "fa fa-building";
        String ISSUE = "fa fa-clipboard";
        String CUSTOMIZE = "fa fa-stack-overflow";
        String CUSTOMER = "fa fa-user-md";
        String AUTHORITY = "fa fa-unlock";
        String MAINTENANCE = "fa fa-briefcase";
        String GROUP = "fa fa-users";
        String USER = "fa fa-user";
        String SPEECHAPI = "fa fa-microphone";
        String MAIL = "fa fa-envelope";
        String MAILSERVER = "fa fa-cloud";
        String MAILACCOUNT = "fa fa-user-plus";
        String FILTER = "fa fa-filter";
        String FOLDER = "fa fa-folder";
        String CUT = "fa fa-cut";
        String REPORT = "fa fa-bar-chart-o";
        String SYSTEM = "fa fa-cogs";
        String SETTING = "fa fa-cog";
        String LABEL = "fa fa-buysellads";
        String LIST = "fa fa-list";
        String JOB = "fa fa-clock-o";
        String TWILIO = "fa fa-info";
        String COMMAND = "fa fa-code";
        String SERVER = "fa fa-server";
        String TWILIO_LOG = "fa fa-volume-up";
    }

    public static String menuIcon(String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        String val = null;
        switch (name) {
            case "COMPANY":
                val = MENU_ICON.COMPANY;
                break;
            case "CompanyController":
                val = MENU_ICON.COMPANY;
                break;
            case "DataController":
                val = MENU_ICON.TWILIO;
                break;
            case "ISSUE":
                val = MENU_ICON.ISSUE;
                break;
            case "ProjectController":
                val = MENU_ICON.ISSUE;
                break;
            case "CUSTOMER":
                val = MENU_ICON.CUSTOMER;
                break;
            case "AUTHORITY":
                val = MENU_ICON.AUTHORITY;
                break;
            case "AuthorityController":
                val = MENU_ICON.AUTHORITY;
                break;
            case "MAINTENANCE":
                val = MENU_ICON.MAINTENANCE;
                break;
            case "MaintenanceController":
                val = MENU_ICON.MAINTENANCE;
                break;
            case "CUSTOMIZE":
                val = MENU_ICON.CUSTOMIZE;
                break;
            case "CustomizeController":
                val = MENU_ICON.CUSTOMIZE;
                break;
            case "SPEECHAPI":
                val = MENU_ICON.SPEECHAPI;
                break;
            case "SpeechAPIController":
                val = MENU_ICON.SPEECHAPI;
                break;
            case "GroupController":
                val = MENU_ICON.GROUP;
                break;
            case "MemberController":
                val = MENU_ICON.USER;
                break;
            case "SYSTEM":
                val = MENU_ICON.SYSTEM;
                break;
            case "LabelController":
                val = MENU_ICON.LABEL;
                break;
            case "ConfigController":
                val = MENU_ICON.SETTING;
                break;
            case "MAIL":
                val = MENU_ICON.MAIL;
                break;
            case "MailAccountController":
                val = MENU_ICON.MAILACCOUNT;
                break;
            case "MailServerController":
                val = MENU_ICON.MAILSERVER;
                break;
            case "MailFilterController":
                val = MENU_ICON.FILTER;
                break;
            case "MailFolderController":
                val = MENU_ICON.FOLDER;
                break;
            case "MailExplodeController":
                val = MENU_ICON.CUT;
                break;
            case "MailListController":
                val = MENU_ICON.LABEL;
                break;
            case "REPORT":
                val = MENU_ICON.REPORT;
                break;
            case "ReportController":
                val = MENU_ICON.REPORT;
                break;
            case "JobController":
                val = MENU_ICON.JOB;
                break;
            case "BatchController":
                val = MENU_ICON.JOB;
                break;   
            case "CommandController":
                val = MENU_ICON.COMMAND;
                break;
            case "ServerController":
                val = MENU_ICON.SERVER;
                break;
            case "TwilioController":
                val = MENU_ICON.TWILIO_LOG;
                break;
            default:
                break;
        }
        return val;
    }

    public interface EXPLODE_RULE {
        String EOF = "__EOF__";
        String EOL = "__EOL__";
    }
}
