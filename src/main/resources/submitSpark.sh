#! /bin/bash

runTime=`date '+%Y%m%d-%H%M%S'`
echo "Script trigerred at $runTime"

nohup spark-submit \
 --deploy-mode client --master yarn \
 --executor-cores 4 --executor-memory 6g --conf spark.yarn.executor.memoryOverhead=1G --conf spark.yarn.driver.memoryOverhead=1G  \
 --jars $(echo /home/sudheerpalyam/jars/*.jar | tr ' ' ',') \
 --class au.com.thoughtworks.assessment.spark.streaming.KafkaStructuredStreaming \
 /home/sudheerpalyam/jars/stock_stream_processing_2.11-0.1.jar \
 --isGlue false \
 --mode  yarn >> ../logs/stock-spark-$runTime.log &

echo "Log file  ../logs/stock-spark-$runTime.log "
echo "Script finished at " `date '+%Y%m%d-%H%M%S'`
