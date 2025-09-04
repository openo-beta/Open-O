#!/bin/bash
# run_ai.sh - Shell script wrapper for AI tools
AI_TOOL=$1
FILE_PATH=$2
shift 2

case "$AI_TOOL" in
    "aider")
        aider "$FILE_PATH" $@ --yes --subtree-only
        ;;
    "claude-code")
        claude --print "$FILE_PATH" $@ --permission-mode acceptEdits
        ;;
esac