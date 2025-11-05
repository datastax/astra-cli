#!/usr/bin/env bash

db1="ct_db_1"
db2="ct_db_2"

create_step "$db1" "astra db create $db1 -r $ASTRA_REGION --async" \
  "$db1" "has been created" "currently has status"

create_step "$db2" "astra db create $db2 -r $ASTRA_REGION --async -k other_keyspace" \
  "$db2" "has been created" "currently has status"

update_step "$db1" "astra db resume $db1" \
  "but is now active after waiting"

update_step "$db2" "astra db resume $db2" \
  "but is now active after waiting"

sweep_step "$db1" "astra db delete $db1 --yes --async"
sweep_step "$db2" "astra db delete $db2 --yes --async"
