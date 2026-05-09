#!/bin/bash

# 1. Get a list of Java files that are currently staged (cached)
STAGED_FILES=$(git diff --cached --name-only --diff-filter=ACMR | grep '\.java$')

# If no Java files are staged, just exit early
if [ -z "$STAGED_FILES" ]; then
  exit 0
fi

echo "Formatting staged Java files..."

# 2. Run the formatter
bazel run //:format

# 3. Only re-add the files that were already staged
# This prevents adding "new" untracked files accidentally.
echo "$STAGED_FILES" | xargs git add