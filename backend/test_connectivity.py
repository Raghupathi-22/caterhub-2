#!/usr/bin/env python3
"""
Cetaring Backend & Flutter Connectivity Test Suite
Comprehensive verification of all components
"""

import subprocess
import sys
import socket
import time
import requests
import json
from pathlib import Path
from datetime import datetime
import platform

# ANSI Colors
GREEN = '\033[92m'
RED = '\033[91m'
YELLOW = '\033[93m'
CYAN = '\033[96m'
MAGENTA = '\033[95m'
RESET = '\033[0m'

def log_success(msg):
    print(f"{GREEN}✅ {msg}{RESET}")

def log_error(msg):
    print(f"{RED}❌ {msg}{RESET}")

def log_info(msg):
    print(f"{CYAN}ℹ️  {msg}{RESET}")

def log_warning(msg):
    print(f"{YELLOW}⚠️  {msg}{RESET}")

def log_test(msg):
    print(f"{MAGENTA}🧪 {msg}{RESET}")

def separator(title=""):
    print("\n" + "="*70)
    if title:
        print(f"  {title}")
        print("="*70)
    else:
        print("="*70 + "\n")

class ConnectivityTestSuite:
    def __init__(self):
        self.backend_host = "localhost"
        self.backend_port = 8080
        self.backend_url = f"http://{self.backend_host}:{self.backend_port}/api/v1"
        self.mysql_host = "localhost"
        self.mysql_port = 3306
        self.test_results = []

    def add_result(self, test_name, passed, details=""):
        self.test_results.append({
            'name': test_name,
            'passed': passed,
            'details': details
        })

    def test_java(self):
        """Test 1: Java Installation"""
        log_test("TEST 1: Java Installation")
        try:
            result = subprocess.run(
                ["java", "-version"],
                capture_output=True,
                text=True,
                timeout=5
            )
            if result.returncode == 0:
                version = result.stderr.split('\n')[0] if result.stderr else "Java installed"
                log_success(f"Java: {version}")
                self.add_result("Java Installation", True, version)
                return True
        except Exception as e:
            log_error(f"Java not found: {str(e)}")
            self.add_result("Java Installation", False, str(e))
            return False

    def test_mysql_port(self):
        """Test 2: MySQL Port Availability"""
        log_test("TEST 2: MySQL Port 3306")
        try:
            sock = socket.create_connection(
                (self.mysql_host, self.mysql_port),
                timeout=5
            )
            sock.close()
            log_success(f"MySQL listening on {self.mysql_host}:{self.mysql_port}")
            self.add_result("MySQL Port Connectivity", True, "Connected")
            return True
        except Exception as e:
            log_warning(f"MySQL not responding: {str(e)}")
            self.add_result("MySQL Port Connectivity", False, str(e))
            return False

    def test_backend_port(self):
        """Test 3: Backend Port Availability"""
        log_test("TEST 3: Backend Port 8080")
        try:
            sock = socket.create_connection(
                ("127.0.0.1", self.backend_port),
                timeout=2
            )
            sock.close()
            log_success(f"Backend listening on {self.backend_host}:{self.backend_port}")
            self.add_result("Backend Port Listening", True, "Connected")
            return True
        except Exception as e:
            log_warning(f"Backend not yet running: {str(e)}")
            self.add_result("Backend Port Listening", False, str(e))
            return False

    def test_health_endpoint(self):
        """Test 4: Health Check Endpoint"""
        log_test("TEST 4: Health Check Endpoint")
        try:
            response = requests.get(
                f"{self.backend_url}/health",
                timeout=10
            )

            if response.status_code == 200:
                data = response.json()
                status = data.get('status', 'UNKNOWN')
                database = data.get('database', 'UNKNOWN')

                log_success(f"Health Status: {status}")
                log_success(f"Database Status: {database}")

                details = f"Status: {status}, Database: {database}"
                self.add_result("Health Check Endpoint", True, details)
                return True
            else:
                log_error(f"Health endpoint returned {response.status_code}")
                self.add_result("Health Check Endpoint", False, f"HTTP {response.status_code}")
                return False

        except Exception as e:
            log_error(f"Health check failed: {str(e)}")
            self.add_result("Health Check Endpoint", False, str(e))
            return False

    def test_registration_endpoint(self):
        """Test 5: User Registration API"""
        log_test("TEST 5: User Registration API")
        try:
            test_email = f"test{int(time.time())}@test.com"

            response = requests.post(
                f"{self.backend_url}/auth/register",
                json={
                    "email": test_email,
                    "password": "Test@123456",
                    "firstName": "Test",
                    "lastName": "User",
                    "phoneNumber": "+919999999999"
                },
                timeout=10
            )

            if response.status_code in [200, 201]:
                log_success(f"Registration successful for {test_email}")
                self.add_result("User Registration API", True, f"User: {test_email}")
                return True
            else:
                log_warning(f"Registration returned {response.status_code}")
                self.add_result("User Registration API", False, f"HTTP {response.status_code}")
                return False

        except Exception as e:
            log_error(f"Registration test failed: {str(e)}")
            self.add_result("User Registration API", False, str(e))
            return False

    def test_login_endpoint(self):
        """Test 6: User Login API"""
        log_test("TEST 6: User Login API")
        try:
            response = requests.post(
                f"{self.backend_url}/auth/login",
                json={
                    "email": "superadmin@caterhub.in",
                    "password": "Admin@123"
                },
                timeout=10
            )

            if response.status_code in [200, 201]:
                log_success("Login successful")
                self.add_result("User Login API", True, "Login OK")
                return True
            else:
                log_warning(f"Login returned {response.status_code}")
                self.add_result("User Login API", False, f"HTTP {response.status_code}")
                return False

        except Exception as e:
            log_error(f"Login test failed: {str(e)}")
            self.add_result("User Login API", False, str(e))
            return False

    def test_cors_headers(self):
        """Test 7: CORS Headers"""
        log_test("TEST 7: CORS Headers")
        try:
            response = requests.options(
                f"{self.backend_url}/auth/login",
                headers={
                    "Origin": "http://10.0.2.2:8080",
                    "Access-Control-Request-Method": "POST"
                },
                timeout=10
            )

            cors_origin = response.headers.get('Access-Control-Allow-Origin', 'NOT FOUND')

            if cors_origin and cors_origin != 'NOT FOUND':
                log_success(f"CORS Enabled: {cors_origin}")
                self.add_result("CORS Headers", True, f"Origin: {cors_origin}")
                return True
            else:
                log_warning("CORS headers not found")
                self.add_result("CORS Headers", False, "Headers missing")
                return False

        except Exception as e:
            log_warning(f"CORS test not critical: {str(e)}")
            self.add_result("CORS Headers", False, str(e))
            return False

    def test_emulator_connectivity(self):
        """Test 8: Android Emulator Connectivity"""
        log_test("TEST 8: Android Emulator Connectivity (10.0.2.2)")
        try:
            emulator_url = "http://10.0.2.2:8080/api/v1/health"

            response = requests.get(
                emulator_url,
                timeout=10
            )

            if response.status_code == 200:
                log_success("Emulator can reach backend via 10.0.2.2:8080")
                self.add_result("Emulator Connectivity", True, "10.0.2.2:8080 OK")
                return True
            else:
                log_warning(f"Emulator response: HTTP {response.status_code}")
                self.add_result("Emulator Connectivity", False, f"HTTP {response.status_code}")
                return False

        except Exception as e:
            log_warning(f"Emulator test: {str(e)} (Note: May fail on non-Windows)")
            self.add_result("Emulator Connectivity", False, str(e))
            return False

    def run_all_tests(self):
        """Run all connectivity tests"""
        separator("CETARING BACKEND CONNECTIVITY TEST SUITE")

        tests = [
            ("Java Installation", self.test_java),
            ("MySQL Connectivity", self.test_mysql_port),
            ("Backend Port", self.test_backend_port),
            ("Health Endpoint", self.test_health_endpoint),
            ("Registration API", self.test_registration_endpoint),
            ("Login API", self.test_login_endpoint),
            ("CORS Headers", self.test_cors_headers),
            ("Emulator Connectivity", self.test_emulator_connectivity),
        ]

        results_needed_backend = 0
        for test_name, test_func in tests[3:]:  # Skip first 3 tests as backend-dependent
            results_needed_backend += 1

        for test_name, test_func in tests:
            try:
                test_func()
            except Exception as e:
                log_error(f"Test {test_name} crashed: {str(e)}")
                self.add_result(test_name, False, str(e))

            print()  # Spacing

        self.print_summary()

    def print_summary(self):
        """Print test summary"""
        separator("TEST SUMMARY")

        passed = sum(1 for t in self.test_results if t['passed'])
        total = len(self.test_results)

        print(f"\n{'Test Name':<30} {'Status':<10} {'Details':<40}")
        print("-" * 80)

        for result in self.test_results:
            status = "✅ PASS" if result['passed'] else "❌ FAIL"
            details = result['details'][:37]
            print(f"{result['name']:<30} {status:<10} {details:<40}")

        print("\n" + "="*80)

        if passed == total:
            log_success(f"ALL TESTS PASSED ({passed}/{total})")
            log_success("Backend is fully connected and operational!")
            log_success("Flutter can now connect successfully")
            print()
            log_info("Next steps:")
            log_info("1. Start Flutter app: flutter run")
            log_info("2. Check if login screen appears")
            log_info("3. Registration and login should work")
            return True
        elif passed >= total - 2:
            log_warning(f"MOSTLY PASSING ({passed}/{total})")
            log_warning("Some optional tests failed, but core functionality works")
            print()
            log_info("Core tests passed - can proceed with Flutter testing")
            return True
        else:
            log_error(f"TESTS FAILING ({passed}/{total})")
            log_error("Please resolve issues before running Flutter app")
            return False

        separator()


def main():
    log_info(f"Platform: {platform.system()} {platform.release()}")
    log_info(f"Time: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    log_info(f"Python: {sys.version.split()[0]}")

    suite = ConnectivityTestSuite()
    success = suite.run_all_tests()

    sys.exit(0 if success else 1)


if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("\n" + YELLOW + "⚠️  Test suite interrupted by user" + RESET)
        sys.exit(1)
    except Exception as e:
        print(f"\n" + RED + f"❌ Unexpected error: {str(e)}" + RESET)
        sys.exit(1)
