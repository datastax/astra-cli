#!/usr/bin/env bash

PCU="ct_pcu"

if [ -z "$ASTRA_RUN_PCU_LIFECYCLE_TESTS" ] || [ "$ASTRA_RUN_PCU_LIFECYCLE_TESTS" != "true" ]; then
  echo "Skipping PCU lifecycle tests. Set ASTRA_RUN_PCU_LIFECYCLE_TESTS=true to enable."
  exit 0
fi

sweeper_for "$PCU" "astra pcu delete $PCU --yes"

create_step "$PCU" "astra pcu create $PCU -c $ASTRA_CLOUD -r $ASTRA_REGION" \
  "$PCU" "has been created with id"

read_step "$PCU" "astra pcu list" \
  "$PCU" "$ASTRA_CLOUD" "$ASTRA_REGION" "■" "CREATED"

read_step "$PCU" "astra pcu get $PCU" \
  "$PCU" "$ASTRA_CLOUD" "$ASTRA_REGION" " 1 " " 0 " "CREATED"

update_step "$PCU" "astra pcu associate $PCU $DB1" \
  "has been associated"

until res=$(astra pcu status "$PCU") && echo "$res" | grep -q "ACTIVE"; do
  echo "Waiting for $PCU to become ACTIVE... (currently $(echo "$res" | jq -r '.data'))"
  sleep 15
done

read_step "$PCU" "astra pcu list-associations $PCU" \
  "$PCU" "$ASTRA_CLOUD" "$ASTRA_REGION" " 1 " " 1 " "ACTIVE"

delete_step "$PCU" "astra pcu delete $PCU --yes" \
  "$PCU" "has been deleted"
