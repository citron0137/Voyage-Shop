# Product Rank API Load Test Script
# Usage: .\run-loadtest.ps1 [command] [options]

# Color definitions
$Red = 'Red'
$Green = 'Green'
$Yellow = 'Yellow'
$Blue = 'Cyan' # Cyan is more visible than Blue in PowerShell

# Show script usage
function Show-Usage {
    Write-Host "Product Rank API Load Test Script" -ForegroundColor $Blue
    Write-Host "Usage: .\run-loadtest.ps1 [command] [options]"
    Write-Host "or:    .\run-loadtest.ps1 <k6-script-path>"
    Write-Host
    Write-Host "Commands:" 
    Write-Host "  start           " -ForegroundColor $Green -NoNewline
    Write-Host "Start test environment (application, DB, Redis, monitoring tools)"
    Write-Host "  stop            " -ForegroundColor $Green -NoNewline
    Write-Host "Stop and remove test environment"
    Write-Host "  data-gen        " -ForegroundColor $Green -NoNewline
    Write-Host "Generate test data"
    Write-Host "  test            " -ForegroundColor $Green -NoNewline
    Write-Host "Run load test"
    Write-Host "  help            " -ForegroundColor $Green -NoNewline
    Write-Host "Show this help message"
    Write-Host
    Write-Host "Data generation options (data-gen command):"
    Write-Host "  -js              " -ForegroundColor $Yellow -NoNewline
    Write-Host "Generate data using JS script"
    Write-Host "  -pg              " -ForegroundColor $Yellow -NoNewline
    Write-Host "Generate data for PostgreSQL (with JS script)"
    Write-Host "  -sql             " -ForegroundColor $Yellow -NoNewline
    Write-Host "Generate data using SQL script"
    Write-Host "  -size=[number]   " -ForegroundColor $Yellow -NoNewline
    Write-Host "Data size for SQL script (1000, 10000, 100000)"
    Write-Host
    Write-Host "Test options (test command):"
    Write-Host "  -basic           " -ForegroundColor $Yellow -NoNewline
    Write-Host "Run basic performance test (default)"
    Write-Host "  -cache           " -ForegroundColor $Yellow -NoNewline
    Write-Host "Run cache efficiency test"
    Write-Host "  -spike           " -ForegroundColor $Yellow -NoNewline
    Write-Host "Run traffic spike test"
    Write-Host
    Write-Host "Examples:"
    Write-Host "  .\run-loadtest.ps1 start       # Start test environment"
    Write-Host "  .\run-loadtest.ps1 data-gen -sql -size=10000  # Generate 10,000 order SQL data"
    Write-Host "  .\run-loadtest.ps1 test -cache # Run cache efficiency test"
    Write-Host "  .\run-loadtest.ps1 stop        # Stop and remove test environment"
}

# Start test environment
function Start-TestEnvironment {
    Write-Host "Starting test environment..." -ForegroundColor $Blue
    docker-compose `
      -f docker-compose.yml `
      -f docker-compose.app.yml `
      -f docker-compose.monitoring.yml `
      up -d
    
    Write-Host "Test environment started." -ForegroundColor $Green
    Write-Host "Grafana dashboard: " -NoNewline
    Write-Host "http://localhost:3000" -ForegroundColor $Yellow -NoNewline
    Write-Host " (account: admin / admin)"
}

# Stop test environment
function Stop-TestEnvironment {
    Write-Host "Stopping and removing test environment..." -ForegroundColor $Blue
    docker-compose `
      -f docker-compose.yml `
      -f docker-compose.app.yml `
      -f docker-compose.loadtest.yml `
      -f docker-compose.monitoring.yml `
      down
    
    Write-Host "Test environment stopped and removed." -ForegroundColor $Green
}

# Generate data with JS script
function Generate-DataWithJS($pgFlag) {
    $script = "cache-test/order-data-generator.js"
    
    if ($pgFlag -eq "-pg") {
        $script = "cache-test/order-data-generator-postgres.js"
        Write-Host "Generating data for PostgreSQL..." -ForegroundColor $Blue
    } else {
        Write-Host "Generating data for MySQL..." -ForegroundColor $Blue
    }
    
    $env:K6_SCRIPT = $script
    docker-compose `
      -f docker-compose.yml `
      -f docker-compose.app.yml `
      -f docker-compose.monitoring.yml `
      -f docker-compose.loadtest.yml `
      up k6 --no-deps
    
    Write-Host "Data generation completed." -ForegroundColor $Green
}

# Generate data with SQL script
function Generate-DataWithSQL($sizeParam) {
    $size = "10000"
    
    if ($sizeParam -match "-size=(.*)") {
        $size = $Matches[1]
    }
    
    $script = "load-tests/cache-test/massive-order-generator-$size.sql"
    Write-Host "Generating ${size} data with SQL script..." -ForegroundColor $Blue
    
    # Check if file exists
    if (!(Test-Path $script)) {
        Write-Host "Error: $script file not found." -ForegroundColor $Red
        Write-Host "Available sizes: 1000, 10000, 100000" -ForegroundColor $Yellow
        return
    }
    
    Get-Content $script | docker exec -i voyage-shop-mysql-1 mysql -u root -proot hhplus
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Data generation completed." -ForegroundColor $Green
    } else {
        Write-Host "Error occurred during data generation." -ForegroundColor $Red
    }
}

