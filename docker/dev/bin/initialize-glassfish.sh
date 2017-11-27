#!/bin/bash

SCRIPT=$(readlink -f "$0")
bin_dir=$(dirname "$SCRIPT")
root_dir=${bin_dir}/..

password="admin"
export password

domains_dir=/opt/glassfish4/glassfish/domains

if [ ! "$(ls -A ${domains_dir})" ]; then
    echo "${domains_dir} is empty, create test domain"
    asadmin create-domain --nopassword domain1
fi

echo "=> Initialize Glassfish"
echo "=> Start Glassfish server"
asadmin start-domain 

echo "=> Modifying password of admin to ${password} in Glassfish"
${bin_dir}/change_admin_password_func.sh ${password}
echo "=> Enabling secure admin login"
${bin_dir}/enable_secure_admin.sh ${password}

echo "=> Stop Glassfish server"
asadmin stop-domain

mkdir -p ${root_dir}/config 
echo "AS_ADMIN_PASSWORD=${password}" > ${root_dir}/config/admin-password
