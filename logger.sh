#!/bin/bash
COUNT=0
while [ true ]; do
	echo `date` - $COUNT
	COUNT=$((COUNT+1))
	sleep 1
done 


