FROM grafana/k6:latest

# 기본 의존성 설치 도구 및 필요한 패키지 설치
USER root
RUN apk add --no-cache \
    git \
    build-base \
    python3 \
    py3-pip \
    ca-certificates

# 최신 Go 설치 (1.24.0 이상)
RUN wget https://go.dev/dl/go1.24.0.linux-amd64.tar.gz && \
    rm -rf /usr/local/go && \
    tar -C /usr/local -xzf go1.24.0.linux-amd64.tar.gz && \
    rm go1.24.0.linux-amd64.tar.gz

# Go 환경변수 설정
ENV PATH=$PATH:/usr/local/go/bin
ENV GOPATH=/root/go
ENV PATH=$PATH:/root/go/bin

# xk6 설치 (k6 확장 빌더)
RUN go install go.k6.io/xk6/cmd/xk6@latest

# k6-sql 확장 모듈 및 데이터베이스 드라이버 빌드
# 모든 패키지의 버전을 명시적으로 지정
RUN xk6 build \
    --with github.com/grafana/xk6-sql@v0.0.1 \
    --with github.com/grafana/xk6-output-prometheus-remote@latest \
    --output /tmp/k6

# 빌드된 k6 바이너리를 원래 위치로 이동
RUN mv /tmp/k6 /usr/bin/k6 && chmod +x /usr/bin/k6

# 디버깅을 위한 정보 출력
RUN echo "K6 version:" && /usr/bin/k6 version

# 작업 디렉토리 설정
WORKDIR /tests

# 기본 실행 명령어 설정
ENTRYPOINT ["k6"] 