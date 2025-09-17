#!/bin/bash

echo "Installing dependencies..."

# Update system packages
sudo yum update -y

# Install Java 17 if not already installed
if ! java -version 2>&1 | grep -q "17"; then
    echo "Installing Java 17..."
    sudo yum install -y java-17-amazon-corretto-devel
fi

# Set Java environment
export JAVA_HOME=/usr/lib/jvm/java-17-amazon-corretto
export PATH=$JAVA_HOME/bin:$PATH

echo "Dependencies installed successfully"