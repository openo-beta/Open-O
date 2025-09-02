#!/bin/bash
# run_ai.sh
AI_TOOL=$1
FILE_PATH=$2
shift 2

case "$AI_TOOL" in
    "aider")
        aider "$FILE_PATH" $@
        ;;
    "claude-code")
        claude "$FILE_PATH" $@
        ;;
    *)
        echo "Unknown AI tool: $AI_TOOL"
        exit 1
        ;;
esac