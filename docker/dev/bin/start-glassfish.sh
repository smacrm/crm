#!/bin/bash

SCRIPT=$(readlink -f "$0")
bin_dir=$(dirname "$SCRIPT")
root_dir=${bin_dir}/..
domains_dir=/opt/glassfish4/glassfish/domains

export bin_dir root_dir domains_dir

set -e

#extlib_dir=${root_dir}/extlib
#if [ "$(ls -A ${extlib_dir})" ]; then
#    echo "${extlib_dir} is not empty, copy files to /opt/glassfish4/glassfish/lib"
#    cp ${extlib_dir}/* /opt/glassfish4/glassfish/lib
#fi

#deploy_dir=${root_dir}/deploy
#if [ "$(ls -A ${deploy_dir})" ]; then
#    echo "${deploy_dir} is not empty, copy files to ${domains_dir}/domain1/autodeploy"
#    cp ${deploy_dir}/* ${domains_dir}/domain1/autodeploy
#fi

echo "=> Starting and running Glassfish server"
DEBUG_MODE=${DEBUG:-false}
echo "=> Debug mode is set to: ${DEBUG_MODE}"
exec asadmin start-domain --debug=${DEBUG_MODE} --watchdog
