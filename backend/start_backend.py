#!/usr/bin/env python3
"""
Cetaring Backend Diagnostic and Startup Script
This script performs comprehensive diagnostics and starts the backend
"""

import subprocess
import sys
import socket
import time
import os
import shutil
from pathlib import Path
from datetime import datetime

# ANSI Colors
GREEN = '\033[92m'
RED = '\033[91m'
YELLOW = '\033[93m'
CYAN = '\033[96m'
RESET = '\033[0m'

def log_success(msg):
    print(f"{GREEN}✅ {msg}{RESET}")

def log_error(msg):
    print(f"{RED}❌ {msg}{RESET}")

def log_info(msg):
    print(f"{CYAN}ℹ️  {msg}{RESET}")

def log_warning(msg):
    print(f"{YELLOW}⚠️  {msg}{RESET}")

def separator(title=""):
    print("\n" + "="*60)
    if title:
        print(f" {title}")
        print("="*60)
    else:
        print("="*60 + "\n")

def test_java():
    """Check if Java is installed"""
    log_info("Checking Java installation...")
    try:
        result = subprocess.run(["java", "-version"], capture_output=True, text=True, timeout=5)
        if result.returncode == 0:
            version_line = result.stderr.split('\n')[0]
            log_success(f"Java is installed: {version_line}")
            return True
    except Exception as e:
        pass
    log_error("Java not found in PATH")
    return False

def test_mysql(host="localhost", port=3306):
    """Test MySQL connectivity"""
    log_info(f"Testing MySQL connectivity on {host}:{port}...")
    try:
        sock = socket.create_connection((host, port), timeout=5)
        sock.close()
        log_success(f"MySQL is running on {host}:{port}")
        return True
    except Exception as e:
        log_warning(f"Cannot connect to MySQL: {str(e)}")
        log_warning("Application will start but database operations may fail")
        return False

def test_port(port=8080):
    """Test if port is available"""
    log_info(f"Checking port {port} availability...")
    try:
        sock = socket.create_connection(("127.0.0.1", port), timeout=1)
        sock.close()
        log_warning(f"Port {port} is already in use - will attempt to start anyway")
        return False
    except Exception:
        log_success(f"Port {port} is available")
        return True

def find_maven():
    """Find Maven executable"""
    possible_paths = [
        "mvn",
        "mvn.cmd",
        r"C:\Program Files\JetBrains\IntelliJ IDEA 2025.2\plugins\maven\lib\maven3\bin\mvn.cmd",
        r"C:\maven\bin\mvn.cmd",
        shutil.which("mvn"),
    ]

    for path in possible_paths:
        if path and Path(path).exists():
            return path
        if path and shutil.which(path):
            return path

    return None

def build_application(backend_dir):
    """Build the application using Maven"""
    log_info("Building application with Maven...")

    maven_path = find_maven()
    if not maven_path:
        log_error("Maven not found. Please install Maven or add to PATH")
        log_info("You can download Maven from: https://maven.apache.org/download.cgi")
        return False

    log_info(f"Using Maven from: {maven_path}")

    try:
        result = subprocess.run(
            [maven_path, "clean", "package", "-DskipTests", "-q"],
            cwd=backend_dir,
            capture_output=True,
            text=True,
            timeout=300
        )

        if result.returncode != 0:
            log_error(f"Maven build failed with exit code {result.returncode}")
            if result.stdout:
                print("STDOUT:", result.stdout[-500:])
            if result.stderr:
                print("STDERR:", result.stderr[-500:])
            return False

        jar_path = backend_dir / "target" / "cetaring-backend-1.0.0.jar"
        if not jar_path.exists():
            log_error("JAR file not created after build")
            return False

        log_success(f"Application built successfully ({jar_path.stat().st_size / 1024 / 1024:.1f} MB)")
        return True

    except subprocess.TimeoutExpired:
        log_error("Maven build timed out (5 minutes)")
        return False
    except Exception as e:
        log_error(f"Build failed: {str(e)}")
        return False

