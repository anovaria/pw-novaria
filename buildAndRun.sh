#!/bin/sh
mvn clean package && docker build -t it.tss/projectwork .
docker rm -f projectwork || true && docker run -d -p 8080:8080 -p 4848:4848 --name projectwork it.tss/projectwork 
