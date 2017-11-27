#!/bin/bash

##########################################################################################################################################
#
# An example :) sh /mnt/script/split_db.sh {master_host} {master_port} {master_user} {master_password} {schema_name} {slave_company_id} {slave_host} {slave_port} {slave_user} {slave_password}
#               sh /mnt/script/split_db_20170804.sh 127.0.0.1 3306 root 123456 crmcloud 1 127.0.0.1 3306 root 123456
#
##########################################################################################################################################

# Đọc thông tin MASTER từ CLI.
MYSQL_MASTER_HOST=$1
MYSQL_MASTER_PORT=$2
MYSQL_MASTER_USER=$3
MYSQL_MASTER_PASSWORD=$4
MYSQL_DATABASE=$5
MYSQL_MASTER_COMPANY_ID=$6

# Đọc thông tin SLAVE từ CLI.
MYSQL_SLAVE_HOST=$7
MYSQL_SLAVE_PORT=$8
MYSQL_SLAVE_USER=$9
MYSQL_SLAVE_PASSWORD=${10}

# Thư mục gốc và tên file exported trên MASTER host.
NOW=`date +%Y_%m_%d_%H_%M`
BACKUP_PARENT_DIR="/tmp"
FILE_DUMP_NAME="bizcrm.sql"

# Thông tin master schema.
MYSQL_DUMP=`which mysqldump`
MYSQL=`which mysql`

function echo_parameter_to_console() {
    echo "MYSQL_MASTER_HOST=${MYSQL_MASTER_HOST}"
    echo "MYSQL_MASTER_PORT=${MYSQL_MASTER_PORT}"
    echo "MYSQL_MASTER_USER=${MYSQL_MASTER_USER}"
    echo "MYSQL_MASTER_PASSWORD=${MYSQL_MASTER_PASSWORD}"
    echo "MYSQL_DATABASE=${MYSQL_DATABASE}"
    echo "MYSQL_MASTER_COMPANY_ID=${MYSQL_MASTER_COMPANY_ID}"
    echo "MYSQL_SLAVE_HOST=${MYSQL_SLAVE_HOST}"
    echo "MYSQL_SLAVE_PORT=${MYSQL_SLAVE_PORT}"
    echo "MYSQL_SLAVE_USER=${MYSQL_SLAVE_USER}"
    echo "MYSQL_SLAVE_PASSWORD=${MYSQL_SLAVE_PASSWORD}"
}

#///////////////////////////////// MASTER FUNCTIONS /////////////////////////////////
function check_master_connection() {
    echo exit | mysql --user=${MYSQL_MASTER_USER} --password=${MYSQL_MASTER_PASSWORD} --host=${MYSQL_MASTER_HOST} --port=${MYSQL_MASTER_PORT} -B 2>/dev/null
    if [ "$?" -gt 0 ]; then
        echo "ERROR: MySQL(MASTER) ${MYSQL_MASTER_USER}/${MYSQL_MASTER_PASSWORD} incorrect"
        exit 1
    else
        echo "Connected to MYSQL database."
    fi
}

function export_master_schema() {
    backup_dir="${BACKUP_PARENT_DIR}/schema/${NOW}"

    mkdir -p "${backup_dir}"
    chmod 700 "${backup_dir}"

    # mysqldump will backup by default all the triggers but NOT the stored procedures/functions
    # there are 2 mysqldump parameters that control this behavior:
    $MYSQL_DUMP --routines --no-data --opt --user=${MYSQL_MASTER_USER} --password=${MYSQL_MASTER_PASSWORD} --host=${MYSQL_MASTER_HOST} --port=${MYSQL_MASTER_PORT} ${MYSQL_DATABASE} > "${backup_dir}/${FILE_DUMP_NAME}"

    chmod 600 "${backup_dir}/${FILE_DUMP_NAME}"
    echo "${backup_dir}/${FILE_DUMP_NAME}"
}

#///////////////////////////////// SALVE FUNCTIONS /////////////////////////////////
function check_slave_connection() {
    echo exit | mysql --user=${MYSQL_SLAVE_USER} --password=${MYSQL_SLAVE_PASSWORD} --host=${MYSQL_SLAVE_HOST} --port=${MYSQL_SLAVE_PORT} -B 2>/dev/null
    if [ "$?" -gt 0 ]; then
        echo "ERROR: MySQL(SLAVE) ${MYSQL_MASTER_USER}/${MYSQL_MASTER_PASSWORD} incorrect"
        exit 1
    else
        echo "Connected to MYSQL database."
    fi
}

