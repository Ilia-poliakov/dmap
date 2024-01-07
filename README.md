![Build Status](https://github.com/ilia-poliakov/dmap/actions/workflows/build.yml/badge.svg)
[![codecov](https://codecov.io/gh/ilia-poliakov/dmap/branch/master/graph/badge.svg?token=BEF5jFY7wv)](https://codecov.io/gh/ilia-poliakov/dmap)
----

## DMap
Distributed in-memory key-value storage with replication based on [Raft](https://raft.github.io) consensus algorithm.

## Key Features
* There are the following types of in-memory K-V storages:
   - Java ConcurrentHashMap
   - Off-heap hash map
   - Off-heap with optimistic read
* Raft consensus algorithm
* Write ahead log for recovering after restarts

### How to run
The simplest way to run it is run in docker-compose
1. ```mvn clean install```
2. ```cd deploy/docker```
3. ```docker compose up -d```