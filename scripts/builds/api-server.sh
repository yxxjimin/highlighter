#!/bin/bash

# Define variables
PROJECT_ROOT=$(dirname "$(dirname "$(dirname "$0")")")
DOCKERFILE_PATH="$PROJECT_ROOT/dockerfiles/Dockerfile.api-server"
IMAGE_NAME="api-server"
BUILD_CONTEXT="$PROJECT_ROOT"

# Build the Docker image
docker build -f "$DOCKERFILE_PATH" -t "$IMAGE_NAME" "$BUILD_CONTEXT"
