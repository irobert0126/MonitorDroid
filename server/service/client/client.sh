#!/usr/bin/env bash

PASS="MTp0ZXN0X3Rva2VuCg=="
PUSH_LOC="/storage/emulated/0/Android/data/com.example.zhaoyan.remote_service/cache"
IP="35.174.171.219:5000"

curl -X POST -H "Authorization: Basic $PASS" -H "Content-Type: application/json" -d @apk.json http://$IP/conf/v1.0/upload

curl -H "Authorization: Basic $PASS" http://$IP/conf/v1.0/download > config.enc

adb push config.enc $PUSH_LOC/config.enc