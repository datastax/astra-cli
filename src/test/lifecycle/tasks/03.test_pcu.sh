#!/usr/bin/env bash

pcu1="ct_pcu_1"
pcu2="ct_pcu_2"

create_step "$pcu1" "astra pcu create $pcu1 -c $ASTRA_CLOUD -r $ASTRA_REGION" \
  "$pcu1" "has been created with id"

create_step "$pcu2" "astra pcu create $pcu2 -c $ASTRA_CLOUD -r $ASTRA_REGION" \
  "$pcu2" "has been created with id"

read_step "$pcu1+$pcu2" "astra pcu list" \
  "$pcu1" "$pcu2" "$ASTRA_CLOUD" "$ASTRA_REGION" "■" "CREATED"

read_step "$pcu1" "astra pcu get $pcu1" \
  "$pcu1" "$ASTRA_CLOUD" "$ASTRA_REGION" " 1 " " 0 " "CREATED"

sweep_step "$pcu1" "astra pcu delete $pcu1 --yes"
sweep_step "$pcu2" "astra pcu delete $pcu2 --yes"
