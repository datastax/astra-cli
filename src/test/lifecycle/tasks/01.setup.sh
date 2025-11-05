#!/usr/bin/env bash

profile="ct_profile"

create_step --no-cleanup "$profile" "astra config create $profile --token $ASTRA_TOKEN --env $ASTRA_ENV --default" \
  "$profile" "successfully created" "is now the default profile"

read_step "$profile" "astra config list" \
  "$profile (in use)"