# Run K6 script directly
function Run-K6Script($scriptPath) {
    Write-Host "Checking script path: $scriptPath" -ForegroundColor $Yellow
    
    # Handle relative path (starting with '.\')
    if ($scriptPath.StartsWith(".\")) {
        $scriptPath = $scriptPath.Substring(2)
    }
    
    # Convert backslashes to forward slashes (for Docker compatibility)
    $scriptPath = $scriptPath.Replace("\", "/")
    
    Write-Host "Processed script path: $scriptPath" -ForegroundColor $Yellow
    
    # Check if file exists
    if (!(Test-Path $scriptPath)) {
        Write-Host "Error: $scriptPath file not found." -ForegroundColor $Red
        
        # Suggest possible path
        $filename = Split-Path $scriptPath -Leaf
        $possiblePath = "cache-test/$filename"
        if (Test-Path $possiblePath) {
            Write-Host "Try this path instead: $possiblePath" -ForegroundColor $Yellow
        }
        return
    }
    
    Write-Host "Running k6 script directly: $scriptPath" -ForegroundColor $Blue
    
    # Get file name and modify path according to Docker volume mapping
    $fileName = Split-Path $scriptPath -Leaf
    
    # Important: Docker-Compose maps ./load-tests to /tests inside container
    # If the script is in load-tests directory, adjust path accordingly
    if ($scriptPath.StartsWith("load-tests/")) {
        # Script is in load-tests directory, use the path relative to /tests
        $relativePath = $scriptPath.Substring("load-tests/".Length)
        Write-Host "Detected script in load-tests directory" -ForegroundColor $Yellow
        Write-Host "Using container path: $relativePath" -ForegroundColor $Yellow
        $env:K6_SCRIPT = $relativePath
    } 
    elseif ($scriptPath -match "cache-test/.*\.js$") {
        # Script is in cache-test directory
        Write-Host "Detected script in cache-test directory" -ForegroundColor $Yellow
        $env:K6_SCRIPT = $scriptPath
    }
    else {
        # Default: just use the filename
        Write-Host "Using script name for k6: $fileName" -ForegroundColor $Yellow
        $env:K6_SCRIPT = $fileName
    }
    
    docker-compose `
      -f docker-compose.yml `
      -f docker-compose.app.yml `
      -f docker-compose.loadtest.yml `
      -f docker-compose.monitoring.yml `
      up k6 --no-deps
    
    Write-Host "Test completed." -ForegroundColor $Green
    Write-Host "Check the results in Grafana dashboard: " -NoNewline
    Write-Host "http://localhost:3000" -ForegroundColor $Yellow
}

# Run load test
function Run-LoadTest($testType) {
    $script = "cache-test/order-item-rank-test.js"
    $testDescription = "Basic Performance"
    
    if ($testType -eq "-cache") {
        $script = "cache-test/order-item-rank-cache-test.js"
        $testDescription = "Cache Efficiency"
    } elseif ($testType -eq "-spike") {
        $script = "cache-test/order-item-rank-spike-test.js"
        $testDescription = "Traffic Spike"
    }
    
    Write-Host "Running $testDescription test..." -ForegroundColor $Blue
    
    # For predefined tests, we know they should be in the cache-test directory
    Write-Host "Using k6 script path: $script" -ForegroundColor $Yellow
    $env:K6_SCRIPT = $script
    
    docker-compose `
      -f docker-compose.yml `
      -f docker-compose.app.yml `
      -f docker-compose.loadtest.yml `
      -f docker-compose.monitoring.yml `
      up k6 --no-deps
    
    Write-Host "$testDescription test completed." -ForegroundColor $Green
    Write-Host "Check the results in Grafana dashboard: " -NoNewline
    Write-Host "http://localhost:3000" -ForegroundColor $Yellow
}

# Main logic
if ($args.Count -eq 0) {
    Show-Usage
    exit
}

# Check if first argument is a file path
$scriptPath = $args[0]
if ($scriptPath.EndsWith(".js")) {
    # Remove '.\'
    if ($scriptPath.StartsWith(".\")) {
        $scriptPath = $scriptPath.Substring(2)
    }
    
    $normalizedPath = $scriptPath.Replace("\", "/")
    Write-Host "Attempting to run script: $normalizedPath" -ForegroundColor $Yellow
    
    # Check if file exists
    if (Test-Path $scriptPath) {
        Run-K6Script $scriptPath
        exit
    } else {
        Write-Host "File not found: $scriptPath" -ForegroundColor $Red
        # Suggest possible path
        $filename = Split-Path $scriptPath -Leaf
        $possiblePath = "cache-test/$filename"
        if (Test-Path $possiblePath) {
            Write-Host "Try this path instead: $possiblePath" -ForegroundColor $Yellow
        }
    }
}

switch ($args[0]) {
    "start" {
        Start-TestEnvironment
        break
    }
    "stop" {
        Stop-TestEnvironment
        break
    }
    "data-gen" {
        if ($args[1] -eq "-js") {
            Generate-DataWithJS $args[2]
        } elseif ($args[1] -eq "-sql") {
            Generate-DataWithSQL $args[2]
        } else {
            Write-Host "Error: Please specify data generation option." -ForegroundColor $Red
            Write-Host "Available options: -js, -sql"
            exit 1
        }
        break
    }
    "test" {
        Run-LoadTest $args[1]
        break
    }
    "help" {
        Show-Usage
        break
    }
    default {
        Write-Host "Error: Unknown command." -ForegroundColor $Red
        Show-Usage
        exit 1
    }
} 