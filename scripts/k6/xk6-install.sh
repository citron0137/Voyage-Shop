#!/bin/bash

# 필요한 도구 설치
echo "Installing required tools..."
which go > /dev/null || { echo "Go is required. Please install it from https://golang.org/dl/"; exit 1; }
which git > /dev/null || { echo "Git is required. Please install it."; exit 1; }

# xk6 설치
echo "Installing xk6..."
go install go.k6.io/xk6/cmd/xk6@latest

# k6 확장 모듈로 빌드
echo "Building k6 with extensions..."
xk6 build \
  --with github.com/grafana/xk6-prometheus-rw \
  --with github.com/grafana/xk6-loki \
  --with github.com/grafana/xk6-output-prometheus-remote \
  --with github.com/grafana/xk6-tracing

echo "Finished building k6 with LGTM extensions."
echo "The binary 'k6' with extensions has been created in the current directory."
echo "You can use it with: ./k6 run your-script.js" 