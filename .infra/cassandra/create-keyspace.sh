#!/bin/sh
set -eu

until cqlsh cassandra 9042 -e "DESCRIBE KEYSPACES" >/dev/null 2>&1; do
  echo "Waiting for Cassandra..."
  sleep 5
done

echo "Ensuring Cassandra keyspace exists: ${CASSANDRA_KEYSPACE}"
cqlsh cassandra 9042 <<-EOSQL
  CREATE KEYSPACE IF NOT EXISTS ${CASSANDRA_KEYSPACE}
  WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};
EOSQL
