import 'package:flutter/material.dart';
import 'package:dio/dio.dart';
import 'dart:developer' as developer;

/// Health Check Response Model
class HealthResponse {
  final String status;
  final String database;
  final DateTime time;

  HealthResponse({
    required this.status,
    required this.database,
    required this.time,
  });

  factory HealthResponse.fromJson(Map<String, dynamic> json) {
    return HealthResponse(
      status: json['status'] ?? 'UNKNOWN',
      database: json['database'] ?? 'UNKNOWN',
      time: json['time'] != null
          ? DateTime.parse(json['time'])
          : DateTime.now(),
    );
  }

  @override
  String toString() =>
      'HealthResponse(status: $status, database: $database, time: $time)';
}

/// Backend Service with improved networking
class BackendService {
  static const String BACKEND_HOST = 'caterhub-production.up.railway.app'; // ✅ CHANGED
   static const int BACKEND_PORT = 443; // ✅ CHANGED (HTTPS)
   static const Duration TIMEOUT = Duration(seconds: 10);
   static const int MAX_RETRIES = 2;


  late Dio _dio;

  BackendService() {
    _initializeDio();
  }

  void _initializeDio() {
    _dio = Dio(
      BaseOptions(
         baseUrl: 'https://$BACKEND_HOST/api/v1', // ✅ CHANGED to HTTPS
         connectTimeout: TIMEOUT,
        receiveTimeout: TIMEOUT,
        sendTimeout: TIMEOUT,
        contentType: 'application/json',
        headers: {
          'Accept': 'application/json',
          'Access-Control-Allow-Origin': '*',
        },
      ),
    );

    // Add request interceptor for logging
    _dio.interceptors.add(InterceptorsWrapper(
      onRequest: (options, handler) {
        developer.log(
          '[${DateTime.now().toIso8601String()}] REQUEST: ${options.method} ${options.path}',
          name: 'BackendService',
        );
        developer.log(
          'Headers: ${options.headers}',
          name: 'BackendService',
        );
        return handler.next(options);
      },
      onResponse: (response, handler) {
        developer.log(
          '[${DateTime.now().toIso8601String()}] RESPONSE: ${response.statusCode} ${response.requestOptions.path}',
          name: 'BackendService',
        );
        developer.log(
          'Data: ${response.data}',
          name: 'BackendService',
        );
        return handler.next(response);
      },
      onError: (error, handler) {
        developer.log(
          '[${DateTime.now().toIso8601String()}] ERROR: ${error.message}',
          name: 'BackendService',
          error: error,
        );
        developer.log(
          'Error Type: ${error.type}',
          name: 'BackendService',
        );
        return handler.next(error);
      },
    ));
  }

  /// Health check with retry logic
  Future<HealthResponse?> healthCheck() async {
    for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
      try {
        developer.log(
          'Health check attempt ${attempt + 1}/${ MAX_RETRIES + 1}',
          name: 'BackendService',
        );

        final response = await _dio.get('/health');

        if (response.statusCode == 200 || response.statusCode == 201) {
          developer.log(
            'Health check successful',
            name: 'BackendService',
          );
          return HealthResponse.fromJson(response.data);
        }
      } on DioException catch (e) {
        developer.log(
          'Attempt ${ attempt + 1} failed: ${e.message}',
          name: 'BackendService',
          error: e,
        );

        if (attempt < MAX_RETRIES) {
          developer.log(
            'Retrying in 2 seconds...',
            name: 'BackendService',
          );
          await Future.delayed(const Duration(seconds: 2));
        }
      } catch (e) {
        developer.log(
          'Unexpected error: $e',
          name: 'BackendService',
          error: e,
        );
      }
    }

    developer.log(
      'Health check failed after ${ MAX_RETRIES + 1} attempts',
      name: 'BackendService',
    );
    return null;
  }

  /// Register new user
  Future<Map<String, dynamic>?> register({
    required String email,
    required String password,
    required String firstName,
    required String lastName,
    required String phoneNumber,
  }) async {
    try {
      developer.log(
        'Registering user: $email',
        name: 'BackendService',
      );

      final response = await _dio.post(
        '/auth/register',
        data: {
          'email': email,
          'password': password,
          'firstName': firstName,
          'lastName': lastName,
          'phoneNumber': phoneNumber,
        },
      );

      if (response.statusCode == 200 || response.statusCode == 201) {
        developer.log(
          'Registration successful',
          name: 'BackendService',
        );
        return response.data;
      }
    } on DioException catch (e) {
      developer.log(
        'Registration failed: ${e.message}',
        name: 'BackendService',
        error: e,
      );
      rethrow;
    }
    return null;
  }

  /// Login user
  Future<Map<String, dynamic>?> login({
    required String email,
    required String password,
  }) async {
    try {
      developer.log(
        'Logging in user: $email',
        name: 'BackendService',
      );

      final response = await _dio.post(
        '/auth/login',
        data: {
          'email': email,
          'password': password,
        },
      );

      if (response.statusCode == 200 || response.statusCode == 201) {
        developer.log(
          'Login successful',
          name: 'BackendService',
        );
        return response.data;
      }
    } on DioException catch (e) {
      developer.log(
        'Login failed: ${e.message}',
        name: 'BackendService',
        error: e,
      );
      rethrow;
    }
    return null;
  }

  /// Get user profile
  Future<Map<String, dynamic>?> getProfile(String token) async {
    try {
      developer.log(
        'Fetching user profile',
        name: 'BackendService',
      );

      final response = await _dio.get(
        '/users/profile',
        options: Options(
          headers: {
            'Authorization': 'Bearer $token',
          },
        ),
      );

      if (response.statusCode == 200) {
        developer.log(
          'Profile fetched successfully',
          name: 'BackendService',
        );
        return response.data;
      }
    } on DioException catch (e) {
      developer.log(
        'Failed to fetch profile: ${e.message}',
        name: 'BackendService',
        error: e,
      );
      rethrow;
    }
    return null;
  }

  /// Dispose resources
  void dispose() {
    _dio.close();
  }
}

