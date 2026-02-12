#!/bin/bash
# Install Playwright browser binaries and system dependencies

echo "Installing Playwright browsers with system dependencies..."
python3 -m playwright install --with-deps chromium

echo "Installation complete!"
