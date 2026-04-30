#!/bin/sh
set -eu

if [ -z "${POSTGRES_MULTIPLE_DATABASES:-}" ]; then
  exit 0
fi

IFS=','
for db in $POSTGRES_MULTIPLE_DATABASES; do
  db="$(echo "$db" | xargs)"
  if [ -z "$db" ]; then
    continue
  fi

  echo "Creating database: $db"
  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    SELECT 'CREATE DATABASE "$db"'
    WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '$db')\gexec
EOSQL
done
