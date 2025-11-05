#!/usr/bin/env bash

SNAPSHOT_DIR="$SCRIPT_DIR/../resources/snapshots"

if [ ! -d "$SNAPSHOT_DIR" ]; then
    print_error "ERROR: Snapshot directory '$SNAPSHOT_DIR' could not be found."
    exit 1
fi

unapproved_files=$(find "$SNAPSHOT_DIR" -type f ! -name "*.approved.txt")

if [[ -n "$unapproved_files" ]]; then
    print_error "ERROR: All snapshot tests MUST be approved and passing before running the lifecycle tests to prevent any unexpected failures."
    exit 1
fi

#dbs=$(astra db list --token "$ASTRA_TOKEN" --env "$ASTRA_ENV" -o json)
#dbs_count=$(echo "$dbs" | jq '.data | length')
#
#if [[ $dbs_count -gt 3 ]]; then
#    print_error "ERROR: More than 3 databases found in the Astra account (found $dbs_count)."
#    print_error "ERROR: Please delete some databases to ensure a clean test environment."
#    print_error "ERROR: The test suite must be able to create two new ephemeral databases."
#    exit 1
#fi
