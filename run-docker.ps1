# Voyage-Shop Docker Compose Script (PowerShell)
# Run all docker compose files except loadtest
# Usage: 
#   .\run-docker.ps1 up -d (or other docker compose commands)
#   .\run-docker.ps1 clean - Delete all containers and volume data

if ($args[0] -eq "clean") {
    Write-Host "Starting data cleanup..."
    docker compose -f docker-compose.yml -f docker-compose.app.yml -f docker-compose.monitoring.yml down -v
    
    # Clean data directory
    Write-Host "Cleaning data directory..."
    if (Test-Path -Path ".\data\mysql") {
        Remove-Item -Path ".\data\mysql\*" -Force -Recurse -ErrorAction SilentlyContinue
    }
    
    Write-Host "All containers, volumes, and data directories have been cleaned."
}
else {
    docker compose -f docker-compose.yml -f docker-compose.app.yml -f docker-compose.monitoring.yml $args
} 