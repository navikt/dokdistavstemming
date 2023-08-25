#!/usr/bin/env sh

if test -f /var/run/secrets/nais.io/srvjiradokdistavstemming/username;
then
    echo "Setting dokdistavstemming_jira_username"
    export dokdistavstemming_jira_username=$(cat /var/run/secrets/nais.io/srvjiradokdistavstemming/username)
fi

if test -f /var/run/secrets/nais.io/srvjiradokdistavstemming/password;
then
    echo "Setting dokdistavstemming_jira_password"
    export dokdistavstemming_jira_password=$(cat /var/run/secrets/nais.io/srvjiradokdistavstemming/password)
fi

if test -f /var/run/secrets/nais.io/srvdokdistavstemming/username;
then
    echo "Setting serviceuser_username"
    export  serviceuser_username=$(cat /var/run/secrets/nais.io/srvdokdistavstemming/username)
fi
if test -f /var/run/secrets/nais.io/srvdokdistavstemming/password;
then
    echo "Setting serviceuser_password"
    export  serviceuser_password=$(cat /var/run/secrets/nais.io/srvdokdistavstemming/password)
fi

if test -f /var/run/secrets/nais.io/certificate/srvdokdistavstemming/keystore
then
    echo "Setting DOKDISTAVSTEMMINGCERT_KEYSTORE"
    CERT_PATH='/var/run/secrets/nais.io/certificate/srvdokdistavstemming/keystore-extracted'
    openssl base64 -d -A -in /var/run/secrets/nais.io/certificate/srvdokdistavstemming/keystore -out $CERT_PATH
    export DOKDISTAVSTEMMINGCERT_KEYSTORE=$CERT_PATH
fi

if test -f /var/run/secrets/nais.io/certificate/srvdokdistavstemming/keystorealias
then
    echo "Setting DOKDISTAVSTEMMINGCERT_KEYSTOREALIAS"
    export DOKDISTAVSTEMMINGCERT_KEYSTOREALIAS=$(cat /var/run/secrets/nais.io/certificate/srvdokdistavstemming/keystorealias)
fi

if test -f /var/run/secrets/nais.io/certificate/srvdokdistavstemming/keystorepassword
then
    echo "Setting DOKDISTAVSTEMMINGCERT_PASSWORD"
    export DOKDISTAVSTEMMINGCERT_PASSWORD=$(cat /var/run/secrets/nais.io/certificate/srvdokdistavstemming/keystorepassword)
fi


