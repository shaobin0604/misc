#!/bin/bash

protoc --proto_path=protos --java_out=src protos/app_info.proto
