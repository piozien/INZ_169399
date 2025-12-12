#!/bin/sh
exec java -Xmx350m -Xss512k -Dserver.port=${PORT} -jar app.jar