#!/bin/bash

# Run a demo node
# Usage: ./run-node.sh <node-name>

if [ $# -ne 1 ]; then
    echo "Usage: $0 <node-name>"
    echo "Example: $0 node1"
    exit 1
fi

NODE_NAME=$1

# Build the demo if needed
if [ ! -f target/jgroups-opentelemetry-demo-1.0.0.Alpha1-SNAPSHOT.jar ]; then
    echo "Building demo application..."
    mvn clean package
fi

# Run the demo
echo "Starting JGroups node: $NODE_NAME"
java -jar target/jgroups-opentelemetry-demo-1.0.0.Alpha1-SNAPSHOT.jar "$NODE_NAME"
