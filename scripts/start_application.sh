#!/bin/bash

echo "Starting Fingrow application..."

# Navigate to application directory
cd /opt/fingrow

# Set Java environment
export JAVA_HOME=/usr/lib/jvm/java-17-amazon-corretto
export PATH=$JAVA_HOME/bin:$PATH

# Set application environment variables
export SPRING_PROFILES_ACTIVE=prod
export DB_URL=${DB_URL}
export DB_USERNAME=${DB_USERNAME}
export DB_PASSWORD=${DB_PASSWORD}
export JWT_SECRET=${JWT_SECRET}
export FINLIFE_API_KEY=${FINLIFE_API_KEY}
export BOND_API_KEY=${BOND_API_KEY}
export KRX_API_KEY=${KRX_API_KEY}

# Make gradlew executable
chmod +x gradlew

# Build and start application
./gradlew build -x test
nohup java -jar build/libs/*.jar > application.log 2>&1 &

echo "Application started successfully"