/// Integration Test Page
class BackendIntegrationTestPage extends StatefulWidget {
  const BackendIntegrationTestPage({Key? key}) : super(key: key);

  @override
  State<BackendIntegrationTestPage> createState() =>
      _BackendIntegrationTestPageState();
}

class _BackendIntegrationTestPageState
    extends State<BackendIntegrationTestPage> {
  final BackendService _backendService = BackendService();
  String _testResult = 'Waiting for tests...\n';
  bool _isLoading = false;

  @override
  void dispose() {
    _backendService.dispose();
    super.dispose();
  }

  void _addResult(String text) {
    setState(() {
      _testResult += text + '\n';
    });
  }

  void _clearResults() {
    setState(() {
      _testResult = '';
    });
  }

  Future<void> _runAllTests() async {
    _clearResults();
    setState(() => _isLoading = true);

    try {
      // Test 1: Health Check
      _addResult('=== TEST 1: HEALTH CHECK ===');
      _addResult('Testing: GET /api/v1/health');
      _addResult('Timeout: 10 seconds');
      _addResult('Retries: 2');
      _addResult('');

      final health = await _backendService.healthCheck();

      if (health != null) {
        _addResult('✅ Health check PASSED');
        _addResult('Status: ${health.status}');
        _addResult('Database: ${health.database}');
        _addResult('Time: ${health.time}');
      } else {
        _addResult('❌ Health check FAILED');
        _addResult('Backend server is offline or unreachable');
        setState(() => _isLoading = false);
        return;
      }

      _addResult('\n=== TEST 2: USER REGISTRATION ===');
      _addResult('Testing: POST /api/v1/auth/register');

      final testEmail = 'test${DateTime.now().millisecond}@test.com';
      final registerResult = await _backendService.register(
        email: testEmail,
        password: 'Test@123456',
        firstName: 'Test',
        lastName: 'User',
        phoneNumber: '+919999999999',
      );

      if (registerResult != null) {
        _addResult('✅ Registration PASSED');
        _addResult('Email: $testEmail');
        _addResult('Response: ${registerResult.toString()}');
      } else {
        _addResult('❌ Registration FAILED');
        _addResult('Could not register user');
      }

      _addResult('\n=== TEST 3: USER LOGIN ===');
      _addResult('Testing: POST /api/v1/auth/login');

      final loginResult = await _backendService.login(
        email: 'superadmin@caterhub.in',
        password: 'Admin@123',
      );

      if (loginResult != null) {
        _addResult('✅ Login PASSED');
        _addResult('Response keys: ${loginResult.keys.join(', ')}');
      } else {
        _addResult('❌ Login FAILED');
        _addResult('Could not login');
      }

      _addResult('\n=== ALL TESTS COMPLETED ===');
    } catch (e) {
      _addResult('\n❌ TEST ERROR: $e');
    } finally {
      setState(() => _isLoading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Backend Integration Tests'),
        backgroundColor: Colors.deepPurple,
      ),
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.all(16.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                ElevatedButton.icon(
                  onPressed: _isLoading ? null : _runAllTests,
                  icon: const Icon(Icons.play_arrow),
                  label: const Text('Run All Tests'),
                  style: ElevatedButton.styleFrom(
                    backgroundColor: Colors.green,
                    padding: const EdgeInsets.symmetric(vertical: 12),
                  ),
                ),
                const SizedBox(height: 12),
                ElevatedButton.icon(
                  onPressed: _clearResults,
                  icon: const Icon(Icons.clear),
                  label: const Text('Clear Results'),
                  style: ElevatedButton.styleFrom(
                    backgroundColor: Colors.orange,
                    padding: const EdgeInsets.symmetric(vertical: 12),
                  ),
                ),
              ],
            ),
          ),
          Expanded(
            child: Container(
              margin: const EdgeInsets.all(16),
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                border: Border.all(color: Colors.grey),
                borderRadius: BorderRadius.circular(8),
                color: Colors.grey[100],
              ),
              child: _isLoading
                  ? const Center(
                      child: CircularProgressIndicator(),
                    )
                  : SingleChildScrollView(
                      child: Text(
                        _testResult,
                        style: const TextStyle(
                          fontFamily: 'Courier',
                          fontSize: 12,
                          color: Colors.black87,
                        ),
                      ),
                    ),
            ),
          ),
        ],
      ),
    );
  }
}

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Cetaring Backend Tests',
      theme: ThemeData(
        primarySwatch: Colors.deepPurple,
        useMaterial3: true,
      ),
      home: const BackendIntegrationTestPage(),
    );
  }
}
