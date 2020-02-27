#!/usr/bin/env sh

if test -f /var/run/secrets/nais.io/dokdistavstemming/username;
then
    echo "Setting SERVICEUSER_USERNAME"
    export SERVICEUSER_USERNAME=$(cat /var/run/secrets/nais.io/srvdokdistavstemming/username)
fi
if test -f /var/run/secrets/nais.io/srvdokdistavstemming/password;
then
    echo "Setting SERVICEUSER_PASSWORD"
    export SERVICEUSER_***passord=gammelt_passord***)
fi


if test -f /var/run/secrets/nais.io/srvjiradokdistavstemming/username;
then
    echo "Setting JIRA_USERNAME"
    export JIRA_USERNAME=$(cat /var/run/secrets/nais.io/srvjiradokdistavstemming/username)
fi

if test -f /var/run/secrets/nais.io/srvjiradokdistavstemming/password;
then
    echo "Setting JIRA_PASSWORD"
    export JIRA_***passord=gammelt_passord***)
fi



