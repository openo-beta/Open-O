#!/bin/bash

# Check for file argument
if [ -z "$1" ]; then
  exit 1
fi

# Set file path
FILE_PATH="$1"

# Read the prompt from stdin
PROMPT=$(cat)

# Pipe prompt and file path directly into aider
{
  echo "/add $FILE_PATH"
  echo "$PROMPT"
} | aider --model sonnet --yes --subtree-only