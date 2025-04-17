# 필요한 도구 설치 확인
Write-Host "확장 모듈이 있는 k6 설치 준비 중..." -ForegroundColor Green

# Go가 설치되어 있는지 확인
if (-not (Get-Command go -ErrorAction SilentlyContinue)) {
    Write-Host "Go가 설치되어 있지 않습니다. https://golang.org/dl/ 에서 설치하세요." -ForegroundColor Red
    exit 1
}

# Git이 설치되어 있는지 확인
if (-not (Get-Command git -ErrorAction SilentlyContinue)) {
    Write-Host "Git이 설치되어 있지 않습니다. 먼저 Git을 설치하세요." -ForegroundColor Red
    exit 1
}

# 작업 디렉토리 생성
$workDir = Join-Path $env:TEMP "xk6-build"
if (-not (Test-Path $workDir)) {
    New-Item -ItemType Directory -Path $workDir | Out-Null
}
Set-Location $workDir

# xk6 설치
Write-Host "xk6 설치 중..." -ForegroundColor Yellow
go install go.k6.io/xk6/cmd/xk6@latest

# GOPATH 확인 및 환경 변수 설정
$goPath = go env GOPATH
$env:PATH = "$goPath\bin;$env:PATH"

# k6 빌드
Write-Host "LGTM 확장 모듈을 포함하여 k6 빌드 중..." -ForegroundColor Yellow
xk6 build `
  --with github.com/grafana/xk6-prometheus-rw `
  --with github.com/grafana/xk6-loki `
  --with github.com/grafana/xk6-output-prometheus-remote `
  --with github.com/grafana/xk6-tracing

# 빌드된 바이너리를 원하는 위치로 복사
$targetDir = "scripts\k6"
Copy-Item -Path "k6.exe" -Destination "$targetDir\k6.exe" -Force

Write-Host "`nLGTM 확장 모듈이 포함된 k6가 성공적으로 빌드되었습니다." -ForegroundColor Green
Write-Host "실행 파일 위치: $targetDir\k6.exe" -ForegroundColor Cyan
Write-Host "사용법: cd $targetDir; .\k6.exe run voyage-shop-load-test.js" -ForegroundColor Cyan 