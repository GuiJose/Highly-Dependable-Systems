#!/bin/bash
initialPort=11000
count=1

while [ $count -le $1 ]; do
  id=$(printf "%d" $((count-1)))
  port=$(printf "%d" $((initialPort))) 
  gnome-terminal \
  -e "bash -c 'mvn exec:java -Dexec.args=\"$id $port\"; bash'"
  >../Common/resources/U"$id"public.key
  >resources/U"$id"private.key
  count=$((count+1))
  initialPort=$((initialPort+1))
done