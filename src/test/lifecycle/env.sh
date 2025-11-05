#!/usr/bin/env bash

source .env

if [ -z "${ASTRA_TOKEN:-}" ]; then
  print_error "Error: ASTRA_TOKEN is not set. Please set it in the .env file."
  exit 1
fi

if [ -z "${ASTRA_CLOUD:-}" ]; then
  print_error "Error: ASTRA_CLOUD is not set. Please set it in the .env file."
  exit 1
fi

if [ -z "${ASTRA_REGION:-}" ]; then
  print_error "Error: ASTRA_REGION is not set. Please set it in the .env file."
  exit 1
fi

export ASTRA_TOKEN
export ASTRA_ENV="${ASTRA_ENV:-prod}"

VALID_CP=("AWS" "AZURE" "GCP")
if [[ ! " ${VALID_CP[*]} " =~  ${ASTRA_CLOUD}  ]]; then
  print_error "Error: ASTRA_CLOUD must be one of: ${VALID_CP[*]}."
  exit 1
fi

if [[ "$ASTRA_REGION" != "${ASTRA_REGION,,}" ]]; then
  print_error "Error: ASTRA_REGION must be in lowercase."
  exit 1
fi

export ASTRA_CLOUD
export ASTRA_REGION

export ASTRA_HOME="${ASTRA_HOME:-.cli_tests_temp/lifecycle}"
export ASTRARC="${ASTRARC:-.cli_tests_temp/.astrarc.lifecycle}"

export ASTRA_IGNORE_MULTIPLE_PATHS=true
export ASTRA_IGNORE_BETA_WARNINGS=true

export ASTRA_EXE_PATH="${ASTRA_EXE_PATH:-build/native/nativeCompile/astra}"

if [ ! -f "$ASTRA_EXE_PATH" ]; then
  print_error "Error: Astra CLI executable not found at \$ASTRA_EXE_PATH. Please build the project first."
  exit 1
fi

astra() {
  "$ASTRA_EXE_PATH" "$@"
}

rm -rf "$ASTRA_HOME"
rm -f "$ASTRARC"
