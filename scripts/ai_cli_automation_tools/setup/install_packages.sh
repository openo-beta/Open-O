#!/bin/bash
set -e  # Exit if any command fails

# Prevent re-installing if already done
if [ -f ".venv/.installed_marker" ]; then
    echo "Packages already installed. Skipping."
    exit 0
fi

# Create the virtual environment if it doesn't exist
if [ ! -d ".venv" ]; then
    echo "Installing Python system packages..."
    apt update
    apt install -y python3-venv python3-pip
    echo "Creating virtual environment..."
    python3 -m venv .venv
fi

# Activate virtual environment
source .venv/bin/activate

echo "Installing required Python packages..."

# Core packages needed for the GitHub code scanning tools
python3 -m pip install requests              # For HTTP requests to GitHub API
python3 -m pip install python-dotenv        # For loading .env files

# Additional packages that might be useful for the automation
python3 -m pip install typing-extensions    # For better type hints (if using older Python)
python3 -m pip install pathlib              # For path handling (usually built-in, but just in case)

# If you plan to extend functionality
python3 -m pip install pyyaml               # If you need to parse YAML configs
python3 -m pip install colorama             # For colored terminal output
python3 -m pip install tabulate             # For nice table formatting of results

# Install uv for package management
python3 -m pip install uv

echo "All packages have been installed successfully."

# Mark installation as complete
touch .venv/.installed_marker