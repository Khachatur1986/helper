#!/bin/bash

ifconfig wlan0 down
macchanger -r wlan0
iwconfig wlan0 mode monitor
ifconfig wlan0 up
iwconfig wlan0 | grep Mode
