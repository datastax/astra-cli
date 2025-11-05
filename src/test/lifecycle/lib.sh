#!/usr/bin/env bash

red="\033[0;31m"
dark_gray="\033[0;90m"
reset="\033[0m"

declare -A live_components
declare -A sweep_steps

function print_live_components() {
  if [ ${#live_components[@]} -eq 0 ]; then
    echo "No cleanup required"
    return
  fi

  print_error "ERROR: There may be leftover components which would require cleanup"
  for name in "${!live_components[@]}"; do
    print_error "---"
    print_error "Component: $name"

    if [ -n "${sweep_steps[$name]}" ]; then
      if eval "${sweep_steps[$name]}" > /dev/null 2>&1; then
        print_error "Swept: true"
      else
        print_error "Swept: false"
      fi
    else
      print_error "No sweep step defined"
    fi

    echo -e "${live_components[$name]}"
  done
  print_error "---"
}

trap print_live_components EXIT

function create_step() {
  if [ "$1" == "--no-cleanup" ]; then
    shift
    no_cleanup=true
  else
    no_cleanup=false
  fi

  echo "Creating component '$1'"
  echo -e "$dark_gray - ${2//"$ASTRA_TOKEN"/"\${token}"}$reset"

  if [ "$no_cleanup" = false ]; then
    live_components["$1"]="$red...$reset"
  fi

  eval "output=\$($2)"

  if [ "$no_cleanup" = false ]; then
    live_components["$1"]="$output"
  fi

  assert_contains "$output" "${@:3}"
}

function read_step() {
  echo "Reading component '$1'"
  echo -e "$dark_gray - ${2//"$ASTRA_TOKEN"/"\${token}"}$reset"
  eval "output=\$($2)"
  assert_contains "$output" "${@:3}"
}

function update_step() {
  echo "Updating component '$1'"
  echo -e "$dark_gray - ${2//"$ASTRA_TOKEN"/"\${token}"}$reset"
  eval "output=\$($2)"
  assert_contains "$output" "${@:3}"
}

function delete_step() {
  echo "Deleting component '$1'"
  echo -e "$dark_gray - ${2//"$ASTRA_TOKEN"/"\${token}"}$reset"
  eval "output=\$($2)"
  assert_contains "$output" "${@:3}"
  unset "live_components[$1]"
}

function sweep_step() {
  sweep_steps["$1"]="$2"
}

print_stacktrace() {
    echo "Stack trace (most recent call last):"
    local i
    for ((i=${#FUNCNAME[@]}-1; i>=1; i--)); do
        local func="${FUNCNAME[$i]}"
        local line="${BASH_LINENO[$((i-1))]}"
        local src="${BASH_SOURCE[$i]}"
        echo "  at ${func}() in ${src}:${line}"
    done
    echo ""
}

assert_contains() {
    local output="$1"
    shift
    local all_passed=true

    for expected in "$@"; do
        if [[ "$output" != *"$expected"* ]]; then
            print_error "ERROR: Output does not contain '$expected'"
            print_error "---- Actual Output ----"
            print_error "$output"
            print_error "------------------------"
            print_stacktrace
            all_passed=false
        fi
    done

    $all_passed || return 1
}

print_error() {
  echo -e "$red$1$reset"
}
