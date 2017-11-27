#!/bin/bash

##########################################################################################################################################
#
# An example :) sh /mnt/script/migrate_data.sh {master_host} {master_port} {master_user} {master_password} {schema_name} {slave_company_id}
#               sh /mnt/script/migrate_data.sh 127.0.0.1 3306 root 123456 crmcloud 49
#
##########################################################################################################################################

# Đọc thông tin MASTER từ CLI.
MYSQL_MASTER_HOST=$1
MYSQL_MASTER_PORT=$2
MYSQL_MASTER_USER=$3
MYSQL_MASTER_PASSWORD=$4
MYSQL_DATABASE=$5
MYSQL_MASTER_COMPANY_ID=$6

function general_schema_name() {
    schema_name=$(printf "%s_%03d" $MYSQL_DATABASE  $MYSQL_MASTER_COMPANY_ID)
    echo $schema_name
}

schema_name=$(general_schema_name)

# Detect paths
MYSQL=`which mysql`

$MYSQL --user=${MYSQL_MASTER_USER} --password=${MYSQL_MASTER_PASSWORD} --host=${MYSQL_MASTER_HOST} --port=${MYSQL_MASTER_PORT} <<EOFMYSQL
    SET FOREIGN_KEY_CHECKS=0;

    UPDATE $MYSQL_DATABASE.crm_company SET manual_id=company_id;
    UPDATE $MYSQL_DATABASE.crm_group SET manual_id=group_id;
    UPDATE $MYSQL_DATABASE.crm_member SET manual_id=member_id;
    
--    INSERT INTO $schema_name.crm_company SELECT * FROM $MYSQL_DATABASE.crm_company WHERE company_id=1;
--    INSERT INTO $schema_name.crm_group SELECT * FROM $MYSQL_DATABASE.crm_group WHERE group_id in (SELECT group_id FROM $MYSQL_DATABASE.crm_member WHERE member_id=1);
--    INSERT INTO $schema_name.crm_member SELECT * FROM $MYSQL_DATABASE.crm_member WHERE member_id=1;
    
    INSERT INTO $schema_name.crm_company SELECT * FROM $MYSQL_DATABASE.crm_company WHERE company_id=$MYSQL_MASTER_COMPANY_ID;
--    INSERT INTO $schema_name.crm_group SELECT * FROM $MYSQL_DATABASE.crm_group WHERE company_id=$MYSQL_MASTER_COMPANY_ID;
--    INSERT INTO $schema_name.crm_member	SELECT * FROM $MYSQL_DATABASE.crm_member WHERE group_id in (SELECT group_id FROM $MYSQL_DATABASE.crm_group WHERE company_id=$MYSQL_MASTER_COMPANY_ID);
    
    INSERT INTO $schema_name.crm_mente_item SELECT * FROM $MYSQL_DATABASE.crm_mente_item WHERE item_level=1 AND company_id=1;
    INSERT INTO $schema_name.crm_mente_option_data_value SELECT * FROM $MYSQL_DATABASE.crm_mente_option_data_value WHERE item_id in (SELECT item_id FROM $MYSQL_DATABASE.crm_mente_item WHERE item_level=1 AND company_id=1);
--    INSERT INTO $schema_name.crm_project_cust_column_width SELECT * FROM $MYSQL_DATABASE.crm_project_cust_column_width WHERE company_id=1;
--    INSERT INTO $schema_name.crm_project_cust_search_list SELECT * FROM $MYSQL_DATABASE.crm_project_cust_search_list WHERE company_id=1;
    
    UPDATE $schema_name.crm_mente_item SET company_id=$MYSQL_MASTER_COMPANY_ID, creator_id=NULL, created_time=NULL, updated_id=NULL, updated_time=NULL;
    UPDATE $schema_name.crm_mente_option_data_value SET company_id=$MYSQL_MASTER_COMPANY_ID, creator_id=NULL, created_time=NULL, updated_id=NULL, updated_time=NULL;
--    UPDATE $schema_name.crm_project_cust_column_width SET company_id=$MYSQL_MASTER_COMPANY_ID;
--    UPDATE $schema_name.crm_project_cust_search_list SET company_id=$MYSQL_MASTER_COMPANY_ID;

--    INSERT INTO $MYSQL_DATABASE.crm_group(group_name, group_deleted, company_id, group_memo, source, target) VALUES ('G_CUSTOMER', 0, $MYSQL_MASTER_COMPANY_ID, 'for customer', 'SYSTEM', 'CUSTOMER');
--    INSERT INTO $schema_name.crm_group SELECT * FROM $MYSQL_DATABASE.crm_group WHERE group_name='G_CUSTOMER' AND company_id=$MYSQL_MASTER_COMPANY_ID;
    
    INSERT INTO $schema_name.crm_prefecture SELECT * FROM $MYSQL_DATABASE.crm_prefecture;
    
    INSERT INTO $schema_name.crm_zip_code select * from $MYSQL_DATABASE.crm_zip_code where locale_code = 'ja';
    UPDATE $schema_name.crm_zip_code set creator_id = NULL, created_time=NULL, updated_id=NULL, updated_time=NULL;
    
    INSERT INTO $schema_name.crm_server SELECT * FROM $MYSQL_DATABASE.crm_server WHERE server_flag=1 AND company_id=1 AND (server_deleted is null or server_deleted=0);
    UPDATE $schema_name.crm_server SET company_id=$MYSQL_MASTER_COMPANY_ID;
    
    SET FOREIGN_KEY_CHECKS=1;
    exit
EOFMYSQL