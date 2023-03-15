#!/bin/bash
initialPort=10000
count=1
> ../Common/Sconfiguration.txt

num1=$1
num2=$2
serversNonBizantine=$((num1 - num2))

while [ $count -le $1 ]; do
  echo "$((count-1)) localhost $initialPort" >> ../Common/Sconfiguration.txt
  initialPort=$((initialPort+1))
  count=$((count+1))
done

count=1
while [ $count -le $serversNonBizantine ]; do
  id=$(printf "%d" $((count-1)))
  gnome-terminal \
  -e "bash -c 'mvn exec:java -Dexec.args=\"$id 0\"; bash'"
   >../Common/resources/S"$id"public.key
   >resources/S"$id"private.key
  count=$((count+1))
done

while [ $count -le $1 ]; do
  id=$(printf "%d" $((count-1)))
  gnome-terminal \
  -e "bash -c 'mvn exec:java -Dexec.args=\"$id 1\"; bash'"
   >../Common/resources/S"$id"public.key
   >resources/S"$id"private.key
  count=$((count+1))
done