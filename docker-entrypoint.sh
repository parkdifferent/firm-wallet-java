#!/bin/sh

case "$SERVICE_TYPE" in
    "access")
        exec java $JAVA_OPTS -jar /app/access.jar
        ;;
    "storage")
        exec java $JAVA_OPTS -jar /app/storage.jar
        ;;
    "query")
        exec java $JAVA_OPTS -jar /app/query.jar
        ;;
    *)
        echo "Unknown service type: $SERVICE_TYPE"
        exit 1
        ;;
esac
