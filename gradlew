#!/bin/sh

APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd -P) || exit
CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar
DEFAULT_JVM_OPTS="-Xmx64m -Xms64m"

if [ -z "$JAVA_HOME" ]; then
    JAVA_COMMAND=$(command -v java 2>/dev/null)
    if [ -n "$JAVA_COMMAND" ]; then
        JAVA_HOME=$(CDPATH= cd -- "$(dirname -- "$JAVA_COMMAND")/.." && pwd -P)
    else
        echo "Java 17 or newer is required. Set JAVA_HOME before running Gradle." >&2
        exit 1
    fi
fi

exec "$JAVA_HOME/bin/java" $DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS "-Dorg.gradle.appname=$(basename "$0")" -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
