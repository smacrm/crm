#!/bin/bash

##########################################################################################################################################
#
# An example :) sh /mnt/script/split_db.sh {master_host} {master_port} {master_user} {master_password} {schema_name} {slave_company_id} {slave_host} {slave_port} {slave_user} {slave_password}
#               sh /mnt/script/clean_slave_database.sh 127.0.0.1 3306 root 123456 crmcloud 46 127.0.0.1 3306 root 123456
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
BACKUP_PARENT_DIR="/mnt/sh"
FILE_DUMP_NAME="bizcrm.sql"

MYSQL_ADMIN=`which mysqladmin` 
MYSQL_DUMP=`which mysqldump`
MYSQL=`which mysql`

function check_master_connection() {
    echo exit | mysql --user=${MYSQL_MASTER_USER} --password=${MYSQL_MASTER_PASSWORD} --host=${MYSQL_MASTER_HOST} --port=${MYSQL_MASTER_PORT} -B 2>/dev/null
    if [ "$?" -gt 0 ]; then
        echo "ERROR: MySQL(MASTER) ${MYSQL_MASTER_USER}/${MYSQL_MASTER_PASSWORD} incorrect"
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
function general_schema_name() {
    schema_name=$(printf "%s_%03d" $MYSQL_DATABASE  $MYSQL_MASTER_COMPANY_ID)
    echo $schema_name
}

check_master_connection
check_slave_connection

schema_name=$(general_schema_name)
$MYSQL_ADMIN --user=${MYSQL_MASTER_USER} --password=${MYSQL_MASTER_PASSWORD} --host=${MYSQL_MASTER_HOST} --port=${MYSQL_MASTER_PORT} -f DROP $schema_name
$MYSQL_ADMIN --user=${MYSQL_SLAVE_USER} --password=${MYSQL_SLAVE_PASSWORD} --host=${MYSQL_SLAVE_HOST} --port=${MYSQL_SLAVE_PORT} -f DROP $schema_name





