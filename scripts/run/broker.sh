#!/bin/bash
# This script runs a single Kafka broker in KRaft mode.
# Set your public IP address via the PUBLIC_IP environment variable if needed;
# otherwise, it defaults to localhost.

PUBLIC_IP=${PUBLIC_IP:-localhost}

docker run -d --name broker -p 9092:9092 \
  -e KAFKA_NODE_ID=1 \
  -e KAFKA_PROCESS_ROLES=broker,controller \
  -e KAFKA_CONTROLLER_QUORUM_VOTERS="1@localhost:9093" \
  -e KAFKA_LISTENERS="PLAINTEXT://:9092,CONTROLLER://:9093" \
  -e KAFKA_ADVERTISED_LISTENERS="PLAINTEXT://${PUBLIC_IP}:9092" \
  -e KAFKA_CONTROLLER_LISTENER_NAMES="CONTROLLER" \
  -e KAFKA_LOG_DIRS="/tmp/kraft-combined-logs" \
  apache/kafka:latest
