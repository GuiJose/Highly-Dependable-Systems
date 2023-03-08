#!/bin/bash
initialPort=10000
count=1
> ../Common/Sconfiguration.txt

while [ $count -le $1 ]; do
  echo "$((count-1)) localhost $initialPort" >> ../Common/Sconfiguration.txt
  initialPort=$((initialPort+1))
  count=$((count+1))
done

count=1
while [ $count -le $1 ]; do
  id=$(printf "%d" $((count-1)))
  gnome-terminal \
  -e "bash -c 'mvn exec:java -Dexec.args="$id"; bash'"
  count=$((count+1))
done