#!/bin/sh
set -e

# Create log directory
mkdir -p /app/logs

# Start nginx in background
nginx -c /etc/nginx/nginx.conf -g "daemon off;" &
NGINX_PID=$!

# Start Java backend
JAVA_OPTS="${JAVA_OPTS:--Xms512m -Xmx1024m -XX:+UseG1GC}"
exec java $JAVA_OPTS -jar /app/phoenix-admin.jar --log.path=/app/logs
