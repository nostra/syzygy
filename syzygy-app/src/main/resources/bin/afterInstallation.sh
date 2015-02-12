#!/bin/bash

# Perform tasks which are not performed (easily) by the
# app assembler

executeDirectory=`dirname $0`
# Need to change to the correct directory in order to 
# get relative paths to be correct
echo "Changing directory to: " ${executeDirectory}
cd ${executeDirectory}

# Top level directory
cd ..
rm -f app/*-app-*.jar
rm -f app/slf4j-api-*.jar
rm -f */placeholder.txt

echo "###############################"
echo "## Start Dropwizard app with: #"
echo "export DROPWIZARD_HOME=`pwd`"
echo "export DROPWIZARD_CONF=\$DROPWIZARD_HOME/etc/dropwizard.yml"
echo "export TMP=\$DROPWIZARD_HOME/tmp"
echo "export TMPDIR=\$TMP"
echo "export DROPWIZARD_RUN=\$TMP"
echo 'export JAVA_OPTIONS="-server -Djava.awt.headless=true -Xmx256m -Xms128m -XX:PermSize=128m -XX:MaxPermSize=256m -XX:NewSize=64m -XX:MaxNewSize=128m"'

echo "./dropwizard.sh start # In order to background the process."
