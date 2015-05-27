#!/bin/bash

docker run --rm -it -p 4001:4001 -p 7001:7001 -v /var/etcd/:/data microbox/etcd:latest -name=foo
