#!/bin/bash

##########################################################################################################################################
#
# An example :) sh /mnt/script/save_to_slave.sh {master_host} {master_port} {master_user} {master_password} {schema_name} {slave_company_id} {slave_host} {slave_port} {slave_user} {slave_password}
#               sh /mnt/script/save_to_slave.sh 127.0.0.1 3306 root 123456 crmcloud 49 127.0.0.1 3306 root 123456
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
BACKUP_PARENT_DIR="/tmp/myscript.$RANDOM" #"/mnt/sh"
FILE_DUMP_NAME="bizcrm.sql"

# Detect paths
MYSQL_DUMP=`which mysqldump`
MYSQL=`which mysql`

#///////////////////////////////// SLAVE FUNCTIONS /////////////////////////////////
function delete_temp_schema() {
    schema_name=$(printf "%s_%03d" $MYSQL_DATABASE  $MYSQL_MASTER_COMPANY_ID)
    $MYSQL --user=${MYSQL_MASTER_USER} --password=${MYSQL_MASTER_PASSWORD} --host=${MYSQL_MASTER_HOST} --port=${MYSQL_MASTER_PORT} -e "DROP DATABASE $schema_name;"
}
function general_schema_name() {
    schema_name=$(printf "%s_%03d" $MYSQL_DATABASE  $MYSQL_MASTER_COMPANY_ID)
    echo $schema_name
}

function export_to_final_schema_data_on_master() {
    schema_name=$(general_schema_name)
    backup_dir="${BACKUP_PARENT_DIR}/schema/final/${NOW}"
    mkdir -p "${backup_dir}"
    chmod 700 "${backup_dir}"

    # mysqldump will backup by default all the triggers but NOT the stored procedures/functions
    # there are 2 mysqldump parameters that control this behavior:
    $MYSQL_DUMP --routines --opt --user=${MYSQL_MASTER_USER} --password=${MYSQL_MASTER_PASSWORD} --host=${MYSQL_MASTER_HOST} --port=${MYSQL_MASTER_PORT} ${schema_name} > "${backup_dir}/${FILE_DUMP_NAME}"
    
    chmod 600 "${backup_dir}/${FILE_DUMP_NAME}"
    echo "${backup_dir}/${FILE_DUMP_NAME}"
}

function create_schema_final_on_slave() {
    schema_name=$(general_schema_name)
    $MYSQL --user=${MYSQL_SLAVE_USER} --password=${MYSQL_SLAVE_PASSWORD} --host=${MYSQL_SLAVE_HOST} --port=${MYSQL_SLAVE_PORT} -e "CREATE SCHEMA $schema_name DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"
    echo $schema_name
}

function import_data_for_schema_final_on_slave() {
    _schema_name=$1
    _dump_file=$2
    $MYSQL --user=${MYSQL_SLAVE_USER} --password=${MYSQL_SLAVE_PASSWORD} --host=${MYSQL_SLAVE_HOST} --port=${MYSQL_SLAVE_PORT} $_schema_name < $_dump_file
}

dump_final_file=$(export_to_final_schema_data_on_master)
echo "The final dump file is saved to: $dump_final_file"

delete_temp_schema
create_schema_final_on_slave

schema_name=$(general_schema_name)
import_data_for_schema_final_on_slave $schema_name $dump_final_file

#mysql --user=bizcrm --password=bizcrm --host=192.168.1.66 --port=3306 -e "CREATE SCHEMA daind DEFAULT CHARACTER SET utf8 COLLATE utf8_bin;"
#mysql --user=bizcrm --password=bizcrm --host=192.168.1.66 --port=3306 crmcloud_095 < /mnt/script/bizcrm.sql