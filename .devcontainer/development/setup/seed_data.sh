#!/bin/bash

set -e  # Exit on any error

echo "[Bootstrap] Seeding initial database files..."

# Seeding initial database files for documents
SOURCE_DIR="/db-data/documents"
DEST_DIR="/var/lib/OscarDocument/oscar/document"

# Make sure the destination directory exists
mkdir -p "$DEST_DIR"

# Check if any files exist in the source
if [ -n "$(ls -A $SOURCE_DIR 2>/dev/null)" ]; then
    cp -vn "$SOURCE_DIR"/*.pdf "$DEST_DIR"/
    echo "[Bootstrap] Done copying PDFs."
else
    echo "[Bootstrap] No PDF files found in $SOURCE_DIR — skipping."
fi