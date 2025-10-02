#!/bin/sh

# Link all files from .githooks to .git/hooks
HOOKS_DIR="./"
GIT_HOOKS_DIR="../.git/hooks"

chmod +x "$HOOKS_DIR"/*
for hook in "$HOOKS_DIR"/*; do
  if [ "${hook##*.}" != "sh" ]; then
    hook_name=$(basename "$hook")
    ln -sf "../../.githooks/$hook_name" "$GIT_HOOKS_DIR/$hook_name"
  fi
done
