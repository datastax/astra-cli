#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TASKS_DIR="$SCRIPT_DIR/tasks"

source "$SCRIPT_DIR/env.sh"
source "$SCRIPT_DIR/lib.sh"
source "$SCRIPT_DIR/precheck.sh"

source "$TASKS_DIR/01.setup.sh"
source "$TASKS_DIR/02.test_dbs.sh"
source "$TASKS_DIR/03.test_pcu.sh"
source "$TASKS_DIR/04.test_colls_and_tables.sh"
#source "$TASKS_DIR/05.test_streaming.sh"
