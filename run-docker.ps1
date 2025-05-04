# Voyage-Shop Docker Compose Script (PowerShell)
# Run all docker compose files except loadtest
# Usage: 
#   .\run-docker.ps1 up -d (or other docker compose commands)
#   .\run-docker.ps1 clean - Delete all containers and volume data
#   .\run-docker.ps1 clean -a - Deep clean (all containers, images, volumes, networks)

if ($args[0] -eq "clean") {
    Write-Host "Starting data cleanup..."
    docker compose -f docker-compose.yml -f docker-compose.app.yml -f docker-compose.monitoring.yml down -v
    
    # 추가 옵션 처리: -a (deep clean)
    if ($args.Length -gt 1 -and $args[1] -eq "-a") {
        Write-Host "Performing deep system cleanup (all containers, images, volumes, networks)..."
        docker system prune -a -f
        docker volume prune -f
        Write-Host "Deep system cleanup completed."
    }
    
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