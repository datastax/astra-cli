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

find "$SNAPSHOT_DIR" -type f -name "*.received.txt" | while IFS= read -r received; do
  approved=$(echo "$received" | sed 's/\.received\.txt$/.approved.txt/')

  if [ ! -f "$approved" ]; then
    echo "============================================================"
    echo "Comparing:"
    echo "  Approved: ⚠️ No corresponding approved snapshot found"
    echo "  Received: $received"
    echo "------------------------------------------------------------"
    sh -c "$CAT_TOOL \"$received\"" || true
    echo "------------------------------------------------------------"
  else
    echo "============================================================"
    echo "Comparing:"
    echo "  Approved: $approved"
    echo "  Received: $received"
    echo "------------------------------------------------------------"

    sh -c "$DIFF_TOOL \"$approved\" \"$received\"" || true
    echo "------------------------------------------------------------"
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
