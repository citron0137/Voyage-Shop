# Voyage Shop Helm Chart Deployment Script (Windows)
Write-Host "Starting Voyage Shop deployment..." -ForegroundColor Cyan

# Path conversion (Windows path → Docker Desktop mount path)
$currentPath = $pwd.Path.Replace('\', '/').Replace('C:/', '/run/desktop/mnt/host/c/')
Write-Host "Current path: $($pwd.Path)" -ForegroundColor Yellow
Write-Host "Converted path: $currentPath" -ForegroundColor Yellow

# Clean up existing deployments
Write-Host "`nCleaning up existing deployments..." -ForegroundColor Cyan
$deployments = helm list -q
if ($deployments) {
    Write-Host "Removing existing Helm releases..." -ForegroundColor Yellow
    foreach ($deployment in $deployments) {
        helm uninstall $deployment
    }
    
    # Clean up Kubernetes resources
    Write-Host "Cleaning up remaining Kubernetes resources..." -ForegroundColor Yellow
    kubectl delete services --all
    kubectl delete deployments --all
    kubectl delete pods --all --force --grace-period=0
    
    # Wait a moment
    Write-Host "Waiting 10 seconds for cleanup to complete..." -ForegroundColor Yellow
    Start-Sleep -Seconds 10
}

# Deploy MySQL
Write-Host "`nStarting MySQL deployment..." -ForegroundColor Cyan
helm install voyage-shop-mysql voyage-shop-mysql --values voyage-shop-mysql/values-local.yaml --set basePath.host="$currentPath"

# Wait for MySQL pod to be ready
Write-Host "`nWaiting for MySQL pod to be ready..." -ForegroundColor Cyan
$wait_success = $false
$retry_count = 0
$max_retries = 5

while (-not $wait_success -and $retry_count -lt $max_retries) {
    try {
        kubectl wait --for=condition=ready pod -l app=mysql --timeout=30s
        $wait_success = $true
    } catch {
        $retry_count++
        Write-Host "Error while waiting for MySQL pod. Retrying $retry_count/$max_retries..." -ForegroundColor Yellow
        Start-Sleep -Seconds 10
    }
}

if (-not $wait_success) {
    Write-Host "MySQL pod is not ready. Check deployment status." -ForegroundColor Red
    kubectl get pods
    exit 1
}

# Check MySQL service name
$mysql_service = kubectl get service -l app=mysql -o jsonpath="{.items[0].metadata.name}"
Write-Host "`nMySQL service name: $mysql_service" -ForegroundColor Cyan

# Deploy Voyage Shop application
Write-Host "`nStarting Voyage Shop application deployment..." -ForegroundColor Cyan
helm install voyage-shop voyage-shop --values voyage-shop/values-local.yaml --set database.host="voyage-shop-mysql-db" --set basePath.host="$currentPath"

# Check deployed resources
Write-Host "`nDeployed Helm releases:" -ForegroundColor Cyan
helm list

Write-Host "`nDeployed pods:" -ForegroundColor Cyan
kubectl get pods

Write-Host "`nDeployed services:" -ForegroundColor Cyan
kubectl get svc

Write-Host "`nVoyage Shop has been successfully deployed!" -ForegroundColor Green
Write-Host "To access the application, open http://localhost:3000 in your browser." -ForegroundColor Green 