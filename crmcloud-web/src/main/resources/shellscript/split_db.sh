#!/bin/bash

##########################################################################################################################################
#
# An example :) sh /mnt/script/split_db.sh {master_host} {master_port} {master_user} {master_password} {schema_name} {slave_company_id} {slave_host} {slave_port} {slave_user} {slave_password}
#               sh /mnt/script/split_db.sh 127.0.0.1 3306 root 123456 crmcloud 41 127.0.0.1 3306 root 123456
#
##########################################################################################################################################

# Đọc thông tin MASTER từ CLI.crmcloud
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

#///////////////////////////////// COMMON FUNCTIONS /////////////////////////////////
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
    echo exit | $MYSQL --user=${MYSQL_MASTER_USER} --password=${MYSQL_MASTER_PASSWORD} --host=${MYSQL_MASTER_HOST} --port=${MYSQL_MASTER_PORT} -B 2>/dev/null
    if [ "$?" -gt 0 ]; then
        echo "ERROR: MySQL(MASTER) ${MYSQL_MASTER_USER}/${MYSQL_MASTER_PASSWORD} incorrect!"
        exit 1
    fi
}

function check_slave_connection() {
    echo exit | $MYSQL --user=${MYSQL_SLAVE_USER} --password=${MYSQL_SLAVE_PASSWORD} --host=${MYSQL_SLAVE_HOST} --port=${MYSQL_SLAVE_PORT} -B 2>/dev/null
    if [ "$?" -gt 0 ]; then
        echo "ERROR: MySQL(SLAVE) ${MYSQL_SLAVE_USER}/${MYSQL_SLAVE_PASSWORD} incorrect!"
        exit 1
    fi
}

function export_to_temp_schema_nodata_on_master() {
    backup_dir="${BACKUP_PARENT_DIR}/schema/${NOW}"
    mkdir -p "${backup_dir}"
    chmod 700 "${backup_dir}"
    
    # mysqldump will backup by default all the triggers but NOT the stored procedures/functions
    # there are 2 mysqldump parameters that control this behavior:
    $MYSQL_DUMP --routines --no-data --opt --user=${MYSQL_MASTER_USER} --password=${MYSQL_MASTER_PASSWORD} --host=${MYSQL_MASTER_HOST} --port=${MYSQL_MASTER_PORT} ${MYSQL_DATABASE} > "${backup_dir}/${FILE_DUMP_NAME}"
    
    chmod 600 "${backup_dir}/${FILE_DUMP_NAME}"
    echo "${backup_dir}/${FILE_DUMP_NAME}"
}

#///////////////////////////////// TEMPLATE FUNCTIONS /////////////////////////////////
function create_schema_temp_on_master() {
    schema_name=$(printf "%s_%03d" $MYSQL_DATABASE  $MYSQL_MASTER_COMPANY_ID)
    $MYSQL --user=${MYSQL_MASTER_USER} --password=${MYSQL_MASTER_PASSWORD} --host=${MYSQL_MASTER_HOST} --port=${MYSQL_MASTER_PORT} -e "CREATE SCHEMA $schema_name DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"
    echo $schema_name
}
function import_data_for_schema_temp_on_master() {
    _schema_name=$1
    _dump_file=$2
    $MYSQL --user=${MYSQL_MASTER_USER} --password=${MYSQL_MASTER_PASSWORD} --host=${MYSQL_MASTER_HOST} --port=${MYSQL_MASTER_PORT} $_schema_name < $_dump_file
}

#//////////////////////////////////////////////////////////////////////////////////
#//////////////////////////////////////////////////////////////////////////////////
#///////////////////////////////// COMMON RUNNING /////////////////////////////////
#echo_parameter_to_console

#//////////////////////////////////////////////////////////////////////////////////
#//////////////////////////////////////////////////////////////////////////////////
#///////////////////////////////// MASTER RUNNING /////////////////////////////////
# Kiểm tra kết nối tới MYSQL(MASTER).
check_master_connection
check_slave_connection

# Tạo DUMP-FILE từ db nguồn.
dump_file=$(export_to_temp_schema_nodata_on_master)
echo "File dump cua main-schema duoc dat vao trong thu muc: $dump_file."

#//////////////////////////////////////////////////////////////////////////////////
#//////////////////////////////////////////////////////////////////////////////////
#///////////////////////////////// TEMPLATE RUNNING ///////////////////////////////
# Phân tích tên SCHEMA với MYSQL_MASTER_COMPANY_ID.
schema_name=$(create_schema_temp_on_master)
echo "$schema_name da duoc tao tren masterdb!"

# Import data tới SCHEMA mới.
import_data_for_schema_temp_on_master $schema_name $dump_file

echo "Bat dau qua trinh lam min du lieu cho temp-schema tren masterdb."
sh /mnt/script/migrate_data.sh ${MYSQL_MASTER_HOST} ${MYSQL_MASTER_PORT} ${MYSQL_MASTER_USER} ${MYSQL_MASTER_PASSWORD} ${MYSQL_DATABASE} ${MYSQL_MASTER_COMPANY_ID}

# echo "Bat dau truncate tat ca tables tren template schema tren MasterDb."
# sh /mnt/script/truncate_database.sh ${MYSQL_MASTER_HOST} ${MYSQL_MASTER_PORT} ${MYSQL_MASTER_USER} ${MYSQL_MASTER_PASSWORD} ${MYSQL_DATABASE} ${MYSQL_MASTER_COMPANY_ID}

#//////////////////////////////////////////////////////////////////////////////////
#//////////////////////////////////////////////////////////////////////////////////
#///////////////////////////////// SLAVE RUNNING //////////////////////////////////
echo "Bat dau qua trinh chuyen schema sang slavedb."
sh /mnt/script/save_to_slave.sh ${MYSQL_MASTER_HOST} ${MYSQL_MASTER_PORT} ${MYSQL_MASTER_USER} ${MYSQL_MASTER_PASSWORD} ${MYSQL_DATABASE} ${MYSQL_MASTER_COMPANY_ID} ${MYSQL_SLAVE_HOST} ${MYSQL_SLAVE_PORT} ${MYSQL_SLAVE_USER} ${MYSQL_SLAVE_PASSWORD}
echo "DONE!"