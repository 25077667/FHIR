
###############################################################################
# (C) Copyright IBM Corp. 2021
#
# SPDX-License-Identifier: Apache-2.0
###############################################################################

set -ex
set -o pipefail

if [ ! -f /opt/ibm-fhir-server/tools/tenant.key ]
then
    echo 'hey_yoh_what' > /opt/ibm-fhir-server/tools/tenant.key
    java -jar /opt/ibm-fhir-server/tools/fhir-persistence-schema-*-cli.jar \
        --db-type db2 --prop db.host=db2 --prop db.port=50000 --prop db.database=fhirdb --prop user=db2inst1 --prop password=hey_yoh_what \
        --create-schemas
    java -jar /opt/ibm-fhir-server/tools/fhir-persistence-schema-*-cli.jar \
        --db-type db2 --prop db.host=db2 --prop db.port=50000 --prop db.database=fhirdb --prop user=db2inst1 --prop password=hey_yoh_what \
        --update-schema
    java -jar /opt/ibm-fhir-server/tools/fhir-persistence-schema-*-cli.jar \
        --db-type db2 --prop db.host=db2 --prop db.port=50000 --prop db.database=fhirdb --prop user=db2inst1 --prop password=hey_yoh_what \
        --allocate-tenant default --tenant-key-file /opt/ibm-fhir-server/tools/tenant.key --grant-to fhirserver
fi

/opt/ol/wlp/bin/server run

# EOF