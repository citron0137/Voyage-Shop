#!/bin/bash
# Voyage-Shop Docker Compose Script
# Run all docker compose files except loadtest
# Usage: 
#   ./run-docker.sh up -d (or other docker compose commands)
#   ./run-docker.sh clean - Delete all containers and volume data
#   ./run-docker.sh clean -a - Deep clean (all containers, images, volumes, networks)

if [ "$1" = "clean" ]; then
  echo "Starting data cleanup..."
  docker compose -f docker-compose.yml -f docker-compose.app.yml -f docker-compose.monitoring.yml down -v
  
  # 추가 옵션 처리: -a (deep clean)
  if [ "$2" = "-a" ]; then
    echo "Performing deep system cleanup (all containers, images, volumes, networks)..."
    docker system prune -a -f
    docker volume prune -f
    echo "Deep system cleanup completed."
  fi
  
  # Clean data directory
  echo "Cleaning data directory..."
  rm -rf ./data/mysql/*
  echo "All containers, volumes, and data directories have been cleaned."
else
  docker compose -f docker-compose.yml -f docker-compose.app.yml -f docker-compose.monitoring.yml "$@"
fi 