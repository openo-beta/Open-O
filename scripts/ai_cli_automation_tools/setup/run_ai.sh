#!/bin/bash
# run_ai.sh - Shell script wrapper for AI tools
# Note: Claude Code is now handled directly in Python for better headless mode support
AI_TOOL=$1
FILE_PATH=$2
shift 2

case "$AI_TOOL" in
    "aider")
        aider "$FILE_PATH" $@
        ;;
    "claude-code")
        # Claude Code headless mode is handled directly in Python
        # This case is kept for backwards compatibility
        echo "Note: Claude Code should be invoked directly from Python for headless mode"
        echo "Falling back to basic invocation..."
        claude "$FILE_PATH" $@
        ;;
    *)
        echo "Unknown AI tool: $AI_TOOL"
        exit 1
        ;;
esac