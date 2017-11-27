#!/bin/bash

set -e

#/usr/bin/mysqld --skip-networking & pid="$!"
"$@" --skip-networking &
pid="$!"

#echo "MySQL init process in progress [${pid}]..."
/etc/init.d/mysql start
mysql -uroot -e'show databases;'

SCHEMA=/schemas/*

CRM_DB="crmcloud"
CRM_CHARACTER_SET="utf8mb4"
CRM_COLLATE="utf8mb4_general_ci"
CRM_USER="crmcloud"
CRM_PASSWORD="123456"

# Create database and user

echo "Create database: ${CRM_DB}"
mysql -uroot <<-EOSQL
	CREATE DATABASE IF NOT EXISTS ${CRM_DB} CHARACTER SET = '${CRM_CHARACTER_SET}' COLLATE = '${CRM_COLLATE}';
	CREATE USER IF NOT EXISTS '${CRM_USER}'@'%' IDENTIFIED BY '${CRM_PASSWORD}' ;
	GRANT ALL ON ${CRM_DB}.* TO '${CRM_USER}'@'%' WITH GRANT OPTION ;
	FLUSH PRIVILEGES ;
EOSQL


mysql -uroot -e'show databases;'

#mysql=( mysql -u"${CRM_USER}" -p"${CRM_PASSWORD}" -h'127.0.0.1' ${CRM_DB} )
mysql=( mysql --default-character-set=utf8 -uroot ${CRM_DB} )
echo "Upgrade ${CRM_DB} by version"
# =========================================================
# Update DB Version
for dir in $SCHEMA
do
	if [ -d "$dir" ]; then
  		echo "Import database version: $dir"
  		for f in $dir/*.zip; do 
			unzip -p $f | "${mysql[@]}" 
		done

	fi
  # take action on each file. $f store current file name
done

# =========================================================

# =========================================================
echo
if ! kill -s TERM "$pid" || ! wait "$pid"; then
	echo >&2 'MySQL init process failed.'
	exit 1
else
	echo >&2 'MySQL init process done.'
	echo
fi
#/etc/init.d/mysql stop
echo 'MySQL init process done. Ready for start up.'
echo

# Start redis server
redis-server /opt/redis/redis.conf

exec "/usr/bin/supervisord"