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

if test -f /secrets/serviceuser/srvdokdistavstemming/username;
then
    echo "Setting serviceuser_username"
    export  serviceuser_username=$(cat /secrets/serviceuser/srvdokdistavstemming/username)
fi
if test -f /secrets/serviceuser/srvdokdistavstemming/password;
then
    echo "Setting serviceuser_password"
    export  serviceuser_password=$(cat /secrets/serviceuser/srvdokdistavstemming/password)
fi


