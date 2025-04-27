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
    kubectl delete pods --all --force --grace-period=0
    
    # Wait a moment
    Write-Host "Waiting 5 seconds for cleanup to complete..." -ForegroundColor Yellow
    Start-Sleep -Seconds 5
}

# Update dependencies
Write-Host "`nUpdating Helm dependencies..." -ForegroundColor Cyan
helm dependency update voyage-shop

# Deploy Voyage Shop (automatically includes MySQL via dependencies)
Write-Host "`nDeploying Voyage Shop (including MySQL)..." -ForegroundColor Cyan
helm install voyage-shop voyage-shop --values voyage-shop/values-local.yaml --set basePath.host="$currentPath" 

# Wait for pods to be ready
Write-Host "`nWaiting for pods to be ready..." -ForegroundColor Cyan
$retry_count = 0
$max_retries = 10

Write-Host "Waiting for MySQL to be ready..." -ForegroundColor Yellow
while ($retry_count -lt $max_retries) {
    $mysqlPod = kubectl get pods -l app=mysql --no-headers 2>$null
    if ($mysqlPod -and $mysqlPod -match "Running") {
        Write-Host "MySQL is ready!" -ForegroundColor Green
        break
    }
    $retry_count++
    Write-Host "Waiting for MySQL... ($retry_count/$max_retries)" -ForegroundColor Yellow
    Start-Sleep -Seconds 5
}

# Check deployed resources
Write-Host "`nDeployed Helm releases:" -ForegroundColor Cyan
helm list

Write-Host "`nDeployed pods:" -ForegroundColor Cyan
kubectl get pods

Write-Host "`nDeployed services:" -ForegroundColor Cyan
kubectl get svc

Write-Host "`nVoyage Shop has been successfully deployed!" -ForegroundColor Green

# Start port forwarding in background
Write-Host "`nSetting up port forwarding (8080 -> voyage-shop service)..." -ForegroundColor Cyan
$job = Start-Job -ScriptBlock {
    kubectl port-forward svc/voyage-shop 8080:80
}

Write-Host "Port forwarding started in background (Job ID: $($job.Id))" -ForegroundColor Yellow
Write-Host "To access the application, open http://localhost:8080 in your browser." -ForegroundColor Green
Write-Host "`nTo stop port forwarding later, run:" -ForegroundColor Yellow
Write-Host "Stop-Job $($job.Id); Remove-Job $($job.Id)" -ForegroundColor Yellow 