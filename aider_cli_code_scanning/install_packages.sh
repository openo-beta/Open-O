#!/bin/bash

set -e  # Exit if any command fails

apt update
apt install -y python3-venv python3-pip

# Create virtual environment
python3 -m venv .venv

# Install packages using venv's pip
./.venv/bin/pip install requests
./.venv/bin/pip install python-dotenv
./.venv/bin/pip install aider-install

echo "All packages have been installed."