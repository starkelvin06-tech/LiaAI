#!/bin/sh

# Gradle startup script for UN*X
#
# Attempt to find and set JAVA_HOME if not set.
if [ -z "$JAVA_HOME" ] ; then
    if [ -n "$JDK_HOME" ] ; then
        JAVA_HOME="$JDK_HOME"
    fi
fi

# Resolve links: $0 may be a link
PRG="$0"
while [ -h "$PRG" ] ; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
        PRG="$link"
    else
        PRG=`dirname "$PRG"`"/$link"
    fi
done
SAVED="`pwd`"
cd "`dirname \"$PRG\"`/" >/dev/null
APP_HOME="`pwd -P`"
cd "$SAVED" >/dev/null

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        JAVACMD="$JAVA_HOME/jre/sh/java"
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
else
    JAVACMD="java"
fi

# Download wrapper jar if missing
if [ ! -f "$CLASSPATH" ]; then
    echo "gradle-wrapper.jar not found, downloading..."
    mkdir -p "$APP_HOME/gradle/wrapper"
    WRAPPER_VERSION="8.11.1"
    WRAPPER_URL="https://raw.githubusercontent.com/gradle/gradle/v${WRAPPER_VERSION}/gradle/wrapper/gradle-wrapper.jar"
    if command -v curl >/dev/null 2>&1; then
        curl -sSL "$WRAPPER_URL" -o "$CLASSPATH"
    elif command -v wget >/dev/null 2>&1; then
        wget -q -O "$CLASSPATH" "$WRAPPER_URL"
    fi
fi

# Execute Gradle
exec "$JAVACMD" "-Dorg.gradle.appname=gradlew" -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
