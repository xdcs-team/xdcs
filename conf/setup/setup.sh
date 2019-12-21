#!/bin/bash

set -e

as_root="/opt/jboss/wildfly"
setup_dir="${as_root}/setup"

# set up the deploy user
${as_root}/bin/add-user.sh deploy deploy

# download the postgres module
postgres_module="${as_root}/modules/org/postgresql/main"
mkdir -p ${postgres_module}
cp ${setup_dir}/postgres-module.xml ${postgres_module}/module.xml
postgres_jdbc_url="https://repo1.maven.org/maven2/org/postgresql/postgresql/42.2.8/postgresql-42.2.8.jar"
wget ${postgres_jdbc_url} -O ${postgres_module}/postgresql.jar

# configure using jboss-cli
${as_root}/bin/jboss-cli.sh --file=${setup_dir}/configure.cli

# remove standalone history
rm -rf ${as_root}/standalone/configuration/standalone_xml_history/current

# create xdcs directory
mkdir -p ${as_root}/xdcs
chown jboss:jboss ${as_root}/xdcs
