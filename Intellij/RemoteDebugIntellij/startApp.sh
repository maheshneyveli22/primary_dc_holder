#!/bin/bash

ME=`whoami`

# function to get the applications process ID
getAppPID()
{
    APP_PID=`ps -fu $ME | grep "DappName=$ME" | grep -v "grep " | awk '{print $2}'`
}

# configurable attributes from app metadata
start_sleep="120"

# check if the app is running
getAppPID
if [ "${APP_PID}" != '' ] ; then
  echo "Application (pid=${APP_PID}) is already running."
  exit
fi

#Adding -Dhttp-agent=java/11.0.8 in java options to spoof user agent
APP_HOME="/prod/jvm/${ME}/"
APP_LOG="${APP_HOME}logs/${ME}.log"
APP_JAR="${APP_HOME}${ME}.jar"
JAVA_OPTS="-XX:+HeapDumpOnOutOfMemoryError -Xmx2G -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -Djavax.net.ssl.trustStore=${APP_HOME}conf/expd-ca-truststore.jks -Djavax.net.ssl.trustStorePassword=expeditors  --add-exports=java.xml/com.sun.org.apache.xerces.internal.parsers=ALL-UNNAMED --add-exports=java.xml/com.sun.org.apache.xerces.internal.dom=ALL-UNNAMED --add-exports=java.xml/com.sun.org.apache.xerces.internal.jaxp=ALL-UNNAMED --add-exports=java.xml/com.sun.org.apache.xerces.internal.util=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.util.concurrent=ALL-UNNAMED -Dspring.profiles.active=dev -Dhttp-agent=java/17.0.6"
JAVA_RT="/opt/java-17/bin/java"
PROGRAM_ARGS="--spring.config.location=${APP_HOME}conf/"

echo "Starting application..."
echo "$JAVA_RT $ME $JAVA_OPTS $APP_JAR $PROGRAM_ARGS $APP_LOG"
mv ${APP_LOG} ${APP_LOG}.1  >/dev/null 2>&1
nohup $JAVA_RT -DappName=$ME -DAPP_HOME=$APP_HOME $JAVA_OPTS -jar $APP_JAR $PROGRAM_ARGS >$APP_LOG 2>&1 &

# sleep and check if app is running
sleep $start_sleep
getAppPID

if [ "${APP_PID}" != '' ] ; then
  echo "Application (pid=${APP_PID}) is running!."
else
  echo "Application did not start!"
  cat ${APP_LOG}
  exit 1
fi
