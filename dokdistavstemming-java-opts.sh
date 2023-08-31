#!/usr/bin/env sh

#JAVA_OPTS="${JAVA_OPTS} -Djavax.net.ssl.keyStore=${DOKDISTAVSTEMMINGCERT_KEYSTORE}"
#JAVA_OPTS="${JAVA_OPTS} -Djavax.net.ssl.keyStoreType=jks"
JAVA_OPTS="${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom -Dspring.profiles.active=nais"
JAVA_OPTS="${JAVA_OPTS} -Xmx2048m"

export JAVA_OPTS