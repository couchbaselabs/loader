#!/usr/bin/env bash

function loader() {
  local BIN_DIR=$(dirname $0)
  local ROOT_DIR=$(dirname $BIN_DIR)
  local LIB_DIR=$ROOT_DIR/lib

  ClientN=$1
  shift
  TableSufix=$1
  shift
  Host=$1
  shift
  User=$1
  shift
  Password=$1
  shift
  Concurrent=$1
  shift
  Repeat=$1
  shift

  local CLASSPATH=
  for JAR in $(ls -1 $LIB_DIR/*.jar)
  do
    CLASSPATH=$CLASSPATH:$JAR
  done
  java -cp $CLASSPATH com.couchbase.bigfun.BatchModeLoadParametersGeneratorEntry -P ../socialGen/bigfundata -d gbook_users -k id -l 0 -h "" -u $User -p $Password -b "" -o query -ah $Host -nm $Concurrent -qf ./target/loader-1.0-SNAPSHOT-binary-assembly/bin/query.txt -nq $Repeat -qts $TableSufix $* > $TableSufix.$Host.$ClientN.query
  java -cp $CLASSPATH com.couchbase.bigfun.BatchModeLoaderEntry $TableSufix.$Host.$ClientN.query $TableSufix.$Host.$ClientN.query.result
}

ClientN=$1
shift
TableSufix=$1
shift
Host=$1
shift
User=$1
shift
Password=$1
shift
Concurrent=$1
shift
Repeat=$1
shift

loader $ClientN $TableSufix $Host $User $Password $Concurrent $Repeat $*
