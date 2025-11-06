#!/usr/bin/env bash

DB1_NAME="ct_db_11"
DB2_NAME="ct_db_22"

sweeper_for "$DB1_NAME" "astra db delete $DB1_NAME --yes --async"
sweeper_for "$DB2_NAME" "astra db delete $DB2_NAME --yes --async"

create_step "$DB1_NAME" "astra db create $DB1_NAME -r $ASTRA_REGION --async" \
  "$DB1_NAME" "has been created" "currently has status"

create_step "$DB2_NAME" "astra db create $DB2_NAME -r $ASTRA_REGION --async -k other_keyspace" \
  "$DB2_NAME" "has been created" "currently has status"

DB1=$(astra db get "$DB1_NAME" --key id)
DB2=$(astra db get "$DB2_NAME" --key id)

sweeper_for "$DB1_NAME" "astra db delete $DB1 --yes --async"
sweeper_for "$DB2_NAME" "astra db delete $DB2 --yes --async"

update_step "$DB1_NAME" "astra db resume $DB1 --spinner" \
  "but is now active after waiting"

update_step "$DB2_NAME" "astra db resume $DB2 --spinner" \
  "active"

read_step "$DB1_NAME+$DB2_NAME" "astra db list" \
  "$DB1_NAME" "$DB2_NAME" "$DB1" "$DB2" "[0] $ASTRA_REGION" "$ASTRA_CLOUD" "■" "ACTIVE"

for DB in "$DB1_NAME" "$DB2_NAME"; do
  read_step "$DB" "astra db get $DB" \
    "Name             │ $DB" \
    "ID               │ $([ "$DB" = "$DB1_NAME" ] && echo "$DB1" || echo "$DB2")" \
    "Cloud Provider   │ $ASTRA_CLOUD" \
    "Region           │ $ASTRA_REGION" \
    "Status           │ ACTIVE" \
    "Vector           │ Enabled" \
    "Default Keyspace │ $([ "$DB" = "$DB1_NAME" ] && echo "default_keyspace" || echo "other_keyspace")" \
    "Keyspaces        │ [0] $([ "$DB" = "$DB1_NAME" ] && echo "default_keyspace" || echo "other_keyspace")" \
    "Regions          │ [0] $ASTRA_REGION"

  read_step "$DB" "astra db status $DB" \
    "Database '$DB' is 'ACTIVE'"

  read_step "$DB" "astra db list-regions $DB" \
    "$ASTRA_REGION" "serverless" "ONLINE"
done
