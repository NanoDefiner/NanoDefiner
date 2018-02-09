#!/bin/bash
echo "Killing process 'java -jar NanoDefiner.war'"
processId=`ps aux | grep "java -jar NanoDefiner.war" | grep -v "grep" | awk '{print $2}'`
if [[ $processId != "" ]]
then
  kill $processId
else
  echo "Process not found, is it running?"
fi

echo "Done, press enter to close..."
read
