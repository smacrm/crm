#!/bin/bash

##########################################################################################################################################
#
# An example :) sh /mnt/script/truncate_database {master_host} {master_port} {master_user} {master_password} {schema_name} {slave_company_id}
#               sh /mnt/script/truncate_database.sh 127.0.0.1 3306 root 123456 crmcloud 40
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

MYSQL=`which mysql`

# $MYSQL --user=${MYSQL_MASTER_USER} --password=${MYSQL_MASTER_PASSWORD} --host=${MYSQL_MASTER_HOST} --port=${MYSQL_MASTER_PORT} -e "SET FOREIGN_KEY_CHECKS=0;"
# $MYSQL --user=${MYSQL_MASTER_USER} --password=${MYSQL_MASTER_PASSWORD} --host=${MYSQL_MASTER_HOST} --port=${MYSQL_MASTER_PORT} -Nse 'show tables' $schema_name | while read table; do $MYSQL --user=${MYSQL_MASTER_USER} --password=${MYSQL_MASTER_PASSWORD} --host=${MYSQL_MASTER_HOST} --port=${MYSQL_MASTER_PORT} -e "truncate table $table" $schema_name; done
# $MYSQL --user=${MYSQL_MASTER_USER} --password=${MYSQL_MASTER_PASSWORD} --host=${MYSQL_MASTER_HOST} --port=${MYSQL_MASTER_PORT} -e "SET FOREIGN_KEY_CHECKS=1;"
# mysql --user=root --password=123456 --host=127.0.0.1 --port=3306 -e "show tables;" crmcloud > "/mnt/script/tables.txt"

$MYSQL --user=${MYSQL_MASTER_USER} --password=${MYSQL_MASTER_PASSWORD} --host=${MYSQL_MASTER_HOST} --port=${MYSQL_MASTER_PORT} <<EOFMYSQL
    SET FOREIGN_KEY_CHECKS=0;

    TRUNCATE TABLE $schema_name.crm_attachment;
    TRUNCATE TABLE $schema_name.crm_auto_mail;
    TRUNCATE TABLE $schema_name.crm_auto_mail_member;
    TRUNCATE TABLE $schema_name.crm_auto_mail_mente;
    TRUNCATE TABLE $schema_name.crm_auto_mail_sent_history;
    TRUNCATE TABLE $schema_name.crm_batch;
    TRUNCATE TABLE $schema_name.crm_command;
    TRUNCATE TABLE $schema_name.crm_company;
    TRUNCATE TABLE $schema_name.crm_company_target_info;
    TRUNCATE TABLE $schema_name.crm_config;
    TRUNCATE TABLE $schema_name.crm_cust_data_special;
    TRUNCATE TABLE $schema_name.crm_cust_target_info;
    TRUNCATE TABLE $schema_name.crm_customer;
    TRUNCATE TABLE $schema_name.crm_database_server;
    TRUNCATE TABLE $schema_name.crm_database_server_company_rel;
    TRUNCATE TABLE $schema_name.crm_div;
    TRUNCATE TABLE $schema_name.crm_escalation;
    TRUNCATE TABLE $schema_name.crm_escalation_sample;
    TRUNCATE TABLE $schema_name.crm_group;
    TRUNCATE TABLE $schema_name.crm_issue;
    TRUNCATE TABLE $schema_name.crm_issue_attachment;
    TRUNCATE TABLE $schema_name.crm_issue_cust_rel;
    TRUNCATE TABLE $schema_name.crm_issue_lamp;
    TRUNCATE TABLE $schema_name.crm_issue_lamp_global;
    TRUNCATE TABLE $schema_name.crm_issue_product;
    TRUNCATE TABLE $schema_name.crm_issue_related;
    TRUNCATE TABLE $schema_name.crm_issue_status_history;
    TRUNCATE TABLE $schema_name.crm_issue_todo;
    TRUNCATE TABLE $schema_name.crm_item;
    TRUNCATE TABLE $schema_name.crm_item_global;
    TRUNCATE TABLE $schema_name.crm_log4j;
    TRUNCATE TABLE $schema_name.crm_mail_account;
    TRUNCATE TABLE $schema_name.crm_mail_data;
    TRUNCATE TABLE $schema_name.crm_mail_explode;
    TRUNCATE TABLE $schema_name.crm_mail_filter;
    TRUNCATE TABLE $schema_name.crm_mail_folder;
    TRUNCATE TABLE $schema_name.crm_mail_person;
    TRUNCATE TABLE $schema_name.crm_mail_server;
    TRUNCATE TABLE $schema_name.crm_member;
    TRUNCATE TABLE $schema_name.crm_mente_item;
    TRUNCATE TABLE $schema_name.crm_mente_item_value;
    TRUNCATE TABLE $schema_name.crm_mente_option_data_value;
    TRUNCATE TABLE $schema_name.crm_method;
    TRUNCATE TABLE $schema_name.crm_module_page_rel;
    TRUNCATE TABLE $schema_name.crm_multiple_data_value;
    TRUNCATE TABLE $schema_name.crm_multiple_member_group_rel;
    TRUNCATE TABLE $schema_name.crm_page;
    TRUNCATE TABLE $schema_name.crm_page_method_rel;
    TRUNCATE TABLE $schema_name.crm_page_tab;
    TRUNCATE TABLE $schema_name.crm_page_tab_div_item_rel;
    TRUNCATE TABLE $schema_name.crm_prefecture;
    TRUNCATE TABLE $schema_name.crm_products;
    TRUNCATE TABLE $schema_name.crm_project_cust_column_width;
    TRUNCATE TABLE $schema_name.crm_project_cust_search_list;
    TRUNCATE TABLE $schema_name.crm_project_role_rel;
    TRUNCATE TABLE $schema_name.crm_property_item_label;
    TRUNCATE TABLE $schema_name.crm_quick_search;
    TRUNCATE TABLE $schema_name.crm_role;
    TRUNCATE TABLE $schema_name.crm_role_page_method_rel;
    TRUNCATE TABLE $schema_name.crm_server;
    TRUNCATE TABLE $schema_name.crm_system_module;
    TRUNCATE TABLE $schema_name.crm_system_use_auth_rel;
    TRUNCATE TABLE $schema_name.crm_tab;
    TRUNCATE TABLE $schema_name.crm_twilio;
    TRUNCATE TABLE $schema_name.crm_twilio_config;
    TRUNCATE TABLE $schema_name.crm_twilio_history;
    TRUNCATE TABLE $schema_name.crm_union_company_rel;
    TRUNCATE TABLE $schema_name.crm_zip_code;
    
    SET FOREIGN_KEY_CHECKS=1;
    exit
EOFMYSQL