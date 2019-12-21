#!/bin/bash

set -e
script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd)"

as_root="/opt/jboss/wildfly"

# download the postgres module
postgres_module="${as_root}/modules/org/postgresql/main"
mkdir -p ${postgres_module}
cp ${script_dir}/postgres-module.xml ${postgres_module}/module.xml
postgres_driver_url="https://repo1.maven.org/maven2/org/postgresql/postgresql/42.2.8/postgresql-42.2.8.jar"
curl ${postgres_driver_url} --output ${postgres_module}/postgresql.jar

# configure using jboss-cli
${as_root}/bin/jboss-cli.sh --file=${script_dir}/add-driver.cli
