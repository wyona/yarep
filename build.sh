#!/bin/sh

echo "INFO: Build Yarep ..."

# ----- Parameters

JAVA_HOME_MACOSX=/System/Library/Frameworks/JavaVM.framework/Home

# ----- Check for JAVA_HOME
JAVA_HOME="$JAVA_HOME"
if [ "$JAVA_HOME" = "" ];then
  echo "ERROR: No JAVA_HOME set!"
  echo "       Have you installed JDK (Java Development Kit)? If so, then set JAVA_HOME ..."
  echo ""
  echo "       Mac OS X : Depending on the shell you're using either use"
  echo "                  export JAVA_HOME=$JAVA_HOME_MACOSX"
  echo "                  or"
  echo "                  setenv JAVA_HOME $JAVA_HOME_MACOSX"
  echo "       Linux   : export JAVA_HOME=/usr/local/j2sdk-..."
  echo "       Windows : Click Start ..."
  exit 1
fi

# ----- Check Java version
# TODO: ....

# ----- Set Environment Variables
ORIGINAL_ANT_HOME=$ANT_HOME
unset ANT_HOME
ANT_HOME=$PWD/tools/apache-ant
#echo $ANT_HOME
OUR_ANT="ant -lib tools/apache-ant_extras"

ORIGINAL_PATH=$PATH
PATH=$PWD/tools/maven-2.0.4/bin:$ANT_HOME/bin:$PATH
#echo $PATH

# ----- Build Yarep ...
#mvn --version
$OUR_ANT -version
$OUR_ANT -f build.xml $@

# ----- Reset Environment Variables
ANT_HOME=$ORIGINAL_ANT_HOME
#echo $ANT_HOME
PATH=$ORIGINAL_PATH
#echo $PATH
