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



