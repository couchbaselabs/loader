HOST=$1
shift
BUCKET=$1
shift
PASSWORD=$1
shift
INSTANCE=$1
shift
DATAPATH=$1
shift
INTERVAL_MS=$1
shift
DURATION_SEC=$1
shift
GUDOC=$1
shift
GMDOC=$1
shift
CMDOC=$1
shift
GUARG=$1
shift
GMARG=$1
shift
CMARG=$1
shift

DIRNAME=`dirname $0`

bash $DIRNAME/mixloader.sh $DATAPATH $INSTANCE $GUDOC gbook_users id $HOST $BUCKET $PASSWORD $INTERVAL_MS $DURATION_SEC $GUARG > mixloader_gbook_users.$HOST.$BUCKET.$INSTANCE.log 2> mixloader_gbook_users.$HOST.$BUCKET.$INSTANCE.err &
pids+=($!)
bash $DIRNAME/mixloader.sh $DATAPATH $INSTANCE $GMDOC gbook_messages message_id $HOST $BUCKET $PASSWORD $INTERVAL_MS $DURATION_SEC $GMARG > mixloader_gbook_messages.$HOST.$BUCKET.$INSTANCE.log 2> mixloader_gbook_messages.$HOST.$BUCKET.$INSTANCE.err &
pids+=($!)
bash $DIRNAME/mixloader.sh $DATAPATH $INSTANCE $CMDOC chirp_messages chirpid $HOST $BUCKET $PASSWORD $INTERVAL_MS $DURATION_SEC $CMARG > mixloader_chirp_messages.$HOST.$BUCKET.$INSTANCE.log 2> mixloader_chirp_messages.$HOST.$BUCKET.$INSTANCE.err &
pids+=($!)

echo "waiting processes"

result=0
for pid in "${pids[@]}"; do
   wait "$pid"
   if [ $? -eq 0 ]
   then
      echo "it worked"
   else
      echo "it failed"
      result=1
   fi
done

echo "finish waiting processes"

exit $result
