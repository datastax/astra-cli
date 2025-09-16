#!/bin/sh

SNAPSHOT_DIR="src/test/resources/snapshots"

if command -v difft >/dev/null 2>&1; then
  DIFF_TOOL="difft"
else
  DIFF_TOOL="diff -u"
fi

if command -v bat >/dev/null 2>&1; then
  CAT_TOOL="bat -n --paging=never"
else
  CAT_TOOL="cat -n"
fi

capitalize_first() {
  first_word=$(echo "$1" | awk '{print $1}')
  rest=$(echo "$1" | cut -d' ' -f2-)
  echo "$(echo "$first_word" | awk '{print toupper(substr($0,1,1)) tolower(substr($0,2))}') $rest"
}

find "$SNAPSHOT_DIR" -type f -name "*.received.txt" | while IFS= read -r received; do
  approved=$(echo "$received" | sed 's/\.received\.txt$/.approved.txt/')

  base_name=$(basename "$received" .received.txt)
  title_raw=$(echo "$base_name" | sed -E 's/_/ /g; s/\.(human|json|csv)//')
  title=$(capitalize_first "$title_raw")

  case "$base_name" in
    *.human) title_suffix="(Human)" ;;
    *.json)  title_suffix="(JSON)" ;;
    *.csv)   title_suffix="(CSV)" ;;
    *)       title_suffix="" ;;
  esac

  title="$title $title_suffix"

  echo   "============================================================"
  echo   "Comparing:"
  echo   "  File: $received"
  printf "  Title: \033[1m%s\033[0m\n" "$title"
  echo   "------------------------------------------------------------"

  if [ ! -f "$approved" ]; then
    sh -c "$CAT_TOOL \"$received\"" || true
  else
    sh -c "$DIFF_TOOL \"$approved\" \"$received\"" || true
  fi

  while :; do
    printf "Accept received as approved? [Y]es / [n]o / [d]elete: "
    if ! IFS= read -r ans < /dev/tty; then
      exit 1
    fi
    case "$ans" in
      n|N )
        echo "❌ Skipped"
        break
        ;;
      d|D )
        rm "$received"
        echo "🗑️ Deleted: $received"
        break
        ;;
      * )
        mv "$received" "$approved"
        echo "✅ Updated: $approved"
        break
        ;;
    esac
  done
  echo
done

echo "🎉 Done reviewing snapshots."