def start_application(backend_dir, port=8080, db_host="localhost", db_port=3306, db_name="caterhub", db_user="root", db_pass="root"):
    """Start the Spring Boot application"""
    jar_path = backend_dir / "target" / "cetaring-backend-1.0.0.jar"

    if not jar_path.exists():
        log_error(f"JAR file not found at {jar_path}")
        return False

    db_url = f"jdbc:mysql://{db_host}:{db_port}/{db_name}?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata"

    java_args = [
        "java",
        "-jar",
        str(jar_path),
        f"--server.port={port}",
        f"--spring.datasource.url={db_url}",
        f"--spring.datasource.username={db_user}",
        f"--spring.datasource.password={db_pass}",
        f"--spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver",
        "--spring.flyway.enabled=true",
        "--spring.jpa.hibernate.ddl-auto=validate",
        "--logging.level.root=INFO",
        "--logging.level.com.daily.cetaring=DEBUG",
        "--logging.level.org.flywaydb=INFO",
    ]

    separator("STARTING BACKEND APPLICATION")
    log_success(f"Backend will be available at:")
    log_success(f"  Health Check: http://localhost:{port}/api/v1/health")
    log_success(f"  Swagger UI:   http://localhost:{port}/swagger-ui.html")
    log_success(f"  API Docs:     http://localhost:{port}/v3/api-docs")
    separator()

    log_info(f"Database: {db_host}:{db_port}/{db_name}")
    log_info(f"Starting time: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    log_info("")
    log_info("Press Ctrl+C to stop the server")
    log_info("")

    try:
        subprocess.run(java_args, check=False)
    except KeyboardInterrupt:
        log_info("\nServer stopped by user")
        return True
    except Exception as e:
        log_error(f"Failed to start application: {str(e)}")
        return False

def main():
    separator("CETARING BACKEND STARTUP SCRIPT")

    backend_dir = Path(__file__).resolve().parent
    if not backend_dir.exists():
        log_error(f"Backend directory not found: {backend_dir}")
        sys.exit(1)

    # Change to backend directory
    os.chdir(backend_dir)

    # Step 1: Check Java
    log_info("[1/6] Checking Java installation")
    if not test_java():
        log_error("Cannot proceed without Java")
        sys.exit(1)
    log_success("Java check passed\n")

    # Step 2: Check MySQL
    log_info("[2/6] Checking MySQL connectivity")
    mysql_ok = test_mysql()
    log_success("MySQL check complete\n") if mysql_ok else log_warning("MySQL check skipped - will handle at runtime\n")

    # Step 3: Check port
    log_info("[3/6] Checking port availability")
    test_port()
    log_success("Port check complete\n")

    # Step 4: Check if JAR exists
    log_info("[4/6] Checking if application is already built")
    jar_path = backend_dir / "target" / "cetaring-backend-1.0.0.jar"

    build_needed = True
    if jar_path.exists():
        jar_age_seconds = time.time() - jar_path.stat().st_mtime
        if jar_age_seconds < 3600:  # Less than 1 hour old
            log_success(f"Recent JAR found ({jar_age_seconds/60:.0f} minutes old)")
            build_needed = False
        else:
            log_warning(f"JAR is {jar_age_seconds/3600:.1f} hours old - rebuild recommended")

    # Step 5: Build if needed
    if build_needed:
        log_info("[5/6] Building application (this may take 1-2 minutes)")
        if not build_application(backend_dir):
            log_error("Build failed - cannot proceed")
            sys.exit(1)
        log_success("Build complete\n")
    else:
        log_info("[5/6] Skipping build - using existing JAR\n")

    # Step 6: Start application
    log_info("[6/6] Starting application")
    if not start_application(backend_dir):
        sys.exit(1)

if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("\n" + YELLOW + "⚠️  Interrupted by user" + RESET)
        sys.exit(0)
    except Exception as e:
        print(f"\n" + RED + f"❌ Unexpected error: {str(e)}" + RESET)
        sys.exit(1)
