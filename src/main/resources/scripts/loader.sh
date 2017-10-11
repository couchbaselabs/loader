#!/usr/bin/env bash

function loader() {
  local BIN_DIR=$(dirname $0)
  local ROOT_DIR=$(dirname $BIN_DIR)
  local LIB_DIR=$ROOT_DIR/lib

  local FileName=$1
  shift

  local CLASSPATH=
  for JAR in $(ls -1 $LIB_DIR/*.jar)
  do
    CLASSPATH=$CLASSPATH:$JAR
  done
  java -cp $CLASSPATH com.couchbase.bigfun.BatchModeLoadParametersGeneratorEntry $* > $FileName 
  java -cp $CLASSPATH com.couchbase.bigfun.BatchModeLoaderEntry $FileName 
}

FileName=$1
shift

loader $FileName $*
