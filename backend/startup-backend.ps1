# Cetaring Backend Startup Script (PowerShell)
# This script handles the complete backend startup with diagnostics

param(
    [switch]$BuildFirst = $false,
    [switch]$SkipMySQL = $false,
    [int]$Port = 8080,
    [string]$DBHost = "localhost",
    [int]$DBPort = 3306,
    [string]$DBName = "caterhub",
    [string]$DBUser = "root",
    [string]$DBPass = "root"
)

# Color output
function Write-Success { Write-Host $args -ForegroundColor Green }
function Write-Error { Write-Host "ERROR: $args" -ForegroundColor Red }
function Write-Info { Write-Host "INFO: $args" -ForegroundColor Cyan }
function Write-Warning { Write-Host "WARNING: $args" -ForegroundColor Yellow }

# Check Java installation
function Test-Java {
    try {
        $javaVersion = java -version 2>&1
        if ($LASTEXITCODE -eq 0) {
            Write-Success "Java is installed"
            Write-Info $javaVersion[0]
            return $true
        }
    } catch {
        Write-Error "Java not found"
        return $false
    }
}

# Test MySQL connectivity
function Test-MySQL {
    Write-Info "Testing MySQL connectivity on $DBHost:$DBPort..."

    try {
        $tcp = New-Object System.Net.Sockets.TcpClient
        $tcp.Connect($DBHost, $DBPort)
        if ($tcp.Connected) {
            Write-Success "MySQL is running on $DBHost:$DBPort"
            $tcp.Close()
            return $true
        }
    } catch {
        Write-Error "Cannot connect to MySQL: $_"
        return $false
    }

    Write-Warning "MySQL is not responding - application will attempt to start and handle connection error"
    return $false
}

# Test port 8080 availability
function Test-Port {
    param([int]$PortNumber)

    try {
        $tcp = New-Object System.Net.Sockets.TcpClient
        $tcp.Connect("127.0.0.1", $PortNumber)
        Write-Warning "Port $PortNumber is already in use"
        $tcp.Close()
        return $false
    } catch {
        Write-Success "Port $PortNumber is available"
        return $true
    }
}

# Build the application
function Build-Application {
    Write-Info "Building application with Maven..."

    Set-Location $PSScriptRoot

    # Try different Maven paths
    $mvnPaths = @(
        "mvn.cmd",
        "C:\Program Files\JetBrains\IntelliJ IDEA 2025.2\plugins\maven\lib\maven3\bin\mvn.cmd",
        "C:\maven\bin\mvn.cmd"
    )

    $mvnFound = $false
    foreach ($mvnPath in $mvnPaths) {
        if (Test-Path $mvnPath) {
            Write-Info "Found Maven at: $mvnPath"
            & $mvnPath clean package -DskipTests -q
            $mvnFound = $true
            break
        }
    }

    if (-not $mvnFound) {
        Write-Warning "Maven not found in standard locations. Please install Maven or add to PATH."
        return $false
    }

    if ($LASTEXITCODE -ne 0) {
        Write-Error "Maven build failed with exit code: $LASTEXITCODE"
        return $false
    }

    if (-not (Test-Path "target\cetaring-backend-1.0.0.jar")) {
        Write-Error "JAR file not created after build"
        return $false
    }

    Write-Success "Application built successfully"
    return $true
}

# Main startup flow
function Start-Backend {
    Write-Info "========================================="
    Write-Info "Cetaring Backend - Startup Script"
    Write-Info "========================================="
    Write-Info ""

    # Step 1: Check Java
    Write-Info "[1/5] Checking Java installation..."
    if (-not (Test-Java)) {
        Write-Error "Java is not installed or not in PATH"
        exit 1
    }
    Write-Success "Java check passed"
    Write-Info ""

    # Step 2: Build if requested or JAR doesn't exist
    if ($BuildFirst -or -not (Test-Path "target\cetaring-backend-1.0.0.jar")) {
        Write-Info "[2/5] Building application..."
        if (-not (Build-Application)) {
            exit 1
        }
        Write-Info ""
    } else {
        Write-Success "[2/5] Using existing JAR file"
        Write-Info ""
    }

    # Step 3: Test MySQL (if not skipped)
    Write-Info "[3/5] Testing MySQL connectivity..."
    if (-not $SkipMySQL) {
        Test-MySQL | Out-Null
    } else {
        Write-Warning "MySQL test skipped"
    }
    Write-Info ""

    # Step 4: Test port availability
    Write-Info "[4/5] Checking port availability..."
    if (-not (Test-Port $Port)) {
        Write-Warning "Port $Port may be in use - attempting to start anyway"
    }
    Write-Info ""

    # Step 5: Start application
    Write-Info "[5/5] Starting Cetaring Backend..."
    Write-Success "=================================================="
    Write-Success "Backend will be available at:"
    Write-Success "  Health Check: http://localhost:$Port/api/v1/health"
    Write-Success "  Swagger UI:   http://localhost:$Port/swagger-ui.html"
    Write-Success "  API Docs:     http://localhost:$Port/v3/api-docs"
    Write-Success "=================================================="
    Write-Info ""

    $dbConnString = "jdbc:mysql://$DBHost`:$DBPort/$DBName`?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata"

    Set-Location $PSScriptRoot

    # Start the JAR
    & java -jar target\cetaring-backend-1.0.0.jar `
        --server.port=$Port `
        --spring.datasource.url=$dbConnString `
        --spring.datasource.username=$DBUser `
        --spring.datasource.password=$DBPass `
        --spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver `
        --spring.flyway.enabled=true `
        --spring.jpa.hibernate.ddl-auto=validate `
        --logging.level.root=INFO `
        --logging.level.com.daily.cetaring=DEBUG `
        --logging.level.org.flywaydb=INFO
}

# Run the startup
try {
    Start-Backend
} catch {
    Write-Error "Startup failed: $_"
    exit 1
}
