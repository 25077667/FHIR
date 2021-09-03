#!/usr/bin/env bash

# ----------------------------------------------------------------------------
# (C) Copyright IBM Corp. 2021
#
# SPDX-License-Identifier: Apache-2.0
# ----------------------------------------------------------------------------

# Updates the audit-kafka certificates 

# Pre: - be sure to wipe the $WORKSPACE/build/audit/kafka/resources of jks files 
# Post: remove crt/key/crl files
create_kafka_ci() { 
    # Update the Kafka JKS
    cd $WORKSPACE/build/audit/kafka/resources
    # Generate CA key
    openssl req -new -newkey rsa:4096 -x509 -keyout kafka-test-1.key -out kafka-test-1.crt -days 10960 -subj '/CN=ca1.test.fhir.ibm.com/OU=TEST/O=IBM FHIR SERVER/L=CAMBRIDGE/ST=MA/C=US' -passin pass:hey_yoh_what -passout pass:hey_yoh_what

    for i in broker1 broker2 producer consumer
    do
        echo $i
        # Create keystores
        keytool -genkey -noprompt \
                    -alias $i \
                    -dname "CN=$i.test.fhir.ibm.com, OU=TEST, O=IBM FHIR SERVER, L=CAMBRIDGE, ST=MA, C=US" \
                    -keystore kafka.$i.keystore.jks \
                    -keysize 4096 \
                    -keyalg RSA \
                    -validity 10960 \
                    -storepass hey_yoh_what \
                    -keypass hey_yoh_what

        # Create CSR, sign the key and import back into keystore
        keytool -keystore kafka.$i.keystore.jks -alias $i -certreq -file $i.csr -storepass hey_yoh_what -keypass hey_yoh_what

        openssl x509 -req -CA kafka-test-1.crt -CAkey kafka-test-1.key -in $i.csr -out $i-ca1-signed.crt -days 10960 -CAcreateserial -passin pass:hey_yoh_what

        keytool -keystore kafka.$i.keystore.jks -alias CARoot -import -file kafka-test-1.crt -storepass hey_yoh_what -keypass hey_yoh_what -noprompt

        keytool -keystore kafka.$i.keystore.jks -alias $i -import -file $i-ca1-signed.crt -storepass hey_yoh_what -keypass hey_yoh_what  -noprompt

        # Create truststore and import the CA cert.
        keytool -keystore kafka.$i.truststore.jks -alias CARoot -import -file kafka-test-1.crt -storepass hey_yoh_what -keypass hey_yoh_what -noprompt

    echo "hey_yoh_what" > ${i}_sslkey_creds
    echo "hey_yoh_what" > ${i}_keystore_creds
    echo "hey_yoh_what" > ${i}_truststore_creds
    done

    cp $WORKSPACE/build/audit/kafka/resources/*.jks $WORKSPACE/build/notifications/kafka/resources/
}

# P12 and Java Key Store
for P12 in `find . \( -name "*.jks" -o -name "*.p12" \) -not -path '*/target/*'`
do 
    echo "-------------"
    echo "STORE: ${P12}"
    keytool -list -keystore $P12 -storepass ${CHANGE_PASSWORD}
done | grep -e 'Alias\|Valid\|STORE\|-------------\|Serial'

create_kafka_ci

# EOF