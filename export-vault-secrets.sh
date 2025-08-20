#!/usr/bin/env sh

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

