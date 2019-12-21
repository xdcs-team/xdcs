#!/bin/bash

set -e
script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd)"

as_root="/opt/jboss/wildfly"

if [ -n "${DEPLOY_USER_NAME}" ] && [ -n "${DEPLOY_USER_PASS}" ]; then
  # set up the deploy user
  ${as_root}/bin/add-user.sh ${DEPLOY_USER_NAME} ${DEPLOY_USER_PASS}
fi

# set up postgres driver
${script_dir}/postgres/setup.sh

# configure using jboss-cli
${as_root}/bin/jboss-cli.sh --file=${script_dir}/configure.cli

# remove standalone history
rm -rf ${as_root}/standalone/configuration/standalone_xml_history/current

# create xdcs directory
mkdir -p ${as_root}/xdcs
chown jboss:jboss ${as_root}/xdcs
