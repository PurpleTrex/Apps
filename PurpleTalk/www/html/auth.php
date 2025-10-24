<?php
  /**
   * PurpleTalk Authentication Backend
   * Connects to Matrix server at purpletalk.devit.dev
   * Handles registration, login, and session management
   */

  // Enable CORS for web app
  header('Access-Control-Allow-Origin: *');
  header('Access-Control-Allow-Methods: POST, GET, OPTIONS');
  header('Access-Control-Allow-Headers: Content-Type, Authorization');
  header('Content-Type: application/json');

  // Handle preflight requests
  if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
      http_response_code(200);
      exit();
  }

  // Matrix server configuration
  define('MATRIX_SERVER', 'https://purpletalk.devit.dev');
  define('MATRIX_DOMAIN', 'purpletalk.devit.dev');

  /**
   * Generate secure Matrix username from phone number
   * Uses SHA256 with a salt for better security
   */
  function phoneToMatrixUsername($phoneNumber) {
      // Remove all non-numeric characters
      $phoneDigits = preg_replace('/[^0-9]/', '', $phoneNumber);

      // Add a salt for extra security (change this to something unique)
      $salt = 'PurpleTalk2025_' . MATRIX_DOMAIN;

      // Create a secure hash of the phone number
      $hash = hash('sha256', $salt . $phoneDigits);

      // Use first 16 characters of hash with 'tel_' prefix
      // This ensures uniqueness while hiding the actual phone number
      return 'tel_' . substr($hash, 0, 16);
  }

  /**
   * Store or update user mapping in local database (optional)
   */
  function storeUserMapping($phoneNumber, $matrixUsername, $matrixId) {
      try {
          // Check if database exists
          if (!file_exists('/var/www/data/purpletalk.db')) {
              return true; // Database is optional
          }

          $db = new SQLite3('/var/www/data/purpletalk.db');

          $stmt = $db->prepare('
              INSERT OR REPLACE INTO user_mappings
              (phone_number, matrix_username, matrix_id, last_login)
              VALUES (:phone, :username, :matrix_id, CURRENT_TIMESTAMP)
          ');

          $stmt->bindValue(':phone', $phoneNumber, SQLITE3_TEXT);
          $stmt->bindValue(':username', $matrixUsername, SQLITE3_TEXT);
          $stmt->bindValue(':matrix_id', $matrixId, SQLITE3_TEXT);

          $stmt->execute();
          $db->close();

          return true;
      } catch (Exception $e) {
          error_log("Failed to store user mapping: " . $e->getMessage());
          return false; // Non-critical error
      }
  }

  // Get JSON input
  $input = json_decode(file_get_contents('php://input'), true);

  if (!$input || !isset($input['action'])) {
      sendError('Invalid request');
  }

  $action = $input['action'];

  // Route to appropriate action
  switch ($action) {
      case 'ping':
          testConnection();
          break;
      case 'register':
          registerUser($input);
          break;
      case 'login':
          loginUser($input);
          break;
      default:
          sendError('Unknown action');
  }

  /**
   * Test Matrix server connection
   */
  function testConnection() {
      $versions = matrixRequest('/_matrix/client/versions', 'GET');

      if ($versions && isset($versions['versions'])) {
          sendSuccess('Server connected', ['versions' => $versions['versions']]);
      } else {
          sendError('Server unreachable');
      }
  }

  /**
   * Register new user on Matrix server
   */
  function registerUser($input) {
      if (!isset($input['phoneNumber']) || !isset($input['password'])) {
          sendError('Phone number and password required');
      }

      $phoneNumber = $input['phoneNumber'];
      $password = $input['password'];

      // Validate phone number format
      $phoneNumber = preg_replace('/[^0-9+]/', '', $phoneNumber);
      if (empty($phoneNumber)) {
          sendError('Invalid phone number format');
      }

      // Validate password length
      if (strlen($password) < 8) {
          sendError('Password must be at least 8 characters');
      }

      // Generate secure username from phone number
      $username = phoneToMatrixUsername($phoneNumber);

      // Prepare registration request
      $requestBody = [
          'username' => $username,
          'password' => $password,
          'initial_device_display_name' => 'PurpleTalk Web',
          'auth' => [
              'type' => 'm.login.dummy'
          ]
      ];

      // Send registration request to Matrix server
      $response = matrixRequest('/_matrix/client/v3/register', 'POST', $requestBody);

      if ($response && isset($response['access_token'])) {
          // Store the mapping (optional)
          storeUserMapping($phoneNumber, $username, $response['user_id']);

          // Registration successful
          sendSuccess('Registration successful', [
              'accessToken' => $response['access_token'],
              'userId' => $response['user_id'],
              'deviceId' => $response['device_id'] ?? 'unknown',
              'homeServer' => MATRIX_DOMAIN
          ]);
      } else {
          // Registration failed
          $errorMessage = 'Registration failed';

          if (isset($response['error'])) {
              $errorMessage = $response['error'];
          }

          // Handle specific Matrix error codes
          if (isset($response['errcode'])) {
              if ($response['errcode'] === 'M_USER_IN_USE') {
                  $errorMessage = 'This phone number is already registered';
              }
          }

          sendError($errorMessage);
      }
  }

  /**
   * Login existing user
   */
  function loginUser($input) {
      if (!isset($input['phoneNumber']) || !isset($input['password'])) {
          sendError('Phone number and password required');
      }

      $phoneNumber = $input['phoneNumber'];
      $password = $input['password'];

      // Clean phone number
      $phoneNumber = preg_replace('/[^0-9+]/', '', $phoneNumber);

      // Generate same username hash for login
      $username = phoneToMatrixUsername($phoneNumber);
      $matrixId = '@' . $username . ':' . MATRIX_DOMAIN;

      // Prepare login request
      $requestBody = [
          'type' => 'm.login.password',
          'identifier' => [
              'type' => 'm.id.user',
              'user' => $matrixId
          ],
          'password' => $password,
          'initial_device_display_name' => 'PurpleTalk Web'
      ];

      // Send login request to Matrix server
      $response = matrixRequest('/_matrix/client/v3/login', 'POST', $requestBody);

      if ($response && isset($response['access_token'])) {
          // Update last login (optional)
          storeUserMapping($phoneNumber, $username, $response['user_id']);

          // Login successful
          sendSuccess('Login successful', [
              'accessToken' => $response['access_token'],
              'userId' => $response['user_id'],
              'deviceId' => $response['device_id'] ?? 'unknown',
              'homeServer' => MATRIX_DOMAIN
          ]);
      } else {
          // Login failed
          $errorMessage = 'Invalid credentials';

          if (isset($response['error'])) {
              $errorMessage = $response['error'];
          }

          // Handle specific Matrix error codes
          if (isset($response['errcode'])) {
              if ($response['errcode'] === 'M_FORBIDDEN') {
                  $errorMessage = 'Invalid phone number or password';
              } else if ($response['errcode'] === 'M_USER_DEACTIVATED') {
                  $errorMessage = 'This account has been deactivated';
              }
          }

          sendError($errorMessage);
      }
  }

  /**
   * Make HTTP request to Matrix server
   */
  function matrixRequest($endpoint, $method = 'GET', $data = null, $accessToken = null) {
      $url = MATRIX_SERVER . $endpoint;

      $ch = curl_init($url);

      // Set request method
      curl_setopt($ch, CURLOPT_CUSTOMREQUEST, $method);

      // Set headers
      $headers = ['Content-Type: application/json'];
      if ($accessToken) {
          $headers[] = 'Authorization: Bearer ' . $accessToken;
      }
      curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);

      // Set request body for POST/PUT
      if ($data && ($method === 'POST' || $method === 'PUT')) {
          curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($data));
      }

      // Return response as string
      curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

      // Disable SSL verification for testing (CHANGE THIS IN PRODUCTION!)
      curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
      curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, 0);

      // Set timeout
      curl_setopt($ch, CURLOPT_TIMEOUT, 30);

      // Execute request
      $response = curl_exec($ch);
      $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);

      if (curl_errno($ch)) {
          $error = curl_error($ch);
          curl_close($ch);
          error_log("Matrix API Error: " . $error);
          return null;
      }

      curl_close($ch);

      // Parse JSON response
      $responseData = json_decode($response, true);

      return $responseData;
  }

  /**
   * Send success response
   */
  function sendSuccess($message, $data = []) {
      $response = array_merge([
          'success' => true,
          'message' => $message
      ], $data);

      echo json_encode($response);
      exit();
  }

  /**
   * Send error response
   */
  function sendError($message, $code = 400) {
      http_response_code($code);
      echo json_encode([
          'success' => false,
          'message' => $message
      ]);
      exit();
  }
  ?>
