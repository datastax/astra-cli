#!/usr/bin/env bash

DB1="ct_db_1"
DB2="ct_db_2"

sweeper_for "$DB1" "astra db delete $DB1 --yes --async"
sweeper_for "$DB2" "astra db delete $DB2 --yes --async"

create_step "$DB1" "astra db create $DB1 -r $ASTRA_REGION --async" \
  "$DB1" "has been created" "currently has status"

create_step "$DB2" "astra db create $DB2 -r $ASTRA_REGION --async -k other_keyspace" \
  "$DB2" "has been created" "currently has status"

DB1_ID=$(astra db get "$DB1" --key id)
DB2_ID=$(astra db get "$DB2" --key id)

update_step "$DB1" "astra db resume $DB1_ID" \
  "but is now active after waiting"

update_step "$DB2" "astra db resume $DB2_ID" \
  "but is now active after waiting"
