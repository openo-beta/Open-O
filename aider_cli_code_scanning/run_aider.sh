#!/bin/bash

# Check for file argument
if [ -z "$1" ]; then
  echo "Usage: ./aider_cli_code_scanning/run_aider.sh <file-path>"
  exit 1
fi

FILE_PATH="$1"

# Read prompt from stdin (sent from Python)
prompt=$(cat)

# Feed the add command and prompt into aider
{
  echo "/add $FILE_PATH"
  echo "$prompt"
} | ./.venv/bin/aider-install --model tinyllama --api-base http://localhost:11434 --yes
