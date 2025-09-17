#!/bin/bash

echo "Stopping Fingrow application..."

# Kill existing Java processes
pkill -f "fingrow" || true
pkill -f "java.*fingrow" || true

# Wait for processes to stop
sleep 5

echo "Application stopped successfully"