/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.utils;

/**
 *
 * @author HUONG
 */
public class InterfaceUtil {
    public static final short DELETED = 1;
    public static final short UN_DELETED = 0;

    public static final boolean EXISTS = false;
    public static final boolean NOT_EXISTS = true;

    public static String FIELD_NAME_PREFIX = "field-";

    public static String EXPIRATION_DATE_COLOR = "#47d998";

    public interface FIELDS {
        String MEMO_VIEW = "view_text_area";
        String DYNAMIC = "dynamic_";
        String LABEL_DYNAMIC = "label.dynamic_";
    }

    public interface SERVER_KEY {
       String ELASTIC = "ELASTIC_ENABLE";
       String REDIS = "REDIS_ENABLE";
    }

    public interface ARRAY_STRING_ICON {
       String LABEL = " > ";
       String VALUE = "_";
       String COMMAR = ",";
    }

    public interface COMPANY_TYPE {
        short OPPORTUNITY = 0;
        short STORE = 1;
        short CUSTOMER = 2;
    }

    public interface MAIL_TYPE {
        int COMPANY = 1;
        int USER = 2;
    }

    public interface PAGE {
        int STATIC = 1;
        int DYNAMIC = 2;
    }

    public interface SELECT_LEVEL {

        int ONE = 1;
        int TWO = 2;
        int THREE = 3;
        int FOUR = 4;
        int FIVE = 5;
    }

    public interface TARGET {

        short MAIL = 1;
        short TEL = 2;
        short MOBILE = 3;
        short FAX = 4;
        short HOMEPAGE = 5;
    }

    public interface ISSUE_TYPE {
        short SUPPORT = 1;
        short REQUEST = 2;
        short COMMENT = 3;
        short CUSTOMER = 4;
        short EMAIL = 5;
        short SIGNATURE = 6;
        
        short CHANGE_STATUS = 7; // Luu lai lich su thay doi status
    }

    public interface CUST_FLAG {

        short NORMAL = 0;
        short SPECIAL = 1;
    }

    public interface CUST_STATUS {

        int ADD_APPLICATION = 0;
        int CORREC_APPLICATION = 1;
        int DELETE_APPLICATION = 2;
        int PENDING_APPLICATION = 3;
        int APPROVAL_APPLICATION = 4;
        int NOTSHOW_APPLICATION = 5;
    }

    public interface FIELD_TYPE{
        int TEXT = 1;
        int TEXTAREA = 2;
        int CHECKBOX = 3;
        int RADIO = 4;
        int BUTTON = 5;
        int DATE = 6;
        int SELECT = 7;
        int RADIO_GROUP = 8;
        int CHECKBOX_GROUP = 9;
    }

    public interface COLS {
        String PRODUCTS = "issue_products_id";
        String PRODUCT = "issue_product_id";
        String PROPOSAL = "issue_proposal_id";
        String EXAPLE_SENTENCE = "example_sentence_type_id";
        String GROUP = "d_o_group";
        String USER = "d_o_user";
        String GROUPS = "d_o_groups";
        String USERS = "d_o_users";
        String AGE = "cust_age_id";
        String COOPERATION = "cust_cooperation_id";
        String SESSION = "cust_tel_session_id";
        String SEX = "cust_sex_id";
        String SPECIAL = "cust_special_id";
        String KEYWORD = "issue_keyword_id";
        String PUBLIC = "issue_public_id";
        String RECEIVE = "issue_receive_id";
        String STATUS = "issue_status_id";
        String IMPORTANCE = "todo_importance_id";
        String PROJECT_PASS_DAYS = "project_pass_days";
        String MAIL_REQUEST = "mail_request_id";
        String CREATOR_ID = "issue_creator_id";
        String PERSON_ID = "issue_receive_person_id";
        String SUPPORT_METHOD = "cust_support_method_id";
        String SUPPORT_CLASS = "cust_support_class_id";
        String COMMENT_PERSON = "issue_comment_person_id";
        String CUSTOMER_SUPPORT_PERSON = "issue_customer_support_person_id";
        String SIGNATURE = "mail_signature_id";
        String CUST_STATUS = "cust_status";
        String CUST_CREATOR_ID = "cust_creator_id";
        
        // danh sách các cột mente trong mảng phục vụ cho việc duyệt các COLUMN dễ hơn.
        public String[] MENTE_COLS = new String[] {
            PRODUCT
            ,PROPOSAL
            ,EXAPLE_SENTENCE
            ,AGE
            ,COOPERATION
            ,SESSION
            ,SEX
            ,SPECIAL
            ,KEYWORD
            ,PUBLIC
            ,RECEIVE
            ,STATUS
            ,IMPORTANCE
            ,MAIL_REQUEST
            ,SUPPORT_METHOD
            ,SUPPORT_CLASS
            ,SIGNATURE
        };
    }
}
