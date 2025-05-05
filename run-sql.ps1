# Voyage-Shop SQL Execution Script (PowerShell)
# Run SQL files inside Docker MySQL container
# Usage: .\run-sql.ps1 -SqlFile path\to\your\sqlfile.sql

param (
    [Parameter(Position=0, ValueFromPipeline=$true)]
    [string]$SqlFile,
    
    [string]$Service = "mysql",
    [string]$User = "root",
    [string]$Password = "root",
    [string]$Database = "hhplus",
    [string]$Charset = "utf8mb4",
    [switch]$Help
)

# Help function
function Show-Help {
    Write-Host "Usage: .\run-sql.ps1 [options] <sql_file_path>"
    Write-Host ""
    Write-Host "Options:"
    Write-Host "  -Help                     Show this help message"
    Write-Host "  -Service NAME             MySQL service name in docker-compose (default: mysql)"
    Write-Host "  -User USERNAME            MySQL username (default: root)"
    Write-Host "  -Password PASSWORD        MySQL password (default: root)"
    Write-Host "  -Database NAME            MySQL database name (default: hhplus)"
    Write-Host "  -Charset CHARSET          MySQL charset (default: utf8mb4)"
    Write-Host ""
    Write-Host "Example:"
    Write-Host "  .\run-sql.ps1 data\my-script.sql"
    Write-Host "  .\run-sql.ps1 -Database otherdb data\my-script.sql"
    exit
}

# Show help if requested
if ($Help -or [string]::IsNullOrEmpty($SqlFile)) {
    Show-Help
}

# Check if file exists
if (-not (Test-Path $SqlFile)) {
    Write-Host "Error: SQL file '$SqlFile' not found" -ForegroundColor Red
    exit 1
}

Write-Host "Executing SQL file: $SqlFile"
Write-Host "Service: $Service"
Write-Host "Database: $Database"

# Get SQL content
try {
    Write-Host "Reading SQL content..." -ForegroundColor Cyan
    $sqlContent = Get-Content -Path $SqlFile -Raw -ErrorAction Stop
    
    Write-Host "Running SQL command..." -ForegroundColor Cyan
    
    # Docker Compose 파일이 존재하는지 확인
    if (-not (Test-Path "docker-compose.yml")) {
        throw "docker-compose.yml file not found"
    }
    
    if (-not (Test-Path "docker-compose.app.yml")) {
        throw "docker-compose.app.yml file not found"
    }
    
    if (-not (Test-Path "docker-compose.monitoring.yml")) {
        throw "docker-compose.monitoring.yml file not found"
    }
    
    # 각 파일을 개별적으로 지정
    $sqlContent | docker compose -f "docker-compose.yml" -f "docker-compose.app.yml" -f "docker-compose.monitoring.yml" exec -T $Service mysql -u"$User" -p"$Password" --default-character-set="$Charset" "$Database"
    
    $exitCode = $LASTEXITCODE
    if ($exitCode -ne 0) {
        throw "SQL execution failed with exit code $exitCode"
    }
    
    Write-Host "SQL execution completed successfully" -ForegroundColor Green
} 
catch {
    Write-Host "Error: SQL execution failed" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    exit 1
} 