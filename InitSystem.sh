#!/bin/bash
initialPort=10000
count=1
> Common/Sconfiguration.txt

num1=$1
num2=$2
serversNonBizantine=$((num1 - num2))

while [ $count -le $1 ]; do
  echo "$((count-1)) localhost $initialPort" >> Common/Sconfiguration.txt
  initialPort=$((initialPort+1))
  count=$((count+1))
done

count=1
while [ $count -le $serversNonBizantine ]; do
  id=$(printf "%d" $((count-1)))
  >Common/resources/S"$id"public.key
  >Server/resources/S"$id"private.key
  gnome-terminal -- bash -c "
    cd Server;
    bash -c 'mvn exec:java -Dexec.args=\"$id 0\"; bash'
    "
  count=$((count+1))
done

while [ $count -le $1 ]; do
  id=$(printf "%d" $((count-1)))
  >Common/resources/S"$id"public.key
  >Server/resources/S"$id"private.key
  gnome-terminal -- bash -c "
    cd Server;
    bash -c 'mvn exec:java -Dexec.args=\"$id 1\"; bash'
    "
  count=$((count+1))
done

initialPort=11000
count=1

sleep 10

while [ $count -le $3 ]; do
  id=$(printf "%d" $((count-1)))
  port=$(printf "%d" $((initialPort)))
  gnome-terminal -- bash -c "
    cd User;
    bash -c 'mvn exec:java -Dexec.args=\"$id $port $num1\"; bash'
    "
  >Common/resources/U"$id"public.key
  >User/resources/U"$id"private.key
  count=$((count+1))
  initialPort=$((initialPort+1))
done
