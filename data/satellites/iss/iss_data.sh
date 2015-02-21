#!/bin/bash


if [ -z "$1" ]; then
	echo "$0 reading_increment_in_seconds num_readings"
	exit 1
fi

if [ -z "$2" ]; then
	echo "$0 reading_increment_in_seconds num_readings"
	exit 1
fi

let END="$2"

while ((i<END)); do
	curl --insecure https://api.wheretheiss.at/v1/satellites/25544
	echo $str
	sleep "$1"
	let i++
done
