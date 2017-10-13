#!/usr/bin/env bash

function mixloader() {
  local BIN_DIR=$(dirname $0)
  local ROOT_DIR=$(dirname $BIN_DIR)
  local LIB_DIR=$ROOT_DIR/lib

  ClientN=$1
  shift
  DocNumber=$1
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
  Interval=$1
  shift
  Duration=$1
  shift

  local CLASSPATH=
  for JAR in $(ls -1 $LIB_DIR/*.jar)
  do
    CLASSPATH=$CLASSPATH:$JAR
  done
  java -cp $CLASSPATH com.couchbase.bigfun.MixModeLoadParametersGeneratorEntry -P ../socialGen/bigfundata -d $TableName -k $KeyField -l $DocNumber -h $Host -u $Bucket -p $Password -b $Bucket -iv $Interval -du $Duration $* > $TableName.$Host.$Bucket.$ClientN.mixload 
  java -cp $CLASSPATH com.couchbase.bigfun.MixModeLoaderEntry $TableName.$Host.$Bucket.$ClientN.mixload $TableName.$Host.$Bucket.$ClientN.mixload.result
}

ClientN=$1
shift
DocNumber=$1
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
Interval=$1
shift
Duration=$1
shift

mixloader $ClientN $DocNumber $TableName $KeyField $Host $Bucket $Password $Interval $Duration $*
