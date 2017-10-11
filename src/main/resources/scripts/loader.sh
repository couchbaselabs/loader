#!/usr/bin/env bash

function loader() {
  local BIN_DIR=$(dirname $0)
  local ROOT_DIR=$(dirname $BIN_DIR)
  local LIB_DIR=$ROOT_DIR/lib

  Action=$1
  shift
  TableName=$1
  shift
  KeyField=$1
  shift
  Host=$1
  shift
  Bucket=$1
  shift
  Password=$1
  shift

  local CLASSPATH=
  for JAR in $(ls -1 $LIB_DIR/*.jar)
  do
    CLASSPATH=$CLASSPATH:$JAR
  done
  java -cp $CLASSPATH com.couchbase.bigfun.BatchModeLoadParametersGeneratorEntry -P ../socialGen/bigfundata -d $TableName -k $KeyField -l 0 -h $Host -u $Bucket -p $Password -b $Bucket -o $Action $* > $TableName.$Action 
  java -cp $CLASSPATH com.couchbase.bigfun.BatchModeLoaderEntry $TableName.$Action $TableName.$Action.result
}

Action=$1
shift
TableName=$1
shift
KeyField=$1
shift
Host=$1
shift
Bucket=$1
shift
Password=$1
shift

loader $Action $TableName $KeyField $Host $Bucket $Password $*