function create_schema_on_slave() {
    schema_name=$(printf "%s_%03d" $MYSQL_DATABASE  $MYSQL_MASTER_COMPANY_ID)
    $MYSQL --user=${MYSQL_SLAVE_USER} --password=${MYSQL_SLAVE_PASSWORD} --host=${MYSQL_SLAVE_HOST} --port=${MYSQL_SLAVE_PORT} -e "CREATE SCHEMA $schema_name DEFAULT CHARACTER SET utf8 COLLATE utf8_bin;"
    echo $schema_name
}

function import_data_on_slave() {
    _schema_name=$1
    _dump_file=$2
    $MYSQL --user=${MYSQL_SLAVE_USER} --password=${MYSQL_SLAVE_PASSWORD} --host=${MYSQL_SLAVE_HOST} --port=${MYSQL_SLAVE_PORT} $_schema_name < $_dump_file
}

#///////////////////////////////// COMMON RUNNING /////////////////////////////////
echo_parameter_to_console

#///////////////////////////////// MASTER RUNNING /////////////////////////////////
# Kiểm tra kết nối tới MYSQL(MASTER).
check_master_connection

# Tạo DUMP-FILE từ db nguồn.
dump_file=$(export_master_schema)
echo "The dump file is saved to :$dump_file"

#///////////////////////////////// SLAVE RUNNING /////////////////////////////////
# Kiểm tra kết nối tới MYSQL(SLAVE).
check_slave_connection

# Phân tích tên SCHEMA với MYSQL_MASTER_COMPANY_ID.
schema_name=$(create_schema_on_slave)
echo "The $schema_name is created!"

# Import data tới SCHEMA mới.
import_data_on_slave $schema_name $dump_file

#///////////////////////////////// MIGRATE DATA /////////////////////////////////
echo "Begin migrating data from MASTER to SALVE..."
$MYSQL --user=${MYSQL_MASTER_USER} --password=${MYSQL_MASTER_PASSWORD} --host=${MYSQL_MASTER_HOST} --port=${MYSQL_MASTER_PORT} <<EOFMYSQL
    SET FOREIGN_KEY_CHECKS=0;

    UPDATE $MYSQL_DATABASE.crm_company SET manual_id=company_id;
    UPDATE $MYSQL_DATABASE.crm_group 	SET manual_id=group_id;
    UPDATE $MYSQL_DATABASE.crm_member 	SET manual_id=member_id;

    INSERT INTO $schema_name.crm_company SELECT * FROM $MYSQL_DATABASE.crm_company WHERE company_id=1;
    INSERT INTO $schema_name.crm_group 	SELECT * FROM $MYSQL_DATABASE.crm_group WHERE group_id in (SELECT group_id FROM $MYSQL_DATABASE.crm_member WHERE member_id=1);
    INSERT INTO $schema_name.crm_member	SELECT * FROM $MYSQL_DATABASE.crm_member WHERE member_id=1;

    INSERT INTO $schema_name.crm_mente_item SELECT * FROM $MYSQL_DATABASE.crm_mente_item WHERE item_level=1 AND company_id=1;
    INSERT INTO $schema_name.crm_mente_option_data_value SELECT * FROM $MYSQL_DATABASE.crm_mente_option_data_value WHERE item_id in (SELECT item_id FROM $MYSQL_DATABASE.crm_mente_item WHERE item_level=1 AND company_id=1);
    INSERT INTO $schema_name.crm_project_cust_column_width SELECT * FROM $MYSQL_DATABASE.crm_project_cust_column_width WHERE company_id=1;
    INSERT INTO $schema_name.crm_project_cust_search_list SELECT * FROM $MYSQL_DATABASE.crm_project_cust_search_list WHERE company_id=1;
    
    INSERT INTO $schema_name.crm_zip_code SELECT * FROM $MYSQL_DATABASE.crm_zip_code;
    INSERT INTO $schema_name.crm_config SELECT * FROM $MYSQL_DATABASE.crm_config;
    -- INSERT INTO $schema_name.crm_database_server SELECT * FROM $MYSQL_DATABASE.crm_database_server;
    
    SET FOREIGN_KEY_CHECKS=1;
EOFMYSQL
echo "DONE!"
