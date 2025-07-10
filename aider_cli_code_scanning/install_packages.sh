#!/bin/bash

set -e  # Exit if any command fails

apt update
apt install -y python3-venv python3-pip

# Create virtual environment
python3 -m venv .venv
source .venv/bin/activate

# Install packages using venv's pip
python3 -m pip install requests
python3 -m pip install python-dotenv
python -m pip install uv  # If you need to install uv
uv tool install --force --python python3.12 --with pip aider-chat@latest

export PATH="/root/.local/bin:$PATH"

echo "All packages have been installed."