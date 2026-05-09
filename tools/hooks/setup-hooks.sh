#!/bin/bash

# Navigate to the root of the repo
REPO_ROOT=$(git rev-parse --show-toplevel)

# Create a symlink from our tracked hook to the git directory
ln -sf "$REPO_ROOT/tools/hooks/pre-commit.sh" "$REPO_ROOT/.git/hooks/pre-commit"

echo "Git hooks installed successfully!"