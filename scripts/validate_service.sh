#!/bin/bash

echo "Validating Fingrow application..."

# Wait for application to start
sleep 30

# Check if application is running
if pgrep -f "java.*fingrow" > /dev/null; then
    echo "Application process is running"

    # Check if application responds to health check
    for i in {1..10}; do
        if curl -f http://localhost:8080/actuator/health 2>/dev/null; then
            echo "Application health check passed"
            exit 0
        fi
        echo "Waiting for application to respond... (attempt $i/10)"
        sleep 10
    done

    echo "Application is not responding to health check"
    exit 1
else
    echo "Application process is not running"
    exit 1
fi