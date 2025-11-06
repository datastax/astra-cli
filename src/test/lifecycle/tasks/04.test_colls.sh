#!/usr/bin/env bash

COLL1="ct_coll_1"
COLL2="ct_coll_2"

create_step --no-cleanup "$COLL1" "astra db create-collection $DB1 -c $COLL1 --if-not-exists --default-id uuidv6" \
  "$COLL1" "has been created"

create_step --no-cleanup "$COLL1" "astra db create-collection $DB1 -c $COLL1 --if-not-exists --default-id uuidv6" \
  "$COLL1" "already exists"

create_step --no-cleanup "$COLL2" "astra db create-collection $DB2 -c $COLL2 -k other_keyspace -d 1024" \
  "$COLL2" "has been created"

read_step "$COLL1" "astra db list-collections $DB1 -ao csv" \
  "OK,,default_keyspace,$COLL1"

read_step "$COLL2" "astra db list-collections $DB2 -ao csv" \
  "OK,,other_keyspace,$COLL2"

update_step "$COLL1" "astra db truncate-collection $DB1 -c $COLL1" \
  "default_keyspace.$COLL1 has been truncated"

update_step "$COLL2" "astra db truncate-collection $DB2 -c $COLL2" \
  "other_keyspace.$COLL2 has been truncated"

astra db describe-collection "$DB1" -c "$COLL1"
astra db describe-collection "$DB2" -c "$COLL2"

delete_step "$COLL1" "astra db delete-collection $DB1 -c $COLL1 --if-exists" \
  "has been deleted"

delete_step "$COLL1" "astra db delete-collection $DB1 -c $COLL1 --if-exists" \
  "does not exist"

delete_step "$COLL2" "astra db delete-collection $DB2 -c $COLL2" \
  "has been deleted"
